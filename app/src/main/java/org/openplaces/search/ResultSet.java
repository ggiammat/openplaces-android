package org.openplaces.search;

import android.os.Parcel;
import android.os.Parcelable;

import org.openplaces.model.OPPlace;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gabriele on 11/10/14.
 */
public class ResultSet implements Parcelable {

    private List<Place> places;

    public ResultSet(){
        this.places = new ArrayList<Place>();
    }

    public static ResultSet buildFromOPPlaces(List<OPPlace> opPlaces) {
        ResultSet rs = new ResultSet();

        for (OPPlace opp : opPlaces) {
            rs.addPlace(new Place(opp));
        }
        return rs;
    }

    public void addPlace(Place place){
        this.places.add(place);
    }


    public ResultSet(Parcel in){
        List<Place> places = new ArrayList<Place>();
        in.readList(places, Place.class.getClassLoader());
        this.places = places;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public ResultSet createFromParcel(Parcel in) {
            return new ResultSet(in);
        }

        public ResultSet[] newArray(int size) {
            return new ResultSet[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeList(this.places);
    }


    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("=== Content of ResultSet ===\n");
        sb.append("Places ("+this.places.size()+"):\n");
        for(Place place: this.places){
            sb.append("* "+ place.toString()+"\n");
        }

        return sb.toString();
    }
}
