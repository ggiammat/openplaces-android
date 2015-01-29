package org.openplaces;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.openplaces.places.Place;
import org.openplaces.search.ResultSet;
import org.openplaces.search.SearchController;
import org.openplaces.search.SearchSuggestionsPopup;
import org.openplaces.tasks.LoadStarredPlaces;
import org.openplaces.tasks.OpenPlacesAsyncTask;
import org.openplaces.widgets.OPChipsEditText;
import org.osmdroid.util.BoundingBoxE6;


public class MapActivity extends Activity {

    public static final String LOGTAG = "OpenPlaces";

    private SlidingUpPanelLayout slidingLayout;

//    private ListManager slm;
//    private IconsManager icoMngr;


    //MapView mapView;
    private TextView rsStatsTV;

    private OPChipsEditText searchET;
    private ImageButton searchButtonAB;

    private ResultSet currentResultSet;
    private SearchController searchController;

    private PlaceDetailFragment placeDetailsFragment;
    private MapFragment mapFragment;

    private int oldMapViewHeight = -1;
    private static final float SLIDING_PANEL_HEIGHT_PERCENT = 0.6f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_map);


        this.slidingLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        this.rsStatsTV = (TextView) findViewById(R.id.resultSetMessages);
        this.placeDetailsFragment = (PlaceDetailFragment) getFragmentManager().findFragmentById(R.id.placeDetailsFragment);
        this.mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);


        this.setupActionBar();
    }

    private void setupActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        //actionBar.setIcon(android.R.drawable.ic_menu_search);

        LayoutInflater inflator = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.actionbar_map_activity, null);
        this.searchET = (OPChipsEditText) v.findViewById(R.id.searchET);
        this.searchButtonAB = (ImageButton) v.findViewById(R.id.searchButtonAB);

        this.searchButtonAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOGTAG, "Search Button Searched!");
                searchController.doSearch();
            }
        });

        this.searchController = new SearchController(this.searchET, this);

        new SearchSuggestionsPopup(this, this.searchController);
        actionBar.setCustomView(v);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if(this.mapFragment.getMapView().getHeight() != this.oldMapViewHeight){
            this.setSlidingPanel();
        }
    }

    private void setSlidingPanel(){
        Log.d(LOGTAG, "Setting up sliding panel...");

        int totalHeight = this.slidingLayout.getHeight();

        SlidingUpPanelLayout.LayoutParams params = (SlidingUpPanelLayout.LayoutParams) this.placeDetailsFragment.getView().getLayoutParams();

        params.height = Math.round(totalHeight * SLIDING_PANEL_HEIGHT_PERCENT);
        this.placeDetailsFragment.getView().setLayoutParams(params);
        int parallaxOffset = Math.round(totalHeight/2 - (totalHeight * (1-SLIDING_PANEL_HEIGHT_PERCENT))/2);

        this.slidingLayout.setParalaxOffset(parallaxOffset);

        this.oldMapViewHeight = totalHeight;
    }

    public void expandSlidingPanel(){
        this.slidingLayout.expandPanel();
    }

    public BoundingBoxE6 getMapVisibleArea(){
        return this.mapFragment.getMapView().getBoundingBox();
    }

    public void setNewResultSet(ResultSet rs){
        this.currentResultSet = rs;

        //select the first place
        if(this.currentResultSet.size()>0){
            this.currentResultSet.setSelected(0);
        }
        if(currentResultSet.size() == 1){
            slidingLayout.showPanel();
            slidingLayout.expandPanel();
        }

        this.currentResultSet.addListener(new ResultSet.ResultSetEventsListener() {
            @Override
            public void onNewPlaceSelected(Place oldSelected, Place newSelected) {
                if(newSelected == null){
                    slidingLayout.hidePanel();
                }
                else {
                    slidingLayout.showPanel();
                }
            }
        });





        this.placeDetailsFragment.setResultSet(this.currentResultSet);
        this.mapFragment.setResultSet(this.currentResultSet);




        if("0".equals(rs.getStat("errorCode"))) {
            this.rsStatsTV.setText("T/N/C: " + rs.size() + "/" + rs.getStat("net") + "/" + rs.getStat("cache"));
        }
        //errors are showed in a Toast





    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_showstarred:
                new LoadStarredPlaces(this.getApplicationContext(), new OpenPlacesAsyncTask.OpenPlacesAsyncTaskListener() {
                    @Override
                    public void taskStarted() {
                        setProgressBarIndeterminate(Boolean.TRUE);
                    }

                    @Override
                    public void taskFinished(Object result, int status) {
                        setProgressBarIndeterminate(Boolean.FALSE);
                        setNewResultSet((ResultSet) result);
                    }
                }).execute();
                break;
            case R.id.action_editmap:
                this.editAction();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void editAction(){
        BoundingBoxE6 bbox = this.mapFragment.getMapView().getBoundingBox();
        if(bbox.getDiagonalLengthInMeters() > 1000){
            Toast.makeText(getApplicationContext(), "Area to edit is too big. Try to zoom in!", Toast.LENGTH_SHORT).show();
            return;
        }

        String editUri = "http://127.0.0.1:8111/load_and_zoom?" +
                "bottom="+bbox.getLatSouthE6()/1e6d+
                "&top="+bbox.getLatNorthE6()/1e6d+
                "&left="+bbox.getLonWestE6()/1e6d+
                "&right="+bbox.getLonEastE6()/1e6d;
        //String editUri = "geo:41.6843531011,12.7788774503";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(editUri));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
        else {
            Log.d(MapActivity.LOGTAG, "Was not possible to resolve activity for uri: " + editUri);
        }
    }

//    @Override
//    public void placeAddedToStarredList(Place place, PlaceList list) {
//        ((Marker) this.currentResultSet.getSelected().getRelatedObject()).setIcon(
//                slm.isStarred(this.currentResultSet.getSelected()) ? this.icoMngr.getSelectedStarredMarker(this.currentResultSet.getSelected()) : this.icoMngr.getSelectedMarker(this.currentResultSet.getSelected()));
//        //this.starButton.setImageResource(slm.isStarred(this.currentResultSet.getSelected()) ? android.R.drawable.star_big_on : android.R.drawable.star_big_off);
//
//    }
//
//    @Override
//    public void placeAddedToAutoList(Place place, PlaceList list) {
//
//    }
//
//    @Override
//    public void placeRemovedFromStarredList(Place place, PlaceList list) {
//        ((Marker) this.currentResultSet.getSelected().getRelatedObject()).setIcon(
//                slm.isStarred(this.currentResultSet.getSelected()) ? this.icoMngr.getSelectedStarredMarker(this.currentResultSet.getSelected()) : this.icoMngr.getSelectedMarker(this.currentResultSet.getSelected()));
//        //this.starButton.setImageResource(slm.isStarred(this.currentResultSet.getSelected()) ? android.R.drawable.star_big_on : android.R.drawable.star_big_off);
//
//    }
//
//    @Override
//    public void placeRemovedFromAutoList(Place place, PlaceList list) {
//
//    }
//
//    @Override
//    public void starredListAdded(PlaceList list) {
//
//    }

}
