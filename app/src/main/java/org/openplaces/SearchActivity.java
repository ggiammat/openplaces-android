package org.openplaces;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TabHost;

import org.openplaces.helpers.HttpHelper;
import org.openplaces.model.OPLocation;
import org.openplaces.model.OPTagsFilter;
import org.openplaces.providers.OpenPlacesProvider;
import org.openplaces.search.PresetSearch;
import org.openplaces.search.PresetSearchesAdapter;
import org.openplaces.search.SearchLocationsAdapter;
import org.openplaces.search.SearchTabLocationsFragment;
import org.openplaces.search.SearchTabPresetsFragment;
import org.openplaces.search.SearchTabsPagerAdapter;
import org.openplaces.utils.GeoFunctions;
import org.openplaces.utils.OPGeoPoint;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;


public class SearchActivity extends FragmentActivity {

    public static final int SEARCH_LOCATION_AROUND_RADIUS = 10;


    private OpenPlacesProvider opp;
    private OPGeoPoint myLocation;
    private ListView locationsList;
    private SearchLocationsAdapter locationsListAdapter;
    private GridView presetsList;
    private PresetSearchesAdapter presetsListAdapter;
    private EditText searchEditText;
    private EditText locationEditText;
    private SearchTabsPagerAdapter tabsPagerAdapter;
    private ViewPager searchTabsViewPager;
//    private TabHost searchTabHost;
//    private HashMap<String, TabInfo> mapTabInfo = new HashMap<String, SearchActivity.TabInfo>();
    private Fragment presetsFrag;
    private Fragment locationFrag;

//    private class TabInfo {
//        private String tag;
//        private Class<?> clss;
//        private Bundle args;
//        private Fragment fragment;
//        TabInfo(String tag, Class<?> clazz, Bundle args) {
//            this.tag = tag;
//            this.clss = clazz;
//            this.args = args;
//        }
//
//    }
//
//    class TabFactory implements TabHost.TabContentFactory {
//
//        private final Context mContext;
//
//        /**
//         * @param context
//         */
//        public TabFactory(Context context) {
//            mContext = context;
//        }
//
//        /** (non-Javadoc)
//         * @see android.widget.TabHost.TabContentFactory#createTabContent(java.lang.String)
//         */
//        public View createTabContent(String tag) {
//            View v = new View(mContext);
//            v.setMinimumWidth(0);
//            v.setMinimumHeight(0);
//            return v;
//        }
//
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);


        setContentView(R.layout.activity_search);

        //this.initialiseTabHost(savedInstanceState);
        this.intialiseViewPager();




        this.opp = new OpenPlacesProvider(
                new HttpHelper(),
                "gabriele.giammatteo@gmail.com",
                OpenPlacesProvider.NOMINATIM_SERVER,
                OpenPlacesProvider.OVERPASS_SERVER,
                OpenPlacesProvider.REVIEW_SERVER_SERVER
        );



//        this.locationsList = (ListView) this.locationFrag.getView().findViewById(R.id.locationsList);
//        this.locationsListAdapter = new SearchLocationsAdapter(this, this.myLocation);
//        this.locationsList.setAdapter(this.locationsListAdapter);

        this.searchEditText = (EditText) findViewById(R.id.searchEditText);
        this.locationEditText = (EditText) findViewById(R.id.locationEditText);
        this.setupListeners();


    }

    @Override
    protected void onResume() {
        super.onResume();
//        ((SearchTabLocationsFragment) this.locationFrag).startSearch();
    }

    private void setupListeners(){
        this.searchEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                ((SearchTabPresetsFragment) presetsFrag).filterPresetsList(s.toString());
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        this.searchEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    searchTabsViewPager.setCurrentItem(0, true);
                }
            }
        });

        this.locationEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                ((SearchTabLocationsFragment) locationFrag).filterLocationsList(s.toString());
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        this.locationEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    searchTabsViewPager.setCurrentItem(1, true);
                }
            }
        });

        this.searchTabsViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                EditText textView;
                if(position == 0){
                    textView = searchEditText;
                }
                else {
                    textView = locationEditText;
                }

                textView.requestFocus();
//                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.showSoftInput(textView, InputMethodManager.SHOW_IMPLICIT);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


//        this.searchTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
//            @Override
//            public void onTabChanged(String s) {
//                //TabInfo newTab = this.mapTabInfo.get(tag);
//                int pos = searchTabHost.getCurrentTab();
//                searchTabsViewPager.setCurrentItem(pos);
//            }
//        });
    }

//    private void initialiseTabHost(Bundle args) {
//        this.searchTabHost = (TabHost) findViewById(R.id.searchTabs);
//        searchTabHost.setup();
//        TabInfo tabInfo = null;
//        SearchActivity.AddTab(this, this.searchTabHost, this.searchTabHost.newTabSpec("Tab1").setIndicator("Tab 1"), (tabInfo = new TabInfo("Tab1", SearchTabPresetsFragment.class, args)));
//        this.mapTabInfo.put(tabInfo.tag, tabInfo);
//        SearchActivity.AddTab(this, this.searchTabHost, this.searchTabHost.newTabSpec("Tab2").setIndicator("Tab 2"), ( tabInfo = new TabInfo("Tab2", SearchTabLocationsFragment.class, args)));
//        this.mapTabInfo.put(tabInfo.tag, tabInfo);
//        // Default to first tab
//        //this.onTabChanged("Tab1");
//        //
//    }

//    private static void AddTab(SearchActivity activity, TabHost tabHost, TabHost.TabSpec tabSpec, TabInfo tabInfo) {
//        // Attach a Tab view factory to the spec
//        tabSpec.setContent(activity.new TabFactory(activity));
//        tabHost.addTab(tabSpec);
//    }

    private void intialiseViewPager() {

        this.presetsFrag = SearchTabPresetsFragment.newInstance(0, "What");
        this.locationFrag = SearchTabLocationsFragment.newInstance(1, "Where");

        List<Fragment> fragments = new Vector<Fragment>();
        fragments.add(this.presetsFrag);
        fragments.add(this.locationFrag);
        this.tabsPagerAdapter  = new SearchTabsPagerAdapter(getSupportFragmentManager(), fragments);
        //
        this.searchTabsViewPager = (ViewPager) super.findViewById(R.id.searchTabsViewPager);
        this.searchTabsViewPager.setAdapter(this.tabsPagerAdapter);

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


}
