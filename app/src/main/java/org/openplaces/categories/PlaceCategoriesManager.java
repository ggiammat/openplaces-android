package org.openplaces.categories;

import android.content.Context;
import android.util.Log;

import org.openplaces.MapActivity;
import org.openplaces.R;
import org.openplaces.model.OPPlaceCategoriesLibrary;
import org.openplaces.model.OPPlaceCategoryInterface;
import org.openplaces.places.Place;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PlaceCategoriesManager {

    public static final int[] STANDARD_LIBS = new int[]{R.raw.food_standard_library, R.raw.general_standard_library};

    private static PlaceCategoriesManager instance = null;
    private Context ctx;

    public static PlaceCategoriesManager getInstance(Context ctx){
        if(instance == null) {
            instance = new PlaceCategoriesManager(ctx);
        }

        return instance;
    }

    private Map<String, List<PlaceCategory>> libraries;

    private PlaceCategoriesManager(Context ctx){
        this.ctx = ctx;
        this.libraries = new HashMap<String, List<PlaceCategory>>();

        //load standard categories library
        for(int id: STANDARD_LIBS){
            InputStream is = this.ctx.getResources().openRawResource(id);
            this.loadFromInputStream(is);
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadFromInputStream(InputStream is){
        OPPlaceCategoriesLibrary lib = OPPlaceCategoriesLibrary.loadFromResource(is);

        //replace objects with PlaceCategory objects that are Parcelable
        List<PlaceCategory> newObjs = new ArrayList<PlaceCategory>();
        for(OPPlaceCategoryInterface o: lib.getCategories()){
            newObjs.add(new PlaceCategory(o));
        }

        this.libraries.put(lib.getLibraryName(), newObjs);
    }

    public List<PlaceCategory> getLibraryCategories(String libraryName){
        return this.libraries.get(libraryName);
    }

    public List<PlaceCategory> getAllCategories(){
        List<PlaceCategory> res = new ArrayList<PlaceCategory>();
        for(String lib: this.libraries.keySet()){
            res.addAll(this.libraries.get(lib));
        }
        return res;
    }

    //TODO: highly inefficent. Scan all categories for each place
    //TODO: take priority into consideration
    public PlaceCategory getPlaceCategory(Place place){
        PlaceCategory matchingCat = null;
        int lastPriority = Integer.MAX_VALUE;

        for(PlaceCategory c: this.getAllCategories()){
            if(c.placeMatchesCategory(place) && c.getPriority() < lastPriority){
                matchingCat = c;
                lastPriority = c.getPriority();
                if(lastPriority == 0){
                    return c;
                }
            }
        }

        return matchingCat;
    }
}
