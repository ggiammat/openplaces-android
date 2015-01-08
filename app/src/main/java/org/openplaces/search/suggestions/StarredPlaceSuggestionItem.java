package org.openplaces.search.suggestions;

import android.app.Application;

import org.openplaces.R;
import org.openplaces.model.OPPlaceInterface;
import org.openplaces.places.Place;
import org.openplaces.search.SearchController;
import org.openplaces.util.IconsManager;

/**
 * Created by ggiammat on 12/28/14.
 */
public class StarredPlaceSuggestionItem extends SuggestionItem {

    private Place place;

    public StarredPlaceSuggestionItem(Place place, SearchController searchController){
        super(searchController);
        this.place = place;
    }

    @Override
    public String getTitle() {
        return this.place.getName() != null ? place.getName() : "";
    }


    @Override
    public void onItemClicked() {
        this.searchController.gotoPlace(this.place);
    }

    @Override
    public void onItemButtonClicked() {
        this.onItemClicked();
    }

    @Override
    public String getSubTitle() {
        return "Place";
    }

    @Override
    public int getButtonImageResource() {
        return R.drawable.ic_search_goto;
    }

    @Override
    public int getImageResource() {
        return IconsManager.getInstance(this.searchController.getAppContext()).getCategoryIconId(this.place.getCategory(), 3224);
    }
}
