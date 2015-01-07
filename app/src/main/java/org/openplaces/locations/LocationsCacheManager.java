package org.openplaces.locations;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.gson.reflect.TypeToken;

import org.openplaces.MapActivity;
import org.openplaces.util.PersistenceManager;
import org.openplaces.model.OPGeoPoint;
import org.openplaces.model.OPLocationInterface;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by gabriele on 11/27/14.
 */
public class LocationsCacheManager {

    public static final int MAX_DISTANCE_FOR_SAME_AROUND_LOCATIONS = 1; //km
    public static final String LOCATIONS_CACHE_FILE = "locations.json";
    public static final String AROUND_POINTS_CACHE_FILE = "around-points.json";
    public static final long LOCATIONS_CACHE_TTL = 60 * 60 * 1000;// 1 hour in ms



    private static LocationsCacheManager instance;

    public static LocationsCacheManager getInstance(Context appContext){
        if(instance == null){
            instance = new LocationsCacheManager(appContext);
        }
        return instance;
    }


    private Context appContext;

    private Set<CachedLocation> cachedLocations;
    private Set<CachedOPGeoPoint> cachedAroundPoints;


    private LocationsCacheManager(Context appContext){
        this.appContext = appContext;
        this.loadCache();
    }


    public List<OPLocationInterface> getCachedLocations(){
        return new ArrayList<OPLocationInterface>(this.cachedLocations);
    }

    private void loadCache(){

        //load cached locations from storage
        this.cachedLocations = (Set<CachedLocation>) PersistenceManager.gsonDeSerializer(LOCATIONS_CACHE_FILE,
                new TypeToken<Set<CachedLocation>>() {
                }.getType());
        if(this.cachedLocations == null){
            Log.d(MapActivity.LOGTAG, "No cached locations found. Initializing an empty cache");
            this.cachedLocations = new HashSet<CachedLocation>();
        }
        else {
            Log.d(MapActivity.LOGTAG, this.cachedLocations.size() + " cached locations loaded");
        }

        //remove locations older than ttl
        long now = new Date().getTime();
        for(CachedLocation loc: new HashSet<CachedLocation>(this.cachedLocations)){
            if(loc.getCacheInsertTime() + LOCATIONS_CACHE_TTL < now){
                this.cachedLocations.remove(loc);
                Log.d(MapActivity.LOGTAG, "Cached location " + loc + " removed because ttl expired");
            }
        }

        //load around points from storage
        this.cachedAroundPoints = (Set<CachedOPGeoPoint>) PersistenceManager.gsonDeSerializer(AROUND_POINTS_CACHE_FILE,
                new TypeToken<Set<CachedOPGeoPoint>>(){}.getType());
        if(this.cachedAroundPoints == null){
            Log.d(MapActivity.LOGTAG, "No cached around points found. Initializing an empty cache");
            this.cachedAroundPoints = new HashSet<CachedOPGeoPoint>();
        }
        else {
            Log.d(MapActivity.LOGTAG, this.cachedAroundPoints.size() + " cached around points loaded");
        }

        //remove around points older than ttl
        for(CachedOPGeoPoint p: new HashSet<CachedOPGeoPoint>(this.cachedAroundPoints)){
            if(p.getCacheInsertTime() + LOCATIONS_CACHE_TTL < now){
                this.cachedAroundPoints.remove(p);
                Log.d(MapActivity.LOGTAG, "Cached around point " + p + " removed because ttl expired");
            }
        }
    }

    public boolean containsLocationAround(Location aroundPoint){
        OPGeoPoint provPoint = new OPGeoPoint(aroundPoint.getLatitude(), aroundPoint.getLongitude());
        for(OPGeoPoint p: this.cachedAroundPoints){
            if(provPoint.distanceFrom(p) <= MAX_DISTANCE_FOR_SAME_AROUND_LOCATIONS){
                return true;
            }
        }
        return false;
    }

    public void updateLocationsCache(Location aroundPoint, List<OPLocationInterface> locs){
        //update locations
        for(OPLocationInterface l: locs){
            CachedLocation cl = new CachedLocation(l, new Date().getTime());
            this.cachedLocations.remove(cl);
            this.cachedLocations.add(cl);
        }

        //update around point if provided
        if(aroundPoint != null){
            CachedOPGeoPoint cp = new CachedOPGeoPoint(new OPGeoPoint(aroundPoint.getLatitude(), aroundPoint.getLongitude()), new Date().getTime());
            this.cachedAroundPoints.remove(cp);
            this.cachedAroundPoints.add(cp);
        }

        this.storeCache();
    }


    private void storeCache(){
        boolean res = PersistenceManager.gsonSerializer(LOCATIONS_CACHE_FILE, this.cachedLocations);
        if(res){
            Log.d(MapActivity.LOGTAG, "Cached locations stored");
        }
        else {
            Log.d(MapActivity.LOGTAG, "Impossible to store cached locations");
        }

        res = PersistenceManager.gsonSerializer(AROUND_POINTS_CACHE_FILE, this.cachedAroundPoints);
        if(res){
            Log.d(MapActivity.LOGTAG, "Cached around points stored");
        }
        else {
            Log.d(MapActivity.LOGTAG, "Impossible to store cached around points");
        }
    }
}
