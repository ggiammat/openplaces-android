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
import org.openplaces.places.Place;

import java.util.List;
import java.util.Map;


public class PlaceDetailsActivity extends FragmentActivity {

    ListManager slm;


    private TextView placeNameTV;
    private TextView placeAddressTV;
    private TextView placeOsmTagsTV;
    private Button callButton;
    private Button editButton;
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
        this.placeOsmTagsTV = (TextView) findViewById(R.id.place_omstags);
        this.callButton = (Button) findViewById(R.id.callButton);
        this.editButton = (Button) findViewById(R.id.editButton);

        this.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String editUri = "geo:"+place.getPosition().getLat()+","+place.getPosition().getLon();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(editUri));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
                else {
                    Log.d(MapActivity.LOGTAG, "Was not possible to resolve activity for uri: " + editUri);
                }
            }
        });

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
        String tagsText = "Tags:\n";
        Map<String, String> tags = place.getOsmTags();
        for(String k: tags.keySet()){
            tagsText += k + ": " + tags.get(k) + "\n";
        }
        this.placeOsmTagsTV.setText(tagsText);


        this.slm = ListManager.getInstance(this);

        this.starredList = slm.getStarredListsFor(this.place);
        Log.d(MapActivity.LOGTAG, "Place starred in " + starredList);



        this.slm.getAutoListByName(ListManager.AUTOLIST_VISITED).addPlaceToList(this.place);


        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment f = new ListManagerFragment();
        Bundle b = new Bundle();
        b.putParcelable("PLACE", this.place);
        f.setArguments(b);
        fragmentTransaction.add(R.id.listFragmentContainer, f);
        fragmentTransaction.commit();

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
