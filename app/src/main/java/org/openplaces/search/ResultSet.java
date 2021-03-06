package org.openplaces.search;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.openplaces.MapActivity;
import org.openplaces.model.OPPlaceInterface;
import org.openplaces.places.Place;
import org.openplaces.categories.PlaceCategoriesManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by gabriele on 11/10/14.
 */
public class ResultSet implements Parcelable, Iterable<Place> {

    public interface ResultSetEventsListener {
        public void onNewPlaceSelected(Place oldSelected, Place newSelected);
    }

    private List<ResultSetEventsListener> mListeners = new ArrayList<ResultSetEventsListener>();

    public void addListener(ResultSetEventsListener listener){
        this.mListeners.add(listener);
    }

    public void removeListener(ResultSetEventsListener listener){
        this.mListeners.remove(listener);
    }

    private void notifyNewPlaceSelected(Place oldPlace, Place newPlace){
        for(ResultSetEventsListener l: this.mListeners){
            l.onNewPlaceSelected(oldPlace, newPlace);
        }
    }

    private List<Place> places;
    private Map<String, String> stats;

    private int selectedIndex;

    public ResultSet(){
        this.places = new ArrayList<Place>();
        this.stats = new HashMap<String, String>();
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

    public void setStat(String name, String value){
        this.stats.put(name, value);
    }

    public String getStat(String name){
        return this.stats.get(name);
    }

    public List<Place> getAllPlaces(){
        return new ArrayList<Place>(this.places);
    }

    public void addPlace(Place place, PlaceCategoriesManager catMan){
        place.setCategory(catMan.getPlaceCategory(place));
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


    public boolean contains(Place place){
        return this.places.contains(place);
    }

    public Place getPlaceAt(int index){
        return this.places.get(index);
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

    public boolean selectNext(){
        return this.setSelected(this.getNextIndex());
    }

    public boolean selectPrevious(){
        return this.setSelected(this.getPreviousIndex());
    }

    public boolean setSelected(int index) {
        if(index == -1 ){
            this.clearSelected();
            return true;
        }
        if (index >= 0 && index < this.places.size()) {
            int c = this.selectedIndex;
            this.selectedIndex = index;
            notifyNewPlaceSelected(c == -1 ? null : this.places.get(c), this.places.get(this.selectedIndex));
            return true;
        }
        return false;
    }

    public void clearSelected(){
        int c = this.selectedIndex;
        this.selectedIndex = -1;
        notifyNewPlaceSelected(c == -1 ? null : this.places.get(c), null);
    }

    public ResultSet(Parcel in){
        List<Place> places = new ArrayList<Place>();
        in.readList(places, Place.class.getClassLoader());
        this.places = places;

        //statistics
        if(in.readByte() == 0x00){
            this.stats = null;
        }
        else {
            int size = in.readInt();
            Map<String, String> stats = new HashMap<String, String>();
            for (int i = 0; i < size; i++) {
                String key = in.readString();
                String value = in.readString();
                stats.put(key, value);
            }
            this.stats = stats;
        }
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

        //statistics
        if(this.stats == null) {
            dest.writeByte((byte) (0x00));
        }
        else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(this.stats.size());
            for(String key : this.stats.keySet()){
                dest.writeString(key);
                dest.writeString(this.stats.get(key));
            }
        }
    }


    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("\n=== Content of ResultSet ===\n");
        sb.append("Places (").append(this.places.size()).append("):\n");
        for(Place place: this.places){
            sb.append("* ").append(place.toString()).append("\n");
        }
        sb.append("============================\n");
        return sb.toString();
    }

    @Override
    public Iterator<Place> iterator() {
        return this.places.iterator();
    }
}
