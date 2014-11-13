package org.openplaces.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.openplaces.MapActivity;
import org.openplaces.model.impl.OPPlaceImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gabriele on 11/10/14.
 */
public class Place implements OPPlaceInterface, Parcelable {

    private OPPlaceInterface mDelegate;

    public Place(OPPlaceInterface place){
        Log.d(MapActivity.LOGTAG, "Creating place from " + place);
        this.mDelegate = place;
    }

    public Place(Parcel in){
        this.mDelegate = new OPPlaceImpl();
        this.setId(in.readLong()); //cannot be null
        this.setName(in.readByte() == 0x00 ? null : in.readString());
        this.setOsmType(in.readByte() == 0x00 ? null : in.readString());
        this.setAverageRating(in.readByte() == 0x00 ? null : in.readDouble());
        this.setNumReviews(in.readByte() == 0x00 ? null : in.readInt());
        if(in.readByte() == 0x00){
            this.setPosition(null);
        }
        else {
            this.setPosition(new OPGeoPoint(in.readDouble(), in.readDouble()));
        }
        this.setAddressString(in.readByte() == 0x00 ? null : in.readString());
        this.setType(in.readByte() == 0x00 ? null : in.readString());


        if(in.readByte() == 0x00){
            this.setAddressTokens(null);
        }
        else {
            int size = in.readInt();
            Map<String, String> addrTokens = new HashMap<String, String>();
            for (int i = 0; i < size; i++) {
                String key = in.readString();
                String value = in.readString();
                addrTokens.put(key, value);
            }
            this.setAddressTokens(addrTokens);
        }
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Place createFromParcel(Parcel in) {
            return new Place(in);
        }

        public Place[] newArray(int size) {
            return new Place[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {

        dest.writeLong(this.getId()); //cannot be null

        if(this.getName() == null){
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeString(this.getName());
        }

        if(this.getOsmType() == null){
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeString(this.getOsmType());
        }
        if (this.getAverageRating() == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(this.getAverageRating());
        }
        if (this.getNumReviews() == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(this.getNumReviews());
        }

        if(this.getPosition() == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeDouble(this.getPosition().getLat());
            dest.writeDouble(this.getPosition().getLon());
        }
        if(this.getAddressString() == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeString(this.getAddressString());
        }
        if(this.getType() == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeString(this.getType());
        }
        if(this.getAddressTokens() == null) {
            dest.writeByte((byte) (0x00));
        }
        else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(this.getAddressTokens().size());
            for(String key : this.getAddressTokens().keySet()){
                dest.writeString(key);
                dest.writeString(this.getAddressTokens().get(key));
            }
        }

    }

    @Override
    public String toString() {
        return this.mDelegate.toString();
    }

    @Override
    public String getName() {
        return this.mDelegate.getName();
    }

    @Override
    public void setName(String s) {
        this.mDelegate.setName(s);
    }

    @Override
    public String getOsmType() {
        return this.mDelegate.getOsmType();
    }

    @Override
    public void setOsmType(String s) {
        this.mDelegate.setOsmType(s);
    }

    @Override
    public Double getAverageRating() {
        return this.mDelegate.getAverageRating();
    }

    @Override
    public void setAverageRating(Double aDouble) {
        this.mDelegate.setAverageRating(aDouble);
    }

    @Override
    public Integer getNumReviews() {
        return this.mDelegate.getNumReviews();
    }

    @Override
    public void setNumReviews(Integer integer) {
        this.mDelegate.setNumReviews(integer);
    }

    @Override
    public long getId() {
        return this.mDelegate.getId();
    }

    @Override
    public void setId(long l) {
        this.mDelegate.setId(l);
    }

    @Override
    public OPGeoPoint getPosition() {
        return this.mDelegate.getPosition();
    }

    @Override
    public void setPosition(OPGeoPoint opGeoPoint) {
        this.mDelegate.setPosition(opGeoPoint);
    }

    @Override
    public String getAddressString() {
        return this.mDelegate.getAddressString();
    }

    @Override
    public void setAddressString(String s) {
        this.mDelegate.setAddressString(s);
    }

    @Override
    public Map<String, String> getAddressTokens() {
        return this.mDelegate.getAddressTokens();
    }

    @Override
    public void setAddressTokens(Map<String, String> stringStringMap) {
        this.mDelegate.setAddressTokens(stringStringMap);
    }

    @Override
    public String getType() {
        return this.mDelegate.getType();
    }

    @Override
    public void setType(String s) {
        this.mDelegate.setType(s);
    }
}
