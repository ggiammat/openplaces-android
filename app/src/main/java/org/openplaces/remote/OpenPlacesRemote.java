package org.openplaces.remote;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import org.openplaces.MapActivity;
import org.openplaces.OPProviderResultObject;
import org.openplaces.OpenPlacesProvider;
import org.openplaces.locations.LocationsCacheManager;
import org.openplaces.model.OPGeoPoint;
import org.openplaces.model.OPLocationInterface;
import org.openplaces.model.OPPlaceInterface;
import org.openplaces.places.Place;
import org.openplaces.categories.PlaceCategoriesManager;
import org.openplaces.places.PlacesCacheManager;
import org.openplaces.search.ResultSet;
import org.openplaces.model.impl.OPLocationImpl;
import org.openplaces.search.SearchQuery;
import org.openplaces.utils.HttpHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.transform.Result;

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

            Place place = this.pcm.getCachedPlace(tokens[0], Long.valueOf(tokens[1]));
            if(place != null){
                //hit the cache
                Log.d(MapActivity.LOGTAG, "Placed was cached. Using it");
                cachedPlaces.add(place);
                getFromNetwork.remove(k);
            }
        }

        //get remaining places from net
        OPProviderResultObject res = this.opp.getPlacesByTypesAndIds(getFromNetwork);
        List<OPPlaceInterface> newPlacesFromNet = res.places;
        ResultSet rs = ResultSet.buildFromOPPlaces(newPlacesFromNet, PlaceCategoriesManager.getInstance(appContext));
        rs.setStat("errorCode", Integer.toString(res.errorCode));
        rs.setStat("errorMessage", res.errorMessage);
        //update cache for new places
        if(rs.size() > 0){
            this.pcm.updatePlacesCache(rs.getAllPlaces());
        }

        //add cached places to rs
        rs.addPlaces(cachedPlaces, PlaceCategoriesManager.getInstance(appContext));

        Log.d(MapActivity.LOGTAG,"getPlacesByTypeAndId -> Tot places: " + rs.size() +
                ", from cache: " + cachedPlaces.size() +
                ", from net: " + newPlacesFromNet.size());
        rs.setStat("net", Integer.toString(newPlacesFromNet.size()));
        rs.setStat("cache", Integer.toString(cachedPlaces.size()));

        return rs;
    }

    public ResultSet search(SearchQuery searchQuery){
        ResultSet rs = this.doSearch(searchQuery);

        //FIXME: should we cache also these places? Maybe we should cache only places in placelists
        this.pcm.updatePlacesCache(rs.getAllPlaces());

        rs.setStat("net", Integer.toString(rs.size()));
        rs.setStat("cache", "0");
        return rs;
    }

    private ResultSet doSearch(SearchQuery query){
        OPProviderResultObject res = null;
        ResultSet rs;

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

        //only locations. Goto locations
        else if(query.getSearchPlaceCategories().isEmpty() && query.getFreeTextQuery().isEmpty() && !query.getSearchLocations().isEmpty()){
            List<OPPlaceInterface> ps = new ArrayList<OPPlaceInterface>();
            for(OPLocationInterface l: query.getSearchLocations()){
                ps.add(l.getAsPlace());
            }
            res =  new OPProviderResultObject();
            res.places = ps;
        }
        else {

//            //if near me now, create a fake bounding box to search
//            if (query.isNearMeNow()) {
//                OPLocationImpl fakeLocation = new OPLocationImpl();
//                fakeLocation.setBoundingBox(GeoFunctions.generateBoundingBox(query.getCurrentLocation(), 50));
//                query.addSearchLocation(fakeLocation);
//            }

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

        rs = ResultSet.buildFromOPPlaces(res.places, PlaceCategoriesManager.getInstance(appContext));
        rs.setStat("errorCode", Integer.toString(res.errorCode));
        rs.setStat("errorMessage", res.errorMessage);

        return rs;
    }

    public List<OPLocationInterface> getLocationsByName(String name){
        //always trigger a newtork call
        OPProviderResultObject res = this.opp.getLocationsByName(name);

        //updates the cache
        this.lcm.updateLocationsCache(null, res.locations);
        return res.locations;
    }

    public ResultSet getKnownPlaces(){
        return ResultSet.buildFromOPPlaces(this.pcm.getCachedPlaces(), PlaceCategoriesManager.getInstance(this.appContext));
    }

    public List<OPLocationInterface> getKnownLocations(){
        return this.lcm.getCachedLocations();
    }

    public void updateKnownLocationsAround(Location point){

        if(this.lcm.containsLocationAround(point)){
            return;
        }

        OPProviderResultObject res = opp.getLocationsAround(new OPGeoPoint(point.getLatitude(), point.getLongitude()), AROUND_LOCATIONS_RADIUS);
        Log.d(MapActivity.LOGTAG, res.locations.size() + " locations fetched from network");

        //updates the cache
        this.lcm.updateLocationsCache(point, res.locations);
    }

}
