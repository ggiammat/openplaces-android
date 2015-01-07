package org.openplaces.search.suggestions;

import org.openplaces.search.SearchController;

/**
 * Created by ggiammat on 12/28/14.
 */
public abstract class SuggestionItem {

    protected SearchController searchController;

    public SuggestionItem(SearchController searchController){
        this.searchController = searchController;
    }

    public abstract String getTitle();

    public abstract String getSubTitle();

    public boolean matches(String text){
        return this.getTitle().toLowerCase().contains(text) ||
                this.getSubTitle().toLowerCase().contains(text);
    }

    public abstract void onItemClicked();

    public abstract void onItemButtonClicked();

    public abstract int getButtonImageResource();

    public abstract int getImageResource();

}
