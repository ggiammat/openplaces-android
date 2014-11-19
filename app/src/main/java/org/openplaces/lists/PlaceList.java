package org.openplaces.lists;

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

    private String name;
    private PlaceListType type;

    //places are encoded in the form osmType:osmId
    private List<String> placesInList;


    public PlaceList(PlaceListType type, String name){
        this.name = name;
        this.type = type;
        this.placesInList = new ArrayList<String>();
    }


    public boolean contains(Place place){
        return this.placesInList.contains(this.encodePlace(place));
    }


    public boolean addPlaceToList(Place place){
        return this.placesInList.add(this.encodePlace(place));
    }

    public boolean removePlaceFromList(Place place){
        return this.placesInList.remove(this.encodePlace(place));
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
}
