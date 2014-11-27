package org.openplaces.remote;

import android.content.Context;
import android.util.Log;

import com.google.gson.reflect.TypeToken;

import org.openplaces.MapActivity;
import org.openplaces.OpenPlacesProvider;
import org.openplaces.cache.PersistenceManager;
import org.openplaces.model.OPGeoPoint;
import org.openplaces.model.OPLocationInterface;
import org.openplaces.utils.HttpHelper;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by gabriele on 11/26/14.
 */
public class OpenPlacesRemote {

    public static final int AROUND_LOCATIONS_RADIUS = 10; //km
    public static final int MAX_DISTANCE_FOR_SAME_AROUND_LOCATIONS = 1; //km

    public static final String LOCATIONS_CACHE_FILE = "locations.json";
    public static final String AROUND_POINTS_CACHE_FILE = "around-points.json";
    public static final long LOCATIONS_CACHE_TTL = 24 * 60 * 60 * 1000;// 1 day in ms

    private static OpenPlacesRemote instance;

    public static OpenPlacesRemote getInstance(Context appContext){
        if(instance == null){
            instance = new OpenPlacesRemote(appContext);
        }
        return instance;
    }


    private Context appContext;
    private OpenPlacesProvider opp;


    private Set<CachedLocation> cachedLocations;
    private Set<CachedOPGeoPoint> cachedAroundLocations;

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

        this.loadCachedLocations();
        this.loadCachedAroundLocations();
    }


    private void loadCachedLocations(){

        this.cachedLocations = (Set<CachedLocation>) PersistenceManager.gsonDeSerializer(LOCATIONS_CACHE_FILE,
                new TypeToken<Set<CachedLocation>>(){}.getType());

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
    }

    private void loadCachedAroundLocations(){
        this.cachedAroundLocations = (Set<CachedOPGeoPoint>) PersistenceManager.gsonDeSerializer(AROUND_POINTS_CACHE_FILE,
                new TypeToken<Set<CachedOPGeoPoint>>(){}.getType());
        if(this.cachedAroundLocations == null){
            Log.d(MapActivity.LOGTAG, "No cached around points found. Initializing an empty cache");
            this.cachedAroundLocations = new HashSet<CachedOPGeoPoint>();
        }
        else {
            Log.d(MapActivity.LOGTAG, this.cachedAroundLocations.size() + " cached around points loaded");
        }

        //remove points older than ttl
        long now = new Date().getTime();
        for(CachedOPGeoPoint p: new HashSet<CachedOPGeoPoint>(this.cachedAroundLocations)){
            if(p.getCacheInsertTime() + LOCATIONS_CACHE_TTL < now){
                this.cachedAroundLocations.remove(p);
                Log.d(MapActivity.LOGTAG, "Cached around point " + p + " removed because ttl expired");
            }
        }
    }

    public void storeCachedLocations(){
        boolean res = PersistenceManager.gsonSerializer(LOCATIONS_CACHE_FILE, this.cachedLocations);
        if(res){
            Log.d(MapActivity.LOGTAG, "Cached locations stored");
        }
        else {
            Log.d(MapActivity.LOGTAG, "Impossible to store cached locations");
        }
    }

    public void storeCachedAroundLocations(){
        boolean res = PersistenceManager.gsonSerializer(AROUND_POINTS_CACHE_FILE, this.cachedAroundLocations);
        if(res){
            Log.d(MapActivity.LOGTAG, "Cached around points stored");
        }
        else {
            Log.d(MapActivity.LOGTAG, "Impossible to store cached around points");
        }
    }

    private void updateCachedLocations(List<OPLocationInterface> locs){
        for(OPLocationInterface l: locs){
            CachedLocation cl = new CachedLocation(l, new Date().getTime());
            this.cachedLocations.remove(cl);
            this.cachedLocations.add(cl);
        }
        this.storeCachedLocations();
    }

    private void updateCachedAroundLocations(OPGeoPoint point){
        CachedOPGeoPoint cp = new CachedOPGeoPoint(point, new Date().getTime());
        this.cachedAroundLocations.remove(cp);
        this.cachedAroundLocations.add(cp);
        this.storeCachedAroundLocations();
    }

    public List<OPLocationInterface> getLocationsByName(String name){
        //always trigger a newtork call
        List<OPLocationInterface> res = this.opp.getLocationsByName(name);

        //updates the cache
        this.updateCachedLocations(res);
        return res;
    }

    public List<OPLocationInterface> getKnownLocations(){
        List<OPLocationInterface> res = new ArrayList<OPLocationInterface>();
        res.addAll(this.cachedLocations);
        return res;
    }

    public void updateKnownLocationsAround(GeoPoint location){
        OPGeoPoint opLocation = new OPGeoPoint(location.getLatitude(), location.getLongitude());

        for(OPGeoPoint p: this.getCachedAroundLocations()){
            if(opLocation.distanceFrom(p) <= MAX_DISTANCE_FOR_SAME_AROUND_LOCATIONS){
                Log.d(MapActivity.LOGTAG, "Around location not updated because already in cache");
                return; //do nothing we assume the locations are already in the cache
            }
        }

        List<OPLocationInterface> res = opp.getLocationsAround(opLocation, AROUND_LOCATIONS_RADIUS);
        Log.d(MapActivity.LOGTAG, res.size() + " locations fetched from network");

        //updates the cache
        this.updateCachedAroundLocations(opLocation);
        this.updateCachedLocations(res);
    }



    private Set<CachedLocation> getCachedLocations() {
        return cachedLocations;
    }

    private void setCachedLocations(Set<CachedLocation> cachedLocations) {
        this.cachedLocations = cachedLocations;
    }

    private Set<CachedOPGeoPoint> getCachedAroundLocations() {
        return cachedAroundLocations;
    }

    private void setCachedAroundLocations(Set<CachedOPGeoPoint> cachedAroundLocations) {
        this.cachedAroundLocations = cachedAroundLocations;
    }
}
