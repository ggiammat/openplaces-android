package org.openplaces.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by ggiammat on 11/12/14.
 */
public class PlaceCategory implements OPPlaceCategoryInterface, Parcelable {
    @Override
    public String getType() {
        return null;
    }

    @Override
    public void setType(String s) {

    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void setName(String s) {

    }

    @Override
    public void setOsmTagFilterGroups(List<OSMTagFilterGroup> filterGroups) {

    }

    @Override
    public List<OSMTagFilterGroup> getOsmTagFilterGroups() {
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
