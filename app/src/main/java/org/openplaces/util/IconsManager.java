package org.openplaces.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;

import org.openplaces.MapActivity;
import org.openplaces.R;
import org.openplaces.places.Place;
import org.openplaces.categories.PlaceCategory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gabriele on 11/20/14.
 */
public class IconsManager {

    private static final String UNKNOWN_CATEGORY_MARKER_SYMBOL = "unknown";

    private static IconsManager instance;
    private Context context;

    public static IconsManager getInstance(Context ctx){
        if(instance == null){
            instance = new IconsManager(ctx);
        }
        return instance;
    }

    private IconsManager(Context context){
        this.context = context;
        this.markersCache = new HashMap<String, Drawable>();
        this.iconsCache = new HashMap<String, Drawable>();
    }

    private Map<String, Drawable> markersCache;
    private Map<String, Drawable> iconsCache;


    public Drawable getPlaceIcon(Place place, boolean bigSize){
        return this.getCategoryIcon(place.getCategory(), bigSize);
    }


    public int getCategoryIconId(PlaceCategory cat, boolean bigSize){
        String symbol = cat != null ? cat.getId() : null;
        if(symbol == null){
            symbol = UNKNOWN_CATEGORY_MARKER_SYMBOL;
        }
        String resourceName = bigSize ? "picbig" : "pic_" + symbol;
        return this.context.getResources().getIdentifier(resourceName, "drawable", this.context.getApplicationContext().getPackageName());
    }

    public Drawable getCategoryIcon(PlaceCategory cat, boolean bigSize){
        String symbol = cat.getId();
        if(symbol == null){
            symbol = UNKNOWN_CATEGORY_MARKER_SYMBOL;
        }

        if(this.iconsCache.containsKey(symbol)){
            return this.iconsCache.get(symbol);
        }

        Drawable icon = this.context.getResources().getDrawable(this.getCategoryIconId(cat, bigSize));

        this.iconsCache.put(symbol, icon);

        return icon;
    }

    public Drawable getMarker(Place place){
        return this.getMarker(false, false,
                place.getCategory() != null ? place.getCategory().getId() : null);
    }

    public Drawable getSelectedMarker(Place place){
        return this.getMarker(true, false,
                place.getCategory() != null ? place.getCategory().getId() : null);
    }

    public Drawable getStrredMarker(Place place){
        return this.getMarker(false, true,
                place.getCategory() != null ? place.getCategory().getId() : null);
    }

    public Drawable getSelectedStarredMarker(Place place){
        return this.getMarker(true, true,
                place.getCategory() != null ? place.getCategory().getId() : null);
    }


    private Drawable getMarker(boolean selected, boolean starred, String symbol){

        if(symbol == null){
            symbol = UNKNOWN_CATEGORY_MARKER_SYMBOL;
        }

        String cacheKey = (selected ? "s:" : "u:") + (starred ? "t:" : "n:") + symbol;

        if(this.markersCache.containsKey(cacheKey)){
            return this.markersCache.get(cacheKey);
        }

        String resourceName = "pic_" + symbol;

        int bgId = -1;
        if(selected && starred){
            bgId = R.drawable.marker_bg_starred_selected;
        }
        else if(selected && !starred){
            bgId = R.drawable.marker_bg_selected;
        }
        else if(!selected && starred){
            bgId = R.drawable.marker_bg_starred;
        }
        else if(!selected && !starred){
            bgId = R.drawable.marker_bg;
        }

        try {
            Drawable icon = new LayerDrawable(new Drawable[]{
                    this.context.getResources().getDrawable(bgId),
                    this.context.getResources().getDrawable(this.context.getResources().getIdentifier(resourceName, "drawable", this.context.getApplicationContext().getPackageName()))});

            this.markersCache.put(cacheKey, icon);
            return icon;
        }
        catch (Exception ex) {
            Log.w(MapActivity.LOGTAG, "ERROR getting drawable resource by name " + resourceName);
            return this.getMarker(selected, starred, UNKNOWN_CATEGORY_MARKER_SYMBOL);
        }


    }
}
