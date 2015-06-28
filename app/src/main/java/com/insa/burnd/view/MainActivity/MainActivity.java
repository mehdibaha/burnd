package com.insa.burnd.view.MainActivity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.insa.burnd.R;
import com.insa.burnd.controller.NewsfeedAdapter;
import com.insa.burnd.sync.SyncAdapter;
import com.insa.burnd.utils.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    private static String TAG = "BURND-MainActivity";

    private Adapter adapter;
    private final MainActivity activity = this;
    private boolean clickedOnSearch;

    // Sync Adapter
    public static final String AUTHORITY = "com.insa.burnd.provider"; // Authority of sync adapter's content provider
    public static final String ACCOUNT_TYPE = "example.com";     // An account type, in the form of a domain name
    public static final String ACCOUNT = "dummyaccount";     // The account name
    private static Account mAccount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initToolbar();

        // Setting pager and tabLayout
        ViewPager viewPager = (ViewPager) findViewById(R.id.main_pager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

        setupSyncadapter();
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(new NewsfeedFragment(), "Newsfeed");
        adapter.addFragment(new MeetingFragment(), "Meeting");
        adapter.addFragment(new SettingsFragment(), "Settings");
        viewPager.setAdapter(adapter);
    }

    private void initToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
    }

    public NewsfeedFragment getNewsfeedFragment() {
        return (NewsfeedFragment) adapter.getItem(0);
    }

    private void setupSyncadapter() {
        mAccount = CreateSyncAccount(this); // Create the dummy account
        /* Turn on periodic syncing */
        ContentResolver.setSyncAutomatically(mAccount, AUTHORITY, true);
        //Si le matchSync est inactif, on l'active.
        if(!SyncAdapter.checkSyncs(mAccount, AUTHORITY, "matchSync")){
            Log.d(TAG, "matchSync" + "Constructing Match Notif");
            Bundle b = new Bundle();
            b.putString("reqID","matchSync");
            ContentResolver.addPeriodicSync(
                    mAccount,
                    AUTHORITY,
                    b,
                    60*10); // every 10 minutes, cause : battery leaks
        }
        Log.d(TAG, "Sync" + "Complete");
    }

    //méthode récupérée sur les tutos android pour le syncAdapter
    public static Account CreateSyncAccount(Context context) {
        // Create the account type and default account
        Account newAccount = new Account(
                ACCOUNT, ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager = (AccountManager) context.getSystemService(ACCOUNT_SERVICE);
        /* Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(newAccount, null, null))
            return newAccount;
        else {
            Log.d(TAG, "error creating account");
            return newAccount;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickedOnSearch = !clickedOnSearch;
                getNewsfeedFragment().hideViews(clickedOnSearch);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            private NewsfeedFragment newsfeedFragment = getNewsfeedFragment();
            private NewsfeedAdapter newsfeedAdapter = newsfeedFragment.getNewsfeedAdapter();

            @Override
            public boolean onQueryTextSubmit(String s) {
                newsfeedAdapter.setFilter(s);
                Toast.makeText(activity, s, Toast.LENGTH_LONG).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(!TextUtils.isEmpty(s))
                    newsfeedAdapter.setFilter(s);
                else
                    newsfeedAdapter.flushFilter();

                return false;
            }
        });
        searchView.setQueryHint(getString(R.string.hint));

        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    // Object qui gère le changement de pages de l'activité + titres
    public static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }

    public static Account getAccount(){
        return mAccount;
    }

}
