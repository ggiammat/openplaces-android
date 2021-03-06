package org.openplaces.lists;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.openplaces.MapActivity;
import org.openplaces.places.Place;

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
public class ListManager implements PlaceList.ListChangedListener {


    private static final String AUTOLISTS_FILE="autolists.json";
    private static final String STARREDLISTS_FILE="starredlists.json";
    public static final String AUTOLIST_VISITED = "Visited";


    private Map<String, PlaceList> starredLists;
    private Map<String, PlaceList> autoLists;

    //used to speedup lookup operations
    private Map<String, Set<String>> perPlaceStarredTable;
    private Map<String, Set<String>> perPlaceAutoTable;

    private List<ListManagerEventListener> listeners = new ArrayList<ListManagerEventListener>();


    private static ListManager instance = null;
    private Context ctx;

    public static ListManager getInstance(Context ctx){
        if(instance == null) {
            instance = new ListManager(ctx);
        }

        return instance;
    }

    private void registerListenerForListsChanges(){
        for(PlaceList l: this.starredLists.values()){
            l.addListChangedListener(this);
        }
        for(PlaceList l: this.autoLists.values()){
            l.addListChangedListener(this);
        }
    }

    public PlaceList getAutoListByName(String name){
        return this.autoLists.get(name);
    }


    public PlaceList getStarredListByName(String name){
        return this.starredLists.get(name);
    }

    public Set<String> getAllStarredPlaces(){
        Set<String> res = new HashSet<String>();

        for(PlaceList l: this.starredLists.values()){
            for(PlaceListItem it: l.getPlacesInList()){
                res.add(it.getOsmType() + ":" + it.getOsmId());
            }
        }

        return res;
    }

    public void addListsEventListener(ListManagerEventListener listener){
        this.listeners.add(listener);
    }

    public PlaceList createNewStarredList(String name){
        PlaceList newList = new PlaceList(PlaceList.PlaceListType.STARREDLIST, name);
        this.starredLists.put(name, newList);
        newList.addListChangedListener(this);
        this.flagDirty();

        //call listeners
        for(ListManagerEventListener l: this.listeners){
            l.starredListAdded(newList);
        }

        return newList;
    }


    public boolean isStarredIn(Place place, PlaceList list){
        String placeEnc = encodePlace(place);
        if(this.perPlaceStarredTable.get(placeEnc) == null ||
                !this.perPlaceStarredTable.get(placeEnc).contains(list.getName())){
            return false;
        }
        return true;
    }

    public boolean isStarred(Place place){
        String placeEnc = encodePlace(place);
        if(this.perPlaceStarredTable.get(placeEnc) == null ||
                this.perPlaceStarredTable.get(placeEnc).isEmpty()){
            return false;
        }
        return true;
    }

    private String encodePlace(Place place){
        return place.getOsmType() + ":" + place.getId();
    }

    private String encodePlace(PlaceListItem item){
        return item.getOsmType() + ":" + item.getOsmId();
    }

    public List<PlaceList> getStarredListsFor(Place place){
        if(!this.isStarred(place)){
            return null;
        }

        List<PlaceList> res = new ArrayList<PlaceList>();
        for(String listName: this.perPlaceStarredTable.get(encodePlace(place))){
            res.add(this.starredLists.get(listName));
        }

        return res;
    }



    private ListManager(Context ctx){
        this.ctx = ctx;
        this.loadLists();
        this.createPerPlaceAutoTable();
        this.createPerPlaceStarredTable();
        this.registerListenerForListsChanges();
        Log.d(MapActivity.LOGTAG, "Lists loaded:");
        Log.d(MapActivity.LOGTAG, this.starredLists.values().toString());
        Log.d(MapActivity.LOGTAG, this.autoLists.values().toString());
    }

    //TODO: should be returned always in the same order
    public List<PlaceList> getStarredLists(){
        return new ArrayList<PlaceList>(this.starredLists.values());
    }
    public List<PlaceList> getAutoLists(){
        return new ArrayList<PlaceList>(this.autoLists.values());
    }

    private void createPerPlaceStarredTable(){
        this.perPlaceStarredTable = new HashMap<String, Set<String>>();
        for(String listName: this.starredLists.keySet()){
            for(PlaceListItem placeEnc: this.starredLists.get(listName)){
                this.updatePerPlaceStarredTable("add", encodePlace(placeEnc), listName);
            }
        }
    }

    private void updatePerPlaceStarredTable(String operation, String placeEnc, String listName){
        Log.d(MapActivity.LOGTAG, "Updating perPlace starred table: " + operation + " " + placeEnc + " in " + listName);
        if("add".equals(operation)) {
            Set<String> listsName = this.perPlaceStarredTable.get(placeEnc);
            if(listsName == null){
                listsName = new HashSet<String>();
                this.perPlaceStarredTable.put(placeEnc, listsName);
            }
            listsName.add(listName);
        }
        else if("remove".equals(operation)) {
            Set<String> listsName = this.perPlaceStarredTable.get(placeEnc);
            if(listsName != null){
                listsName.remove(listName);
            }
        }
    }

    private void createPerPlaceAutoTable(){
        this.perPlaceAutoTable = new HashMap<String, Set<String>>();
        for(String listName: this.autoLists.keySet()){
            for(PlaceListItem placeEnc: this.autoLists.get(listName)){
                this.updatePerPlaceAutoTable("add", encodePlace(placeEnc), listName);
            }
        }
    }

    private void updatePerPlaceAutoTable(String operation, String placeEnc, String listName){
        Log.d(MapActivity.LOGTAG, "Updating perPlace auto table: " + operation + " " + placeEnc + " in " + listName);
        if("add".equals(operation)) {
            Set<String> listsName = this.perPlaceAutoTable.get(placeEnc);
            if(listsName == null){
                listsName = new HashSet<String>();
                this.perPlaceAutoTable.put(placeEnc, listsName);
            }
            listsName.add(listName);
        }
        else if("remove".equals(operation)) {
            Set<String> listsName = this.perPlaceAutoTable.get(placeEnc);
            if(listsName != null){
                listsName.remove(listName);
            }
        }
    }



    private void flagDirty(){
        //TODO make it async??
        Log.d(MapActivity.LOGTAG, "List manager is changed. Storing new content");
        this.storeStarredLists();
    }



    private void loadLists() {
        this.starredLists = new HashMap<String, PlaceList>();
        this.autoLists = new HashMap<String, PlaceList>();
        if(!this.isExternalStorageReadable()){
            Log.w(MapActivity.LOGTAG, "External Storage not available for reading. Impossible to load starred lists. Aborting operation");
            return;
        }

        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "open-places");


        try {
            File f = new File(dir, STARREDLISTS_FILE);
            Gson gson = new Gson();
            Reader fr = new FileReader(f);
            Type t = new TypeToken<Map<String, PlaceList>>(){}.getType();
            this.starredLists = gson.fromJson(fr, t);
            fr.close();


            Log.d(MapActivity.LOGTAG, "Starred lists loaded from " + f.getAbsolutePath());
        }
        catch (FileNotFoundException e){
            Log.w(MapActivity.LOGTAG, "Starred lists file not found. Initializing default lists");
            this.initializeDefaultStarredLists();
        }
        catch (IOException e) {
            e.printStackTrace();
            Log.w(MapActivity.LOGTAG, "Loading of starred lists failed: " + e);
        }


        try {
            File f2 = new File(dir ,AUTOLISTS_FILE);
            Gson gson2 = new Gson();
            Reader fr2 = new FileReader(f2);
            Type t2 = new TypeToken<Map<String, PlaceList>>(){}.getType();
            this.autoLists = gson2.fromJson(fr2, t2);
            fr2.close();


            Log.d(MapActivity.LOGTAG, "Auto lists loaded from " + f2.getAbsolutePath());
        }
        catch (FileNotFoundException e){
            Log.w(MapActivity.LOGTAG, "Auto lists file not found. Initializing default lists");
            this.initializeDefaultAutoLists();
        }
        catch (IOException e) {
            e.printStackTrace();
            Log.w(MapActivity.LOGTAG, "Loading of auto lists failed: " + e);
        }


    }

    private void initializeDefaultStarredLists(){
        this.starredLists.put("Favourites", new PlaceList(PlaceList.PlaceListType.STARREDLIST, "Favourites"));
        this.starredLists.put("Restaurants", new PlaceList(PlaceList.PlaceListType.STARREDLIST, "Restaurants"));
        this.starredLists.put("Todo", new PlaceList(PlaceList.PlaceListType.STARREDLIST, "Todo"));
    }

    private void initializeDefaultAutoLists(){
        this.autoLists.put(AUTOLIST_VISITED, new PlaceList(PlaceList.PlaceListType.AUTOLIST, "Visited"));
    }

    private void storeStarredLists(){
        if(!this.isExternalStorageWritable()){
            Log.w(MapActivity.LOGTAG, "External Storage not available for writing. Impossible to store lists. Aborting operation");
            return;
        }
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "open-places");
        if(!dir.exists()){
            dir.mkdirs();
        }
        File f = new File(dir, STARREDLISTS_FILE);
        File f2 = new File(dir, AUTOLISTS_FILE);


        Gson gson = new Gson();
        String starredSerialization = gson.toJson(this.starredLists);
        String autoSerialization = gson.toJson(this.autoLists);


        try {
            if(!f.exists()){
                f.createNewFile();
            }
            PrintWriter out = new PrintWriter(f);
            out.print(starredSerialization);
            out.close();
            if(!f2.exists()){
                f2.createNewFile();
            }
            PrintWriter out2 = new PrintWriter(f2);
            out2.print(autoSerialization);
            out2.close();
            Log.d(MapActivity.LOGTAG, "lists stored to " + f.getAbsolutePath());
        } catch (IOException e) {
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

    @Override
    public void placeAdded(PlaceList list, Place place, PlaceListItem item) {

        String placeEnc = item.getOsmType() + ":" + item.getOsmId();

        if(list.getType().equals(PlaceList.PlaceListType.AUTOLIST)){
            this.updatePerPlaceAutoTable("add", placeEnc, list.getName());
            //call listeners
            for(ListManagerEventListener l: this.listeners){
                l.placeAddedToAutoList(place, list);
            }
        }
        else if(list.getType().equals(PlaceList.PlaceListType.STARREDLIST)){
            this.updatePerPlaceStarredTable("add", placeEnc, list.getName());
            //call listeners
            for(ListManagerEventListener l: this.listeners){
                l.placeAddedToStarredList(place, list);
            }
        }
        else {
            Log.d(MapActivity.LOGTAG, "list type not recognized: " + list.getType());
        }

        this.flagDirty();


    }

    @Override
    public void placeRemoved(PlaceList list, Place place, PlaceListItem item) {

        String placeEnc = item.getOsmType() + ":" + item.getOsmId();


        if(list.getType().equals(PlaceList.PlaceListType.AUTOLIST)){
            this.updatePerPlaceAutoTable("remove", placeEnc, list.getName());
            //call listeners
            for(ListManagerEventListener l: this.listeners){
                l.placeRemovedFromAutoList(place, list);
            }
        }
        else if(list.getType().equals(PlaceList.PlaceListType.STARREDLIST)){
            this.updatePerPlaceStarredTable("remove", placeEnc, list.getName());
            //call listeners
            for(ListManagerEventListener l: this.listeners){
                l.placeRemovedFromStarredList(place, list);
            }
        }
        else {
            Log.d(MapActivity.LOGTAG,"list type not recognized: " + list.getType());
        }

        this.flagDirty();

    }

}
