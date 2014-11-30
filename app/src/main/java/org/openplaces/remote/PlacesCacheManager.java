package org.openplaces.remote;

import android.content.Context;
import android.util.Log;

import com.google.gson.reflect.TypeToken;

import org.openplaces.MapActivity;
import org.openplaces.cache.PersistenceManager;
import org.openplaces.model.OPLocationInterface;
import org.openplaces.model.OPPlaceInterface;
import org.openplaces.model.Place;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by gabriele on 11/27/14.
 */
public class PlacesCacheManager {

    public static final String PLACES_CACHE_FILE = "places.json";
    public static final long PLACES_CACHE_TTL = 24 * 60 * 60 * 1000;// 1 day in ms


    private static PlacesCacheManager instance;

    public static PlacesCacheManager getInstance(Context appContext){
        if(instance == null){
            instance = new PlacesCacheManager(appContext);
        }
        return instance;
    }


    private Context appContext;

    private Map<String, CachedPlace> placesCache;

    private PlacesCacheManager(Context appContext){
        this.appContext = appContext;
        this.loadCache();
    }


    public Place getCachedPlace(String osmType, long id){
        Log.d(MapActivity.LOGTAG, "Getting cached place " + osmType + ":" + id);
        return this.placesCache.get(CachedPlace.getCacheKey(osmType, id));
    }

    public List<OPPlaceInterface> getCachedPlaces(){
        return new ArrayList<OPPlaceInterface>(this.placesCache.values());
    }

    private void loadCache(){

        //load cached places from storage
        this.placesCache = (Map<String, CachedPlace>) PersistenceManager.gsonDeSerializer(PLACES_CACHE_FILE,
                new TypeToken<Map<String, CachedPlace>>() {}.getType());
        if(this.placesCache == null){
            Log.d(MapActivity.LOGTAG, "No cached places found. Initializing an empty cache");
            this.placesCache = new HashMap<String, CachedPlace>();
        }
        else {
            Log.d(MapActivity.LOGTAG, this.placesCache.size() + " cached places loaded");
        }

        //remove places older than ttl
        long now = new Date().getTime();
        for(String pKey: new HashSet<String>(this.placesCache.keySet())){
            if(this.placesCache.get(pKey).getCacheInsertTime() + PLACES_CACHE_TTL < now){
                this.placesCache.remove(pKey);
                Log.d(MapActivity.LOGTAG, "Cached place " + this.placesCache.get(pKey) + " removed because ttl expired");
            }
        }
    }

    public void updatePlacesCache(List<Place> places){
        //update locations
        for(Place l: places){
            Log.d(MapActivity.LOGTAG, "Updating cache for " + l);
            CachedPlace cl = null;
            if(l instanceof CachedPlace){
                cl = (CachedPlace) l;
            }
            else {
                cl = new CachedPlace(l, new Date().getTime());
            }
            this.placesCache.remove(cl);
            this.placesCache.put(cl.getCacheKey(), cl);
        }

        this.storeCache();
    }

    public void updatePlacesCache(Place place){
        CachedPlace cl = new CachedPlace(place, new Date().getTime());
        this.placesCache.remove(cl);
        this.placesCache.put(cl.getCacheKey(), cl);

        this.storeCache();
    }

    private void storeCache(){
        Log.d(MapActivity.LOGTAG, "Storing cached pleaces: " + this.placesCache.toString());
        boolean res = PersistenceManager.gsonSerializer(PLACES_CACHE_FILE, this.placesCache);
        if(res){
            Log.d(MapActivity.LOGTAG, "Cached places stored");
        }
        else {
            Log.d(MapActivity.LOGTAG, "Impossible to store cached places");
        }
    }
}
