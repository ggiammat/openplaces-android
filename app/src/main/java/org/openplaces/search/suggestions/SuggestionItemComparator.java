package org.openplaces.search.suggestions;

import android.location.Location;
import android.util.Log;

import org.openplaces.MapActivity;
import org.openplaces.categories.PlaceCategory;
import org.openplaces.model.OPGeoPoint;
import org.openplaces.search.SearchController;
import org.openplaces.utils.GeoFunctions;

import java.util.Comparator;

/**
 * Created by ggiammat on 1/3/15.
 */
public class SuggestionItemComparator implements Comparator<SuggestionItem> {

    private SearchController searchController;
    private OPGeoPoint searchPosition;


    public SuggestionItemComparator(SearchController searchController, Location searchPosition){
        this.searchController = searchController;
        this.searchPosition = new OPGeoPoint(searchPosition.getLatitude(), searchPosition.getLongitude());
    }

    @Override
    public int compare(SuggestionItem item1, SuggestionItem item2) {

        Log.d(MapActivity.LOGTAG, "Comparing: " + item1.getTitle() + " vs. " + item2.getTitle());

        //return 0;

        /*
        SORT HOMOGENEOUS ITEMS
         */
        if(item1.getClass().equals(item2.getClass())){
//            Log.d(MapActivity.LOGTAG, "homogeneous comparator for: " + item1.getClass());
//            if(item1 instanceof LocationSuggestionItem){
//                int i = compareLocations((LocationSuggestionItem) item1, (LocationSuggestionItem) item2);
//                return i;
//            }

            return 0;
        }

         /*
        SORT HETEREOGENEOUS ITEMS
         */
        else {
            //empty search box
            if("".equals(this.searchController.getQueryET().getText().toString())){
                return this.sortHeterogeneousSearchBoxEmpty(item1, item2);
            }

            //no categories
            else if(this.searchController.getCategories().isEmpty()){
                return this.sortHetereogeneousNoCategories(item1, item2);
            }

            //with categories
            else if(!this.searchController.getCategories().isEmpty()){
                return this.sortHetereogeneousWithCategories(item1, item2);
            }

            return 0;
        }
    }

    private int sortHetereogeneousWithCategories(SuggestionItem item1, SuggestionItem item2){
        if(item1 instanceof LocationSuggestionItem){
            return -1;
        }

        if(item2 instanceof LocationSuggestionItem) {
            return 1;
        }

        return 0;
    }

    private int sortHetereogeneousNoCategories(SuggestionItem item1, SuggestionItem item2){
        if(item1 instanceof PlaceCategorySuggestionItem){
            return -1;
        }

        if(item2 instanceof PlaceCategorySuggestionItem) {
            return 1;
        }

        return 0;
    }

    private int sortHeterogeneousSearchBoxEmpty(SuggestionItem item1, SuggestionItem item2){
        if(item1 instanceof ListSuggestionItem){
            return -1;
        }

        if(item2 instanceof ListSuggestionItem) {
            return 1;
        }

        return 0;
    }



    private int compareLocations(LocationSuggestionItem item1, LocationSuggestionItem item2){
        Double d1 = GeoFunctions.distance(item1.getLocation().getPosition(), this.searchPosition);
        Double d2 = GeoFunctions.distance(item2.getLocation().getPosition(), this.searchPosition);

        Log.d(MapActivity.LOGTAG, "result: " + d1 + " vs. " + d2);
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
