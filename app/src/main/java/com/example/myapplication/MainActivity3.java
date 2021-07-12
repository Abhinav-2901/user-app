package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.Stack;

public class MainActivity3 extends AppCompatActivity {
    BottomNavigationView navigation;
    FragmentManager manager;
    Fragment fragment_home;
    Fragment fragment_rewards;
    private String mCurrentTab;
    private HashMap<String, Stack<Fragment>> mStacks;
    public static final String TAB_HOME = "tab_home";

    public static final String TAB_REWARDS = "tab_rewards";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitybottomnav);

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setItemIconTintList(null);

        manager = getSupportFragmentManager();
        mStacks = new HashMap<String, Stack<Fragment>>();
        mStacks.put(TAB_HOME, new Stack<Fragment>());

        mStacks.put(TAB_REWARDS, new Stack<Fragment>());

        navigation.setSelectedItemId(R.id.navigation_home);
    }
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_menu:

                    Fragment fragment_home = manager.findFragmentByTag(TAB_HOME);
                    if (fragment_home != null) {
                        if (fragment_home.isAdded()) {
                            showFragment(TAB_HOME, fragment_home, true);
                            return true;
                        } else {
                            selectedTab(TAB_HOME);
                        }
                    } else {
                        selectedTab(TAB_HOME);
                    }
                    return true;
                case R.id.navigation_home:
                    Fragment fragment_stores = manager.findFragmentByTag(TAB_REWARDS);
                    if (fragment_stores != null) {
                        if (fragment_stores.isAdded()) {
                            showFragment(TAB_REWARDS, fragment_stores, true);
                            return true;
                        } else {
                            selectedTab(TAB_REWARDS);
                        }
                    } else {
                        selectedTab(TAB_REWARDS);
                    }
                    return true;




            }
            return false;
        }

    };


    private void showFragment(String tag, Fragment fragment, boolean shouldAdd) {

        mCurrentTab = tag;

        fragment_home = manager.findFragmentByTag(TAB_HOME);

        fragment_rewards = manager.findFragmentByTag(TAB_REWARDS);

        FragmentTransaction ft = manager.beginTransaction();
        if (fragment_home != null) {
            if (fragment_home.isAdded()) {
                ft.hide(fragment_home);
            }
        }

        if (fragment_rewards != null) {
            if (fragment_rewards.isAdded()) {
                ft.hide(fragment_rewards);
            }
        }


        ft.show(fragment);
        ft.commit();


    }
    public void pushFragments(String tag, Fragment fragment, boolean shouldAdd) {
        if (shouldAdd)
            mStacks.get(tag).push(fragment);

        FragmentTransaction ft = manager.beginTransaction();
        ft.add(R.id.container_main, fragment, tag);
        ft.addToBackStack(tag);
        ft.commit();

    }

    private void selectedTab(String tabId) {
        mCurrentTab = tabId;

        if (mStacks.get(tabId).size() == 0) {
            /*
             *    First time this tab is selected. So add first fragment of that tab.
             *    Dont need animation, so that argument is false.
             *    We are adding a new fragment which is not present in stack. So add to stack is true.
             */
            if (tabId.equals(TAB_HOME)) {
                pushFragments(tabId, new Home(), true);
            }  else if (tabId.equals(TAB_REWARDS)) {
                pushFragments(tabId, new Homew(), true);
            }
        } else {
            /*
             *    We are switching tabs, and target tab is already has atleast one fragment.
             *    No need of animation, no need of stack pushing. Just show the target fragment
             */
            pushFragments(tabId, mStacks.get(tabId).lastElement(), false);
        }
    }

}