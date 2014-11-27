package org.openplaces.remote;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import org.openplaces.MapActivity;
import org.openplaces.OpenPlacesProvider;
import org.openplaces.model.OPGeoPoint;
import org.openplaces.model.OPLocationInterface;
import org.openplaces.model.OPPlaceInterface;
import org.openplaces.model.Place;
import org.openplaces.model.PlaceCategoriesManager;
import org.openplaces.model.ResultSet;
import org.openplaces.model.impl.OPLocationImpl;
import org.openplaces.search.SearchQueryBuilder;
import org.openplaces.utils.GeoFunctions;
import org.openplaces.utils.HttpHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by gabriele on 11/26/14.
 */
public class OpenPlacesRemote {

    public static final int AROUND_LOCATIONS_RADIUS = 10; //km

    private static OpenPlacesRemote instance;

    public static OpenPlacesRemote getInstance(Context appContext){
        if(instance == null){
            instance = new OpenPlacesRemote(appContext);
        }
        return instance;
    }


    private Context appContext;
    private OpenPlacesProvider opp;
    private LocationsCacheManager lcm;
    private PlacesCacheManager pcm;


    private OpenPlacesRemote(Context appContext){
        this.appContext = appContext;

        //TODO: get parameters from app preferences
        this.opp = new OpenPlacesProvider(
                new HttpHelper(),
                "gabriele.giammatteo@gmail.com",
                OpenPlacesProvider.NOMINATIM_SERVER,
                OpenPlacesProvider.OVERPASS_SERVER,
                OpenPlacesProvider.REVIEW_SERVER_SERVER
        );

        this.lcm = LocationsCacheManager.getInstance(appContext);
        this.pcm = PlacesCacheManager.getInstance(appContext);
    }


    public ResultSet getPlacesByTypesAndIds(Set<String> placesTypesAndIds) {

        Log.d(MapActivity.LOGTAG,"getPlacesByTypeAndId -> getting places " + placesTypesAndIds.toString());

        List<Place> cachedPlaces = new ArrayList<Place>();

        Set<String> getFromNetwork = new HashSet<String>(placesTypesAndIds);

        //remove invalid keys and places that are in the cache
        for(String k: placesTypesAndIds){
            String[] tokens = k.split(":");
            if(tokens.length != 2){
                //invalid placeKey. Ignoring
                getFromNetwork.remove(k);
                continue;
            }

//            Place place = this.pcm.getCachedPlace(tokens[0], Long.valueOf(tokens[1]));
//            if(place != null){
//                //hit the cache
//                Log.d(MapActivity.LOGTAG, "Placed was cached. Using it");
//                cachedPlaces.add(place);
//                getFromNetwork.remove(k);
//            }
        }

        //get remaining places from net
        List<OPPlaceInterface> newPlacesFromNet = this.opp.getPlacesByTypesAndIds(getFromNetwork);
        ResultSet rs = ResultSet.buildFromOPPlaces(newPlacesFromNet, PlaceCategoriesManager.getInstance(appContext));

        //update cache for new places
//        if(rs.size() > 0){
//            this.pcm.updatePlacesCache(rs.getAllPlaces());
//        }

        //add cached places to rs
        rs.addPlaces(cachedPlaces);

        Log.d(MapActivity.LOGTAG,"getPlacesByTypeAndId -> Tot places: " + rs.size() +
                ", from cache: " + cachedPlaces.size() +
                ", from net: " + newPlacesFromNet.size());

        return rs;
    }

    public ResultSet search(SearchQueryBuilder searchQuery){
        List<OPPlaceInterface> res = this.doSearch(searchQuery);
        ResultSet rs = ResultSet.buildFromOPPlaces(res, PlaceCategoriesManager.getInstance(this.appContext));

        //FIXME: should we cache also these places? Maybe we should cache only places in placelists
        //this.pcm.updatePlacesCache(rs.getAllPlaces());

        return rs;
    }

    private List<OPPlaceInterface> doSearch(SearchQueryBuilder query){
        List<OPPlaceInterface> res = new ArrayList<OPPlaceInterface>();

        //no categories and locations set. Pure free text search via Nominatim
        if(query.getSearchLocations().isEmpty() && query.getSearchPlaceCategories().isEmpty() && !query.getFreeTextQuery().isEmpty()){
            res = this.opp.getPlacesByFreeQuery(query.getFreeTextQuery());
        }
        //only place categories (and free text selected). Search in the current bounding box
        else if(query.getSearchLocations().isEmpty() && !query.isNearMeNow() && !query.getSearchPlaceCategories().isEmpty()){
            OPLocationImpl fakeLocationVisibleMap = new OPLocationImpl();
            fakeLocationVisibleMap.setBoundingBox(query.getVisibleMapBB());
            query.addSearchLocation(fakeLocationVisibleMap);

            if(query.getFreeTextQuery() != null && !query.getFreeTextQuery().trim().isEmpty()){
                res = this.opp.getPlaces(query.getSearchPlaceCategories(), query.getSearchLocations(), query.getFreeTextQuery());
            }
            else{
                res = this.opp.getPlaces(query.getSearchPlaceCategories(), query.getSearchLocations());
            }
        }
        else {

            //if near me now, create a fake bounding box to search
            if (query.isNearMeNow()) {
                OPLocationImpl fakeLocation = new OPLocationImpl();
                fakeLocation.setBoundingBox(GeoFunctions.generateBoundingBox(query.getCurrentLocation(), 50));
                query.addSearchLocation(fakeLocation);
            }

            if(query.isVisibleArea()){
                OPLocationImpl fakeLocationVisibleMap = new OPLocationImpl();
                fakeLocationVisibleMap.setBoundingBox(query.getVisibleMapBB());
                query.addSearchLocation(fakeLocationVisibleMap);
            }

            if(query.getFreeTextQuery() != null && !query.getFreeTextQuery().trim().isEmpty()){
                res = this.opp.getPlaces(query.getSearchPlaceCategories(), query.getSearchLocations(), query.getFreeTextQuery());
            }
            else{
                res = this.opp.getPlaces(query.getSearchPlaceCategories(), query.getSearchLocations());
            }

        }

        return res;
    }

    public List<OPLocationInterface> getLocationsByName(String name){
        //always trigger a newtork call
        List<OPLocationInterface> res = this.opp.getLocationsByName(name);

        //updates the cache
        this.lcm.updateLocationsCache(null, res);
        return res;
    }

    public List<OPPlaceInterface> getKnownPlaces(){
        return this.pcm.getCachedPlaces();
    }

    public List<OPLocationInterface> getKnownLocations(){
        return this.lcm.getCachedLocations();
    }

    public void updateKnownLocationsAround(Location point){

        if(this.lcm.containsLocationAround(point)){
            return;
        }

        List<OPLocationInterface> res = opp.getLocationsAround(new OPGeoPoint(point.getLatitude(), point.getLongitude()), AROUND_LOCATIONS_RADIUS);
        Log.d(MapActivity.LOGTAG, res.size() + " locations fetched from network");

        //updates the cache
        this.lcm.updateLocationsCache(point, res);
    }

}
