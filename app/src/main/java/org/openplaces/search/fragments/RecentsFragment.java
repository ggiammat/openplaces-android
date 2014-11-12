package org.openplaces.search.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.openplaces.R;
import org.openplaces.search.SearchLocationsAdapter;

/**
 * Created by ggiammat on 11/12/14.
 */
public class RecentsFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.searchtab_recents, container, false);
        return view;
    }

}
