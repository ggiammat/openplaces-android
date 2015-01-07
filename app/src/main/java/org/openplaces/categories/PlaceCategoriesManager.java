package org.openplaces.categories;

import android.content.Context;

import org.openplaces.R;
import org.openplaces.model.OPPlaceCategoriesLibrary;
import org.openplaces.model.OPPlaceCategoryInterface;
import org.openplaces.places.Place;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gabriele on 11/13/14.
 */
public class PlaceCategoriesManager {

    public static final String STANDARD_LIBRARY = "standard";

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
        InputStream is = this.ctx.getResources().openRawResource(R.raw.default_categories_library);
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
    public PlaceCategory getPlaceCategory(Place place){
        int maxMatchIndex = -1;
        PlaceCategory matchingCat = null;

        for(PlaceCategory c: this.getAllCategories()){
            int i = c.placeMatchesCategory(place);
            if(i > maxMatchIndex){
                maxMatchIndex = i;
                matchingCat = c;
            }
        }

        return matchingCat;
    }
}
