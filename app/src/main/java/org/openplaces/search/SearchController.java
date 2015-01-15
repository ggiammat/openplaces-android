package org.openplaces.search;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.ReplacementSpan;
import android.util.Log;

import org.openplaces.MapActivity;

import org.openplaces.lists.PlaceList;
import org.openplaces.model.OPBoundingBox;
import org.openplaces.model.OPGeoPoint;
import org.openplaces.model.OPLocationInterface;
import org.openplaces.model.OPPlaceCategoryInterface;
import org.openplaces.model.OPPlaceInterface;
import org.openplaces.categories.PlaceCategoriesManager;
import org.openplaces.search.suggestions.LocationSuggestionItem;
import org.openplaces.search.suggestions.PlaceCategorySuggestionItem;
import org.openplaces.tasks.LoadListTask;
import org.openplaces.tasks.OpenPlacesAsyncTask;
import org.openplaces.tasks.SearchTask;
import org.openplaces.widgets.OPChipsEditText;
import org.osmdroid.util.BoundingBoxE6;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ggiammat on 12/28/14.
 */
public class SearchController {

    public interface SearchQueryListener {
        public void searchStarted(SearchQuery sq);
        public void searchEnded(SearchQuery sq, ResultSet rs);
        public void searchQueryChanged(List<OPPlaceCategoryInterface> searchQueryCategories,
                                       List<OPLocationInterface> searchQueryLocations,
                                       String searchQueryFreeText, String searchQueryCurrentTokenFreeText);
    }

    private OPChipsEditText searchBox;

    private List<OPPlaceCategoryInterface> searchQueryCategories;
    private List<OPLocationInterface> searchQueryLocations;

    private List<SearchQueryListener> listeners;
    private MapActivity mapActivity;
    private Context appContext;


    public SearchController(final OPChipsEditText searchBox, final MapActivity mapActivity){
        this.searchBox = searchBox;
        this.appContext = mapActivity.getApplicationContext();
        this.mapActivity = mapActivity;
        this.listeners = new ArrayList<SearchQueryListener>();

        //TODO: init locations and categories
        this.searchQueryCategories = new ArrayList<OPPlaceCategoryInterface>();
        this.searchQueryLocations = new ArrayList<OPLocationInterface>();

        this.searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
            @Override
            public void afterTextChanged(Editable editable) {
                notifySearchQueryChanged();
            }
        });

        this.searchBox.addChipsWatcherListener(new OPChipsEditText.ChipsWatcher() {
            @Override
            public void onChipAdded(ReplacementSpan chip, Object relatedObj) {
                //do nothing.
            }

            @Override
            public void onChipRemoved(ReplacementSpan chip, Object relatedObj) {
                if (relatedObj instanceof PlaceCategorySuggestionItem) {
                    searchQueryCategories.remove(((PlaceCategorySuggestionItem) relatedObj).getCategory());
                } else if (relatedObj instanceof LocationSuggestionItem) {
                    searchQueryLocations.remove(((LocationSuggestionItem) relatedObj).getLocation());
                } else {
                    Log.w(MapActivity.LOGTAG, "Unknown chip type deleted");
                }
            }
        });
    }


    public void notifySearchStarted(SearchQuery sq){
        mapActivity.setProgressBarIndeterminateVisibility(Boolean.TRUE);
        getSearchBox().setEnabled(false);
        getSearchBox().clearFocus();

        for(SearchQueryListener l: this.listeners){
            l.searchStarted(sq);
        }
    }

    public void notifySearchFinshed(SearchQuery sq, ResultSet resultSet){
        mapActivity.setProgressBarIndeterminateVisibility(Boolean.FALSE);
        getSearchBox().setEnabled(true);
        for(SearchQueryListener l: this.listeners){
            l.searchEnded(sq, resultSet);
        }
    }

    public void notifySearchQueryChanged(){
        String freeText = this.searchBox.getUnChipedText();
        String lastToken = this.searchBox.getLastUnchipedToken();
        for(SearchQueryListener l: this.listeners){
            l.searchQueryChanged(this.searchQueryCategories, this.searchQueryLocations, freeText, lastToken);
        }
    }

    private SearchQuery buildSearchQuery(){
        SearchQuery sq = new SearchQuery();
        for(OPPlaceCategoryInterface c: this.getSearchQueryCategories()){
            sq.addSearchPlaceCateogry(c);
        }
        for(OPLocationInterface l: this.getSearchQueryLocations()){
            sq.addSearchLocation(l);
        }

        Location location = this.getSearchPosition();
        sq.setCurrentLocation(
                new OPGeoPoint(location.getLatitude(), location.getLongitude()));

        sq.setFreeTextQuery(this.getSearchQueryFreeText());

        BoundingBoxE6 bbox = this.getSearchBoundingBox();
        sq.setVisibleMapBB(new OPBoundingBox(
                (double) bbox.getLatNorthE6()/1E6d,
                (double) bbox.getLonEastE6()/1E6d,
                (double) bbox.getLatSouthE6()/1E6d,
                (double) bbox.getLonWestE6()/1E6d));

        return  sq;
    }

    public Context getAppContext() {
        return appContext;
    }

    public MapActivity getMapActivity() {
        return mapActivity;
    }

    public OPChipsEditText getSearchBox() {
        return searchBox;
    }

    public List<OPPlaceCategoryInterface> getSearchQueryCategories() {
        return searchQueryCategories;
    }

    public List<OPLocationInterface> getSearchQueryLocations() {
        return searchQueryLocations;
    }

    public String getSearchQueryFreeText() {
        return searchBox.getUnChipedText();
    }

    public String getSearchQueryCurrentTokenFreeText() {
        return searchBox.getLastUnchipedToken();
    }



    public Location getSearchPosition(){
        //TODO: this returns my location. What if we move the map? The center of the search becomes
        //the center of the map?
        //TODO: this function is called for each location item in the suggestions list. Make it more efficient?
        LocationManager locationManager = (LocationManager) mapActivity.getSystemService(Context.LOCATION_SERVICE);
        String locationProvider = LocationManager.NETWORK_PROVIDER;
        Location myLocation = locationManager.getLastKnownLocation(locationProvider);
        return myLocation;
    }

    public BoundingBoxE6 getSearchBoundingBox(){
        return this.mapActivity.getMapVisibleArea();
    }


    public void doSearch() {
        final SearchQuery sq = buildSearchQuery();
        new SearchTask(sq, this.appContext, new OpenPlacesAsyncTask.OpenPlacesAsyncTaskListener() {
            @Override
            public void taskStarted() {
                notifySearchStarted(sq);
            }

            @Override
            public void taskFinished(Object result, int status) {
                notifySearchFinshed(null, (ResultSet) result);
                mapActivity.setNewResultSet((ResultSet) result);
            }
        }).execute();
    }



    public void addSearchQueryCategory(PlaceCategorySuggestionItem item){
        this.searchQueryCategories.add(item.getCategory());
        this.searchBox.appendChip(item.getTitle(), item);
        this.notifySearchQueryChanged();
    }

    public void addSearchQueryLocation(LocationSuggestionItem item){
        this.searchQueryLocations.add(item.getLocation());
        this.searchBox.appendChip(item.getTitle(), item);
        this.notifySearchQueryChanged();
    }

    public void addListener(SearchQueryListener listener){
        this.listeners.add(listener);
    }

    public void gotoPlace(OPPlaceInterface place){

        notifySearchStarted(null);


        List<OPPlaceInterface> places = new ArrayList<OPPlaceInterface>();
        places.add(place);
        ResultSet rs = ResultSet.buildFromOPPlaces(places, PlaceCategoriesManager.getInstance(getAppContext()));

        notifySearchFinshed(null, null);

        mapActivity.setNewResultSet(rs);
        mapActivity.expandSlidingPanel();

    }


    public void showList(PlaceList list){

        new LoadListTask(list, this.getAppContext(), new OpenPlacesAsyncTask.OpenPlacesAsyncTaskListener() {
            @Override
            public void taskStarted() {
                mapActivity.setProgressBarIndeterminateVisibility(Boolean.TRUE);
                notifySearchStarted(null);
            }

            @Override
            public void taskFinished(Object result, int status) {
                mapActivity.setProgressBarIndeterminateVisibility(Boolean.FALSE);
                notifySearchFinshed(null, (ResultSet) result);
                mapActivity.setNewResultSet((ResultSet) result);
            }

        }).execute();

    }

    public void searchLocationByName(String name){

        //new SearchLocationByName().execute(name);
    }

    public void updateAroundLocations(){
        //new UpdateAroundLocations().execute();
    }

//    private class UpdateAroundLocations extends AsyncTask<String, Integer, List<OPLocationInterface>> {
//
//        protected List<OPLocationInterface> doInBackground(String... query) {
//
//            LocationManager locationManager = (LocationManager) mapActivity.getSystemService(Context.LOCATION_SERVICE);
//            String locationProvider = LocationManager.NETWORK_PROVIDER;
//            Location myLocation = locationManager.getLastKnownLocation(locationProvider);
//            opr.updateKnownLocationsAround(myLocation);
//            List<OPLocationInterface> res = opr.getKnownLocations();
//            return res;
//        }
//
//        protected void onProgressUpdate(Integer... progress) {
//        }
//
//        protected void onPreExecute() {
//            mapActivity.setProgressBarIndeterminateVisibility(Boolean.TRUE);
//
//        }
//
//        protected void onPostExecute(List<OPLocationInterface> result) {
//            mapActivity.setProgressBarIndeterminateVisibility(Boolean.FALSE);
//
//            notifyNewLocations(result);
//
//        }
//    }
//
//    private class SearchLocationByName extends AsyncTask<String, Integer, List<OPLocationInterface>> {
//
//        protected List<OPLocationInterface> doInBackground(String... query) {
//
//            Log.d(MapActivity.LOGTAG, "Searching for locations by name: " + query[0]);
//
//            List<OPLocationInterface> res = opr.getLocationsByName(query[0]);
//
//            return res;
//        }
//
//        protected void onProgressUpdate(Integer... progress) {
//        }
//
//        protected void onPreExecute() {
//            mapActivity.setProgressBarIndeterminateVisibility(Boolean.TRUE);
//        }
//
//        protected void onPostExecute(List<OPLocationInterface> result) {
//
//            mapActivity.setProgressBarIndeterminateVisibility(Boolean.FALSE);
//
//            notifyNewLocations(result);
//        }
//    }


//
//    private class LoadStarredPlacesTask extends AsyncTask<String, Integer, ResultSet> {
//
//        protected ResultSet doInBackground(String... query) {
//
//            ListManager lm = ListManager.getInstance(mapActivity.getApplicationContext());
//            return opr.getPlacesByTypesAndIds(lm.getAllStarredPlaces());
//
//
//        }
//
//        protected void onProgressUpdate(Integer... progress) {
//        }
//
//        protected void onPreExecute() {
//            mapActivity.setProgressBarIndeterminateVisibility(Boolean.TRUE);
//
//        }
//
//        protected void onPostExecute(ResultSet result) {
//
//            mapActivity.setProgressBarIndeterminateVisibility(Boolean.FALSE);
//
//            notifyNewPlaces(result.getAllPlaces());
//        }
//    }

}
