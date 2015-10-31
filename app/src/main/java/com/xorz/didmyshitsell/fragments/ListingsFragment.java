package com.xorz.didmyshitsell.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.xorz.didmyshitsell.R;
import com.xorz.didmyshitsell.ShitSellingApplication;
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

public class ListingsFragment extends Fragment {
    private final static String TAG = "ListingsFragment";
    private final static String API = "https://api.guildwars2.com/v2/commerce/transactions";
    private final static String API_LISTINGS = "/current";
    private final static String API_HISTORY = "/history";
    private final static String API_BUYS = "/buys";
    private final static String API_SELLS = "/sells";
    private final static String BUY_LISTINGS_ENDPOINT = API + API_LISTINGS + API_BUYS;
    private final static String SELL_LISTINGS_ENDPOINT = API + API_LISTINGS + API_SELLS;
    private final static String BUY_HISTORY_ENDPOINT = API + API_HISTORY + API_BUYS;
    private final static String SELL_HISTORY_ENDPOINT = API + API_HISTORY + API_SELLS;
    private final static String WALLET_ENDPOINT = "https://api.guildwars2.com/v2/account/wallet";
    private final static String AUTH_HEADER_KEY = "Authorization";

    public final static String LISTINGS_TAG = "listings";
    public final static String HISTORY_TAG = "history";

    public final static String ARGS_KEY_MODE = "mode";

    public final static int MODE_LISTINGS = 1;
    public final static int MODE_HISTORY = 2;

    private boolean isQueryingAPI = false;
    private ListView buyList;
    private ListView sellList;
    private View wallet;
    private ListingsAdapter buyListAdapter;
    private ListingsAdapter sellListAdapter;

    private int mode;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mode = getArguments().getInt(ARGS_KEY_MODE);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_listings, container, false);

        buyList = (ListView) root.findViewById(R.id.buy_listings);
        sellList = (ListView) root.findViewById(R.id.sell_listings);
        wallet = root.findViewById(R.id.wallet);

        buyListAdapter = new ListingsAdapter(getContext());
        buyList.setAdapter(buyListAdapter);

        sellListAdapter = new ListingsAdapter(getContext());
        sellList.setAdapter(sellListAdapter);

        TextView title = (TextView) root.findViewById(R.id.listings_title);
        if (mode == MODE_LISTINGS) {
            title.setText("Current Transactions");
        } else if (mode == MODE_HISTORY) {
            title.setText("Completed Transactions");
        }

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        requestListingsInfo();
    }

    private void requestListingsInfo() {
        if (isQueryingAPI) return;
        isQueryingAPI = true;

        String authHeader = "Bearer " + ((ShitSellingApplication) getActivity().getApplication()).getUserAPIKey();

        String buyUrl;
        String sellUrl;

        switch (mode) {
            case MODE_HISTORY:
                buyUrl = BUY_HISTORY_ENDPOINT;
                sellUrl = SELL_HISTORY_ENDPOINT;
                break;

            case MODE_LISTINGS:
            default:
                buyUrl = BUY_LISTINGS_ENDPOINT;
                sellUrl = SELL_LISTINGS_ENDPOINT;
                break;
        }

        // build the buy menu_listings request
        final Request buyRequest = new Request.Builder()
                .url(buyUrl)
                .header(AUTH_HEADER_KEY, authHeader)
                .build();

        // build the sell menu_listings request
        final Request sellRequest = new Request.Builder()
                .url(sellUrl)
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

                GW2Items.retrieveItemNames(namesToQuery, getActivity(), new Runnable() {
                    @Override
                    public void run() {
                        sellListAdapter.notifyDataSetChanged();
                    }
                });

            }
        });

        httpThread.start();
    }

    private void updateBuyList(List<Transaction> list) {
        buyListAdapter.clear();
        buyListAdapter.addAll(list);
    }

    private void updateSellList(List<Transaction> list) {
        // Reset and add all the transactions to the adapter
        sellListAdapter.clear();
        sellListAdapter.addAll(list);
    }

    private void updateWallet(Response resp) {
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

        ((TextView) wallet).setText(String.format(Locale.US, "Total Funds: %s", Utilities.formatGold(w.gold)));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_listings, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            requestListingsInfo();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
