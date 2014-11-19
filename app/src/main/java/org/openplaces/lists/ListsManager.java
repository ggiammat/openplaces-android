package org.openplaces.lists;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.openplaces.MapActivity;
import org.openplaces.model.Place;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by ggiammat on 11/17/14.
 */
public class ListsManager {


    private static final String AUTOLISTS_FILE="autolists.json";
    private static final String STARREDLISTS_FILE="starredlists.json";


    private Map<String, PlaceList> starredLists;
    private Map<String, PlaceList> autoLists;

    //used to speedup lookup operations
    private Map<String, Set<String>> perPlaceStarredTable;
    private Map<String, Set<String>> perPlaceAutoTable;

    private List<ListsEventsListener> listeners = new ArrayList<ListsEventsListener>();


    private static ListsManager instance = null;
    private Context ctx;

    public static ListsManager getInstance(Context ctx){
        if(instance == null) {
            instance = new ListsManager(ctx);
        }

        return instance;
    }

    public void addListsEventListener(ListsEventsListener listener){
        this.listeners.add(listener);
    }


    public void starPlace(Place place, String listName){
        this.addPlaceToList(PlaceList.PlaceListType.STARREDLIST, listName, place);
        this.flagDirty();

        //call listeners
        for(ListsEventsListener l: this.listeners){
            l.placeAddedToStarredList(place, this.starredLists.get(listName));
        }
    }

    public List<PlaceList> getStarredListsFor(Place place){
        String placeEnc = PlaceList.encodePlace(place);
        if(this.perPlaceStarredTable.get(placeEnc) == null){
            return null;
        }

        List<PlaceList> res = new ArrayList<PlaceList>();
        for(String listName: this.perPlaceStarredTable.get(placeEnc)){
            res.add(this.starredLists.get(listName));
        }

        return res;
    }


    public void unstarPlace(Place place, String listName){
        this.addPlaceToList(PlaceList.PlaceListType.STARREDLIST, listName, place);
        this.flagDirty();

        //call listeners
        for(ListsEventsListener l: this.listeners){
            l.placeRemovedFromStarredList(place, this.starredLists.get(listName));
        }
    }

    public void addPlaceToAutoList(Place place, String listName){
        this.addPlaceToList(PlaceList.PlaceListType.AUTOLIST, listName, place);
        this.flagDirty();

        //call listeners
        for(ListsEventsListener l: this.listeners){
            l.placeAddedToAutoList(place, this.starredLists.get(listName));
        }
    }


    public void removePlaceFromAutolist(Place place, String listName){
        this.addPlaceToList(PlaceList.PlaceListType.AUTOLIST, listName, place);
        this.flagDirty();

        //call listeners
        for(ListsEventsListener l: this.listeners){
            l.placeRemovedFromAutoList(place, this.starredLists.get(listName));
        }
    }










    private ListsManager(Context ctx){
        this.ctx = ctx;
        //this.loadStarredLists();
    }

    private void createPerPlaceStarredTable(){
        this.perPlaceStarredTable = new HashMap<String, Set<String>>();
        for(String listName: this.starredLists.keySet()){
            for(String placeEnc: this.starredLists.get(listName)){
                this.addtoPerPlaceStarredTable(placeEnc, listName);
            }
        }
    }

    private void updatePerPlaceStarredTable(String operation, String placeEnc, String listName){
        if("add".equals(operation)) {
            this.addtoPerPlaceStarredTable(placeEnc, listName);
        }
        else if("remove".equals(operation)) {
            this.removeFromPerPlaceStarredTable(placeEnc, listName);
        }
    }

    private void addtoPerPlaceStarredTable(String placeEnc, String listName){
        Set<String> listsName = this.perPlaceStarredTable.get(placeEnc);
        if(listsName == null){
            listsName = new HashSet<String>();
            this.perPlaceStarredTable.put(listName, listsName);
        }
        listsName.add(placeEnc);
    }


    private void createPerPlaceAutoTable(){
        this.perPlaceAutoTable = new HashMap<String, Set<String>>();
        for(String listName: this.autoLists.keySet()){
            for(String placeEnc: this.autoLists.get(listName)){
                this.addtoPerPlaceAutoTable(placeEnc, listName);
            }
        }
    }

    private void updatePerPlaceAutoTable(String operation, String placeEnc, String listName){
        if("add".equals(operation)) {
            this.addtoPerPlaceAutoTable(placeEnc, listName);
        }
        else if("remove".equals(operation)) {
            this.removeFromPerPlaceAutoTable(placeEnc, listName);
        }
    }


    private void removeFromPerPlaceStarredTable(String placeEnc, String listName){
        Set<String> listsName = this.perPlaceStarredTable.get(placeEnc);
        if(listsName != null){
            listsName.remove(listName);
        }
    }

    private void removeFromPerPlaceAutoTable(String placeEnc, String listName){
        Set<String> listsName = this.perPlaceAutoTable.get(placeEnc);
        if(listsName != null){
            listsName.remove(listName);
        }
    }

    private void addtoPerPlaceAutoTable(String placeEnc, String listName){
        Set<String> listsName = this.perPlaceAutoTable.get(placeEnc);
        if(listsName == null){
            listsName = new HashSet<String>();
            this.perPlaceAutoTable.put(listName, listsName);
        }
        listsName.add(placeEnc);
    }



    private void removePlaceFromList(PlaceList.PlaceListType type, String listName, Place place){
        if(type.equals(PlaceList.PlaceListType.AUTOLIST)){
            PlaceList list = this.autoLists.get(listName);
            if(list != null) {
                list.removePlaceFromList(place);
                this.updatePerPlaceAutoTable("remove", PlaceList.encodePlace(place), listName);
            }
            else {
                Log.d(MapActivity.LOGTAG, "AutoList " + listName + " not exists. Aborting operation");
            }
        }
        else if(type.equals(PlaceList.PlaceListType.STARREDLIST)){
            PlaceList list = this.starredLists.get(listName);
            if(list != null) {
                list.removePlaceFromList(place);
                this.updatePerPlaceStarredTable("remove", PlaceList.encodePlace(place), listName);
            }
            else {
                Log.d(MapActivity.LOGTAG, "Starred " + listName + " not exists. Aborting operation");
            }
        }
        else {
            Log.d(MapActivity.LOGTAG, "List type " + type + " not recognized. Aborting operation");
        }
    }


    private void addPlaceToList(PlaceList.PlaceListType type, String listName, Place place){
        if(type.equals(PlaceList.PlaceListType.AUTOLIST)){
            PlaceList list = this.autoLists.get(listName);
            if(list == null){
                Log.d(MapActivity.LOGTAG, "AutoList " + listName + " not exists. Creating it");
                list = new PlaceList(PlaceList.PlaceListType.AUTOLIST, listName);
                this.autoLists.put(listName, list);
            }
            list.addPlaceToList(place);
            this.updatePerPlaceAutoTable("add", PlaceList.encodePlace(place), listName);
        }
        else if(type.equals(PlaceList.PlaceListType.STARREDLIST)){
            PlaceList list = this.starredLists.get(listName);
            if(list == null){
                Log.d(MapActivity.LOGTAG, "StarredList " + listName + " not exists. Creating it");
                list = new PlaceList(PlaceList.PlaceListType.STARREDLIST, listName);
                this.starredLists.put(listName, list);
            }
            list.addPlaceToList(place);
            this.updatePerPlaceStarredTable("add", PlaceList.encodePlace(place), listName);
        }
        else {
            Log.d(MapActivity.LOGTAG, "List type " + type + " not recognized. Aborting operation");
        }
    }


    private void flagDirty(){
        //TODO make it async??
        Log.d(MapActivity.LOGTAG, "List manager is changed. Storing new content");
        this.storeStarredLists();
    }



    private void loadStarredLists() {
        this.starredLists = new HashMap<String, PlaceList>();
        this.autoLists = new HashMap<String, PlaceList>();
        if(!this.isExternalStorageReadable()){
            Log.w(MapActivity.LOGTAG, "External Storage not available for reading. Impossible to load starred lists. Aborting operation");
            return;
        }

        try {
            File f = new File(this.ctx.getExternalFilesDir(null), STARREDLISTS_FILE);
            Gson gson = new Gson();
            Reader fr = new FileReader(f);
            Type t = new TypeToken<Map<String, PlaceList>>(){}.getType();
            this.starredLists = gson.fromJson(fr, t);
            fr.close();

            File f2 = new File(this.ctx.getExternalFilesDir(null), AUTOLISTS_FILE);
            Gson gson2 = new Gson();
            Reader fr2 = new FileReader(f2);
            Type t2 = new TypeToken<Map<String, PlaceList>>(){}.getType();
            this.autoLists = gson.fromJson(fr2, t2);
            fr.close();

            Log.d(MapActivity.LOGTAG, "Lists loaded from " + f.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            Log.w(MapActivity.LOGTAG, "Loading of lists failed: " + e);
        }
    }


    private void storeStarredLists(){
        if(!this.isExternalStorageWritable()){
            Log.w(MapActivity.LOGTAG, "External Storage not available for writing. Impossible to store lists. Aborting operation");
            return;
        }
        File f = new File(this.ctx.getExternalFilesDir(null), AUTOLISTS_FILE);
        File f2 = new File(this.ctx.getExternalFilesDir(null), STARREDLISTS_FILE);


        Gson gson = new Gson();
        String starredSerialization = gson.toJson(this.starredLists);
        String autoSerialization = gson.toJson(this.autoLists);

        try {
            PrintWriter out = new PrintWriter(f);
            out.print(starredSerialization);
            out.close();
            PrintWriter out2 = new PrintWriter(f2);
            out2.print(autoSerialization);
            out2.close();
            Log.d(MapActivity.LOGTAG, "lists stored to " + f.getAbsolutePath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.w(MapActivity.LOGTAG, "Storing of starred lists failed: " + e);
        }
    }


    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

//    public Set<String> getAllStarredPlaces(){
//        Set<String> res = new HashSet<String>();
//        for(String listName: this.starredLists.keySet()){
//            res.addAll(this.starredLists.get(listName));
//        }
//        return res;
//    }
//
//    public Set<String> getStarredLists(){
//        return this.starredLists.keySet();
//    }
//
//    public void starPlace(String listName, Place place){
//        String key = this.encodePlace(place);
//        if(this.starredLists.containsKey(listName)){
//            if(!this.starredLists.get(listName).contains(key)){
//                this.starredLists.get(listName).add(key);
//                Log.d(MapActivity.LOGTAG, "Place " + place + " added to " + listName);
//                this.flagDirty();
//            }
//            else {
//                Log.d(MapActivity.LOGTAG, "Place " + place + " already starred in " + listName + ". No actions.");
//            }
//        }
//        else {
//            Log.d(MapActivity.LOGTAG, "Starred list " + listName + " does not exits. Cannot star place " + place);
//        }
//    }
//
//    public void unstarPlace(String listName, Place place){
//        String key = this.encodePlace(place);
//        if(this.starredLists.containsKey(listName)){
//            if(this.starredLists.get(listName).contains(key)){
//                this.starredLists.get(listName).remove(key);
//                Log.d(MapActivity.LOGTAG, "Place " + place + " removed from " + listName);
//                this.flagDirty();
//            }
//            else {
//                Log.d(MapActivity.LOGTAG, "Place " + place + " not starred in " + listName + ". No actions.");
//            }
//        }
//        else {
//            Log.d(MapActivity.LOGTAG, "Starred list " + listName + " does not exits. Cannot unstar place " + place);
//        }    }
//
//    public void addStarredList(String listName){
//        if(this.starredLists.containsKey(listName)){
//            Log.d(MapActivity.LOGTAG, "Starred list " + listName + " already exists. Do not create again");
//        }
//        this.starredLists.put(listName, new HashSet<String>());
//        Log.d(MapActivity.LOGTAG, "Starred list " + listName + " created");
//        this.flagDirty();
//    }
//
//
//    /**
//     *
//     * @return the name of the list where the place is starred in, or null if the place is not starred
//     */
//    public String getStarredList(Place place){
//        String key = this.encodePlace(place);
//        for(String listName: this.starredLists.keySet()){
//            if(this.starredLists.get(listName).contains(key)){
//                return listName;
//            }
//        }
//        return null;
//    }
//
//    private String encodePlace(Place place){
//        return place.getOsmType() + ":" + place.getId();
//    }
//
//
//    private void flagDirty(){
//        Log.d(MapActivity.LOGTAG, "Starred list manager is changed. Storing new content");
//        this.storeStarredLists();
//    }
//
//
//

}
