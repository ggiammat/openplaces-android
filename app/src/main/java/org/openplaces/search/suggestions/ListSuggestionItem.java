package org.openplaces.search.suggestions;

import org.openplaces.R;
import org.openplaces.lists.PlaceList;
import org.openplaces.search.SearchController;

/**
 * Created by ggiammat on 12/31/14.
 */
public class ListSuggestionItem extends SuggestionItem {

    private PlaceList list;

    public ListSuggestionItem(PlaceList list, SearchController searchController){
        super(searchController);
        this.list = list;
    }

    @Override
    public String getTitle() {
        return this.list.getName();
    }

    @Override
    public String getSubTitle() {
        return "List " + this.list.getType() + " (" + this.list.size()+")";
    }

    @Override
    public void onItemClicked() {
        this.searchController.showList(this.list);
    }

    @Override
    public void onItemButtonClicked() {
        this.onItemClicked();
    }

    @Override
    public int getButtonImageResource() {
        return R.drawable.ic_search_goto;
    }

    @Override
    public int getImageResource() {
        return R.drawable.ic_starred_list;
    }



}
