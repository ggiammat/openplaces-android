package org.openplaces.search.suggestions;

import android.location.Location;
import android.util.Log;

import org.openplaces.MapActivity;
import org.openplaces.categories.PlaceCategory;
import org.openplaces.model.OPGeoPoint;
import org.openplaces.search.SearchController;
import org.openplaces.utils.GeoFunctions;

import java.util.Comparator;
import java.util.List;

/**
 * Created by ggiammat on 1/3/15.
 */
public class SuggestionItemComparator implements Comparator<SuggestionItem> {

    private SearchController searchController;
    private OPGeoPoint searchPosition;

    private Class[] emptySearchBoxOrder;
    private Class[] noCategoriesOrder;
    private Class[] withCategoriesOrder;

    public SuggestionItemComparator(SearchController searchController, Location searchPosition){
        this.searchController = searchController;
        this.searchPosition = new OPGeoPoint(searchPosition.getLatitude(), searchPosition.getLongitude());

        this.emptySearchBoxOrder = new Class[]{ListSuggestionItem.class, StarredPlaceSuggestionItem.class, PlaceCategorySuggestionItem.class, SearchLocationByNameSuggestionItem.class, LocationSuggestionItem.class};
        this.noCategoriesOrder = new Class[]{PlaceCategorySuggestionItem.class, LocationSuggestionItem.class, SearchLocationByNameSuggestionItem.class, ListSuggestionItem.class, StarredPlaceSuggestionItem.class};
        this.withCategoriesOrder = new Class[]{LocationSuggestionItem.class, PlaceCategorySuggestionItem.class, SearchLocationByNameSuggestionItem.class, ListSuggestionItem.class, StarredPlaceSuggestionItem.class};
    }

    @Override
    public int compare(SuggestionItem item1, SuggestionItem item2) {
        /*
        SORT HOMOGENEOUS ITEMS
         */
        if(item1.getClass().equals(item2.getClass())){
            if(item1 instanceof LocationSuggestionItem){
                int i = compareLocations((LocationSuggestionItem) item1, (LocationSuggestionItem) item2);
                return i;
            }

            return 0;
        }

         /*
        SORT HETEREOGENEOUS ITEMS
         */
        else {
            //empty search box
            if("".equals(this.searchController.getSearchBox().getText().toString())){
                return this.sortSuggestionClasses(this.emptySearchBoxOrder, item1, item2);
            }

            //no categories
            else if(this.searchController.getSearchQueryCategories().isEmpty()){
                return this.sortSuggestionClasses(this.noCategoriesOrder, item1, item2);
            }

            //with categories
            else if(!this.searchController.getSearchQueryCategories().isEmpty()){
                return this.sortSuggestionClasses(this.withCategoriesOrder, item1, item2);
            }

            Log.w(MapActivity.LOGTAG, "Unexpected search status for sorting");
            return 0;
        }
    }

    private int sortSuggestionClasses(Class[] classesOrder, SuggestionItem item1, SuggestionItem item2){

        for(Class c: classesOrder){
            if(item1.getClass().equals(c)){
                return -1;
            }
            if(item2.getClass().equals(c)){
                return +1;
            }
        }
        Log.w(MapActivity.LOGTAG, "Unexpected class for items: " + item1.getTitle() + ", " + item2.getTitle());
        return 0;
    }


    private int compareLocations(LocationSuggestionItem item1, LocationSuggestionItem item2){
        Double d1 = GeoFunctions.distance(item1.getLocation().getPosition(), this.searchPosition);
        Double d2 = GeoFunctions.distance(item2.getLocation().getPosition(), this.searchPosition);
        //return d1.intValue() - d2.intValue();

        if(d1<d2){
            return -1;
        }
        else if(d1>d2){
            return 1;
        }
        else {
            return 0;
        }
    }
}
