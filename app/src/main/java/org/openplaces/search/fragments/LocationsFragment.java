package org.openplaces.search.fragments;

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
import android.widget.ListView;

import org.openplaces.MapActivity;
import org.openplaces.OpenPlacesProvider;
import org.openplaces.R;
import org.openplaces.SearchActivity;
import org.openplaces.model.OPGeoPoint;
import org.openplaces.model.OPLocationInterface;
import org.openplaces.remote.OpenPlacesRemote;
import org.openplaces.search.SearchLocationsAdapter;
import org.openplaces.utils.GeoFunctions;
import org.openplaces.utils.HttpHelper;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gabriele on 11/7/14.
 */
public class LocationsFragment extends Fragment {

    private Location myLocation;
    private ListView locationsList;
    private SearchLocationsAdapter locationsListAdapter;
    private OpenPlacesRemote opp;


    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        this.opp = OpenPlacesRemote.getInstance(getActivity().getApplicationContext());


        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        String locationProvider = LocationManager.NETWORK_PROVIDER;
        this.myLocation = locationManager.getLastKnownLocation(locationProvider);
        Log.d(MapActivity.LOGTAG, "Location retrieved is " + this.myLocation);

        this.startSearch();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.searchtab_locations, container, false);


        this.locationsList = (ListView) view.findViewById(R.id.locationsList);
        this.locationsListAdapter = new SearchLocationsAdapter(view.getContext(), new OPGeoPoint(this.myLocation.getLatitude(), this.myLocation.getLongitude()));
        this.locationsList.setAdapter(this.locationsListAdapter);

        this.setUpListeners();

        return view;
    }

    private void setUpListeners(){
        this.locationsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(isAdded()){
                    if(i== SearchLocationsAdapter.NEAR_ME_NOW_LOCATION_POSITION) {
                        ((SearchActivity) getActivity()).setNearMeNowSearchLocation(true);
                    }
                    else if(i== SearchLocationsAdapter.CURRENT_BB_LOCATION_POSITION) {
                        ((SearchActivity) getActivity()).setCurrentViewSearchLocation(true);
                    }
                    else {
                        ((SearchActivity) getActivity()).addSearchLocation((OPLocationInterface) locationsListAdapter.getItem(i), true);
                    }
                }
            }
        });

        this.locationsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if(isAdded()){
                    if(position== SearchLocationsAdapter.NEAR_ME_NOW_LOCATION_POSITION) {
                        ((SearchActivity) getActivity()).setNearMeNowSearchLocation(false);
                    }
                    else if(position== SearchLocationsAdapter.CURRENT_BB_LOCATION_POSITION) {
                        ((SearchActivity) getActivity()).setCurrentViewSearchLocation(false);
                    }
                    else {
                        ((SearchActivity) getActivity()).addSearchLocation((OPLocationInterface) locationsListAdapter.getItem(position), false);
                    }
                }
                return true;
            }
        });

        ((SearchActivity) getActivity()).addSearchLocationListener(new SearchActivity.SearchLocationChangedListener() {
            @Override
            public void onSearchLocationChanged(String text) {
                locationsListAdapter.getFilter().filter(text.toLowerCase());
            }

            @Override
            public void onSearchLocationTokenCompleted(String text) {
                new SearchLocationByName().execute(text.trim().toLowerCase());
            }
        });
    }

    public void startSearch(){
        new SearchTask().execute();
    }

    public void filterLocationsList(String filterText){
        this.locationsListAdapter.getFilter().filter(filterText);
    }


    private class SearchTask extends AsyncTask<String, Integer, List<OPLocationInterface>> {

        protected List<OPLocationInterface> doInBackground(String... query) {

            try {
                opp.updateKnownLocationsAround(myLocation);
                List<OPLocationInterface> res = opp.getKnownLocations();
                return res;
            }
            catch (Exception ex){
                ex.printStackTrace();
                return new ArrayList<OPLocationInterface>();
            }
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPreExecute() {
            if(isAdded()){
                getActivity().setProgressBarIndeterminateVisibility(Boolean.TRUE);
            }
        }

        protected void onPostExecute(List<OPLocationInterface> result) {

            GeoFunctions.sortByDistanceFromPoint(result, new OPGeoPoint(myLocation.getLatitude(), myLocation.getLongitude()));
            locationsListAdapter.setLocations(result);

            if(isAdded()){
                getActivity().setProgressBarIndeterminateVisibility(Boolean.FALSE);
            }

        }
    }

    private class SearchLocationByName extends AsyncTask<String, Integer, List<OPLocationInterface>> {

        protected List<OPLocationInterface> doInBackground(String... query) {

            Log.d(MapActivity.LOGTAG, "Searching for locations by name: " + query[0]);

            List<OPLocationInterface> res = opp.getLocationsByName(query[0]);

            return res;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPreExecute() {
            if(isAdded()){
                getActivity().setProgressBarIndeterminateVisibility(Boolean.TRUE);
                ((SearchActivity) getActivity()).setLocationEditTextEnabled(false);
            }
        }

        protected void onPostExecute(List<OPLocationInterface> result) {

            GeoFunctions.sortByDistanceFromPoint(result, new OPGeoPoint(myLocation.getLatitude(), myLocation.getLongitude()));
            locationsListAdapter.setLocations(result);

            if(isAdded()){
                getActivity().setProgressBarIndeterminateVisibility(Boolean.FALSE);
                ((SearchActivity) getActivity()).setLocationEditTextEnabled(true);
            }

        }
    }
}
