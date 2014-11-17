package org.openplaces.starred;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import org.openplaces.model.Place;

import java.util.Set;

/**
 * Created by ggiammat on 11/17/14.
 */
public class StarredListChooserFragment extends DialogFragment {

    Place place;
    StarredListsManager slm;

    @Override
    public void setArguments(Bundle args) {
        this.place = (Place) args.getParcelable("PLACE");
    }

    String[] starredLists;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        this.starredLists = slm.getStarredLists().toArray(new String[ slm.getStarredLists().size() + 1 ]);
        this.starredLists[this.starredLists.length - 1] = "Create new...";

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose a List")
                .setItems(this.starredLists, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == starredLists.length - 1) {
                            DialogFragment frag = new CreateNewStarredListFragment();
                            Bundle b = new Bundle();
                            b.putParcelable("PLACE", place);
                            frag.setArguments(b);
                            frag.show(getFragmentManager(), "newStarredListDialog");

                        }
                        else {
                            slm.starPlace(starredLists[which], place);
                            ((PlaceStarCapability)getActivity()).placeIsNowStarred(starredLists[which]);
                        }
                    }
                });
        return builder.create();
    }



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.slm = StarredListsManager.getInstance(getActivity());
    }


    public interface PlaceStarCapability{
        public void placeIsNowStarred(String listName);

        public void placeIsNowUnstarred();
    }
}
