package org.openplaces.search.suggestions;

import org.openplaces.R;
import org.openplaces.categories.PlaceCategory;
import org.openplaces.search.SearchController;
import org.openplaces.util.IconsManager;

/**
 * Created by ggiammat on 12/28/14.
 */
public class PlaceCategorySuggestionItem extends SuggestionItem {

    public PlaceCategory getCategory() {
        return category;
    }

    private PlaceCategory category;


    public PlaceCategorySuggestionItem(PlaceCategory category, SearchController searchController){
        super(searchController);
        this.category = category;
    }

    @Override
    public String getTitle() {
        return this.category.getName();
    }

    @Override
    public String getSubTitle(){
        return "Category";
    }

    @Override
    public void onItemClicked() {
        this.onItemButtonClicked();
        this.searchController.doSearch();
    }

    @Override
    public void onItemButtonClicked() {
        this.searchController.addPlaceCategory(this);
    }

    @Override
    public int getButtonImageResource() {
        return R.drawable.ic_commit_search_api_holo_light;
    }

    @Override
    public int getImageResource() {
        return IconsManager.getInstance(this.searchController.getAppContext()).getCategoryIconId(this.category, 3224);
    }
}
