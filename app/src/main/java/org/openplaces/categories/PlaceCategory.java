package org.openplaces.categories;


import android.os.Parcel;
import android.os.Parcelable;

import org.openplaces.model.OPPlaceCategoryInterface;
import org.openplaces.model.OPPlaceInterface;
import org.openplaces.model.OSMTagFilterGroup;
import org.openplaces.model.impl.OPPlaceCategoryImpl;

import java.util.List;
import java.util.Map;

/**
 * Created by gabriele on 11/20/14.
 */
public class PlaceCategory implements OPPlaceCategoryInterface, Parcelable {
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        //TODO add localized names and OSMTagFilters to parcel
        if(this.getId() == null){
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeString(this.getId());
        }

        if(this.getIcon() == null){
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeString(this.getIcon());
        }

        dest.writeInt(this.getPriority());
    }

    public PlaceCategory(Parcel in){
        this.mDelegate = new OPPlaceCategoryImpl();
        this.setId(in.readByte() == 0x00 ? null : in.readString());
        this.setIcon(in.readByte() == 0x00 ? null : in.readString());
        this.setPriority(in.readInt());
    }


    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public PlaceCategory createFromParcel(Parcel in) {
            return new PlaceCategory(in);
        }

        public PlaceCategory[] newArray(int size) {
            return new PlaceCategory[size];
        }
    };

    private OPPlaceCategoryInterface mDelegate;

    public PlaceCategory(OPPlaceCategoryInterface cat){
        this.mDelegate = cat;
    }


    @Override
    public void setOsmTagFilterGroups(List<OSMTagFilterGroup> osmTagFilterGroups) {
        this.mDelegate.setOsmTagFilterGroups(osmTagFilterGroups);
    }

    @Override
    public String getId() {
        return this.mDelegate.getId();
    }

    @Override
    public void setId(String s) {
        this.mDelegate.setId(s);
    }

    @Override
    public List<OSMTagFilterGroup> getOsmTagFilterGroups() {
        return this.mDelegate.getOsmTagFilterGroups();
    }

    @Override
    public Map<String, String> getNames() {
        return null;
    }

    @Override
    public void setNames(Map<String, String> stringStringMap) {

    }

    @Override
    public String getFirstNameMatch(String text) {
        return this.mDelegate.getFirstNameMatch(text);
    }

    @Override
    public boolean placeMatchesCategory(OPPlaceInterface place) {
        return this.mDelegate.placeMatchesCategory(place);
    }

    @Override
    public void setPriority(int i) {
        this.mDelegate.setPriority(i);
    }

    @Override
    public String getIcon() {
        return this.mDelegate.getIcon();
    }

    @Override
    public void setIcon(String s) {
     this.mDelegate.setIcon(s);
    }

    @Override
    public int getPriority() {
        return this.mDelegate.getPriority();
    }


    @Override
    public String toString() {
        return this.mDelegate.toString();
    }
}
