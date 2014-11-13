package org.openplaces.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ggiammat on 11/12/14.
 */
public class Location implements OPLocationInterface, Parcelable {

    private OPLocationInterface mDelegate;

    public Location(OPLocationInterface mDelegate){
        this.mDelegate = mDelegate;
    }

    @Override
    public String getType() {
        return this.mDelegate.getType();
    }

    @Override
    public void setType(String s) {

    }

    @Override
    public String getDisplayName() {
        return this.mDelegate.getDisplayName();
    }

    @Override
    public void setDisplayName(String s) {

    }

    @Override
    public long getId() {
        return this.mDelegate.getId();
    }

    @Override
    public void setId(long l) {

    }

    @Override
    public OPBoundingBox getBoundingBox() {
        return this.mDelegate.getBoundingBox();
    }

    @Override
    public void setBoundingBox(OPBoundingBox opBoundingBox) {

    }

    @Override
    public OPGeoPoint getPosition() {
        return this.mDelegate.getPosition();
    }

    @Override
    public void setPosition(OPGeoPoint opGeoPoint) {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
