package org.openplaces.search;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.ReplacementSpan;
import android.util.Log;

import org.openplaces.MapActivity;
import org.openplaces.lists.ListManager;
import org.openplaces.lists.PlaceList;
import org.openplaces.lists.PlaceListItem;
import org.openplaces.model.OPBoundingBox;
import org.openplaces.model.OPGeoPoint;
import org.openplaces.model.OPLocationInterface;
import org.openplaces.model.OPPlaceCategoryInterface;
import org.openplaces.model.OPPlaceInterface;
import org.openplaces.places.Place;
import org.openplaces.categories.PlaceCategoriesManager;
import org.openplaces.remote.OpenPlacesRemote;
import org.openplaces.search.suggestions.LocationSuggestionItem;
import org.openplaces.search.suggestions.PlaceCategorySuggestionItem;
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

    public OPChipsEditText getQueryET() {
        return queryET;
    }

    public void setQueryET(OPChipsEditText queryET) {
        this.queryET = queryET;
    }

    private OPChipsEditText queryET;

    public List<OPPlaceCategoryInterface> getCategories() {
        return categories;
    }

    public List<OPLocationInterface> getLocations() {
        return locations;
    }

    private List<OPPlaceCategoryInterface> categories;
    private List<OPLocationInterface> locations;

    private List<SearchQueryListener> listeners;
    private MapActivity mapActivity;
    private OpenPlacesRemote opr;

    public Context getAppContext() {
        return appContext;
    }

    private Context appContext;

    public void doSearch() {
        new SearchTask(buildSearchQuery()).execute();
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

    private SearchQuery buildSearchQuery(){
        SearchQuery sq = new SearchQuery();
        for(OPPlaceCategoryInterface c: this.categories){
            sq.addSearchPlaceCateogry(c);
        }
        for(OPLocationInterface l: this.locations){
            sq.addSearchLocation(l);
        }

        LocationManager locationManager = (LocationManager) this.mapActivity.getSystemService(Context.LOCATION_SERVICE);
        String locationProvider = LocationManager.NETWORK_PROVIDER;
        Location location = locationManager.getLastKnownLocation(locationProvider);
        sq.setCurrentLocation(
                new OPGeoPoint(location.getLatitude(), location.getLongitude()));

        sq.setFreeTextQuery(this.queryET.getUnChipedText().trim());

        BoundingBoxE6 bbox = this.mapActivity.getMapVisibleArea();
        sq.setVisibleMapBB(new OPBoundingBox(
                (double) bbox.getLatNorthE6()/1E6d,
                (double) bbox.getLonEastE6()/1E6d,
                (double) bbox.getLatSouthE6()/1E6d,
                (double) bbox.getLonWestE6()/1E6d));

        return  sq;
    }

    private class SearchTask extends AsyncTask<Void, Integer, ResultSet> {

        private SearchQuery sq;

        public SearchTask(SearchQuery sq){
            this.sq = sq;
        }


        protected ResultSet doInBackground(Void... v) {

            return opr.search(this.sq);
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPreExecute() {
            mapActivity.setProgressBarIndeterminateVisibility(Boolean.TRUE);
            notifySearchStarted(this.sq);
            queryET.setEnabled(false);
            queryET.clearFocus();
//            searchEditText.setEnabled(false);
//            locationEditText.setEnabled(false);
//            searchButton.setEnabled(false);
        }

        protected void onPostExecute(ResultSet result) {

//            searchEditText.setEnabled(true);
//            locationEditText.setEnabled(true);
//            searchButton.setEnabled(true);

            mapActivity.setProgressBarIndeterminateVisibility(Boolean.FALSE);
            queryET.setEnabled(true);


            if(result == null){
                Log.d(MapActivity.LOGTAG, "result is null");
                return;
            }

            //ResultSet rs = ResultSet.buildFromOPPlaces(result, PlaceCategoriesManager.getInstance(SearchActivity.this));
            Log.d(MapActivity.LOGTAG, result.toString());

            notifySearchEnded(this.sq, result);

        }
    }

    public interface SearchQueryListener {
        public void freeTextQueryChanged(String freeTextQuery);
        public void searchStarted(SearchQuery sq);
        public void searchEnded(SearchQuery sq, ResultSet rs);
        public void newLocationsAvailable(List<OPLocationInterface> locs);
        public void newPlacesAvailable(List<Place> places);
    }


    public SearchController(final OPChipsEditText queryET, final MapActivity mapActivity){
        this.queryET = queryET;
        this.appContext = mapActivity.getApplicationContext();
        this.opr = OpenPlacesRemote.getInstance(mapActivity.getApplicationContext());
        this.mapActivity = mapActivity;
        this.listeners = new ArrayList<SearchQueryListener>();
        this.categories = new ArrayList<OPPlaceCategoryInterface>();
        this.locations = new ArrayList<OPLocationInterface>();

        this.queryET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
            @Override
            public void afterTextChanged(Editable editable) {
                notifyFreeTextChanged();
            }
        });

        this.queryET.addChipsWatcherListener(new OPChipsEditText.ChipsWatcher() {
            @Override
            public void onChipAdded(ReplacementSpan chip, Object relatedObj) {
                //do nothing
            }

            @Override
            public void onChipRemoved(ReplacementSpan chip, Object relatedObj) {
                if(relatedObj instanceof PlaceCategorySuggestionItem){
                    categories.remove(((PlaceCategorySuggestionItem)relatedObj).getCategory());
                }
                else if(relatedObj instanceof  LocationSuggestionItem){
                    locations.remove(((LocationSuggestionItem)relatedObj).getLocation());
                }
                else {
                    Log.w(MapActivity.LOGTAG, "Unknown chip type deleted");
                }
            }
        });
    }

    public void addPlaceCategory(PlaceCategorySuggestionItem item){
        this.categories.add(item.getCategory());
        this.queryET.appendChip(item.getTitle(), item);
    }

    public void addLocation(LocationSuggestionItem item){
        this.locations.add(item.getLocation());
        this.queryET.appendChip(item.getTitle(), item);
    }

    public void addListener(SearchQueryListener listener){
        this.listeners.add(listener);
    }

    public void notifySearchStarted(SearchQuery sq){
        for(SearchQueryListener l: this.listeners){
            l.searchStarted(sq);
        }
    }


    public void notifyNewLocations(List<OPLocationInterface> locs){
        for(SearchQueryListener l: this.listeners){
            l.newLocationsAvailable(locs);
        }
    }

    public void notifySearchEnded(SearchQuery sq, ResultSet rs){
        for(SearchQueryListener l: this.listeners){
            l.searchEnded(sq, rs);
        }
    }

    public String getFreeText(){
        return this.queryET.getUnChipedText();
    }

    public void notifyFreeTextChanged(){
        String unchipedText = this.queryET.getLastUnchipedToken();
        for(SearchQueryListener l: this.listeners){
            l.freeTextQueryChanged(unchipedText);
        }
    }

    public void gotoPlace(OPPlaceInterface place){

        notifySearchStarted(null);

        ResultSet rs = new ResultSet();
        rs.addPlace(new Place(place),
                PlaceCategoriesManager.getInstance(mapActivity.getApplicationContext()));

        notifySearchEnded(null, rs);
    }


    public void showList(PlaceList list){

        new LoadPlaceList().execute(list);

    }

    public void searchLocationByName(String name){
        new SearchLocationByName().execute(name);
    }

    public void updateAroundLocations(){
        new UpdateAroundLocations().execute();
    }

    private class UpdateAroundLocations extends AsyncTask<String, Integer, List<OPLocationInterface>> {

        protected List<OPLocationInterface> doInBackground(String... query) {

            LocationManager locationManager = (LocationManager) mapActivity.getSystemService(Context.LOCATION_SERVICE);
            String locationProvider = LocationManager.NETWORK_PROVIDER;
            Location myLocation = locationManager.getLastKnownLocation(locationProvider);
            opr.updateKnownLocationsAround(myLocation);
            List<OPLocationInterface> res = opr.getKnownLocations();
            return res;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPreExecute() {
            mapActivity.setProgressBarIndeterminateVisibility(Boolean.TRUE);

        }

        protected void onPostExecute(List<OPLocationInterface> result) {
            mapActivity.setProgressBarIndeterminateVisibility(Boolean.FALSE);

            notifyNewLocations(result);

        }
    }

    private class SearchLocationByName extends AsyncTask<String, Integer, List<OPLocationInterface>> {

        protected List<OPLocationInterface> doInBackground(String... query) {

            Log.d(MapActivity.LOGTAG, "Searching for locations by name: " + query[0]);

            List<OPLocationInterface> res = opr.getLocationsByName(query[0]);

            return res;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPreExecute() {
            mapActivity.setProgressBarIndeterminateVisibility(Boolean.TRUE);
        }

        protected void onPostExecute(List<OPLocationInterface> result) {

            mapActivity.setProgressBarIndeterminateVisibility(Boolean.FALSE);

            notifyNewLocations(result);
        }
    }


    public void updateStarredPlaces(){
        new LoadStarredPlacesTask().execute();
    }

    public void notifyNewPlaces(List<Place> places){
        for(SearchQueryListener l: this.listeners){
            l.newPlacesAvailable(places);
        }
    }


    private class LoadStarredPlacesTask extends AsyncTask<String, Integer, ResultSet> {

        protected ResultSet doInBackground(String... query) {

            ListManager lm = ListManager.getInstance(mapActivity.getApplicationContext());
            return opr.getPlacesByTypesAndIds(lm.getAllStarredPlaces());


        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPreExecute() {
            mapActivity.setProgressBarIndeterminateVisibility(Boolean.TRUE);

        }

        protected void onPostExecute(ResultSet result) {

            mapActivity.setProgressBarIndeterminateVisibility(Boolean.FALSE);

            notifyNewPlaces(result.getAllPlaces());
        }
    }

    private class LoadPlaceList extends AsyncTask<PlaceList, Integer, ResultSet> {

        protected ResultSet doInBackground(PlaceList... query) {

            ListManager lm = ListManager.getInstance(mapActivity.getApplicationContext());
            PlaceList list = query[0];
            if(list == null){
                return opr.getPlacesByTypesAndIds(lm.getAllStarredPlaces());
            }
            else {

                Set<String> places = new HashSet<String>();
                for(PlaceListItem item: list.getPlacesInList()){
                    places.add(item.getOsmType()+":"+item.getOsmId());
                }
                return opr.getPlacesByTypesAndIds(places);
            }
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPreExecute() {

            mapActivity.setProgressBarIndeterminateVisibility(Boolean.TRUE);
            notifySearchStarted(null);
        }

        protected void onPostExecute(ResultSet result) {

            mapActivity.setProgressBarIndeterminateVisibility(Boolean.FALSE);

            notifySearchEnded(null, result);
        }
    }
}
