package org.openplaces.model;

import android.content.Context;

import org.openplaces.R;

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


    private Map<String, OPPlaceCategoriesLibrary> libraries;

    private PlaceCategoriesManager(Context ctx){
        this.ctx = ctx;
        this.libraries = new HashMap<String, OPPlaceCategoriesLibrary>();

        //load standard categories library
        InputStream is = this.ctx.getResources().openRawResource(R.raw.default_categorie_library);
        OPPlaceCategoriesLibrary lib = OPPlaceCategoriesLibrary.loadFromResource(is);
        this.libraries.put(lib.getLibraryName(), lib);
    }

    //TODO: make it more efficient. As it is now, new PlaceCateogry are created at each invocation
    public List<PlaceCategory> getLibraryCategories(String libraryName){
        List<PlaceCategory> ret = new ArrayList<PlaceCategory>();
        for(OPPlaceCategoryInterface c: this.libraries.get(libraryName).getCategories()){
            ret.add(new PlaceCategory(c));
        }
        return ret;
    }
}
