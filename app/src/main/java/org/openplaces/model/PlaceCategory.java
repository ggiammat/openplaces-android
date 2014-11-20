package org.openplaces.model;


import android.os.Parcel;
import android.os.Parcelable;

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
        if(this.getName() == null){
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeString(this.getName());
        }
        if(this.getType() == null){
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeString(this.getType());
        }
        if(this.getSymbol() == null){
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeString(this.getSymbol());
        }
    }

    public PlaceCategory(Parcel in){
        this.mDelegate = new OPPlaceCategoryImpl();
        this.setName(in.readByte() == 0x00 ? null : in.readString());
        this.setType(in.readByte() == 0x00 ? null : in.readString());
        this.setSymbol(in.readByte() == 0x00 ? null : in.readString());
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
    public String getType() {
        return this.mDelegate.getType();
    }

    @Override
    public void setType(String category) {
        this.mDelegate.setType(category);
    }

    @Override
    public String getName() {
        return this.mDelegate.getName();
    }

    @Override
    public void setName(String name) {
        this.mDelegate.setName(name);
    }

    @Override
    public void setOsmTagFilterGroups(List<OSMTagFilterGroup> osmTagFilterGroups) {
        this.mDelegate.setOsmTagFilterGroups(osmTagFilterGroups);
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
        this.mDelegate.setLocalization(localization);
    }

    @Override
    public String getFirstNameMatch(String text) {
        return this.mDelegate.getFirstNameMatch(text);
    }

    public String getSymbol(){
        return this.mDelegate.getSymbol();
    }

    public void setSymbol(String symbol){
        this.mDelegate.setSymbol(symbol);
    }

    @Override
    public int placeMatchesCategory(OPPlaceInterface place) {
        return this.mDelegate.placeMatchesCategory(place);
    }
}
