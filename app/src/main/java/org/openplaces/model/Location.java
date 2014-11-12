package org.openplaces.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ggiammat on 11/12/14.
 */
public class Location implements OPLocationInterface, Parcelable {
    @Override
    public String getType() {
        return null;
    }

    @Override
    public void setType(String s) {

    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public void setDisplayName(String s) {

    }

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public void setId(long l) {

    }

    @Override
    public OPBoundingBox getBoundingBox() {
        return null;
    }

    @Override
    public void setBoundingBox(OPBoundingBox opBoundingBox) {

    }

    @Override
    public OPGeoPoint getPosition() {
        return null;
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
