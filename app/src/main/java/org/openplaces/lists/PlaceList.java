package org.openplaces.lists;

import android.util.Log;

import org.openplaces.MapActivity;
import org.openplaces.model.Place;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ggiammat on 11/19/14.
 */
public class PlaceList implements Iterable<String> {

    public enum PlaceListType{
        AUTOLIST, STARREDLIST
    };

    public interface ListChangedListener{
        public void placeAdded(PlaceList list, Place place, String placeEnc);
        public void placeRemoved(PlaceList list, Place place, String placeEnc);
    }

    private String name;
    private PlaceListType type;

    //places are encoded in the form osmType:osmId
    private List<String> placesInList;

    //declared transient to avoid gson serialization
    private transient List<ListChangedListener> listeners;

    public PlaceList(){
        this.placesInList = new ArrayList<String>();
        this.listeners = new ArrayList<ListChangedListener>();
    }

    public PlaceList(PlaceListType type, String name){
        this();
        this.name = name;
        this.type = type;
    }

    public void addListChangedListener(ListChangedListener listener){
        this.listeners.add(listener);
    }


    public boolean contains(Place place){
        return this.placesInList.contains(this.encodePlace(place));
    }


    public boolean addPlaceToList(Place place){
        String placeEnc = this.encodePlace(place);
        boolean res = this.placesInList.add(placeEnc);
        if(res){
            for(ListChangedListener l: this.listeners){
                l.placeAdded(this, place, placeEnc);
            }
            Log.d(MapActivity.LOGTAG, "Place " + place + " added to list " + this.getName() + "(" + this.getType() + ")");
        }
        return res;
    }

    public boolean removePlaceFromList(Place place){
        String placeEnc = this.encodePlace(place);
        boolean res = this.placesInList.remove(placeEnc);
        if(res){
            for(ListChangedListener l: this.listeners){
                l.placeRemoved(this, place, placeEnc);
            }
            Log.d(MapActivity.LOGTAG, "Place " + place + " removed from list " + this.getName() + "(" + this.getType() + ")");
        }
        return res;
    }


    public int size(){
        return this.placesInList.size();
    }

    public static String encodePlace(Place place){
        return place.getOsmType() + ":" + place.getId();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getPlacesInList() {
        return placesInList;
    }

    public void setPlacesInList(List<String> placesInList) {
        this.placesInList = placesInList;
    }

    public PlaceListType getType() {
        return type;
    }

    public void setType(PlaceListType type) {
        this.type = type;
    }

    @Override
    public Iterator<String> iterator() {
        return this.placesInList.iterator();
    }


    @Override
    public String toString() {
        return this.getType() + "-" + this.getName() + "[" + this.placesInList + "]";
    }
}
