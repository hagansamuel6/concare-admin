package io.icode.concareghadmin.application.activities.adapters;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

// ViewPager Adapter Class
public class ViewPagerAdapter extends FragmentPagerAdapter {

    ArrayList<Fragment> fragments;
    ArrayList<String> titles;

    public ViewPagerAdapter(FragmentManager fm){
        super(fm);
        this.fragments = new ArrayList<>();
        this.titles = new ArrayList<>();
    }

    // get the positions of the fragments
    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    // get Count of fragments
    @Override
    public int getCount() {
        return fragments.size();
    }

    //method to add Fragment and their titles to the list
    public void addFragment(Fragment fragment, String title){
        fragments.add(fragment);
        titles.add(title);
    }

    // Press Ctrl + 0
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }
}