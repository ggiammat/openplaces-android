package org.openplaces;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.openplaces.model.Place;


public class PlaceDetailsActivity extends Activity {

    private TextView placeNameTV;
    private TextView placeAddressTV;
    private TextView placeOsmIdTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_details);

        this.placeNameTV = (TextView) findViewById(R.id.place_name);
        this.placeAddressTV = (TextView) findViewById(R.id.place_address);
        this.placeOsmIdTV = (TextView) findViewById(R.id.place_osmid);

        Intent intent = getIntent();
        Place place = intent.getParcelableExtra("PLACE");

        this.placeNameTV.setText(place.getName());
        this.placeAddressTV.setText(place.getAddressString());
        this.placeOsmIdTV.setText(Long.toString(place.getId()));

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
