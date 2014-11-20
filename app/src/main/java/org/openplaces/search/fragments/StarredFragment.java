package org.openplaces.search.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.openplaces.MapActivity;
import org.openplaces.OpenPlacesProvider;
import org.openplaces.R;
import org.openplaces.lists.ListsManager;
import org.openplaces.model.IconsManager;
import org.openplaces.model.OPPlaceInterface;
import org.openplaces.model.Place;
import org.openplaces.model.PlaceCategoriesManager;
import org.openplaces.model.PlaceCategory;
import org.openplaces.model.ResultSet;
import org.openplaces.utils.HttpHelper;

import java.util.ArrayList;
import java.util.List;


public class StarredFragment extends Fragment {

    ListsManager lm;
    OpenPlacesProvider opp;
    StarredPlacesListAdapter adapter;
    List<Place> starredPlaces;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        View view = inflater.inflate(R.layout.searchtab_starred, container, false);

        this.starredPlaces = new ArrayList<Place>();
        this.adapter = new StarredPlacesListAdapter(getActivity(), this.starredPlaces);
        ListView list = (ListView) view.findViewById(R.id.searchStarredList);
        list.setAdapter(this.adapter);

        new LoadStarredPlacesTask().execute();

        return view;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.lm = ListsManager.getInstance(getActivity().getApplicationContext());


        this.opp = new OpenPlacesProvider(
                new HttpHelper(),
                "gabriele.giammatteo@gmail.com",
                OpenPlacesProvider.NOMINATIM_SERVER,
                OpenPlacesProvider.OVERPASS_SERVER,
                OpenPlacesProvider.REVIEW_SERVER_SERVER
        );

    }


    private class LoadStarredPlacesTask extends AsyncTask<String, Integer, List<OPPlaceInterface>> {

        protected List<OPPlaceInterface> doInBackground(String... query) {


            List<OPPlaceInterface> res = opp.getPlacesByTypesAndIds(lm.getAllStarredPlaces());
            return res;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPreExecute() {
            if(isAdded()){
                getActivity().setProgressBarIndeterminateVisibility(Boolean.TRUE);
            }
        }

        protected void onPostExecute(List<OPPlaceInterface> result) {

            if(isAdded()){
                getActivity().setProgressBarIndeterminateVisibility(Boolean.FALSE);

                ResultSet rs = ResultSet.buildFromOPPlaces(result, PlaceCategoriesManager.getInstance(getActivity().getApplicationContext()));
                Log.d(MapActivity.LOGTAG, rs.toString());
                starredPlaces.clear();
                for(Place p: rs){
                    starredPlaces.add(p);
                }
                adapter.notifyDataSetChanged();

            }
        }
    }


    public class StarredPlacesListAdapter extends BaseAdapter implements Filterable {

        private Context context;
        private LayoutInflater inflater;

        private List<Place> starredPlaces;
        private List<Place> filteredStarredPlaces;
        private String currentFilterText;

        private Filter filter = new Filter() {
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                filteredStarredPlaces = (List<Place>) results.values;
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                //Log.d(MapActivity.LOGTAG, "Filtering preset starredPlaces on: " + constraint);

                FilterResults results = new FilterResults();
                List<Place> filteringResults = new ArrayList<Place>();
                currentFilterText = constraint.toString();
                for (Place s : starredPlaces) {
                    if (s.getName().toLowerCase().contains(currentFilterText.toLowerCase())) {
                        filteringResults.add(s);
                    }
                }

                results.count = filteringResults.size();
                results.values = filteringResults;
                return results;
            }
        };


        private IconsManager icoMgnr;

        public StarredPlacesListAdapter(Context context, List<Place> starredPlaces) {
            this.context = context;
            this.starredPlaces = starredPlaces;
            this.filteredStarredPlaces = this.starredPlaces;
            this.currentFilterText = "";
            this.icoMgnr = IconsManager.getInstance(context);
            inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public Filter getFilter() {
            return this.filter;
        }

        @Override
        public int getCount() {
            return this.filteredStarredPlaces.size();
        }

        @Override
        public Object getItem(int position) {
            return this.filteredStarredPlaces.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.searchtab_starred_item, null);
            }

            TextView textView = (TextView) convertView.findViewById(R.id.searchStarredListItemName);

            Place p = (Place) this.getItem(position);

            String matchedName = p.getName();
            if(matchedName == null){
                matchedName = "???";
            }
            if (!"".equals(this.currentFilterText.trim())) {
                matchedName = matchedName.replaceAll(this.currentFilterText, "<b>" + this.currentFilterText + "</b>");
            }
            textView.setText(Html.fromHtml(matchedName));

            return convertView;
        }
    }
}