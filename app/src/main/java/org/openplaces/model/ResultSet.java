package org.openplaces.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.openplaces.MapActivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by gabriele on 11/10/14.
 */
public class ResultSet implements Parcelable, Iterable<Place> {

    private List<Place> places;

    private int selectedIndex;

    public ResultSet(){
        this.places = new ArrayList<Place>();
        this.selectedIndex = -1;
    }

    public static ResultSet buildFromOPPlaces(List<OPPlaceInterface> opPlaces, PlaceCategoriesManager catMan) {
        ResultSet rs = new ResultSet();

        for (OPPlaceInterface opp : opPlaces) {
            Place p = new Place(opp);
            rs.addPlace(p, catMan);
        }
        return rs;
    }

    public List<Place> getAllPlaces(){
        return new ArrayList<Place>(this.places);
    }

    public void addPlace(Place place, PlaceCategoriesManager catMan){
        place.setCategory(catMan.getPlaceCategory(place));
        Log.d(MapActivity.LOGTAG, "Place matched category " + place.getCategory());
        this.places.add(place);
    }

    public void addPlaces(Collection<Place> places, PlaceCategoriesManager catMan){
        for (Place p : places) {
            this.addPlace(p, catMan);
        }
    }

    public int indexOf(Place place){
        return this.places.indexOf(place);
    }

    public Place getSelected(){
        if(this.selectedIndex>=0 && this.selectedIndex < this.places.size()){
            return this.places.get(this.selectedIndex);
        }
        return null;
    }

    public int size(){
        return this.places.size();
    }

    public int getPreviousIndex(){
        if(this.selectedIndex == -1){
            return -1;
        }
        int i = this.selectedIndex - 1;
        if(i<0)
            i = this.places.size()-1;


        return i;
    }

    public int getNextIndex(){
        if(this.selectedIndex == -1){
            return -1;
        }
        int i = this.selectedIndex + 1;
        if(i>=this.places.size())
            i = 0;

        return i;
    }

    public boolean setSelected(int index) {
        if(index == -1 ){
            this.clearSelected();
            return true;
        }
        if (index >= 0 && index < this.places.size()) {
            this.selectedIndex = index;
            return true;
        }
        return false;
    }

    public void clearSelected(){
        this.selectedIndex = -1;
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
        StringBuffer sb = new StringBuffer("\n=== Content of ResultSet ===\n");
        sb.append("Places ("+this.places.size()+"):\n");
        for(Place place: this.places){
            sb.append("* "+ place.toString()+"\n");
        }
        sb.append("============================\n");
        return sb.toString();
    }

    @Override
    public Iterator<Place> iterator() {
        return this.places.iterator();
    }
}
