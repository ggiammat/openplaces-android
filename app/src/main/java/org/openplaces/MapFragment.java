package org.openplaces;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.openplaces.lists.ListManager;
import org.openplaces.lists.ListManagerEventListener;
import org.openplaces.lists.PlaceList;
import org.openplaces.places.Place;
import org.openplaces.search.ResultSet;
import org.openplaces.util.IconsManager;
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


public class MapFragment extends Fragment {


    private MapView mapView;
    private ResultSet resultSet;
    private ListManager slm;
    private IconsManager icoMngr;

    private GridMarkerClusterer resultSetMarkersOverlay;
    private Marker.OnMarkerClickListener markersClickListener;
    MyLocationNewOverlay oMapLocationOverlay;

    private ResultSet.ResultSetEventsListener rsListener = new ResultSet.ResultSetEventsListener() {
        @Override
        public void onNewPlaceSelected(Place oldSelected, Place newSelected) {
            if(oldSelected != null){
                ((Marker) oldSelected.getRelatedObject()).setIcon(
                        slm.isStarred(oldSelected) ? icoMngr.getStrredMarker(oldSelected) : icoMngr.getMarker(oldSelected));
                mapView.invalidate();
            }

            if(newSelected != null) {
                ((Marker) newSelected.getRelatedObject()).setIcon(
                        slm.isStarred(newSelected) ? icoMngr.getSelectedStarredMarker(newSelected) : icoMngr.getSelectedMarker(newSelected));
                mapView.invalidate();
                mapView.getController().animateTo(new GeoPoint(newSelected.getPosition().getLat(), newSelected.getPosition().getLon()));
            }
        }
    };

    public MapFragment() {
        // Required empty public constructor
    }

    public MapView getMapView(){
        return this.mapView;
    }

    public void setResultSet(ResultSet rs){


        this.resultSet = rs;

        this.resultSetMarkersOverlay.getItems().clear();



        BoundingBoxE6 zoomTo = null;

        if(rs.size() > 0) {

            double minLat = Double.MAX_VALUE;
            double maxLat = Double.MIN_VALUE;
            double minLon = Double.MAX_VALUE;
            double maxLon = Double.MIN_VALUE;

            for (Place p : this.resultSet) {
                Marker marker = new Marker(mapView);

                marker.setOnMarkerClickListener(markersClickListener);
                marker.setPosition(new GeoPoint(p.getPosition().getLat(), p.getPosition().getLon()));
                marker.setIcon(slm.isStarred(p) ? this.icoMngr.getStrredMarker(p) : this.icoMngr.getMarker(p));
                marker.setRelatedObject(Integer.valueOf(this.resultSet.indexOf(p)));
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


        this.resultSet.addListener(rsListener);
    }



    private void initMapView(){
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

        this.oMapLocationOverlay = new MyLocationNewOverlay(getActivity().getApplicationContext(),this.mapView);
        this.mapView.getOverlays().add(oMapLocationOverlay);
        oMapLocationOverlay.enableMyLocation();

        oMapLocationOverlay.enableFollowLocation();

        oMapLocationOverlay.setDrawAccuracyEnabled(true);
        CompassOverlay compassOverlay = new CompassOverlay(getActivity(), this.mapView);
        compassOverlay.enableCompass();
        this.mapView.getOverlays().add(compassOverlay);

        this.resultSetMarkersOverlay = new GridMarkerClusterer(getActivity());
        Drawable clusterIconD = getResources().getDrawable(R.drawable.marker_cluster);
        Bitmap clusterIcon = ((BitmapDrawable)clusterIconD).getBitmap();
        this.resultSetMarkersOverlay.setIcon(clusterIcon);
        mapView.getOverlayManager().add(this.resultSetMarkersOverlay);

        this.markersClickListener = new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                resultSet.setSelected((Integer) marker.getRelatedObject());
                return true;
            }
        };

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(getActivity(), new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {

                if(resultSet != null){
                    resultSet.clearSelected();
                }
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint geoPoint) {
                Toast.makeText(getActivity(), "Tap on (" + geoPoint.getLatitude() + "," + geoPoint.getLongitude() + ")", Toast.LENGTH_LONG).show();
                return true;
            }
        });

        this.mapView.getOverlays().add(0, mapEventsOverlay);

        this.mapView.invalidate();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_map, container, false);

        this.mapView = (MapView) v.findViewById(R.id.mapView);
        this.initMapView();
        return v;
    }



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.icoMngr = IconsManager.getInstance(this.getActivity().getApplicationContext());
        this.slm = ListManager.getInstance(this.getActivity().getApplicationContext());

        this.slm.addListsEventListener(new ListManagerEventListener() {
            @Override
            public void placeAddedToStarredList(Place place, PlaceList list) {
                if(resultSet.contains(place)){
                    //not using directly palce because could be a different object and not have the relatedObject
                    Marker m = (Marker) resultSet.getPlaceAt(resultSet.indexOf(place)).getRelatedObject();
                    if(place.equals(resultSet.getSelected())){
                        m.setIcon(icoMngr.getSelectedStarredMarker(place));
                    }
                    else {
                        m.setIcon(icoMngr.getStrredMarker(place));
                    }
                }
            }

            @Override
            public void placeAddedToAutoList(Place place, PlaceList list) {

            }

            @Override
            public void placeRemovedFromStarredList(Place place, PlaceList list) {
                if(resultSet.contains(place)){
                    //not using directly palce because could be a different object and not have the relatedObject
                    Marker m = (Marker) resultSet.getPlaceAt(resultSet.indexOf(place)).getRelatedObject();
                    if(place.equals(resultSet.getSelected())){
                        if(slm.isStarred(place)) { //could be starred in other lists
                            m.setIcon(icoMngr.getSelectedStarredMarker(place));
                        }
                        else {
                            m.setIcon(icoMngr.getSelectedMarker(place));
                        }
                    }
                    else {
                        if(slm.isStarred(place)) { //could be starred in other lists
                            m.setIcon(icoMngr.getStrredMarker(place));
                        }
                        else {
                            m.setIcon(icoMngr.getMarker(place));
                        }
                    }
                }
            }

            @Override
            public void placeRemovedFromAutoList(Place place, PlaceList list) {

            }

            @Override
            public void starredListAdded(PlaceList list) {

            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
