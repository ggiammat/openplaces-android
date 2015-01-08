package org.openplaces.locations;

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
        this.setOsmTags(loc.getOsmTags());
    }

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
