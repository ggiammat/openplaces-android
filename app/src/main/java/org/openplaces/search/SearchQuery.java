package org.openplaces.search;

import android.util.Log;

import org.openplaces.MapActivity;
import org.openplaces.OpenPlacesProvider;
import org.openplaces.model.OPBoundingBox;
import org.openplaces.model.OPGeoPoint;
import org.openplaces.model.OPLocationInterface;
import org.openplaces.model.OPPlaceCategoryInterface;
import org.openplaces.model.OPPlaceInterface;
import org.openplaces.model.impl.OPLocationImpl;
import org.openplaces.utils.GeoFunctions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ggiammat on 11/13/14.
 */
public class SearchQuery {

    private boolean nearMeNow;
    private boolean visibleArea;
    private OPGeoPoint currentLocation;
    private OPBoundingBox visibleMapBB;
    private String freeTextQuery;

    public SearchQuery(){
    }

    private List<OPPlaceCategoryInterface> searchPlaceCategories = new ArrayList<OPPlaceCategoryInterface>();
    private List<OPLocationInterface> searchLocations = new ArrayList<OPLocationInterface>();

    public void addSearchPlaceCateogry(OPPlaceCategoryInterface placeCategory){
        Log.d(MapActivity.LOGTAG, "Adding place category for search: " + placeCategory);
        this.searchPlaceCategories.add(placeCategory);
    }

    public void removeSearchPlaceCateogry(OPPlaceCategoryInterface placeCategory){
        Log.d(MapActivity.LOGTAG, "Removing place category for search: " + placeCategory);
        this.searchPlaceCategories.remove(placeCategory);
    }


    public void removeSearchLocation(OPLocationInterface loc){
        Log.d(MapActivity.LOGTAG, "Removing location for search: " + loc);
        this.searchLocations.remove(loc);
    }

    public void addSearchLocation(OPLocationInterface loc){
//        if(nearMeNow){
//            Log.d(MapActivity.LOGTAG, "Not adding location for search beacuse near_me_now is set");
//            return;
//        }
        Log.d(MapActivity.LOGTAG, "Adding location for search: " + loc);
        this.searchLocations.add(loc);
    }

    public List<OPPlaceCategoryInterface> getSearchPlaceCategories(){
        return this.searchPlaceCategories;
    }

    public List<OPLocationInterface> getSearchLocations(){
        return this.searchLocations;
    }

    public boolean isNearMeNow() {
        return nearMeNow;
    }

    public void reseatSearchLocations(){
        Log.d(MapActivity.LOGTAG, "Resetting search locations");
        this.searchLocations = new ArrayList<OPLocationInterface>();
    }

    public void setNearMeNow(boolean nearMeNow) {
        Log.d(MapActivity.LOGTAG, "Setting near_me_now search to: " + nearMeNow);
        this.nearMeNow = nearMeNow;
        if(this.nearMeNow == true){
            //this.reseatSearchLocations();
        }
    }




    public OPGeoPoint getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(OPGeoPoint currentLocation) {
        this.currentLocation = currentLocation;
    }

    public String getFreeTextQuery() {
        return freeTextQuery;
    }

    public void setFreeTextQuery(String freeTextQuery) {
        Log.d(MapActivity.LOGTAG, "Setting free text query to: " + freeTextQuery);
        this.freeTextQuery = freeTextQuery;
    }

    public boolean isVisibleArea() {
        return visibleArea;
    }

    public void setVisibleArea(boolean visibleArea) {
        Log.d(MapActivity.LOGTAG, "Setting visible_area search to: " + visibleArea);
        this.visibleArea = visibleArea;
    }

    public OPBoundingBox getVisibleMapBB() {
        return visibleMapBB;
    }

    public void setVisibleMapBB(OPBoundingBox visibleMapBB) {
        this.visibleMapBB = visibleMapBB;
    }
}
