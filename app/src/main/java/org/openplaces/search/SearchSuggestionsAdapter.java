package org.openplaces.search;

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
import org.openplaces.lists.ListManager;
import org.openplaces.lists.PlaceList;
import org.openplaces.model.OPLocationInterface;
import org.openplaces.categories.PlaceCategoriesManager;
import org.openplaces.categories.PlaceCategory;
import org.openplaces.places.Place;
import org.openplaces.remote.OpenPlacesRemote;
import org.openplaces.search.suggestions.ListSuggestionItem;
import org.openplaces.search.suggestions.LocationSuggestionItem;
import org.openplaces.search.suggestions.PlaceCategorySuggestionItem;
import org.openplaces.search.suggestions.SearchLocationByNameSuggestionItem;
import org.openplaces.search.suggestions.StarredPlaceSuggestionItem;
import org.openplaces.search.suggestions.SuggestionItem;
import org.openplaces.search.suggestions.SuggestionItemComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ggiammat on 12/28/14.
 */
public class SearchSuggestionsAdapter extends BaseAdapter implements Filterable {


    private Context context;
    private SuggestionItemComparator comparator;
    private Context appContext;
    private PlaceCategoriesManager pcm;
    private ListManager lm;
    private OpenPlacesRemote opr;
    private LayoutInflater inflater;
    private SearchController searchController;
    private List<SuggestionItem> allItems;

    private View.OnClickListener onItemButtonClickedListener =  new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            SuggestionItem item = (SuggestionItem) view.getTag();
            item.onItemButtonClicked();
            //add new chips could change the sorting, so filter again
            filter.filter(searchController.getQueryET().getLastUnchipedToken());
        }
    };


    private List<SuggestionItem> items;

    public SearchSuggestionsAdapter(Context context, final SearchController searchController){

        this.items = new ArrayList<SuggestionItem>();
        this.searchController = searchController;
        this.comparator = new SuggestionItemComparator(this.searchController, this.searchController.getSearchPosition());
        this.appContext = context.getApplicationContext();
        this.context = context;
        this.inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.pcm = PlaceCategoriesManager.getInstance(this.appContext);
        this.opr = OpenPlacesRemote.getInstance(this.appContext);
        this.lm = ListManager.getInstance(this.appContext);


        this.searchController.addListener(new SearchController.SearchQueryListener() {
            @Override
            public void freeTextQueryChanged(String freeTextQuery) {
                filter.filter(freeTextQuery);
            }

            @Override
            public void searchStarted(SearchQuery sq) {

            }

            @Override
            public void searchEnded(SearchQuery sq, ResultSet rs) {

            }

            @Override
            public void newLocationsAvailable(List<OPLocationInterface> locs) {
                for(OPLocationInterface l: locs){
                    allItems.add(new LocationSuggestionItem(l, searchController));
                }
                filter.filter(searchController.getQueryET().getLastUnchipedToken());
            }

            @Override
            public void newPlacesAvailable(List<Place> places) {
                for(Place l: places){
                    allItems.add(new StarredPlaceSuggestionItem(l, searchController));
                }
                filter.filter(searchController.getQueryET().getLastUnchipedToken());
            }
        });

        this.reloadAllItems();
        this.setItems(this.allItems);

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

        Pattern p = Pattern.compile(searchController.getFreeText(), Pattern.CASE_INSENSITIVE);
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

    public void setItems(List<SuggestionItem> items){
        this.items = items;
         Collections.sort(this.items, this.comparator);
        notifyDataSetChanged();
    }

    public void reloadAllItems(){

        allItems = new ArrayList<SuggestionItem>();

        for(OPLocationInterface l: opr.getKnownLocations()){
            allItems.add(new LocationSuggestionItem(l, this.searchController));
        }

        allItems.add(new SearchLocationByNameSuggestionItem(this.searchController));

        for(PlaceCategory c: pcm.getAllCategories()){
            allItems.add(new PlaceCategorySuggestionItem(c, this.searchController));
        }

        for(Place p: opr.getKnownPlaces()){
            allItems.add(new StarredPlaceSuggestionItem(p, this.searchController));
        }

        for(PlaceList list: this.lm.getStarredLists()){
            allItems.add(new ListSuggestionItem(list, this.searchController));
        }

        for(PlaceList list: this.lm.getAutoLists()){
            allItems.add(new ListSuggestionItem(list, this.searchController));
        }

    }

    private Filter filter = new Filter(){
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            setItems((List<SuggestionItem>) results.values);
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            Log.d(MapActivity.LOGTAG, "Filtering with: "+constraint);
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
            return results;
        }
    };


    @Override
    public Filter getFilter() {
        return this.filter;
    }
}
