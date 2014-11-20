package org.openplaces.search.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.openplaces.R;
import org.openplaces.SearchActivity;
import org.openplaces.model.OPPlaceCategoryInterface;
import org.openplaces.model.PlaceCategoriesManager;
import org.openplaces.model.PlaceCategory;
import org.openplaces.search.PlaceCategoriesAdapter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by gabriele on 11/7/14.
 */
public class CategoriesFragment extends Fragment {

    private GridView presetsList;
    private PlaceCategoriesAdapter presetsListAdapter;


    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.searchtab_presets, container, false);
        this.presetsList = (GridView) view.findViewById(R.id.presetsSearchList);
        this.presetsListAdapter = new PlaceCategoriesAdapter(view.getContext(), this.loadPlaceCategories());
        this.presetsList.setAdapter(this.presetsListAdapter);
        this.setUpListeners();
        return view;
    }

    private void setUpListeners(){
        this.presetsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(isAdded()){
                    ((SearchActivity) getActivity()).addSearchPlaceCategory((OPPlaceCategoryInterface) presetsListAdapter.getItem(i), true);
                }
            }
        });
        this.presetsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if(isAdded()){
                    ((SearchActivity) getActivity()).addSearchPlaceCategory((OPPlaceCategoryInterface) presetsListAdapter.getItem(position), false);
                }
                return true;
            }
        });

        ((SearchActivity) getActivity()).addSearchTextListener(new SearchActivity.SearchTextChangedListener() {
            @Override
            public void onSearchTextChanged(String text) {
                presetsListAdapter.getFilter().filter(text.toLowerCase());
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

    private List<PlaceCategory> loadPlaceCategories(){
        if(!isAdded()){
            return new ArrayList<PlaceCategory>();
        }

        PlaceCategoriesManager pcm = PlaceCategoriesManager.getInstance(getActivity());
        return pcm.getAllCategories();
    }
}
