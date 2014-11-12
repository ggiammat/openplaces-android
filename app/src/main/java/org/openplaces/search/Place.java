package org.openplaces.search;

import android.os.Parcel;
import android.os.Parcelable;

import org.openplaces.model.OPPlace;
import org.openplaces.model.OverpassElement;
import org.osmdroid.util.GeoPoint;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gabriele on 11/10/14.
 */
public class Place extends OPPlace implements Parcelable {

    private GeoPoint placePosition;

    public Place(OverpassElement el) {
        super(el);
    }

    public Place(OPPlace opp){
        super();
        this.setName(opp.getName());
        this.setId(opp.getId());
        this.setPosition(opp.getPosition()); //not parcelable, so use placePostion to keep the coordinates
        this.setPlacePosition(new GeoPoint(opp.getPosition().getLat(), opp.getPosition().getLon()));
        this.setAddressTokens(opp.getAddressTokens());
    }


    public Place(Parcel in){
        this.setId(in.readLong()); //cannot be null
        this.setName(in.readByte() == 0x00 ? null : in.readString());
        this.setOsmType(in.readByte() == 0x00 ? null : in.readString());
        this.setAverageRating(in.readByte() == 0x00 ? null : in.readDouble());
        this.setNumReviews(in.readByte() == 0x00 ? null : in.readInt());
        this.setPlacePosition(in.readByte() == 0x00 ? null : (GeoPoint) in.readValue(GeoPoint.class.getClassLoader()));
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

        if(this.getPlacePosition() == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeValue(this.getPlacePosition());
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
        return super.toString();
    }

    public GeoPoint getPlacePosition() {
        return placePosition;
    }

    public void setPlacePosition(GeoPoint placePosition) {
        this.placePosition = placePosition;
    }
}
