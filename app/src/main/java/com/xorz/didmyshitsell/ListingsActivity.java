package com.xorz.didmyshitsell;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
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
import android.widget.TextView;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOError;
import java.io.IOException;

public class ListingsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final static String TAG = "ListingsActivity";

    private final static String BUY_LISTINGS_ENDPOINT = "https://api.guildwars2.com/v2/commerce/transactions/current/buys";
    private final static String SELL_LISTINGS_ENDPOINT = "https://api.guildwars2.com/v2/commerce/transactions/current/sells";
    private final static String WALLET_ENDPOINT = "https://api.guildwars2.com/v2/account/wallet";
    private final static String AUTH_HEADER_KEY = "Authorization";

    private boolean isQueryingAPI = false;
    private View buyList;
    private View sellList;
    private View wallet;

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

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Remember the views
        buyList = findViewById(R.id.buy_listings);
        sellList = findViewById(R.id.sell_listings);
        wallet = findViewById(R.id.wallet);

        //Populate the listings, ie. do the actual app stuff
        requestListingsInfo();
    }

    private void requestListingsInfo() {
        if (isQueryingAPI) return;
        isQueryingAPI = true;

        String authHeader = "Bearer " + ((ShitSellingApplication) getApplication()).getUserAPIKey();

        // build the buy listings request
        final Request buyRequest = new Request.Builder()
                .url(BUY_LISTINGS_ENDPOINT)
                .header(AUTH_HEADER_KEY, authHeader)
                .build();

        // build the sell listings request
        final Request sellRequest = new Request.Builder()
                .url(SELL_LISTINGS_ENDPOINT)
                .header(AUTH_HEADER_KEY, authHeader)
                .build();

        // build the wallet update request
        final Request walletRequest = new Request.Builder()
                .url(WALLET_ENDPOINT)
                .header(AUTH_HEADER_KEY, authHeader)
                .build();

        // build the http thread runnable
        Thread httpThread = new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = ((ShitSellingApplication) getApplication()).getHttpClient();

                // execute the request for buy listings
                try {
                    // hit the api server
                    final Response buyResponse = client.newCall(buyRequest).execute();

                    // post back to UI thread to deal with the data
                    buyList.post(new Runnable() {
                        @Override
                        public void run() {
                            updateBuyList(buyResponse);
                        }
                    });
                } catch (IOException e) {
                    Log.e(TAG, "IOException requesting buy listings", e);
                }

                // sells
                try {
                    final Response sellResponse = client.newCall(sellRequest).execute();
                    sellList.post(new Runnable() {
                        @Override
                        public void run() {
                            updateSellList(sellResponse);
                        }
                    });
                } catch (IOException e) {
                    Log.e(TAG, "IOException requesting buy listings", e);
                }

                // gold
                try {
                    final Response walletResponse = client.newCall(walletRequest).execute();
                    wallet.post(new Runnable() {
                        @Override
                        public void run() {
                            updateWallet(walletResponse);
                        }
                    });
                } catch (IOException e) {
                    Log.e(TAG, "IOException requesting wallet", e);
                }

            }
        });

        httpThread.start();
    }

    private void updateBuyList (Response resp) {
        try {
            ((TextView) buyList).setText(resp.body().string());
        } catch (IOException e) {
            Log.e(TAG, "IOException getting response body I don't even know how this happens?");
        }
    }

    private void updateSellList(Response resp) {
        try {
            ((TextView) sellList).setText(resp.body().string());
        } catch (IOException e) {
            Log.e(TAG, "IOException getting response body I don't even know how this happens?");
        }
    }

    private void updateWallet(Response resp) {
        try {
            ((TextView) wallet).setText(resp.body().string());
        } catch (IOException e) {
            Log.e(TAG, "IOException getting response body I don't even know how this happens?");
        }
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
        getMenuInflater().inflate(R.menu.listings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            // Go to settings
//        } else if (id == R.id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
//
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
