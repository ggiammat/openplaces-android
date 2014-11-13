package org.openplaces.search;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.openplaces.search.fragments.CategoriesFragment;
import org.openplaces.search.fragments.LocationsFragment;
import org.openplaces.search.fragments.RecentsFragment;
import org.openplaces.search.fragments.StarredFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gabriele on 11/7/14.
 */
public class SearchTabsPagerAdapter extends FragmentPagerAdapter {


    public final static int RECENTS_FRAGMENT_POSITION = 0;
    public final static int CATEGORIES_FRAGMENT_POSITION = 1;
    public final static int LOCATIONS_FRAGMENT_POSITION = 2;
    public final static int STARRED_FRAGMENT_POSITION = 3;

    private List<Fragment> fragments;

    public SearchTabsPagerAdapter(FragmentManager fm) {
        super(fm);

        this.fragments = new ArrayList<Fragment>();

        this.fragments.add(new RecentsFragment());
        this.fragments.add(new CategoriesFragment());
        this.fragments.add(new LocationsFragment());
        this.fragments.add(new StarredFragment());
    }


    @Override
    public Fragment getItem(int position) {
        return this.fragments.get(position);
    }


    @Override
    public int getCount() {
        return this.fragments.size();
    }

    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {

        //TODO: localization
        if(position == 0){
            return "Recents";
        }
        else if(position == 1) {
            return "Categories";
        }
        else if(position == 2) {
            return "Locations";
        }
        else if(position == 3) {
            return "Starred";
        }

        return "???";
    }
}
