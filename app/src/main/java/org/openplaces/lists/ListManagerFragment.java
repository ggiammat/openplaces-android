package org.openplaces.lists;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.openplaces.R;
import org.openplaces.places.Place;

/**
 * Created by gabriele on 11/24/14.
 */
public class ListManagerFragment extends android.app.DialogFragment implements AdapterView.OnItemClickListener {

    Place place;
    ListManager slm;
    StarredListsAdapter listsAdapter;
    private View.OnClickListener setNoteClickListener;

    @Override
    public void setArguments(Bundle args) {
        this.place = (Place) args.getParcelable("PLACE");
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.listmanager_fragment, container, false);


        this.setNoteClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlaceList l = (PlaceList) view.getTag();
                DialogFragment frag = new AddNoteFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelable("PLACE", place);
                bundle.putString("LISTNAME", l.getName());
                frag.setArguments(bundle);
                frag.show(getFragmentManager(), "addNoteFragmentInListManagerFragment");
            }
        };



        this.listsAdapter = new StarredListsAdapter(this.setNoteClickListener, getActivity(), this.place);

        ListView starredLists = (ListView) v.findViewById(R.id.starredLists);
        starredLists.setAdapter(this.listsAdapter);
        starredLists.setOnItemClickListener(this);


        Button newListButton = (Button) v.findViewById(R.id.createNew);
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




        return v;
    }



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.slm = ListManager.getInstance(getActivity());
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




    private class StarredListsAdapter extends BaseAdapter {

        private Context context;
        private ListManager lm;
        private Place place;
        private View.OnClickListener setNoteClickListener;

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

        public StarredListsAdapter(View.OnClickListener setNoteClickListener, Context context, Place place) {
            this.context = context;
            this.setNoteClickListener = setNoteClickListener;
            this.place = place;
            this.lm = ListManager.getInstance(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(this.context).inflate(R.layout.listmanager_listitem, parent, false);
            }

            ImageView setNoteButton = (ImageView) convertView.findViewById(R.id.setNoteIV);
            TextView text = (TextView) convertView.findViewById(R.id.starredListText);
            ImageView img = (ImageView) convertView.findViewById(R.id.starImage);
            TextView noteText = (TextView) convertView.findViewById(R.id.textView4);

            PlaceList l = (PlaceList) getItem(position);
            text.setText(l.getName() + " (" + l.size() + ")");
            if(this.lm.isStarredIn(this.place, l)){
                img.setImageResource(android.R.drawable.btn_star_big_on);
                setNoteButton.setTag(l);
                setNoteButton.setOnClickListener(this.setNoteClickListener);

                if(l.getPlaceListItemByPlace(this.place).getNote() != null){
                    setNoteButton.setImageResource(R.drawable.note_on);

                    noteText.setText(l.getPlaceListItemByPlace(this.place).getNote());
                }
                else {
                    setNoteButton.setImageResource(R.drawable.note_off);
                    noteText.setText("");
                }

            }
            else {
                img.setImageResource(android.R.drawable.btn_star_big_off);
                setNoteButton.setTag(null);
                setNoteButton.setOnClickListener(null);
                setNoteButton.setImageResource(R.drawable.note_off);
                noteText.setText("");
            }



            return convertView;
        }
    }
}
