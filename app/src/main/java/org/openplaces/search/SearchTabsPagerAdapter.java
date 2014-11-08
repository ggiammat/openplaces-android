package org.openplaces.search;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by gabriele on 11/7/14.
 */
public class SearchTabsPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragments;
    /**
     * @param fm
     * @param fragments
     */
    public SearchTabsPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }
    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentPagerAdapter#getItem(int)
     */
    @Override
    public Fragment getItem(int position) {
        return this.fragments.get(position);
    }

    /* (non-Javadoc)
     * @see android.support.v4.view.PagerAdapter#getCount()
     */
    @Override
    public int getCount() {
        return this.fragments.size();
    }

    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {
        if(position == 0){
            return "What";
        }
        else if(position == 1) {
            return "Where";
        }
        return "Page " + position;
    }
}
