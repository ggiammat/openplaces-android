package org.openplaces.lists;

import android.util.Log;

import org.openplaces.MapActivity;
import org.openplaces.places.Place;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ggiammat on 11/19/14.
 */
public class PlaceList implements Iterable<PlaceListItem> {

    public enum PlaceListType{
        AUTOLIST, STARREDLIST
    };

    public interface ListChangedListener{
        public void placeAdded(PlaceList list, Place place, PlaceListItem placeEnc);
        public void placeRemoved(PlaceList list, Place place, PlaceListItem placeEnc);
    }

    private String name;
    private PlaceListType type;

    //places are encoded in the form osmType:osmId
    private List<PlaceListItem> placesInList;

    //declared transient to avoid gson serialization
    private transient List<ListChangedListener> listeners;

    public PlaceList(){
        this.placesInList = new ArrayList<PlaceListItem>();
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

    public PlaceListItem getPlaceListItemByPlace(Place place){
        if(this.placesInList.contains(encodePlace(place))){
           return this.placesInList.get(this.placesInList.indexOf(encodePlace(place)));
        }
        else {
            return null;
        }
    }

    public boolean addNote(Place place, String note){
        PlaceListItem item = this.addOrUpdatePlaceListItem(place);
        item.setNote(note);

        for(ListChangedListener l: this.listeners){
            l.placeAdded(this, place, item);
        }
        return true;
    }

    private PlaceListItem addOrUpdatePlaceListItem(Place place){
        PlaceListItem res = encodePlace(place);
        if(this.placesInList.contains(res)){
            res = this.placesInList.get(this.placesInList.indexOf(res));
            res.setModifiedDate(new Date().getTime());
        }
        else {
            this.placesInList.add(res);
        }

        return res;
    }

    public void addPlaceToList(Place place){

        PlaceListItem item = this.addOrUpdatePlaceListItem(place);

        for(ListChangedListener l: this.listeners){
            l.placeAdded(this, place, item);
        }
        Log.d(MapActivity.LOGTAG, "Place " + place + " added to list " + this.getName() + "(" + this.getType() + ")");

    }

    public boolean removePlaceFromList(Place place){
        PlaceListItem placeEnc = this.encodePlace(place);
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

    private PlaceListItem encodePlace(Place place){
        return new PlaceListItem(place.getId(), place.getOsmType());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PlaceListItem> getPlacesInList() {
        return placesInList;
    }

    public void setPlacesInList(List<PlaceListItem> placesInList) {
        this.placesInList = placesInList;
    }

    public PlaceListType getType() {
        return type;
    }

    public void setType(PlaceListType type) {
        this.type = type;
    }

    @Override
    public Iterator<PlaceListItem> iterator() {
        return this.placesInList.iterator();
    }


    @Override
    public String toString() {
        return this.getType() + "-" + this.getName() + "[" + this.placesInList + "]";
    }
}
