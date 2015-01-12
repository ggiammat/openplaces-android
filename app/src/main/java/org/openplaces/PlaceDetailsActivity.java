package org.openplaces;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.openplaces.lists.ListManager;
import org.openplaces.lists.ListManagerFragment;
import org.openplaces.lists.PlaceList;
import org.openplaces.places.Place;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.List;
import java.util.Map;


public class PlaceDetailsActivity extends FragmentActivity {


    private Place place;


    private TextView placeNameTV;
    private TextView placeAddressTV;
    private TextView placeOsmTagsTV;

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        savedInstanceState.putParcelable("PLACE", this.place);
        Log.d(MapActivity.LOGTAG, "Place stored");
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            Log.d(MapActivity.LOGTAG, "Place re-stored");
            this.place = (Place) savedInstanceState.getParcelable("PLACE");
        } else {
            Intent intent = getIntent();
            this.place = intent.getParcelableExtra("PLACE");

            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment f = new ListManagerFragment();
            Bundle b = new Bundle();
            b.putParcelable("PLACE", this.place);
            f.setArguments(b);
            fragmentTransaction.add(R.id.listFragmentContainer, f);
            fragmentTransaction.commit();
        }


        setContentView(R.layout.activity_place_details);

        setTitle(this.place.getName());


        this.placeNameTV = (TextView) findViewById(R.id.place_name);
        this.placeAddressTV = (TextView) findViewById(R.id.place_address);
        this.placeOsmTagsTV = (TextView) findViewById(R.id.place_omstags);


        this.placeNameTV.setText(place.getName());
        this.placeAddressTV.setText(place.getAddressString());
        String tagsText = "Tags:\n";
        Map<String, String> tags = place.getOsmTags();
        for(String k: tags.keySet()){
            tagsText += k + ": " + tags.get(k) + "\n";
        }
        this.placeOsmTagsTV.setText(tagsText);



        initMiniMap();
    }



    private void initMiniMap(){
        final MapView minimap = (MapView) findViewById(R.id.miniMap);
        IMapController mapController = minimap.getController();
        GeoPoint startPoint = new GeoPoint(
                place.getPosition().getLat(),
                place.getPosition().getLon());
        mapController.setZoom(18);
        mapController.setCenter(startPoint);
        minimap.invalidate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_place_details, menu);

        if(this.place.getOsmTags().get("phone") == null) {
            menu.findItem(R.id.action_call).setEnabled(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_call:
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + place.getOsmTags().get("phone")));
                startActivity(callIntent);
                break;
            case R.id.action_edit:
                this.editAction();
                break;
            case R.id.action_share:
                this.shareAction();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void editAction(){
        String editUri = "geo:"+place.getPosition().getLat()+","+place.getPosition().getLon();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(editUri));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
        else {
            Log.d(MapActivity.LOGTAG, "Was not possible to resolve activity for uri: " + editUri);
        }
    }

    private void shareAction(){
        String shareBody = place.getName() + ", " + place.getCategory().getName() + "\n";
        shareBody += place.getAddressString() != null ? place.getAddressString() + "\n" : "";
        shareBody += place.getOsmTags().get("phone") != null ? place.getOsmTags().get("phone") + "\n" : "";
        shareBody += "http://www.openstreetmap.org/#map=19/" + place.getPosition().getLat() + "/" + place.getPosition().getLon();
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "OpenPlaces");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share using..."));
    }
}
