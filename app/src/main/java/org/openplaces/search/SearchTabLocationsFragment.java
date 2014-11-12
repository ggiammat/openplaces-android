package org.openplaces.search;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.openplaces.MapActivity;
import org.openplaces.R;
import org.openplaces.SearchActivity;
import org.openplaces.helpers.HttpHelper;
import org.openplaces.model.OPLocation;
import org.openplaces.providers.OpenPlacesProvider;
import org.openplaces.utils.GeoFunctions;
import org.openplaces.utils.OPGeoPoint;

import java.util.List;

/**
 * Created by gabriele on 11/7/14.
 */
public class SearchTabLocationsFragment extends Fragment {

    private OPGeoPoint myLocation;
    private ListView locationsList;
    private SearchLocationsAdapter locationsListAdapter;
    private OpenPlacesProvider opp;
    // Store instance variables
    private String title;
    private int page;

    // newInstance constructor for creating fragment with arguments
    public static SearchTabLocationsFragment newInstance(int page, String title) {
        SearchTabLocationsFragment fragmentFirst = new SearchTabLocationsFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("someInt", 0);
        title = getArguments().getString("someTitle");



        this.opp = new OpenPlacesProvider(
                new HttpHelper(),
                "gabriele.giammatteo@gmail.com",
                OpenPlacesProvider.NOMINATIM_SERVER,
                OpenPlacesProvider.OVERPASS_SERVER,
                OpenPlacesProvider.REVIEW_SERVER_SERVER
        );


        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        String locationProvider = LocationManager.NETWORK_PROVIDER;
        Location location = locationManager.getLastKnownLocation(locationProvider);
        this.myLocation = new OPGeoPoint(location.getLatitude(), location.getLongitude());
        Log.d(MapActivity.LOGTAG, "Location retrieved is " + this.myLocation);

        this.startSearch();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.searchtab_locations, container, false);


        this.locationsList = (ListView) view.findViewById(R.id.locationsList);
        this.locationsListAdapter = new SearchLocationsAdapter(view.getContext(), this.myLocation);
        this.locationsList.setAdapter(this.locationsListAdapter);

        this.setUpListeners();

        return view;
    }

    private void setUpListeners(){
        this.locationsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(isAdded()){
                    ((SearchActivity) getActivity()).setSearchLocation((OPLocation) locationsListAdapter.getItem(i));
                }
            }
        });
    }

    public void startSearch(){
        new SearchTask().execute();
    }

    public void filterLocationsList(String filterText){
        this.locationsListAdapter.getFilter().filter(filterText);
    }


    private class SearchTask extends AsyncTask<String, Integer, List<OPLocation>> {

        protected List<OPLocation> doInBackground(String... query) {

            System.out.println("OPP is " + opp);
            List<OPLocation> res = opp.getLocationsAround(myLocation, SearchActivity.SEARCH_LOCATION_AROUND_RADIUS);
            return res;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPreExecute() {
            if(isAdded()){
                getActivity().setProgressBarIndeterminateVisibility(Boolean.TRUE);
            }
        }

        protected void onPostExecute(List<OPLocation> result) {

            GeoFunctions.sortByDistanceFromPoint(result, myLocation);
            locationsListAdapter.setLocations(result);

            if(isAdded()){
                getActivity().setProgressBarIndeterminateVisibility(Boolean.FALSE);
            }

        }
    }
}
