package org.openplaces.search;

import android.content.Context;

import org.openplaces.model.OPGeoPoint;
import org.openplaces.model.OPLocationInterface;

import java.util.List;

/**
 * Created by ggiammat on 11/17/14.
 */
public class LocationsCacheManager {

    private static LocationsCacheManager instance = null;
    private Context ctx;

    public static LocationsCacheManager getInstance(Context ctx){
        if(instance == null) {
            instance = new LocationsCacheManager(ctx);
        }

        return instance;
    }


    public LocationsCacheManager(Context ctx){
        this.ctx = ctx;
    }

    public boolean cacheContainsLocationsFor(OPGeoPoint point){
        return false;
    }


    public List<OPLocationInterface> getCachedLocations(){
        return null;
    }


    public void addToCache(List<OPLocationInterface> locations){

    }
}
