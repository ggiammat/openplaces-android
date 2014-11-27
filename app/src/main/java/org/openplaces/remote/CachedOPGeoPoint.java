package org.openplaces.remote;

import org.openplaces.model.OPGeoPoint;

/**
 * Created by gabriele on 11/26/14.
 */
public class CachedOPGeoPoint extends OPGeoPoint {

    private long cacheInsertTime;


    public CachedOPGeoPoint(OPGeoPoint point, long cacheInsertTime) {
        super(point.getLat(), point.getLon());
        this.cacheInsertTime = cacheInsertTime;
    }

    public CachedOPGeoPoint(double lat, double lon) {
        super(lat, lon);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CachedOPGeoPoint that = (CachedOPGeoPoint) o;

        if (this.getLat() != that.getLat()) return false;
        if (this.getLon() != that.getLon()) return false;
        return true;
    }

    @Override
    public int hashCode() {
        //TODO: check if this is valid
        return (int) ((this.getLat()*this.getLon()));
    }

    public long getCacheInsertTime() {
        return cacheInsertTime;
    }

    public void setCacheInsertTime(long cacheInsertTime) {
        this.cacheInsertTime = cacheInsertTime;
    }
}
