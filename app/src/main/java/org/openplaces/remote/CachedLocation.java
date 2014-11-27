package org.openplaces.remote;

import org.openplaces.model.OPBoundingBox;
import org.openplaces.model.OPGeoPoint;
import org.openplaces.model.OPLocationInterface;
import org.openplaces.model.impl.OPLocationImpl;

/**
 * Created by gabriele on 11/26/14.
 */
public class CachedLocation extends OPLocationImpl {

    private long cacheInsertTime;

    public CachedLocation(OPLocationInterface loc, long cacheInsertTime){
        this.cacheInsertTime = cacheInsertTime;
        this.setBoundingBox(loc.getBoundingBox());
        this.setPosition(loc.getPosition());
        this.setType(loc.getType());
        this.setId(loc.getId());
        this.setDisplayName(loc.getDisplayName());
    }
//
//    transient private OPLocationInterface mDelegate;
//
//    private long cacheInsertTime;
//
//    public CachedLocation(OPLocationInterface mDelegate, long cacheInsertTime){
//        this.mDelegate = mDelegate;
//        this.cacheInsertTime = cacheInsertTime;
//    }
//
//    @Override
//    public String getType() {
//        return this.mDelegate.getType();
//    }
//
//    @Override
//    public void setType(String s) {
//        this.mDelegate.setType(s);
//    }
//
//    @Override
//    public String getDisplayName() {
//        return this.mDelegate.getDisplayName();
//    }
//
//    @Override
//    public void setDisplayName(String s) {
//        this.mDelegate.setDisplayName(s);
//    }
//
//    @Override
//    public long getId() {
//        return this.mDelegate.getId();
//    }
//
//    @Override
//    public void setId(long l) {
//        this.mDelegate.setId(l);
//    }
//
//    @Override
//    public OPBoundingBox getBoundingBox() {
//        return this.mDelegate.getBoundingBox();
//    }
//
//    @Override
//    public void setBoundingBox(OPBoundingBox opBoundingBox) {
//        this.mDelegate.setBoundingBox(opBoundingBox);
//    }
//
//    @Override
//    public OPGeoPoint getPosition() {
//        return this.mDelegate.getPosition();
//    }
//
//    @Override
//    public void setPosition(OPGeoPoint opGeoPoint) {
//        this.mDelegate.setPosition(opGeoPoint);
//    }
//
//    public long getCacheInsertTime() {
//        return cacheInsertTime;
//    }
//
//    public void setCacheInsertTime(long cacheInsertTime) {
//        this.cacheInsertTime = cacheInsertTime;
//    }
//
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CachedLocation that = (CachedLocation) o;

        if (this.getId() != that.getId()) return false;

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
