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
 * Created by gabriele on 11/24/14.
 */
public class AddNoteFragment extends DialogFragment {

    String listName;
    Place place;
    ListManager slm;


    @Override
    public void setArguments(Bundle args) {
        this.place = (Place) args.getParcelable("PLACE");
        this.listName = args.getString("LISTNAME");
    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add note");

        // Set an EditText view to get user input
        final EditText input = new EditText(getActivity());
        PlaceList list = this.slm.getStarredListByName(listName);
        input.setText(list.getPlaceListItemByPlace(this.place).getNote());
        builder.setView(input);


        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                PlaceList list = slm.getStarredListByName(listName);
                list.addNote(place, input.getText().toString());
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
