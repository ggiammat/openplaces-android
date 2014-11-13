package org.openplaces.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;
import java.util.Map;

/**
 * Created by ggiammat on 11/12/14.
 */
public class PlaceCategory implements OPPlaceCategoryInterface, Parcelable {

    private OPPlaceCategoryInterface mDelegate;

    public PlaceCategory(OPPlaceCategoryInterface mDelegate){
        this.mDelegate = mDelegate;
    }


    public String getFirstMatchingName(String text){
        if(this.getName().toLowerCase().contains(text)){
            return this.getName();
        }
        for(String n: this.getLocalizedNames().values()){
            if(n.toLowerCase().contains(text)){
                return n;
            }
        }

        return null;
    }

    @Override
    public String getType() {
        return this.mDelegate.getType();
    }

    @Override
    public void setType(String s) {

    }

    @Override
    public String getName() {
        return this.mDelegate.getName();
    }

    @Override
    public void setName(String s) {

    }

    @Override
    public void setOsmTagFilterGroups(List<OSMTagFilterGroup> filterGroups) {

    }

    @Override
    public List<OSMTagFilterGroup> getOsmTagFilterGroups() {
        return this.mDelegate.getOsmTagFilterGroups();
    }

    @Override
    public Map<String, String> getLocalizedNames() {
        return this.mDelegate.getLocalizedNames();
    }

    @Override
    public void setLocalization(Map<String, String> localization) {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }



}
