package org.openplaces.lists;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.openplaces.R;
import org.openplaces.model.Place;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by ggiammat on 11/17/14.
 */
public class StarredListChooserFragment extends DialogFragment implements AdapterView.OnItemClickListener {

    Place place;
    ListsManager slm;
    StarredListsAdapter listsAdapter;

    @Override
    public void setArguments(Bundle args) {
        this.place = (Place) args.getParcelable("PLACE");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
//
//        this.starredLists = new ArrayList<PlaceList>(this.slm.getStarredLists());
//        this.selectedLists = this.slm.getStarredListsFor(place);
//
//        String[] names = new String[this.starredLists.size() + 1];
//        boolean[] checked = new boolean[this.starredLists.size() + 1];
//        for(int i = 0; i < this.starredLists.size(); i++){
//            names[i] = this.starredLists.get(i).getName();
//            if(this.selectedLists == null){
//                checked[i] = false;
//                continue;
//            }
//            checked[i] = this.selectedLists.contains(this.starredLists.get(i));
//        }
//        names[this.starredLists.size()] = "Create new...";
//        checked[this.starredLists.size()] = false;

        Dialog d = new Dialog(getActivity());
        d.setTitle("Toogle Place lists");
        d.setContentView(R.layout.starredlists_chooser);

        this.listsAdapter = new StarredListsAdapter(getActivity(), this.place);

        ListView starredLists = (ListView) d.findViewById(R.id.starredLists);
        starredLists.setAdapter(this.listsAdapter);
        starredLists.setOnItemClickListener(this);


        Button newListButton = (Button) d.findViewById(R.id.createNew);
        newListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment frag = new CreateNewStarredListFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelable("PLACE", place);
                frag.setArguments(bundle);
                frag.show(getFragmentManager(), "newStarredListDialog");

                //the list should be redrawn on dismiss. It happens because the newList Frag is
                //on top of this dialog
            }
        });
//        newList.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                            DialogFragment frag = new CreateNewStarredListFragment();
//                            Bundle bundle = new Bundle();
//                            bundle.putParcelable("PLACE", place);
//                            frag.setArguments(bundle);
//                            frag.show(getFragmentManager(), "newStarredListDialog");
//
//                            frag.onDismiss(new DialogInterface() {
//                                @Override
//                                public void cancel() {
//                                }
//
//                                @Override
//                                public void dismiss() {
//                                    listsAdapter.notifyDataSetChanged();
//
//                                }
//                            });
//            }
//        });

//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setTitle("Toogle Place starred lists")
//                .setMultiChoiceItems(names, checked, new DialogInterface.OnMultiChoiceClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
//                        if(i == starredLists.size()) {
//                            DialogFragment frag = new CreateNewStarredListFragment();
//                            Bundle bundle = new Bundle();
//                            bundle.putParcelable("PLACE", place);
//                            frag.setArguments(bundle);
//                            frag.show(getFragmentManager(), "newStarredListDialog");
//
//                        }
//                        else {
//                            if(b == true) {
//                                starredLists.get(i).addPlaceToList(place);
//                            }
//                            else {
//                                starredLists.get(i).removePlaceFromList(place);
//
//                                }
//                        }
//                    }
//                });
        return d;
    }



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.slm = ListsManager.getInstance(getActivity());
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        PlaceList list = this.slm.getStarredLists().get(i);
        if(this.slm.isStarredIn(place, list)){
            //remove
            list.removePlaceFromList(place);

        }
        else {
            //add
            list.addPlaceToList(place);
        }

        this.listsAdapter.notifyDataSetChanged();
    }


    public interface PlaceStarCapability{
        public void placeIsNowStarred(String listName);

        public void placeIsNowUnstarred();
    }


    private class StarredListsAdapter extends BaseAdapter {

        private Context context;
        private ListsManager lm;
        private Place place;

        @Override
        public int getCount() {
            return this.lm.getStarredLists().size();
        }

        @Override
        public Object getItem(int i) {
            return this.lm.getStarredLists().get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        //        private List<PlaceList> activeLists;
        public StarredListsAdapter(Context context, Place place) {
            this.context = context;
            this.place = place;
            this.lm = ListsManager.getInstance(context);
        }
//
//        public StarredListsAdapter(Context context, ArrayList<PlaceList> lists, List<PlaceList> activeLists) {
//            super(context, 0, lists);
//            this.activeLists = activeLists;
//        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
//            // Get the data item for this position
//            User user = getItem(position);
//            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(this.context).inflate(R.layout.starredlists_chooser_item, parent, false);
            }

            TextView text = (TextView) convertView.findViewById(R.id.starredListText);
            ImageView img = (ImageView) convertView.findViewById(R.id.starImage);
            PlaceList l = (PlaceList) getItem(position);
            text.setText(l.getName() + " (" + l.size() + ")");
            if(this.lm.isStarredIn(this.place, l)){
                img.setImageResource(android.R.drawable.star_big_on);
            }
            else {
                img.setImageResource(android.R.drawable.star_big_off);
            }




            return convertView;


//            // Lookup view for data population
//            TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
//            TextView tvHome = (TextView) convertView.findViewById(R.id.tvHome);
//            // Populate the data into the template view using the data object
//            tvName.setText(user.name);
//            tvHome.setText(user.hometown);
//            // Return the completed view to render on screen
//            return convertView;
        }
    }
}
