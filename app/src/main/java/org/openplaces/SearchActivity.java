package org.openplaces;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ListView;

import org.openplaces.helpers.HttpHelper;
import org.openplaces.model.OPLocation;
import org.openplaces.providers.OpenPlacesProvider;
import org.openplaces.search.SearchLocationsAdapter;
import org.openplaces.utils.GeoFunctions;
import org.openplaces.utils.OPGeoPoint;

import java.util.List;


public class SearchActivity extends Activity {

    private static final String TAG = "OpenPlaceSearch";
    private static final int SEARCH_LOCATION_AROUND_RADIUS = 10;


    private OpenPlacesProvider opp;
    private OPGeoPoint myLocation;
    private ListView locationsList;
    private SearchLocationsAdapter locationsListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);


        setContentView(R.layout.activity_search);

        this.opp = new OpenPlacesProvider(
                new HttpHelper(),
                "gabriele.giammatteo@gmail.com",
                OpenPlacesProvider.NOMINATIM_SERVER,
                OpenPlacesProvider.OVERPASS_SERVER,
                OpenPlacesProvider.REVIEW_SERVER_SERVER
        );

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        String locationProvider = LocationManager.NETWORK_PROVIDER;
        Location location = locationManager.getLastKnownLocation(locationProvider);
        this.myLocation = new OPGeoPoint(location.getLatitude(), location.getLongitude());
        Log.d(TAG, "Location retrieved is " + this.myLocation);

        this.locationsList = (ListView) findViewById(R.id.locationsList);
        this.locationsListAdapter = new SearchLocationsAdapter(this, this.myLocation);
        this.locationsList.setAdapter(this.locationsListAdapter);

        this.startSearch();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startSearch(){
        new SearchTask().execute();
    }

    private class SearchTask extends AsyncTask<String, Integer, List<OPLocation>> {



        protected List<OPLocation> doInBackground(String... query) {

            List<OPLocation> res = opp.getLocationsAround(myLocation, SEARCH_LOCATION_AROUND_RADIUS);
            return res;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(Boolean.TRUE);
        }

        protected void onPostExecute(List<OPLocation> result) {

            GeoFunctions.sortByDistanceFromPoint(result, myLocation);
            locationsListAdapter.setLocations(result);

            setProgressBarIndeterminateVisibility(Boolean.FALSE);

        }
    }
}
