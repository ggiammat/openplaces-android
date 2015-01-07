package org.openplaces.search.suggestions;

import org.openplaces.R;
import org.openplaces.search.SearchController;

/**
 * Created by ggiammat on 12/30/14.
 */
public class SearchLocationByNameSuggestionItem extends SuggestionItem {

    public SearchLocationByNameSuggestionItem(SearchController searchController){
        super(searchController);
    }

    @Override
    public String getTitle() {
        return "Search locations named \""+searchController.getQueryET().getLastUnchipedToken() +"\"...";
    }

    @Override
    public String getSubTitle() {
        return "Search";
    }

    @Override
    public boolean matches(String text) {
        return text.length() > 0;
    }

    @Override
    public void onItemClicked() {
        this.searchController.searchLocationByName(this.searchController.getQueryET().getLastUnchipedToken());
    }

    @Override
    public void onItemButtonClicked() {
        this.onItemClicked();
    }

    @Override
    public int getButtonImageResource() {
        return R.drawable.search_locations_32;
    }

    @Override
    public int getImageResource() {
        return R.drawable.location_32;
    }
}
