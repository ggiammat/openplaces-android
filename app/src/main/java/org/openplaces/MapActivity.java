package org.openplaces;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ListPopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import org.openplaces.lists.ListManager;
import org.openplaces.lists.ListManagerEventListener;
import org.openplaces.lists.ListManagerFragment;
import org.openplaces.lists.PlaceList;
import org.openplaces.lists.PlaceListItem;
import org.openplaces.model.IconsManager;
import org.openplaces.model.OPPlaceInterface;
import org.openplaces.model.Place;
import org.openplaces.model.PlaceCategoriesManager;
import org.openplaces.model.ResultSet;
import org.openplaces.remote.OpenPlacesRemote;
import org.openplaces.utils.HttpHelper;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MapActivity extends FragmentActivity implements ListManagerEventListener {

    public static final String LOGTAG = "OpenPlaceSearch";

    ListManager slm;
    Button searchButton;
    Button showStarredButton;
    ImageButton starButton;
    MapView mapView;
    private ResultSet resultSet;
    private GridMarkerClusterer resultSetMarkersOverlay;
    private Marker.OnMarkerClickListener markersClickListener;
    private TextView placeNameLabelTV;
    private TextView numPlacesTV;
    MyLocationNewOverlay oMapLocationOverlay;
    //private OpenPlacesProvider opp;
    private IconsManager icoMngr;
    private OpenPlacesRemote opr;
    private ListPopupWindow selectListsToShowPopup;
    private List<String> selectListsToShowItems;
    private ListManagerFragment listsManagerFragment;


//    private View.OnClickListener unStarPlaceListener;
//    private View.OnClickListener starPlaceListener;

    //TODO: these will be replaced by places icons... one day
    //Drawable iconSelected;
    //Drawable iconUnselected;
    //Drawable iconStarred;
    //Drawable iconStarredSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActionBar();
        actionBar.hide();

        setContentView(R.layout.activity_map);

        this.opr = OpenPlacesRemote.getInstance(this.getApplicationContext());

        this.icoMngr = IconsManager.getInstance(this);

        this.slm = ListManager.getInstance(this);
        this.slm.addListsEventListener(this);
        this.showStarredButton = (Button) findViewById(R.id.showStarred);
        this.searchButton = (Button) findViewById(R.id.searchButton);
        this.starButton = (ImageButton) findViewById(R.id.starButtonMapView);
        this.placeNameLabelTV = (TextView) findViewById(R.id.textView1);
        this.selectListsToShowPopup = new ListPopupWindow(this);
        this.selectListsToShowPopup.setModal(true);
        selectListsToShowPopup.setAnchorView(showStarredButton);
        selectListsToShowPopup.setWidth(500);
        selectListsToShowPopup.setHeight(700);
        this.numPlacesTV = (TextView) findViewById(R.id.numPlaces);


        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        this.listsManagerFragment = new ListManagerFragment();


//        this.iconUnselected = new LayerDrawable(new Drawable[]{
//                getResources().getDrawable(R.drawable.marker_bg),
//                getResources().getDrawable(R.drawable.pic_chinese_restaurant_3224)});
//        this.iconSelected = new LayerDrawable(new Drawable[]{
//                getResources().getDrawable(R.drawable.marker_bg_selected),
//                getResources().getDrawable(R.drawable.pic_fast_food_3224)});
//        this.iconStarred = new LayerDrawable(new Drawable[]{
//                getResources().getDrawable(R.drawable.marker_bg_starred),
//                getResources().getDrawable(R.drawable.pic_cafe_3224)});
//        this.iconStarredSelected = new LayerDrawable(new Drawable[]{
//                getResources().getDrawable(R.drawable.marker_bg_starred_selected),
//                getResources().getDrawable(R.drawable.pic_unknown_3224)});

        this.initMapView();
        this.setUpListeners();


    }


    private void initMapView(){
        this.mapView = (MapView) findViewById(R.id.mapView);
        this.mapView.setMultiTouchControls(true);

        this.mapView.setTileSource(TileSourceFactory.MAPNIK);
        //this.mapView.setBuiltInZoomControls(true);
        this.mapView.setMinZoomLevel(null);
        this.mapView.setMaxZoomLevel(null);

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


//    public void placeIsNowStarred(String listName){
//        this.starButton.setText("Unstar (" + listName + ")");
//        //this.starButton.setOnClickListener(unStarPlaceListener);
//    }
//
//    public void placeIsNowUnstarred(){
//        this.starButton.setText("Star");
//        //this.starButton.setOnClickListener(starPlaceListener);
//    }

    public void clearSelectedPlace(Boolean hidePanel){

        if(resultSet != null && resultSet.getSelected() != null){

            this.setSelectedPlace(-1);
            mapView.invalidate();

            if(hidePanel){
                GridLayout tv = (GridLayout) findViewById(R.id.detailsPanel);
                tv.setVisibility(View.INVISIBLE);
            }
        }
    }


    public void setSelectedPlace(int index){
        Log.d(LOGTAG, "Selecting place at index " + index);
        Place oldSelected = resultSet.getSelected();
        resultSet.setSelected(index);
        Log.d(LOGTAG, "Old selected place was " + oldSelected);
        if(oldSelected != null){
            ((Marker) oldSelected.getRelatedObject()).setIcon(
                    slm.isStarred(oldSelected) ? this.icoMngr.getStrredMarker(oldSelected) : this.icoMngr.getMarker(oldSelected));
        }
        this.updateSelectedPlace();
    }

    public void updateSelectedPlace(){

        Place newSelectedPlace = resultSet.getSelected();

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


    private void setUpListeners(){

        this.selectListsToShowPopup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String clickedListName = null;
                if(i > 0){
                    clickedListName = selectListsToShowItems.get(i).replaceAll("\\(.*\\)$", "").trim();
                }
                selectListsToShowPopup.dismiss();
                new LoadStarredPlaces().execute(clickedListName);
            }
        });

        this.starButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                DialogFragment f = new StarredListChooserFragment();
//                Bundle b = new Bundle();
//                b.putParcelable("PLACE", resultSet.getSelected());
//                f.setArguments(b);
//                f.show(getSupportFragmentManager(), "StarredListChooser");

                Bundle b = new Bundle();
                b.putParcelable("PLACE", resultSet.getSelected());
                listsManagerFragment.setArguments(b);
                listsManagerFragment.show(getFragmentManager(), "listsManagerFragmentInMapActivity");
            }
        });

//        this.unStarPlaceListener = new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                slm.unstarPlace(slm.getStarredList(resultSet.getSelected()), resultSet.getSelected());
//                placeIsNowUnstarred();
//            }
//        };
//
//        this.starPlaceListener = new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                DialogFragment f = new StarredListChooserFragment();
//                Bundle b = new Bundle();
//                b.putParcelable("PLACE", resultSet.getSelected());
//                f.setArguments(b);
//                f.show(getSupportFragmentManager(), "StarredListChooser");
//            }
//        };

        this.showStarredButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                selectListsToShowItems = new ArrayList<String>();
                for(PlaceList l: slm.getStarredLists()){
                    selectListsToShowItems.add(l.getName() + " (" + l.size() + ")");
                }
                selectListsToShowItems.add(0, "ALL");

                selectListsToShowPopup.setAdapter(new ArrayAdapter<String>(MapActivity.this, R.layout.select_starredliststoshow_item, selectListsToShowItems));
                selectListsToShowPopup.show();
            }
        });

        this.searchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent searchIntent = new Intent(MapActivity.this, SearchActivity.class);
                searchIntent.putExtra("VISIBLEAREA", (Parcelable) mapView.getBoundingBox());
                startActivityForResult(searchIntent, 1);
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
                Toast.makeText(MapActivity.this, "Tap on (" + geoPoint.getLatitude() + "," + geoPoint.getLongitude() + ")", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        this.mapView.getOverlays().add(0, mapEventsOverlay);

        Button prevPlace = (Button) findViewById(R.id.button2);
        prevPlace.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(resultSet != null)
                    setSelectedPlace(resultSet.getPreviousIndex());
            }
        });

        Button nextPlace = (Button) findViewById(R.id.button1);
        nextPlace.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(resultSet != null)
                    setSelectedPlace(resultSet.getNextIndex());
            }
        });

        this.placeNameLabelTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(resultSet != null && resultSet.getSelected() != null){
                    Intent intent = new Intent(MapActivity.this, PlaceDetailsActivity.class);
                    intent.putExtra("PLACE", resultSet.getSelected());
                    startActivity(intent);
                }
            }
        });
    }

    private void setNewResultSet(ResultSet rs){
        System.out.println(rs);
        resultSet = rs;

        resultSetMarkersOverlay.getItems().clear();

        GridLayout tv = (GridLayout) findViewById(R.id.detailsPanel);
        tv.setVisibility(View.INVISIBLE);

        BoundingBoxE6 zoomTo = null;

        if(rs.size() > 0) {

            double minLat = Double.MAX_VALUE;
            double maxLat = Double.MIN_VALUE;
            double minLon = Double.MAX_VALUE;
            double maxLon = Double.MIN_VALUE;

            for (Place p : resultSet) {
                Marker marker = new Marker(mapView);

                marker.setOnMarkerClickListener(markersClickListener);
                marker.setPosition(new GeoPoint(p.getPosition().getLat(), p.getPosition().getLon()));
                marker.setIcon(slm.isStarred(p) ? this.icoMngr.getStrredMarker(p) : this.icoMngr.getMarker(p));
                marker.setRelatedObject(Integer.valueOf(resultSet.indexOf(p)));
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


        this.numPlacesTV.setText("Showing " + rs.size() + " Places");

        resultSetMarkersOverlay.invalidate();
        mapView.invalidate();

        if(zoomTo != null){
            this.oMapLocationOverlay.disableFollowLocation();
            //mapView.getController().animateTo(zoomTo.getCenter());
            mapView.zoomToBoundingBox(zoomTo);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && data !=null) {
            ResultSet rs = data.getParcelableExtra("RESULTSET");
            setNewResultSet(rs);
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

    @Override
    public void placeAddedToStarredList(Place place, PlaceList list) {
        ((Marker) this.resultSet.getSelected().getRelatedObject()).setIcon(
                slm.isStarred(this.resultSet.getSelected()) ? this.icoMngr.getSelectedStarredMarker(this.resultSet.getSelected()) : this.icoMngr.getSelectedMarker(this.resultSet.getSelected()));
        this.starButton.setImageResource(slm.isStarred(this.resultSet.getSelected()) ? android.R.drawable.star_big_on : android.R.drawable.star_big_off);

    }

    @Override
    public void placeAddedToAutoList(Place place, PlaceList list) {

    }

    @Override
    public void placeRemovedFromStarredList(Place place, PlaceList list) {
        ((Marker) this.resultSet.getSelected().getRelatedObject()).setIcon(
                slm.isStarred(this.resultSet.getSelected()) ? this.icoMngr.getSelectedStarredMarker(this.resultSet.getSelected()) : this.icoMngr.getSelectedMarker(this.resultSet.getSelected()));
        this.starButton.setImageResource(slm.isStarred(this.resultSet.getSelected()) ? android.R.drawable.star_big_on : android.R.drawable.star_big_off);

    }

    @Override
    public void placeRemovedFromAutoList(Place place, PlaceList list) {

    }

    @Override
    public void starredListAdded(PlaceList list) {

    }

    private class LoadStarredPlaces extends AsyncTask<String, Integer, ResultSet> {

        protected ResultSet doInBackground(String... query) {

            String listName = query[0];
            Log.d(LOGTAG, "Getting places in starred list " + listName);
            if(listName == null){
                return opr.getPlacesByTypesAndIds(slm.getAllStarredPlaces());
            }
            else {
                Set<String> places = new HashSet<String>();
                for(PlaceListItem item: slm.getStarredListByName(listName).getPlacesInList()){
                    places.add(item.getOsmType()+":"+item.getOsmId());
                }
                return opr.getPlacesByTypesAndIds(places);
            }
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(Boolean.TRUE);
        }

        protected void onPostExecute(ResultSet result) {

            setProgressBarIndeterminateVisibility(Boolean.FALSE);

            Log.d(MapActivity.LOGTAG, result.toString());
            setNewResultSet(result);
        }
    }
}
