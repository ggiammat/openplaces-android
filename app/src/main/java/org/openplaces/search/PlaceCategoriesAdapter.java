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
import android.widget.TextView;

import org.openplaces.MapActivity;
import org.openplaces.R;
import org.openplaces.model.PlaceCategory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by gabriele on 11/7/14.
 */
public class PlaceCategoriesAdapter extends BaseAdapter implements Filterable {

    private Context context;
    private LayoutInflater inflater;

    private List<PlaceCategory> searches;
    private List<PlaceCategory> filteredSearches;
    private String currentFilterText;

    private Filter filter = new Filter(){
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            filteredSearches = (List<PlaceCategory>) results.values;
            notifyDataSetChanged();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            //Log.d(MapActivity.LOGTAG, "Filtering preset searches on: " + constraint);

            FilterResults results = new FilterResults();
            List<PlaceCategory> filteringResults = new ArrayList<PlaceCategory>();
            currentFilterText = constraint.toString();
            for(PlaceCategory s: searches){
                if(s.getFirstMatchingName(currentFilterText) != null){
                    filteringResults.add(s);
                }
            }

            results.count = filteringResults.size();
            results.values = filteringResults;
            return results;
        }
    };


    public PlaceCategoriesAdapter(Context context, List<PlaceCategory> presetSearches){
        this.context = context;
        this.searches = presetSearches;
        this.filteredSearches = this.searches;
        this.currentFilterText = "";
        inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public Filter getFilter() {
        return this.filter;
    }

    @Override
    public int getCount() {
        return this.filteredSearches.size();
    }

    @Override
    public Object getItem(int position) {
        return this.filteredSearches.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listitem_preset_search, null);
        }
        TextView textView = (TextView) convertView.findViewById(R.id.presetSearchText);

        PlaceCategory s = (PlaceCategory) this.getItem(position);

        String text = s.getFirstMatchingName(this.currentFilterText);
        text = text.replaceAll(this.currentFilterText, "<b>" + this.currentFilterText + "</b>");

        textView.setText(Html.fromHtml(text));

        return convertView;
    }
}