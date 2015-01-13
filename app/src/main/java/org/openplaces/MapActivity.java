package org.openplaces;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.openplaces.lists.ListManager;
import org.openplaces.lists.ListManagerEventListener;
import org.openplaces.lists.ListManagerFragment;
import org.openplaces.lists.PlaceList;
import org.openplaces.lists.PlaceListItem;
import org.openplaces.tasks.LoadStarredPlaces;
import org.openplaces.tasks.OpenPlacesAsyncTask;
import org.openplaces.util.IconsManager;
import org.openplaces.places.Place;
import org.openplaces.search.ResultSet;
import org.openplaces.search.SearchController;
import org.openplaces.search.SearchSuggestionsPopup;
import org.openplaces.widgets.OPChipsEditText;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.clustering.GridMarkerClusterer;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;


public class MapActivity extends FragmentActivity implements ListManagerEventListener {

    public static final String LOGTAG = "OpenPlaces";

    private ListManager slm;
    private IconsManager icoMngr;


    MapView mapView;
    private TextView rsStatsTV;

    private OPChipsEditText searchET;
    private ImageButton searchButtonAB;

    ImageButton starButton;
    private TextView placeNameLabelTV;

    private ResultSet currentResultSet;
    private SearchController searchController;


    private GridMarkerClusterer resultSetMarkersOverlay;
    private Marker.OnMarkerClickListener markersClickListener;
    MyLocationNewOverlay oMapLocationOverlay;

    private ListManagerFragment listsManagerFragment;


    //search


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_map);

        this.icoMngr = IconsManager.getInstance(this);

        this.slm = ListManager.getInstance(this);
        this.slm.addListsEventListener(this);
        this.starButton = (ImageButton) findViewById(R.id.starButtonMapView);
        this.placeNameLabelTV = (TextView) findViewById(R.id.textView1);
        this.rsStatsTV = (TextView) findViewById(R.id.resultSetMessages);


        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        this.listsManagerFragment = new ListManagerFragment();


        this.setupActionBar();
        this.initMapView();
        this.setUpListeners();
        this.setupSearch();

    }

    private void setupSearch(){
        this.searchET.setHint("Search the map!");


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

        this.searchController = new SearchController(this.searchET, this);
        this.searchButtonAB = (ImageButton) v.findViewById(R.id.searchButtonAB);

        new SearchSuggestionsPopup(this, this.searchController);
        actionBar.setCustomView(v);

    }

    private void initMapView(){
        this.mapView = (MapView) findViewById(R.id.mapView);
        this.mapView.setMultiTouchControls(true);

        this.mapView.setTileSource(TileSourceFactory.MAPNIK);
        //this.mapView.setBuiltInZoomControls(true);
        this.mapView.setMinZoomLevel(null);
        this.mapView.setMaxZoomLevel(26);

        //set initial position and zoom
        GeoPoint startPoint = new GeoPoint(41.666667, 12.783333); //Velletri, home sweet home :)

        IMapController mapController = this.mapView.getController();

        mapController.setZoom(16);
        mapController.setCenter(startPoint);

        this.oMapLocationOverlay = new MyLocationNewOverlay(getApplicationContext(),this.mapView);
        this.mapView.getOverlays().add(oMapLocationOverlay);
        oMapLocationOverlay.enableMyLocation();

        oMapLocationOverlay.enableFollowLocation();

        oMapLocationOverlay.setDrawAccuracyEnabled(true);
        CompassOverlay compassOverlay = new CompassOverlay(this, this.mapView);
        compassOverlay.enableCompass();
        this.mapView.getOverlays().add(compassOverlay);

        this.resultSetMarkersOverlay = new GridMarkerClusterer(MapActivity.this);
        Drawable clusterIconD = getResources().getDrawable(R.drawable.marker_cluster);
        Bitmap clusterIcon = ((BitmapDrawable)clusterIconD).getBitmap();
        this.resultSetMarkersOverlay.setIcon(clusterIcon);
        mapView.getOverlayManager().add(this.resultSetMarkersOverlay);

        this.mapView.invalidate();
    }


    public void clearSelectedPlace(Boolean hidePanel){

        if(currentResultSet != null && currentResultSet.getSelected() != null){

            this.setSelectedPlace(-1);
            mapView.invalidate();

            if(hidePanel){
                GridLayout tv = (GridLayout) findViewById(R.id.detailsPanel);
                tv.setVisibility(View.INVISIBLE);
            }
        }
    }


    public void setSelectedPlace(int index){

        //"unselect" old selected place
        Place oldSelected = currentResultSet.getSelected();
        if(oldSelected != null){
            ((Marker) oldSelected.getRelatedObject()).setIcon(
                    slm.isStarred(oldSelected) ? this.icoMngr.getStrredMarker(oldSelected) : this.icoMngr.getMarker(oldSelected));
        }

        this.currentResultSet.setSelected(index);
        Place newSelectedPlace = currentResultSet.getSelected();

        Log.d(LOGTAG, "Selected place is " + newSelectedPlace);

        if(newSelectedPlace == null){
            return;
        }



        ((Marker) newSelectedPlace.getRelatedObject()).setIcon(
                slm.isStarred(newSelectedPlace) ? this.icoMngr.getSelectedStarredMarker(newSelectedPlace) : this.icoMngr.getSelectedMarker(newSelectedPlace));

        mapView.invalidate();
        mapView.getController().animateTo(new GeoPoint(newSelectedPlace.getPosition().getLat(), newSelectedPlace.getPosition().getLon()));

        TextView t1 = (TextView) findViewById(R.id.textView1);
        t1.setText(newSelectedPlace.getName());

        TextView t2 = (TextView) findViewById(R.id.textView2);
        PlaceListItem item = this.slm.getAutoListByName(ListManager.AUTOLIST_VISITED).getPlaceListItemByPlace(newSelectedPlace);
        if(item != null){

            t2.setText("Last visited on " + item.getModifiedDate() + "|" + item.getNote());
        }
        else {
            t2.setText("Never visited");
            //new UpdatePlaceTask().execute();
        }

        this.starButton.setImageResource(slm.isStarred(newSelectedPlace) ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
//
//        String starredList = slm.getStarredListsFor(newSelectedPlace);
//        if(starredList != null){
//            this.placeIsNowStarred(starredList);
//        }
//        else {
//            this.placeIsNowUnstarred();
//        }

        GridLayout panel = (GridLayout) findViewById(R.id.detailsPanel);
        panel.setVisibility(View.VISIBLE);
    }

    public BoundingBoxE6 getMapVisibleArea(){
        return this.mapView.getBoundingBox();
    }

    private void setUpListeners(){


        this.searchButtonAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOGTAG, "Search Button Searched!");
                searchController.doSearch();
            }
        });


        this.starButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle b = new Bundle();
                b.putParcelable("PLACE", currentResultSet.getSelected());
                listsManagerFragment.setArguments(b);
                listsManagerFragment.show(getFragmentManager(), "listsManagerFragmentInMapActivity");
            }
        });



        this.markersClickListener = new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                setSelectedPlace((Integer) marker.getRelatedObject());
                return true;
            }
        };

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this, new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
                clearSelectedPlace(true);
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint geoPoint) {
                Toast.makeText(MapActivity.this, "Tap on (" + geoPoint.getLatitude() + "," + geoPoint.getLongitude() + ")", Toast.LENGTH_LONG).show();
                return true;
            }
        });

        this.mapView.getOverlays().add(0, mapEventsOverlay);

        Button prevPlace = (Button) findViewById(R.id.button2);
        prevPlace.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(currentResultSet != null)
                    setSelectedPlace(currentResultSet.getPreviousIndex());
            }
        });

        Button nextPlace = (Button) findViewById(R.id.button1);
        nextPlace.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(currentResultSet != null)
                    setSelectedPlace(currentResultSet.getNextIndex());
            }
        });

        this.placeNameLabelTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentResultSet != null && currentResultSet.getSelected() != null){
                    Intent intent = new Intent(MapActivity.this, PlaceDetailsActivity.class);
                    intent.putExtra("PLACE", currentResultSet.getSelected());
                    startActivity(intent);
                }
            }
        });
    }



    public void setNewResultSet(ResultSet rs){
        this.currentResultSet = rs;
        resultSetMarkersOverlay.getItems().clear();

        GridLayout tv = (GridLayout) findViewById(R.id.detailsPanel);
        tv.setVisibility(View.INVISIBLE);

        BoundingBoxE6 zoomTo = null;

        if(rs.size() > 0) {

            double minLat = Double.MAX_VALUE;
            double maxLat = Double.MIN_VALUE;
            double minLon = Double.MAX_VALUE;
            double maxLon = Double.MIN_VALUE;

            for (Place p : currentResultSet) {
                Marker marker = new Marker(mapView);

                marker.setOnMarkerClickListener(markersClickListener);
                marker.setPosition(new GeoPoint(p.getPosition().getLat(), p.getPosition().getLon()));
                marker.setIcon(slm.isStarred(p) ? this.icoMngr.getStrredMarker(p) : this.icoMngr.getMarker(p));
                marker.setRelatedObject(Integer.valueOf(currentResultSet.indexOf(p)));
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                marker.setInfoWindowAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                p.setRelatedObject(marker);
                resultSetMarkersOverlay.add(marker);


                minLat = Math.min(minLat, p.getPosition().getLat());
                maxLat = Math.max(maxLat, p.getPosition().getLat());
                minLon = Math.min(minLon, p.getPosition().getLon());
                maxLon = Math.max(maxLon, p.getPosition().getLon());

            }

            zoomTo = new BoundingBoxE6(maxLat, maxLon, minLat, minLon);
            Log.d(LOGTAG, "Moving map to " + zoomTo);
        }


        if("0".equals(rs.getStat("errorCode"))) {
            this.rsStatsTV.setText("T/N/C: " + rs.size() + "/" + rs.getStat("net") + "/" + rs.getStat("cache"));
        }
        else if("1".equals(rs.getStat("errorCode"))) {
            Toast.makeText(getApplicationContext(), "Search area is too big. Try to zoom in!", Toast.LENGTH_LONG).show();
        }
        else {
            this.rsStatsTV.setText("ERROR!! " + rs.getStat("errorMessage"));
        }

        resultSetMarkersOverlay.invalidate();
        mapView.invalidate();

        if(zoomTo != null){
            this.oMapLocationOverlay.disableFollowLocation();
            //mapView.getController().animateTo(zoomTo.getCenter());
            //reset maxzoom and set again because zoomToBoundingBox does not work with zoom > max
            // tile zoom
            this.mapView.setMaxZoomLevel(null);
            mapView.zoomToBoundingBox(zoomTo);
            this.mapView.setMaxZoomLevel(26);
        }
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
        BoundingBoxE6 bbox = mapView.getBoundingBox();
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

    @Override
    public void placeAddedToStarredList(Place place, PlaceList list) {
        ((Marker) this.currentResultSet.getSelected().getRelatedObject()).setIcon(
                slm.isStarred(this.currentResultSet.getSelected()) ? this.icoMngr.getSelectedStarredMarker(this.currentResultSet.getSelected()) : this.icoMngr.getSelectedMarker(this.currentResultSet.getSelected()));
        this.starButton.setImageResource(slm.isStarred(this.currentResultSet.getSelected()) ? android.R.drawable.star_big_on : android.R.drawable.star_big_off);

    }

    @Override
    public void placeAddedToAutoList(Place place, PlaceList list) {

    }

    @Override
    public void placeRemovedFromStarredList(Place place, PlaceList list) {
        ((Marker) this.currentResultSet.getSelected().getRelatedObject()).setIcon(
                slm.isStarred(this.currentResultSet.getSelected()) ? this.icoMngr.getSelectedStarredMarker(this.currentResultSet.getSelected()) : this.icoMngr.getSelectedMarker(this.currentResultSet.getSelected()));
        this.starButton.setImageResource(slm.isStarred(this.currentResultSet.getSelected()) ? android.R.drawable.star_big_on : android.R.drawable.star_big_off);

    }

    @Override
    public void placeRemovedFromAutoList(Place place, PlaceList list) {

    }

    @Override
    public void starredListAdded(PlaceList list) {

    }

}
