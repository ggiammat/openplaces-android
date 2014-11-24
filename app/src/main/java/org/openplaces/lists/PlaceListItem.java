package org.openplaces.lists;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by gabriele on 11/24/14.
 */
public class PlaceListItem {

    private long osmId;
    private String osmType;

    private long addedDate;
    private long modifiedDate;

    private String note;

    public PlaceListItem(long osmId, String osmType){
        this.osmId = osmId;
        this.osmType = osmType;
        this.addedDate = new Date().getTime();
        this.modifiedDate = this.addedDate;
    }


    public long getOsmId() {
        return osmId;
    }

    public void setOsmId(long osmId) {
        this.osmId = osmId;
    }

    public String getOsmType() {
        return osmType;
    }

    public void setOsmType(String osmType) {
        this.osmType = osmType;
    }

    public long getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(long addedDate) {
        this.addedDate = addedDate;
    }

    public long getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(long modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlaceListItem that = (PlaceListItem) o;

        if (osmId != that.osmId) return false;
        if (osmType != null ? !osmType.equals(that.osmType) : that.osmType != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (osmId ^ (osmId >>> 32));
        result = 31 * result + (osmType != null ? osmType.hashCode() : 0);
        return result;
    }
}
