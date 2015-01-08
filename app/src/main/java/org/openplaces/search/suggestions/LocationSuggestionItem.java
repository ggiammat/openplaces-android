package org.openplaces.search.suggestions;

import android.location.Location;

import org.openplaces.R;
import org.openplaces.model.OPGeoPoint;
import org.openplaces.model.OPLocationInterface;
import org.openplaces.search.SearchController;
import org.openplaces.utils.GeoFunctions;

/**
 * Created by ggiammat on 12/28/14.
 */
public class LocationSuggestionItem extends SuggestionItem {


    private OPLocationInterface location;


    public OPLocationInterface getLocation() {
        return location;
    }


    public LocationSuggestionItem(OPLocationInterface location, SearchController searchController){
        super(searchController);
        this.location = location;
    }


    @Override
    public String getTitle() {
        return this.location.getDisplayName();
    }

    @Override
    public String getSubTitle() {
        Location l = this.searchController.getSearchPosition();
        Double d = GeoFunctions.distance(new OPGeoPoint(l.getLatitude(), l.getLongitude()), this.location.getPosition());
        return Math.round(d/1000) + " km from you";
    }


    @Override
    public void onItemClicked() {
        this.onItemButtonClicked();
        this.searchController.doSearch();
    }

    @Override
    public void onItemButtonClicked() {
        this.searchController.addSearchQueryLocation(this);
    }

    @Override
    public int getButtonImageResource() {
        return R.drawable.ic_search_add;
    }

    @Override
    public int getImageResource() {
        if("city".equals(this.location.getType())){
            return R.drawable.ic_city;
        }
        else{
            return R.drawable.ic_village;
        }
    }

}
