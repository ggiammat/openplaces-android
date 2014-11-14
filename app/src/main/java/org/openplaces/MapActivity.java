package org.openplaces;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.openplaces.model.Place;
import org.openplaces.model.ResultSet;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.clustering.GridMarkerClusterer;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapControllerOld;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;


public class MapActivity extends Activity {

    public static final String LOGTAG = "OpenPlaceSearch";


    Button searchButton;
    MapView mapView;
    private ResultSet resultSet;
    private GridMarkerClusterer resultSetMarkersOverlay;
    private Marker.OnMarkerClickListener markersClickListener;
    private TextView placeNameLabelTV;
    private TextView numPlacesTV;
    MyLocationNewOverlay oMapLocationOverlay;

    //TODO: these will be replaced by places icons... one day
    Drawable iconSelected;
    Drawable iconUnselected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActionBar();
        actionBar.hide();

        setContentView(R.layout.activity_map);



        this.searchButton = (Button) findViewById(R.id.searchButton);

        this.placeNameLabelTV = (TextView) findViewById(R.id.textView1);

        this.numPlacesTV = (TextView) findViewById(R.id.numPlaces);

        this.iconSelected = getResources().getDrawable(R.drawable.marker_red);
        this.iconUnselected = getResources().getDrawable(R.drawable.marker_gray);

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
            ((Marker) oldSelected.getRelatedObject()).setIcon(iconUnselected);
        }
        this.updateSelectedPlace();
    }

    public void updateSelectedPlace(){

        Place newSelectedPlace = resultSet.getSelected();

        Log.d(LOGTAG, "Selected place is " + newSelectedPlace);

        if(newSelectedPlace == null){
            return;
        }



        ((Marker) newSelectedPlace.getRelatedObject()).setIcon(iconSelected);

        mapView.invalidate();

        TextView t1 = (TextView) findViewById(R.id.textView1);
        t1.setText(newSelectedPlace.getName());

        TextView t2 = (TextView) findViewById(R.id.textView2);
        if(newSelectedPlace.getNumReviews()!=null && newSelectedPlace.getAverageRating() != null){
            t2.setText(newSelectedPlace.getAverageRating() + " on " + newSelectedPlace.getNumReviews() + " reviews");
        }
        else {
            t2.setText("");
            //new UpdatePlaceTask().execute();
        }

        GridLayout panel = (GridLayout) findViewById(R.id.detailsPanel);
        panel.setVisibility(View.VISIBLE);

    }


    private void setUpListeners(){
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
            ResultSet rs = data.getParcelableExtra("RESULTSET");
            System.out.println(rs);
            resultSet = rs;

            resultSetMarkersOverlay.getItems().clear();
            BoundingBoxE6 zoomTo = null;

            if(rs.size() > 0){

                double minLat = Double.MAX_VALUE;
                double maxLat = Double.MIN_VALUE;
                double minLon = Double.MAX_VALUE;
                double maxLon = Double.MIN_VALUE;

                for(Place p: resultSet){
                    Marker marker = new Marker(mapView);

                    marker.setOnMarkerClickListener(markersClickListener);
                    marker.setPosition(new GeoPoint(p.getPosition().getLat(), p.getPosition().getLon()));
                    marker.setIcon(iconUnselected);
                    marker.setRelatedObject(Integer.valueOf(resultSet.indexOf(p)));
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

            setProgressBarIndeterminateVisibility(Boolean.FALSE);

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
}
