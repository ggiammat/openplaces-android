package org.openplaces;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.openplaces.lists.ListManager;
import org.openplaces.lists.ListManagerFragment;
import org.openplaces.lists.PlaceList;
import org.openplaces.lists.PlaceListItem;
import org.openplaces.model.Place;

import java.util.List;


public class PlaceDetailsActivity extends FragmentActivity {

    ListManager slm;


    private TextView placeNameTV;
    private TextView placeAddressTV;
    private TextView placeOsmIdTV;
    private Button starPlace;
    private Button callButton;
    private Place place;
    private View.OnClickListener unStarPlaceListener;
    private View.OnClickListener starPlaceListener;
    List<PlaceList> starredList;


    private String placeCallNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_details);

        this.placeNameTV = (TextView) findViewById(R.id.place_name);
        this.placeAddressTV = (TextView) findViewById(R.id.place_address);
        this.placeOsmIdTV = (TextView) findViewById(R.id.place_osmid);
        this.starPlace = (Button) findViewById(R.id.starPlace);
        this.callButton = (Button) findViewById(R.id.callButton);

        Intent intent = getIntent();
        this.place = intent.getParcelableExtra("PLACE");

        if(place.getOsmTags().get("phone") != null){
            this.callButton.setText("Call: " + place.getOsmTags().get("phone"));
            this.callButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + place.getOsmTags().get("phone")));
                    startActivity(callIntent);
                }
            });
        }

        this.placeNameTV.setText(place.getName());
        this.placeAddressTV.setText(place.getAddressString());
        this.placeOsmIdTV.setText(Long.toString(place.getId()));


        this.slm = ListManager.getInstance(this);

        this.starredList = slm.getStarredListsFor(this.place);
        Log.d(MapActivity.LOGTAG, "Place starred in " + starredList);

        this.unStarPlaceListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //slm.unstarPlace(starredList, place);
                placeIsNowUnstarred();
            }
        };

        this.starPlaceListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                DialogFragment f = new StarredListChooserFragment();
//                Bundle b = new Bundle();
//                b.putParcelable("PLACE", place);
//                f.setArguments(b);
//                f.show(getSupportFragmentManager(), "StarredListChooser");
            }
        };



        if(this.starredList != null){
            //this.placeIsNowStarred(this.starredList);
        }
        else {
            this.placeIsNowUnstarred();
        }



        this.slm.getAutoListByName(ListManager.AUTOLIST_VISITED).addPlaceToList(this.place);
        PlaceListItem item = this.slm.getAutoListByName(ListManager.AUTOLIST_VISITED).getPlaceListItemByPlace(place);
        if(item != null){

            starPlace.setText("Last visited: " + item.getModifiedDate());
        }


        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment f = new ListManagerFragment();
        Bundle b = new Bundle();
        b.putParcelable("PLACE", this.place);
        f.setArguments(b);
        fragmentTransaction.add(R.id.listFragmentContainer, f);
        fragmentTransaction.commit();

    }

    /**
     * called on star operation is ok
     */
    public void placeIsNowStarred(String listName){
        starPlace.setText("Unstar (" + listName + ")");
        starPlace.setOnClickListener(unStarPlaceListener);
        //this.starredList = listName;
    }

    public void placeIsNowUnstarred(){
        starPlace.setText("Star");
        starPlace.setOnClickListener(starPlaceListener);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_place_details, menu);
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
