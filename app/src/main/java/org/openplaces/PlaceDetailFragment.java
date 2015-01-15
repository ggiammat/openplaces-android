package org.openplaces;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.openplaces.lists.ListManagerFragment;
import org.openplaces.places.Place;
import org.openplaces.search.ResultSet;

import java.util.Map;


public class PlaceDetailFragment extends Fragment {

    private ResultSet resultSet;
    TextView placeNameTV;

    private ResultSet.ResultSetEventsListener rsListener = new ResultSet.ResultSetEventsListener() {
        @Override
        public void onNewPlaceSelected(Place oldSelected, Place newSelected) {
                updateViews();
        }
    };

    public void setResultSet(ResultSet rs){
        if(this.resultSet != null) {
            this.resultSet.removeListener(rsListener);
        }
        this.resultSet = rs;
        this.resultSet.addListener(rsListener);
        this.updateViews();
    }

    private void updateViews(){
        Place p = this.resultSet.getSelected();
        if(p != null){
            this.placeNameTV.setText(p.getName());
        }
        else {
            this.placeNameTV.setText("??");
        }
    }


    public PlaceDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_place_detail, container, false);
        this.placeNameTV = (TextView) v.findViewById(R.id.placeDetailName);

        Button prevPlace = (Button) v.findViewById(R.id.button2);
        prevPlace.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(resultSet != null)
                    resultSet.selectPrevious();
            }
        });

        Button nextPlace = (Button) v.findViewById(R.id.button1);
        nextPlace.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(resultSet != null)
                    resultSet.selectNext();
            }
        });

        ImageButton starButton = (ImageButton) v.findViewById(R.id.starButtonMapView);
        starButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(resultSet == null || resultSet.getSelected() == null){
                    return;
                }
                Bundle b = new Bundle();
                b.putParcelable("PLACE", resultSet.getSelected());

                ListManagerFragment listsManagerFragment = new ListManagerFragment();
                listsManagerFragment.setArguments(b);
                listsManagerFragment.show(getFragmentManager(), "listsManagerFragmentInMapActivity");
            }
        });

        ImageButton shareButton = (ImageButton) v.findViewById(R.id.placeDetailShare);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(resultSet == null || resultSet.getSelected() == null){
                    return;
                }
                shareAction(resultSet.getSelected());
            }
        });

        ImageButton tagsButton = (ImageButton) v.findViewById(R.id.placeDetailTags);
        tagsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(resultSet == null || resultSet.getSelected() == null){
                    return;
                }
                String tagsText = "===== All Tags =====\n";
                Map<String, String> tags = resultSet.getSelected().getOsmTags();
                for(String k: tags.keySet()){
                    tagsText += k + ": " + tags.get(k) + "\n";
                }
                Toast.makeText(getActivity().getApplicationContext(), tagsText, Toast.LENGTH_LONG).show();
            }
        });

        ImageButton editButton = (ImageButton) v.findViewById(R.id.placeDetailEdit);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(resultSet == null || resultSet.getSelected() == null){
                    return;
                }
                editAction(resultSet.getSelected());
            }
        });

        ImageButton callButton = (ImageButton) v.findViewById(R.id.placeDetailCall);
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(resultSet == null || resultSet.getSelected() == null || !resultSet.getSelected().getOsmTags().containsKey("phone")){
                    return;
                }
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + resultSet.getSelected().getOsmTags().get("phone")));
                startActivity(callIntent);
            }
        });
        return v;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void editAction(Place place){
        String editUri = "geo:"+place.getPosition().getLat()+","+place.getPosition().getLon();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(editUri));
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
        else {
            Log.d(MapActivity.LOGTAG, "Was not possible to resolve activity for uri: " + editUri);
        }
    }

    private void shareAction(Place place){
        String shareBody = place.getName() + ", " + place.getCategory().getName() + "\n";
        shareBody += place.getAddressString() != null ? place.getAddressString() + "\n" : "";
        shareBody += place.getOsmTags().get("phone") != null ? place.getOsmTags().get("phone") + "\n" : "";
        shareBody += "http://www.openstreetmap.org/#map=19/" + place.getPosition().getLat() + "/" + place.getPosition().getLon();
        shareBody += "\n--\nOpen Places";
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "OpenPlaces");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share using..."));
    }
}
