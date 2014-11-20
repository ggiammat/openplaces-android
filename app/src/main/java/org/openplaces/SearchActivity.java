package org.openplaces;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.ReplacementSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;

import org.openplaces.model.OPBoundingBox;
import org.openplaces.model.OPGeoPoint;
import org.openplaces.model.OPLocationInterface;
import org.openplaces.model.OPPlaceCategoryInterface;
import org.openplaces.model.OPPlaceInterface;
import org.openplaces.model.PlaceCategoriesManager;
import org.openplaces.model.impl.OPLocationImpl;
import org.openplaces.search.PlaceCategoriesAdapter;
import org.openplaces.model.ResultSet;
import org.openplaces.search.SearchLocationsAdapter;
import org.openplaces.search.SearchQueryBuilder;
import org.openplaces.search.SearchTabsPagerAdapter;
import org.openplaces.utils.HttpHelper;
import org.openplaces.widgets.OPChipsEditText;
import org.osmdroid.util.BoundingBoxE6;


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
    private OPChipsEditText locationEditText;
    private SearchTabsPagerAdapter tabsViewPagerAdapter;
    private ViewPager tabsViewPager;
    private Button searchButton;
    private BoundingBoxE6 mapViewVisibleArea;

    private OPLocationInterface specialLocationNearMeNow = new OPLocationImpl();
    private OPLocationInterface specialLocationInVisibleArea = new OPLocationImpl();

    private SearchQueryBuilder searchQueryBuilder;

    public interface SearchTextChangedListener {
        public void onSearchTextChanged(String text);
    }

    public interface SearchLocationChangedListener {
        public void onSearchLocationChanged(String text);
        public void onSearchLocationTokenCompleted(String text);
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
        this.searchQueryBuilder = new SearchQueryBuilder(this.opp);

        this.tabsViewPagerAdapter = new SearchTabsPagerAdapter(getSupportFragmentManager());
        this.tabsViewPager = (ViewPager) super.findViewById(R.id.searchTabsViewPager);
        this.tabsViewPager.setAdapter(this.tabsViewPagerAdapter);
        this.tabsViewPager.setCurrentItem(SearchTabsPagerAdapter.CATEGORIES_FRAGMENT_POSITION, false);

        this.searchTextListeners = new ArrayList<SearchTextChangedListener>();
        this.searchLocationListeners = new ArrayList<SearchLocationChangedListener>();

        this.searchEditText = (OPChipsEditText) findViewById(R.id.searchEditText);
        this.locationEditText = (OPChipsEditText) findViewById(R.id.locationEditText);
        this.searchButton = (Button) findViewById(R.id.startSearch);


        Intent i = getIntent();
        this.mapViewVisibleArea = i.getParcelableExtra("VISIBLEAREA");
        this.setupListeners();

    }


    public SearchQueryBuilder getQueryBuilder(){
        return this.searchQueryBuilder;
    }


    public void setCurrentViewSearchLocation(boolean startSearch){
        this.locationEditText.appendChip("Visible area", this.specialLocationInVisibleArea);
        if(startSearch){
            new SearchTask().execute();
        }
    }

    public void setNearMeNowSearchLocation(boolean startSearch){
        this.locationEditText.appendChip("Near me now", this.specialLocationNearMeNow);
        if(startSearch){
            new SearchTask().execute();
        }
    }

    public void addSearchLocation(OPLocationInterface loc, boolean startSearch){
        this.locationEditText.appendChip(loc.getDisplayName(), loc);
        if(startSearch){
            new SearchTask().execute();
        }
    }

    public void addSearchPlaceCategory(OPPlaceCategoryInterface s, boolean moveFocusToLocation){
        this.searchEditText.appendChip(s.getFirstNameMatch(this.searchEditText.getUnChipedText()), s);
        if(moveFocusToLocation){
            this.locationEditText.requestFocus();
        }
    }

    public void addSearchTextListener(SearchTextChangedListener listener){
        this.searchTextListeners.add(listener);
    }

    public void addSearchLocationListener(SearchLocationChangedListener listener){
        this.searchLocationListeners.add(listener);
    }

    public void setLocationEditTextEnabled(boolean status){
        this.locationEditText.setEnabled(status);
    }

    private void setupListeners(){
        this.searchEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                String textForFiltering = searchEditText.getUnChipedText();
                for(SearchTextChangedListener listener: searchTextListeners){
                    listener.onSearchTextChanged(textForFiltering);
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
            }
        });


        this.searchEditText.addChipsWatcherListener(new OPChipsEditText.ChipsWatcher() {
            @Override
            public void onChipAdded(ReplacementSpan chip, Object relatedObj) {
                searchQueryBuilder.addSearchPlaceCateogry((OPPlaceCategoryInterface) relatedObj);
            }

            @Override
            public void onChipRemoved(ReplacementSpan chip, Object relatedObj) {
                searchQueryBuilder.removeSearchPlaceCateogry((OPPlaceCategoryInterface) relatedObj);
            }
        });

        this.locationEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                String textForFiltering = locationEditText.getUnChipedText();
                for(SearchLocationChangedListener listener: searchLocationListeners){
                    listener.onSearchLocationChanged(textForFiltering);
                }

                if(s.toString().endsWith(" ")){
                    for(SearchLocationChangedListener listener: searchLocationListeners){
                        listener.onSearchLocationTokenCompleted(textForFiltering);
                    }
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

        this.locationEditText.addChipsWatcherListener(new OPChipsEditText.ChipsWatcher() {
            @Override
            public void onChipAdded(ReplacementSpan chip, Object relatedObj) {
                if(relatedObj.equals(specialLocationNearMeNow)){
                    searchQueryBuilder.setNearMeNow(true);

                }
                else if(relatedObj.equals(specialLocationInVisibleArea)){
                    searchQueryBuilder.setVisibleArea(true);
                }
                else {
                    searchQueryBuilder.addSearchLocation((OPLocationInterface) relatedObj);
                }
            }

            @Override
            public void onChipRemoved(ReplacementSpan chip, Object relatedObj) {
                if(relatedObj.equals(specialLocationNearMeNow)){
                    searchQueryBuilder.setNearMeNow(false);
                }
                else if(relatedObj.equals(specialLocationInVisibleArea)){
                    searchQueryBuilder.setVisibleArea(false);
                }
                else {
                    searchQueryBuilder.removeSearchLocation((OPLocationInterface) relatedObj);
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

            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            String locationProvider = LocationManager.NETWORK_PROVIDER;
            Location location = locationManager.getLastKnownLocation(locationProvider);
            searchQueryBuilder.setCurrentLocation(
                    new OPGeoPoint(location.getLatitude(), location.getLongitude()));

            searchQueryBuilder.setFreeTextQuery(searchEditText.getUnChipedText().trim());

            searchQueryBuilder.setVisibleMapBB(new OPBoundingBox(
                    (double) mapViewVisibleArea.getLatNorthE6()/1E6d,
                    (double) mapViewVisibleArea.getLonEastE6()/1E6d,
                    (double) mapViewVisibleArea.getLatSouthE6()/1E6d,
                    (double) mapViewVisibleArea.getLonWestE6()/1E6d));

           List<OPPlaceInterface> res = searchQueryBuilder.doSearch();
           return res;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(Boolean.TRUE);
            searchEditText.setEnabled(false);
            locationEditText.setEnabled(false);
            searchButton.setEnabled(false);
        }

        protected void onPostExecute(List<OPPlaceInterface> result) {

            setProgressBarIndeterminateVisibility(Boolean.FALSE);
            searchEditText.setEnabled(true);
            locationEditText.setEnabled(true);
            searchButton.setEnabled(true);

            if(result == null){
                Log.d(MapActivity.LOGTAG, "result is null");
                return;
            }

            ResultSet rs = ResultSet.buildFromOPPlaces(result, PlaceCategoriesManager.getInstance(SearchActivity.this));
            Log.d(MapActivity.LOGTAG, rs.toString());

            Intent intent=new Intent();
            intent.putExtra("RESULTSET", rs);

            setResult(1,intent);

            finish();

        }
    }

}
