package com.xorz.didmyshitsell;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
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

    private final static String BUY_LISTINGS_ENDPOINT = "https://api.guildwars2.com/v2/commerce/transactions/current/buys";
    private final static String SELL_LISTINGS_ENDPOINT = "https://api.guildwars2.com/v2/commerce/transactions/current/sells";
    private final static String WALLET_ENDPOINT = "https://api.guildwars2.com/v2/account/wallet";
    private final static String AUTH_HEADER_KEY = "Authorization";

    private boolean isQueryingAPI = false;
    private ListView buyList;
    private ListView sellList;
    private View wallet;
    private ListingsAdapter buyListAdapter;
    private ListingsAdapter sellListAdapter;

    public static SparseArray<String> itemNames = new SparseArray<>();

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
        buyList = (ListView) findViewById(R.id.buy_listings);
        sellList = (ListView) findViewById(R.id.sell_listings);
        wallet = findViewById(R.id.wallet);

        buyListAdapter = new ListingsAdapter(this, new ArrayList<Transaction>());

        sellListAdapter = new ListingsAdapter(this, new ArrayList<Transaction>());
        sellList.setAdapter(sellListAdapter);

        //Populate the transaction listings, ie. do the actual app stuff
        requestListingsInfo();
    }

    private void requestListingsInfo() {
        if (isQueryingAPI) return;
        isQueryingAPI = true;

        String authHeader = "Bearer " + ((ShitSellingApplication) getApplication()).getUserAPIKey();

        // build the buy menu_listings request
        final Request buyRequest = new Request.Builder()
                .url(BUY_LISTINGS_ENDPOINT)
                .header(AUTH_HEADER_KEY, authHeader)
                .build();

        // build the sell menu_listings request
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
                OkHttpClient client = ShitSellingApplication.getHttpClient();
                List<Integer> namesToQuery = new ArrayList<>();

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

                // execute the request for buy menu_listings
                try {
                    // hit the api server
                    final Response buyResponse = client.newCall(buyRequest).execute();

                    String json = buyResponse.body().string();
                    final List<Transaction> list = Utilities.parseTransactions(json);

                    for (Transaction t : list) {
                        namesToQuery.add(t.itemId);
                    }

                    // post back to UI thread to deal with the data
                    buyList.post(new Runnable() {
                        @Override
                        public void run() {
                            updateBuyList(list);
                        }
                    });
                } catch (IOException e) {
                    Log.e(TAG, "IOException requesting buy listings", e);
                } catch (JSONException e) {
                    Log.e(TAG, "Exception parsing response JSON", e);
                } catch (ParseException e) {
                    Log.e(TAG, "Exception parsing date formats", e);
                }

                // sells
                try {
                    // Execute the api call
                    final Response sellResponse = client.newCall(sellRequest).execute();

                    // Get JSON from the response and parse it into POJOs
                    String json = sellResponse.body().string();
                    final List<Transaction> list = Utilities.parseTransactions(json);

                    // Add all the item ids in the transactions to the list of item names to get
                    for (Transaction t : list) {
                        namesToQuery.add(t.itemId);
                    }

                    // Add the transactions to the views
                    sellList.post(new Runnable() {
                        @Override
                        public void run() {
                            updateSellList(list);
                        }
                    });
                } catch (IOException e) {
                    Log.e(TAG, "IOException requesting buy listings", e);
                } catch (JSONException e) {
                    Log.e(TAG, "Exception parsing response JSON", e);
                } catch (ParseException e) {
                    Log.e(TAG, "Exception parsing date format", e);
                }

                isQueryingAPI = false;

                GW2Items.retrieveItemNames(namesToQuery, ListingsActivity.this, new Runnable() {
                    @Override
                    public void run() {
                        sellListAdapter.notifyDataSetChanged();
                    }
                });

            }
        });

        httpThread.start();
    }

    private void updateBuyList (List<Transaction> list) {
        buyListAdapter.clear();
        buyListAdapter.addAll(list);
    }

    private void updateSellList(List<Transaction> list) {
        // Reset and add all the transactions to the adapter
        sellListAdapter.clear();
        sellListAdapter.addAll(list);
    }

    private void updateWallet(Response resp){
        String json;
        try {
            json = resp.body().string();
        } catch (IOException e) {
            Log.e(TAG, "IOException getting response body I don't even know how this happens?");
            return;
        }

        Wallet w;
        try {
            w = Utilities.parseWallet(json);
        } catch (JSONException e) {
            Log.e(TAG, "Exception while parsing response JSON", e);
            return;
        }

        ((TextView) wallet).setText(Utilities.formatGold(w.gold));
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
        getMenuInflater().inflate(R.menu.menu_listings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            requestListingsInfo();
        }

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
