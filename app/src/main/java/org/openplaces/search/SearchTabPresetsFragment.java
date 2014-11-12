package org.openplaces.search;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;

import org.openplaces.R;
import org.openplaces.SearchActivity;
import org.openplaces.model.OPLocation;
import org.openplaces.model.OPTagsFilter;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by gabriele on 11/7/14.
 */
public class SearchTabPresetsFragment extends Fragment {

    private GridView presetsList;
    private PresetSearchesAdapter presetsListAdapter;

    // Store instance variables
    private String title;
    private int page;

    // newInstance constructor for creating fragment with arguments
    public static SearchTabPresetsFragment newInstance(int page, String title) {
        SearchTabPresetsFragment fragmentFirst = new SearchTabPresetsFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }


    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("someInt", 0);
        title = getArguments().getString("someTitle");
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.searchtab_presets, container, false);
        this.presetsList = (GridView) view.findViewById(R.id.presetsSearchList);
        this.presetsListAdapter = new PresetSearchesAdapter(view.getContext(), this.loadPresetSearches());
        this.presetsList.setAdapter(this.presetsListAdapter);

        this.setUpListeners();
        return view;
    }

    private void setUpListeners(){
        this.presetsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(isAdded()){
                    ((SearchActivity) getActivity()).addPresetSearch((PresetSearch) presetsListAdapter.getItem(i));
                }
            }
        });
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.presetsList = (GridView) getView().findViewById(R.id.presetsSearchList);
    }

    public void filterPresetsList(String filterText){
        this.presetsListAdapter.getFilter().filter(filterText);
    }

    private List<PresetSearch> loadPresetSearches(){

        List<PresetSearch> presets = new LinkedList<PresetSearch>();

        PresetSearch p1 = new PresetSearch(
                "restaurant",
                new OPTagsFilter().
                        setTagFilter("amenity", OPTagsFilter.TagFilterOperation.IS_EQUALS_TO, "restaurant"));
        p1.addOtherName("ristorante");

        PresetSearch p2 = new PresetSearch(
                "supermarket",
                new OPTagsFilter().
                        setTagFilter("shop", OPTagsFilter.TagFilterOperation.IS_EQUALS_TO, "supermarket"));
        p2.addOtherName("supermercato");

        PresetSearch p3 = new PresetSearch(
                "cinema",
                new OPTagsFilter().
                        setTagFilter("amenity", OPTagsFilter.TagFilterOperation.IS_EQUALS_TO, "cinema"));

        presets.add(p1);
        presets.add(p2);
        presets.add(p3);

        return presets;
    }
}
