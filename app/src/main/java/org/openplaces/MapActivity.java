package org.openplaces;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import org.openplaces.search.ResultSet;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;


public class MapActivity extends Activity {

    public static final String LOGTAG = "OpenPlaceSearch";


    Button searchButton;
    MapView mapView;
    private ResultSet resultSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActionBar();
        actionBar.hide();

        setContentView(R.layout.activity_map);



        this.searchButton = (Button) findViewById(R.id.searchButton);

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

        mapController.setZoom(17);
        mapController.setCenter(startPoint);

        MyLocationNewOverlay oMapLocationOverlay = new MyLocationNewOverlay(getApplicationContext(),this.mapView);
        this.mapView.getOverlays().add(oMapLocationOverlay);
        oMapLocationOverlay.enableMyLocation();
        oMapLocationOverlay.enableFollowLocation();
        oMapLocationOverlay.setDrawAccuracyEnabled(true);
        CompassOverlay compassOverlay = new CompassOverlay(this, this.mapView);
        compassOverlay.enableCompass();
        this.mapView.getOverlays().add(compassOverlay);



//        //add listener
//        this.mapViewListener = new MapViewListener(this);
//        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this, this.mapViewListener);
//        this.mapView.getOverlays().add(0, mapEventsOverlay);

        this.mapView.invalidate();
    }

    private void setUpListeners(){
        this.searchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent searchIntent = new Intent(MapActivity.this, SearchActivity.class);
                startActivityForResult(searchIntent, 1);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
            ResultSet rs = (ResultSet) data.getParcelableExtra("RESULTSET");
            System.out.println(rs);
            resultSet = rs;

//            if(mapMarkersOverlay == null){
//                mapView.getOverlayManager().remove(mapMarkersOverlay);
//                mapMarkersOverlay = new GridMarkerClusterer(MapActivity.this);
//                Drawable clusterIconD = getResources().getDrawable(R.drawable.marker_cluster);
//                Bitmap clusterIcon = ((BitmapDrawable)clusterIconD).getBitmap();
//                mapMarkersOverlay.setIcon(clusterIcon);
//                mapView.getOverlayManager().add(mapMarkersOverlay);
//            }
//            else {
//                mapMarkersOverlay.getItems().clear();
//            }
//
//
//
//
//
//            Drawable icon = getResources().getDrawable(R.drawable.ic_launcher);
//            for(OSMPlace p: currentResultSet){
//                Marker marker = new Marker(mapView);
//
//                marker.setOnMarkerClickListener(mapViewListener);
//                marker.setPosition(new GeoPoint(p.getLat(), p.getLon()));
//                marker.setIcon(icon);
//                marker.setRelatedObject(p);
//                p.setRelatedObject(marker);
//                mapMarkersOverlay.add(marker);
//            }
//            mapMarkersOverlay.invalidate();
//            mapView.invalidate();

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
