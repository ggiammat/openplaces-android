package org.openplaces;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.openplaces.model.Place;
import org.openplaces.starred.StarredListChooserFragment;
import org.openplaces.starred.StarredListsManager;


public class PlaceDetailsActivity extends FragmentActivity implements StarredListChooserFragment.PlaceStarCapability {

    StarredListsManager slm = StarredListsManager.getInstance(this);


    private TextView placeNameTV;
    private TextView placeAddressTV;
    private TextView placeOsmIdTV;
    private Button starPlace;
    private Place place;
    private View.OnClickListener unStarPlaceListener;
    private View.OnClickListener starPlaceListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_details);

        this.placeNameTV = (TextView) findViewById(R.id.place_name);
        this.placeAddressTV = (TextView) findViewById(R.id.place_address);
        this.placeOsmIdTV = (TextView) findViewById(R.id.place_osmid);
        this.starPlace = (Button) findViewById(R.id.starPlace);

        Intent intent = getIntent();
        this.place = intent.getParcelableExtra("PLACE");

        this.placeNameTV.setText(place.getName());
        this.placeAddressTV.setText(place.getAddressString());
        this.placeOsmIdTV.setText(Long.toString(place.getId()));


        final String starredList = slm.getStarredList(this.place.getId());
        this.unStarPlaceListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slm.unstarPlace(starredList, place.getId());
                placeIsNowUnstarred();
            }
        };

        this.starPlaceListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment f = new StarredListChooserFragment();
                Bundle b = new Bundle();
                b.putLong("PLACEID", place.getId());
                f.setArguments(b);
                f.show(getSupportFragmentManager(), "StarredListChooser");
            }
        };



        if(starredList != null){
            this.starPlace.setText("Unstar (" + starredList + ")");
            this.starPlace.setOnClickListener(unStarPlaceListener);
        }
        else {
            this.starPlace.setText("Star");
            this.starPlace.setOnClickListener(starPlaceListener);
        }



    }

    /**
     * called on star operation is ok
     */
    public void placeIsNowStarred(String listName){
        starPlace.setText("Unstar (" + listName + ")");
        starPlace.setOnClickListener(unStarPlaceListener);
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
