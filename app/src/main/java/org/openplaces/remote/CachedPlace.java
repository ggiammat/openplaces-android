package org.openplaces.remote;

import org.openplaces.model.OPPlaceInterface;
import org.openplaces.model.Place;

/**
 * Created by gabriele on 11/27/14.
 */
public class CachedPlace extends Place {


    private long cacheInsertTime;


    public CachedPlace(Place place, long cacheInsertTime){
        super(place.getmDelegate());
        this.cacheInsertTime = cacheInsertTime;
    }

    public String getCacheKey(){
        return getCacheKey(this.getOsmType(), this.getId());
    }

    public static String getCacheKey(String osmType, long id){
        return osmType + ":" + id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CachedPlace that = (CachedPlace) o;

        if (this.getCacheKey() != that.getCacheKey()) return false;

        return true;
    }

    @Override
    public int hashCode() {
        //TODO: check if this is valid
        return (int) ((this.getId()*this.getId()));
    }

    public long getCacheInsertTime() {
        return cacheInsertTime;
    }

    public void setCacheInsertTime(long cacheInsertTime) {
        this.cacheInsertTime = cacheInsertTime;
    }
}
