package org.openplaces.starred;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.openplaces.MapActivity;
import org.openplaces.model.Place;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
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
public class StarredListsManager {


    private static final String STORAGE_NAME="starredLists.json";

    private Map<String, Set<String>> starredLists;

    private static StarredListsManager instance = null;
    private Context ctx;

    public static StarredListsManager getInstance(Context ctx){
        if(instance == null) {
            instance = new StarredListsManager(ctx);
        }

        return instance;
    }


    public StarredListsManager(Context ctx){
        this.ctx = ctx;
        this.loadStarredLists();
    }


    public Set<String> getAllStarredPlaces(){
        Set<String> res = new HashSet<String>();
        for(String listName: this.starredLists.keySet()){
            res.addAll(this.starredLists.get(listName));
        }
        return res;
    }

    public Set<String> getStarredLists(){
        return this.starredLists.keySet();
    }

    public void starPlace(String listName, Place place){
        String key = this.encodePlace(place);
        if(this.starredLists.containsKey(listName)){
            if(!this.starredLists.get(listName).contains(key)){
                this.starredLists.get(listName).add(key);
                Log.d(MapActivity.LOGTAG, "Place " + place + " added to " + listName);
                this.flagDirty();
            }
            else {
                Log.d(MapActivity.LOGTAG, "Place " + place + " already starred in " + listName + ". No actions.");
            }
        }
        else {
            Log.d(MapActivity.LOGTAG, "Starred list " + listName + " does not exits. Cannot star place " + place);
        }
    }

    public void unstarPlace(String listName, Place place){
        String key = this.encodePlace(place);
        if(this.starredLists.containsKey(listName)){
            if(this.starredLists.get(listName).contains(key)){
                this.starredLists.get(listName).remove(key);
                Log.d(MapActivity.LOGTAG, "Place " + place + " removed from " + listName);
                this.flagDirty();
            }
            else {
                Log.d(MapActivity.LOGTAG, "Place " + place + " not starred in " + listName + ". No actions.");
            }
        }
        else {
            Log.d(MapActivity.LOGTAG, "Starred list " + listName + " does not exits. Cannot unstar place " + place);
        }    }

    public void addStarredList(String listName){
        if(this.starredLists.containsKey(listName)){
            Log.d(MapActivity.LOGTAG, "Starred list " + listName + " already exists. Do not create again");
        }
        this.starredLists.put(listName, new HashSet<String>());
        Log.d(MapActivity.LOGTAG, "Starred list " + listName + " created");
        this.flagDirty();
    }


    /**
     *
     * @return the name of the list where the place is starred in, or null if the place is not starred
     */
    public String getStarredList(Place place){
        String key = this.encodePlace(place);
        for(String listName: this.starredLists.keySet()){
            if(this.starredLists.get(listName).contains(key)){
                return listName;
            }
        }
        return null;
    }

    private String encodePlace(Place place){
        return place.getOsmType() + ":" + place.getId();
    }


    private void flagDirty(){
        Log.d(MapActivity.LOGTAG, "Starred list manager is changed. Storing new content");
        this.storeStarredLists();
    }



    private void loadStarredLists() {
        this.starredLists = new HashMap<String, Set<String>>();
        if(!this.isExternalStorageReadable()){
            Log.w(MapActivity.LOGTAG, "External Storage not available for reading. Impossible to load starred lists. Aborting operation");
            return;
        }

        try {
            File f = new File(this.ctx.getExternalFilesDir(null), STORAGE_NAME);
            Gson gson = new Gson();
            Reader fr = new FileReader(f);
            Type t = new TypeToken<Map<String, Set<String>>>(){}.getType();
            this.starredLists = gson.fromJson(fr, t);

            fr.close();
            Log.d(MapActivity.LOGTAG, "Starred lists loaded from " + f.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            Log.w(MapActivity.LOGTAG, "Loading of starred lists failed: " + e);
        }
    }


    private void storeStarredLists(){
        if(!this.isExternalStorageWritable()){
            Log.w(MapActivity.LOGTAG, "External Storage not available for writing. Impossible to store starred lists. Aborting operation");
            return;
        }
        File f = new File(this.ctx.getExternalFilesDir(null), STORAGE_NAME);


        Gson gson = new Gson();
        String serialization = gson.toJson(this.starredLists);
        try {
            PrintWriter out = new PrintWriter(f);
            out.print(serialization);
            out.close();
            Log.d(MapActivity.LOGTAG, "Starred lists stored to " + f.getAbsolutePath());
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
}
