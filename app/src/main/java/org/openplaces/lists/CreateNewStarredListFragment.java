package org.openplaces.lists;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;

import org.openplaces.places.Place;

/**
 * Created by ggiammat on 11/17/14.
 */
public class CreateNewStarredListFragment extends DialogFragment {

    ListManager slm;
    Place place;


    @Override
    public void setArguments(Bundle args) {
        this.place = (Place) args.getParcelable("PLACE");
    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Create new Starred list");

        // Set an EditText view to get user input
        final EditText input = new EditText(getActivity());
        builder.setView(input);


        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                PlaceList l = slm.createNewStarredList(input.getText().toString());
                l.addPlaceToList(place);
               // ((StarredListChooserFragment.PlaceStarCapability)getActivity()).placeIsNowStarred(input.getText().toString());
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        return builder.create();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.slm = ListManager.getInstance(getActivity());
    }
}
