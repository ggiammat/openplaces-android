package org.openplaces.starred;

import android.content.Context;
import android.util.Log;

import org.openplaces.MapActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by ggiammat on 11/17/14.
 */
public class StarredListsManager {


    private Map<String, List<Long>> starredLists;

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
        this.addStarredList("Favourites");
        this.addStarredList("Restaurants");
    }


    private void loadStarredLists() {
        this.starredLists = new HashMap<String, List<Long>>();
    }


    public Set<String> getStarredLists(){
        return this.starredLists.keySet();
    }

    public void starPlace(String listName, long placeId){
        if(this.starredLists.containsKey(listName)){
            if(!this.starredLists.get(listName).contains(placeId)){
                this.starredLists.get(listName).add(placeId);
                Log.d(MapActivity.LOGTAG, "Place " + placeId + " added to " + listName);
            }
            else {
                Log.d(MapActivity.LOGTAG, "Place " + placeId + " already starred in " + listName + ". No actions.");
            }
        }
        else {
            Log.d(MapActivity.LOGTAG, "Starred list " + listName + " does not exits. Cannot star place " + placeId);
        }
    }

    public void unstarPlace(String listName, long placeId){
        if(this.starredLists.containsKey(listName)){
            if(this.starredLists.get(listName).contains(placeId)){
                this.starredLists.get(listName).remove(placeId);
                Log.d(MapActivity.LOGTAG, "Place " + placeId + " removed from " + listName);
            }
            else {
                Log.d(MapActivity.LOGTAG, "Place " + placeId + " not starred in " + listName + ". No actions.");
            }
        }
        else {
            Log.d(MapActivity.LOGTAG, "Starred list " + listName + " does not exits. Cannot unstar place " + placeId);
        }    }

    public void addStarredList(String listName){
        this.starredLists.put(listName, new ArrayList<Long>());
        Log.d(MapActivity.LOGTAG, "Starred list " + listName + " created");
    }


    /**
     *
     * @param placeId
     * @return the name of the list where the place is starred in, or null if the place is not starred
     */
    public String getStarredList(Long placeId){
        for(String listName: this.starredLists.keySet()){
            if(this.starredLists.get(listName).contains(placeId)){
                return listName;
            }
        }
        return null;
    }

}
