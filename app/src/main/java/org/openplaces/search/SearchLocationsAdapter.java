package org.openplaces.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.openplaces.R;
import org.openplaces.model.OPLocation;
import org.openplaces.utils.GeoFunctions;
import org.openplaces.utils.OPGeoPoint;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by gabriele on 11/6/14.
 */
public class SearchLocationsAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private List<OPLocation> locations;
    private OPGeoPoint myLocation;
    private DecimalFormat distanceFormatter = new DecimalFormat("#.##");

    public SearchLocationsAdapter(Context context, OPGeoPoint myLocation){
        this.context = context;
        this.myLocation = myLocation;
        this.locations = new LinkedList<OPLocation>();
        inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    public void setLocations(List<OPLocation> locations){
        this.locations = locations;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.locations.size();
    }

    @Override
    public Object getItem(int position) {
        return this.locations.get(position);
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

        OPLocation loc = (OPLocation) this.getItem(position);

        locNameTV.setText(loc.getDisplayName());
        String distance = this.distanceFormatter.format(
                GeoFunctions.distance(myLocation, loc.getPosition())/1000d);
        locInfoTV.setText(distance + " km");

        return convertView;
    }
}
