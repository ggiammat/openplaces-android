package org.openplaces.search;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.openplaces.MapActivity;
import org.openplaces.R;
import org.openplaces.categories.PlaceCategory;
import org.openplaces.lists.ListManager;
import org.openplaces.categories.PlaceCategoriesManager;
import org.openplaces.lists.PlaceList;
import org.openplaces.model.OPLocationInterface;
import org.openplaces.model.OPPlaceCategoryInterface;
import org.openplaces.places.Place;
import org.openplaces.remote.OpenPlacesRemote;
import org.openplaces.search.suggestions.ListSuggestionItem;
import org.openplaces.search.suggestions.LocationSuggestionItem;
import org.openplaces.search.suggestions.PlaceCategorySuggestionItem;
import org.openplaces.search.suggestions.SearchLocationByNameSuggestionItem;
import org.openplaces.search.suggestions.StarredPlaceSuggestionItem;
import org.openplaces.search.suggestions.SuggestionItem;
import org.openplaces.search.suggestions.SuggestionItemComparator;
import org.openplaces.tasks.LoadLocationsAround;
import org.openplaces.tasks.LoadStarredPlaces;
import org.openplaces.tasks.OpenPlacesAsyncTask;
import org.openplaces.tasks.SearchLocationsByName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ggiammat on 12/28/14.
 */
public class SearchSuggestionsAdapter extends BaseAdapter implements Filterable {


    private Activity hostingActivity;
    private SuggestionItemComparator comparator;
    private Context appContext;
    private PlaceCategoriesManager pcm;
    private ListManager lm;
    private OpenPlacesRemote opr;
    private LayoutInflater inflater;
    private SearchController searchController;
    private List<SuggestionItem> allItems;
    private String currentFilterText;

    private View.OnClickListener onItemButtonClickedListener =  new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            SuggestionItem item = (SuggestionItem) view.getTag();
            item.onItemButtonClicked();
            //add new chips could change the sorting, so filter again
            filter.filter(searchController.getSearchQueryCurrentTokenFreeText());
        }
    };


    private List<SuggestionItem> items;

    public SearchSuggestionsAdapter(Activity hostingActivity, final SearchController searchController){
        this.hostingActivity = hostingActivity;
        this.searchController = searchController;
        this.searchController.addListener(new SearchController.SearchQueryListener() {
            @Override
            public void searchStarted(SearchQuery sq) {

            }

            @Override
            public void searchEnded(SearchQuery sq, ResultSet rs) {

            }

            @Override
            public void searchQueryChanged(List<OPPlaceCategoryInterface> searchQueryCategories, List<OPLocationInterface> searchQueryLocations, String searchQueryFreeText, String searchQueryCurrentTokenFreeText) {
                if(!searchQueryCurrentTokenFreeText.equals(currentFilterText)){
                    getFilter().filter(searchQueryCurrentTokenFreeText);
                }
            }
        });


        this.items = new ArrayList<SuggestionItem>();
        this.comparator = new SuggestionItemComparator(this.searchController, this.searchController.getSearchPosition());
        this.appContext = this.hostingActivity.getApplicationContext();
        this.inflater = (LayoutInflater) this.hostingActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.pcm = PlaceCategoriesManager.getInstance(this.appContext);
        this.opr = OpenPlacesRemote.getInstance(this.appContext);
        this.lm = ListManager.getInstance(this.appContext);


        this.initItems();
    }

    @Override
    public int getCount() {
        return this.items.size();
    }

    @Override
    public Object getItem(int i) {
        return this.items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = inflater.inflate(R.layout.list_search_suggestions_item, null);
        }

        SuggestionItem item = (SuggestionItem) this.getItem(position);
        TextView locNameTV = (TextView) view.findViewById(R.id.suggestionTitle);
        TextView subtitleTV = (TextView) view.findViewById(R.id.suggestionSubtitle);
        ImageButton suggestionButton = (ImageButton) view.findViewById(R.id.suggestionButton);
        ImageView suggestionImage = (ImageView) view.findViewById(R.id.suggestionImg);
        suggestionImage.setImageResource(item.getImageResource());
        suggestionButton.setOnClickListener(this.onItemButtonClickedListener); //can be done only when created
        suggestionButton.setTag(item);
        suggestionButton.setImageResource(item.getButtonImageResource());


        String title = ((SuggestionItem) this.getItem(position)).getTitle();

        Pattern p = Pattern.compile(searchController.getSearchQueryCurrentTokenFreeText(), Pattern.CASE_INSENSITIVE);
        Matcher titleMatcher = p.matcher(title);

        //FIXME: this replace only the first match
        if(titleMatcher.find()){
            String toReplace = title.substring(titleMatcher.start(), titleMatcher.end());
            title = title.replace(toReplace, "<b>"+toReplace+"</b>");
        }

        locNameTV.setText(Html.fromHtml(title));
        subtitleTV.setText(((SuggestionItem) this.getItem(position)).getSubTitle());

        return view;
    }

    public void setFilteredItems(List<SuggestionItem> items){
        if(items == null){
            return;
        }
        this.items = items;
        Collections.sort(this.items, this.comparator);
        notifyDataSetChanged();
    }

    public void initItems(){

        allItems = new ArrayList<SuggestionItem>();

        for(PlaceCategory c: pcm.getAllCategories()){
            allItems.add(new PlaceCategorySuggestionItem(c, this.searchController));
        }

        allItems.add(new SearchLocationByNameSuggestionItem(this.searchController, this));


        for(PlaceList list: this.lm.getStarredLists()){
            allItems.add(new ListSuggestionItem(list, this.searchController));
        }

        for(PlaceList list: this.lm.getAutoLists()){
            allItems.add(new ListSuggestionItem(list, this.searchController));
        }

        getFilter().filter(searchController.getSearchQueryCurrentTokenFreeText());


        //start asynch updates
        this.loadStarredPlaces();
        this.loadAroundLocations();
//
//        for(OPLocationInterface l: opr.getKnownLocations()){
//            allItems.add(new LocationSuggestionItem(l, this.searchController));
//        }
//
//        Log.d(MapActivity.LOGTAG, "Added " + opr.getKnownLocations().size() + " locations");
//
//        allItems.add(new SearchLocationByNameSuggestionItem(this.searchController));
//
//        for(PlaceCategory c: pcm.getAllCategories()){
//            allItems.add(new PlaceCategorySuggestionItem(c, this.searchController));
//        }
//
//        Log.d(MapActivity.LOGTAG, "Added " + pcm.getAllCategories().size() + " categories");
//
//        for(Place p: opr.getKnownPlaces()){
//            allItems.add(new StarredPlaceSuggestionItem(p, this.searchController));
//        }
//
//        Log.d(MapActivity.LOGTAG, "Added " + opr.getKnownPlaces().size() + " places");
//
//
//        for(PlaceList list: this.lm.getStarredLists()){
//            allItems.add(new ListSuggestionItem(list, this.searchController));
//        }
//
//        for(PlaceList list: this.lm.getAutoLists()){
//            allItems.add(new ListSuggestionItem(list, this.searchController));
//        }

    }

    private Filter filter = new Filter(){
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            setFilteredItems((List<SuggestionItem>) results.values);

        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            Log.d(MapActivity.LOGTAG, "Filtering suggestion items with: "+constraint);
            FilterResults results = new FilterResults();

            List<SuggestionItem> filteredItems = new ArrayList<SuggestionItem>();

            String filterText = constraint.toString().trim().toLowerCase();
            for(SuggestionItem i: allItems){
                if(i.matches(filterText)){
                    filteredItems.add(i);
                }
            }

            results.count = filteredItems.size();
            results.values = filteredItems;
            currentFilterText = filterText;
            return results;
        }
    };


    @Override
    public Filter getFilter() {
        return this.filter;
    }


    public void addLocationsByName(String name){
        new SearchLocationsByName(name, this.appContext, new OpenPlacesAsyncTask.OpenPlacesAsyncTaskListener() {
            @Override
            public void taskStarted() {
                hostingActivity.setProgressBarIndeterminateVisibility(Boolean.TRUE);
            }

            @Override
            public void taskFinished(Object result, int status) {
                hostingActivity.setProgressBarIndeterminateVisibility(Boolean.FALSE);

                for(OPLocationInterface l: ((LocationResultSet) result).getLocations()){
                    Log.d(MapActivity.LOGTAG, "Adding location suggestion " + l);
                    allItems.add(new LocationSuggestionItem(l, searchController));
                }
                getFilter().filter(searchController.getSearchQueryCurrentTokenFreeText());
            }
        }).execute();
    }


    public void loadStarredPlaces(){

        new LoadStarredPlaces(this.appContext, new OpenPlacesAsyncTask.OpenPlacesAsyncTaskListener() {
            @Override
            public void taskStarted() {
                hostingActivity.setProgressBarIndeterminateVisibility(Boolean.TRUE);
            }

            @Override
            public void taskFinished(Object result, int status) {
                hostingActivity.setProgressBarIndeterminateVisibility(Boolean.FALSE);
                List<SuggestionItem> items = new ArrayList<SuggestionItem>();
                for(Place p: (ResultSet) result){
                    items.add(new StarredPlaceSuggestionItem(p, searchController));
                }
                allItems.addAll(items);
                getFilter().filter(searchController.getSearchQueryCurrentTokenFreeText());
            }
        }).execute();

    }

    public void loadAroundLocations(){
        new LoadLocationsAround(this.searchController.getSearchPosition(), this.appContext, new OpenPlacesAsyncTask.OpenPlacesAsyncTaskListener() {
            @Override
            public void taskStarted() {
                hostingActivity.setProgressBarIndeterminateVisibility(Boolean.TRUE);
            }

            @Override
            public void taskFinished(Object result, int status) {
                hostingActivity.setProgressBarIndeterminateVisibility(Boolean.FALSE);

                for(OPLocationInterface l: ((LocationResultSet) result).getLocations()){
                    allItems.add(new LocationSuggestionItem(l, searchController));
                }
                getFilter().filter(searchController.getSearchQueryCurrentTokenFreeText());
            }
        }).execute();
    }
}
