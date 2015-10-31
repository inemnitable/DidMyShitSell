package com.xorz.didmyshitsell;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.xorz.didmyshitsell.adapters.ListingsAdapter;
import com.xorz.didmyshitsell.fragments.ListingsFragment;
import com.xorz.didmyshitsell.objects.Transaction;
import com.xorz.didmyshitsell.objects.Wallet;
import com.xorz.didmyshitsell.utilities.GW2Items;
import com.xorz.didmyshitsell.utilities.Utilities;

import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ListingsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final static String TAG = "ListingsActivity";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        String apiKey = ((ShitSellingApplication) getApplication()).getUserAPIKey();
        if (apiKey.equals("")) {
            setInitialViews();
        } else {
            setListingsViews();
        }
    }

    private void setInitialViews() {
        setContentView(R.layout.set_api_key_initial);
        Button submit = (Button) findViewById(R.id.button_submit_api_key);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText input = (EditText) findViewById(R.id.input_api_key);
                String newKey = input.getText().toString();
                if (!"".equals(newKey)) {
                    ((ShitSellingApplication) getApplication()).setUserAPIKey(newKey);
                    setListingsViews();
                }
            }
        });
    }

    private void setListingsViews() {
        setContentView(R.layout.activity_listings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Fragment listings = new ListingsFragment();
        Bundle args = new Bundle();
        args.putInt(ListingsFragment.ARGS_KEY_MODE, ListingsFragment.MODE_LISTINGS);
        listings.setArguments(args);
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().add(R.id.fragment_container, listings, ListingsFragment.LISTINGS_TAG).commit();
        fm.executePendingTransactions();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        // Fragments will take care of inflating options, so do nothing, but
        // return true to indicate that we have a menu
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            // TODO: go to settings
        } else if (id == R.id.nav_listings) {

            // get the fragment for listings if it exists
            FragmentManager fm = getSupportFragmentManager();
            ListingsFragment listings = (ListingsFragment) fm.findFragmentByTag(ListingsFragment.LISTINGS_TAG);

            // otherwise make a new one
            if (listings == null) {
                listings = new ListingsFragment();
                Bundle args = new Bundle();
                args.putInt(ListingsFragment.ARGS_KEY_MODE, ListingsFragment.MODE_LISTINGS);
                listings.setArguments(args);
            }

            // if it's not already visible, replace the current fragment with it
            if (!listings.isVisible()) {
                fm.beginTransaction().replace(R.id.fragment_container, listings, ListingsFragment.LISTINGS_TAG).commit();
            }

            fm.executePendingTransactions();

        } else if (id == R.id.nav_history) {
            FragmentManager fm = getSupportFragmentManager();
            ListingsFragment listings = (ListingsFragment) fm.findFragmentByTag(ListingsFragment.HISTORY_TAG);
            if (listings == null) {
                listings = new ListingsFragment();
                Bundle args = new Bundle();
                args.putInt(ListingsFragment.ARGS_KEY_MODE, ListingsFragment.MODE_HISTORY);
                listings.setArguments(args);
            }

            if (!listings.isVisible()) {
                fm.beginTransaction().replace(R.id.fragment_container, listings, ListingsFragment.HISTORY_TAG).commit();
            }

            fm.executePendingTransactions();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
