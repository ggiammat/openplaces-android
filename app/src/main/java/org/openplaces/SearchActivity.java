package org.openplaces;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;

import org.openplaces.model.OPGeoPoint;
import org.openplaces.model.OPLocationInterface;
import org.openplaces.model.OPPlaceCategoryInterface;
import org.openplaces.model.OPPlaceInterface;
import org.openplaces.search.PlaceCategoriesAdapter;
import org.openplaces.model.ResultSet;
import org.openplaces.search.SearchLocationsAdapter;
import org.openplaces.search.SearchQueryBuilder;
import org.openplaces.search.SearchTabsPagerAdapter;
import org.openplaces.utils.HttpHelper;
import org.openplaces.widgets.OPChipsEditText;


import java.util.ArrayList;
import java.util.List;


public class SearchActivity extends FragmentActivity {

    public static final int SEARCH_LOCATION_AROUND_RADIUS = 10;


    private OpenPlacesProvider opp;
    private OPGeoPoint myLocation;
    private ListView locationsList;
    private SearchLocationsAdapter locationsListAdapter;
    private GridView presetsList;
    private PlaceCategoriesAdapter presetsListAdapter;
    private OPChipsEditText searchEditText;
    private EditText locationEditText;
    private SearchTabsPagerAdapter tabsViewPagerAdapter;
    private ViewPager tabsViewPager;
    private Button searchButton;

    private SearchQueryBuilder searchQueryBuilder = new SearchQueryBuilder();

    public interface SearchTextChangedListener {
        public void onSearchTextChanged(String text);
    }

    public interface SearchLocationChangedListener {
        public void onSearchLocationChanged(String text);
    }

    private List<SearchTextChangedListener> searchTextListeners;
    private List<SearchLocationChangedListener> searchLocationListeners;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);


        setContentView(R.layout.activity_search);






        this.opp = new OpenPlacesProvider(
                new HttpHelper(),
                "gabriele.giammatteo@gmail.com",
                OpenPlacesProvider.NOMINATIM_SERVER,
                OpenPlacesProvider.OVERPASS_SERVER,
                OpenPlacesProvider.REVIEW_SERVER_SERVER
        );


        this.tabsViewPagerAdapter = new SearchTabsPagerAdapter(getSupportFragmentManager());
        this.tabsViewPager = (ViewPager) super.findViewById(R.id.searchTabsViewPager);
        this.tabsViewPager.setAdapter(this.tabsViewPagerAdapter);
        this.tabsViewPager.setCurrentItem(SearchTabsPagerAdapter.CATEGORIES_FRAGMENT_POSITION, false);

        this.searchTextListeners = new ArrayList<SearchTextChangedListener>();
        this.searchLocationListeners = new ArrayList<SearchLocationChangedListener>();

        this.searchEditText = (OPChipsEditText) findViewById(R.id.searchEditText);
        this.locationEditText = (EditText) findViewById(R.id.locationEditText);
        this.searchButton = (Button) findViewById(R.id.startSearch);
        this.setupListeners();

    }


    public SearchQueryBuilder getQueryBuilder(){
        return this.searchQueryBuilder;
    }

    public void setSearchLocation(OPLocationInterface loc){
        this.searchQueryBuilder.addSearchLocation(loc);
    }

    public void addSearchPlaceCategory(OPPlaceCategoryInterface s){
        this.searchQueryBuilder.addSearchPlaceCateogry(s);
        //this.searchEditText.appendChip(s.getName());
    }

    public void addSearchTextListener(SearchTextChangedListener listener){
        this.searchTextListeners.add(listener);
    }

    public void addSearchLocationListener(SearchLocationChangedListener listener){
        this.searchLocationListeners.add(listener);
    }

    private void setupListeners(){
        this.searchEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                for(SearchTextChangedListener listener: searchTextListeners){
                    listener.onSearchTextChanged(s.toString());
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        this.searchEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    tabsViewPager.setCurrentItem(SearchTabsPagerAdapter.CATEGORIES_FRAGMENT_POSITION, true);
                }
                else {
                    searchEditText.setChips();
                }
            }
        });

        this.locationEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                for(SearchLocationChangedListener listener: searchLocationListeners){
                    listener.onSearchLocationChanged(s.toString());
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        this.locationEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    tabsViewPager.setCurrentItem(SearchTabsPagerAdapter.LOCATIONS_FRAGMENT_POSITION, true);
                }
            }
        });

        this.searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SearchTask().execute();
            }
        });

        this.tabsViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                EditText textView;
                if (position == SearchTabsPagerAdapter.CATEGORIES_FRAGMENT_POSITION) {
                    textView = searchEditText;
                    textView.requestFocus();
                } else if (position == SearchTabsPagerAdapter.LOCATIONS_FRAGMENT_POSITION){
                    textView = locationEditText;
                    textView.requestFocus();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private class SearchTask extends AsyncTask<String, Integer, List<OPPlaceInterface>> {

        protected List<OPPlaceInterface> doInBackground(String... query) {

           List<OPPlaceInterface> res = opp.getPlaces(searchQueryBuilder.getSearchPlaceCategories(), searchQueryBuilder.getSearchLocations());
           return res;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(Boolean.TRUE);

        }

        protected void onPostExecute(List<OPPlaceInterface> result) {

            setProgressBarIndeterminateVisibility(Boolean.FALSE);

            if(result == null){
                Log.d(MapActivity.LOGTAG, "result is null");
                return;
            }

            ResultSet rs = ResultSet.buildFromOPPlaces(result);
            Log.d(MapActivity.LOGTAG, rs.toString());

            Intent intent=new Intent();
            intent.putExtra("RESULTSET", rs);

            setResult(1,intent);

            finish();

        }
    }

}
