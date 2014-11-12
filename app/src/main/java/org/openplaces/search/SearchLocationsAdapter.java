package org.openplaces.search;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import org.openplaces.R;
import org.openplaces.model.Location;
import org.openplaces.model.OPGeoPoint;
import org.openplaces.model.OPLocationInterface;
import org.openplaces.model.impl.OPLocationImpl;
import org.openplaces.utils.GeoFunctions;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by gabriele on 11/6/14.
 */
public class SearchLocationsAdapter extends BaseAdapter implements Filterable {

    private Context context;
    private LayoutInflater inflater;
    private List<Location> locations;
    private List<Location> filteredLocations;
    private OPGeoPoint myLocation;
    private DecimalFormat distanceFormatter = new DecimalFormat("#.##");
    private String currentFilterText;

    private Filter filter = new Filter(){
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            filteredLocations = (List<Location>) results.values;
            notifyDataSetChanged();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            List<OPLocationInterface> filteringResults = new ArrayList<OPLocationInterface>();
            filteringResults.add(0, new Location()); //add placeholder for "Near me now" location
            currentFilterText = constraint.toString();
            for(OPLocationInterface l: locations){
                if(l.getDisplayName().toLowerCase().contains(currentFilterText.toLowerCase())){
                    filteringResults.add(l);
                }
            }

            results.count = filteringResults.size();
            results.values = filteringResults;
            return results;
        }
    };

    public SearchLocationsAdapter(Context context, OPGeoPoint myLocation){
        this.context = context;
        this.myLocation = myLocation;
        this.locations = new LinkedList<Location>();
        this.filteredLocations = this.locations;
        inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public Filter getFilter() {
        return this.filter;
    }

    public void setLocations(List<Location> locations){
        this.locations = locations;
        this.getFilter().filter("");
    }

    @Override
    public int getCount() {
        return this.filteredLocations.size();
    }

    @Override
    public Object getItem(int position) {
        return this.filteredLocations.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listitem_search_location, null);
        }
        TextView locNameTV = (TextView) convertView.findViewById(R.id.loc_name);
        TextView locInfoTV = (TextView) convertView.findViewById(R.id.loc_info);

        if(position == 0){
            //TODO add localization
            locNameTV.setText("[Near me now]");
            locNameTV.setTypeface(null, Typeface.ITALIC);
            locInfoTV.setText("");
        }
        else {
            Location loc = (Location) this.getItem(position);

            locNameTV.setText(loc.getDisplayName());
            locNameTV.setTypeface(null, Typeface.NORMAL);
            String distance = this.distanceFormatter.format(
                    GeoFunctions.distance(myLocation, loc.getPosition())/1000d);
            locInfoTV.setText(distance + " km");
        }

        return convertView;
    }
}
