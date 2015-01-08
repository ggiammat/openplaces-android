package org.openplaces.search.suggestions;

import org.openplaces.R;
import org.openplaces.search.SearchController;
import org.openplaces.search.SearchSuggestionsAdapter;

/**
 * Created by ggiammat on 12/30/14.
 */
public class SearchLocationByNameSuggestionItem extends SuggestionItem {

    private SearchSuggestionsAdapter adapter;
    public SearchLocationByNameSuggestionItem(SearchController searchController, SearchSuggestionsAdapter adapter){
        super(searchController);
        this.adapter = adapter;
    }

    @Override
    public String getTitle() {
        return "Search locations named \""+searchController.getSearchQueryCurrentTokenFreeText() +"\"...";
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
        this.adapter.addLocationsByName(this.searchController.getSearchQueryCurrentTokenFreeText());
    }

    @Override
    public void onItemButtonClicked() {
        this.onItemClicked();
    }

    @Override
    public int getButtonImageResource() {
        return R.drawable.ic_search_locations;
    }

    @Override
    public int getImageResource() {
        return R.drawable.ic_search_locations;
    }
}
