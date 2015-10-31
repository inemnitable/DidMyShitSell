package com.xorz.didmyshitsell.utilities;

import android.app.Activity;
import android.util.Log;

import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.xorz.didmyshitsell.ShitSellingApplication;

import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GW2Items {
    private final static String TAG = "GW2Items";
    private final static String ITEMS_ENDPOINT = "https://api.guildwars2.com/v2/items";

    private static Map<Integer, String> itemNames = new HashMap<>();
    private static Set<Integer> namesToGet = new HashSet<>();

    private static boolean querying = false;

    public static String getItemName(int itemId) {
        String name = itemNames.get(itemId);
        if (name == null) {
            namesToGet.add(itemId);
        }
        return name;
    }

    // Retrieves the item names for the specified list of ids asynchronously. After names have
    // been downloaded, the provided callback will be run on the UI thread of the provided activity
    public static void retrieveItemNames(Iterable<Integer> itemIds, final Activity ctx, final Runnable callback) {
        for (int id : itemIds) {
            if (itemNames.get(id) == null) {
                namesToGet.add(id);
            }
        }

        if (querying) return;

        querying = true;
        // TODO: get item names from server
        String query = Utilities.join(namesToGet, ",");
        final Request req = new Request.Builder()
                .url(HttpUrl.parse(ITEMS_ENDPOINT)
                        .newBuilder()
                        .addQueryParameter("ids", query)
                        .build())
                .build();

        Thread httpThread = new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = ShitSellingApplication.getHttpClient();
                Map<Integer, String> names;
                try {
                    Response resp = client.newCall(req).execute();
                    String json = resp.body().string();
                    names = Utilities.parseItems(json);
                } catch (IOException e) {
                    Log.e(TAG, "IOException getting item info", e);
                    return;
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing JSON for items", e);
                    return;
                }

                itemNames.putAll(names);

                if (ctx != null && !ctx.isFinishing() && callback != null) {
                    ctx.runOnUiThread(callback);
                }

                querying = false;
            }
        });

        httpThread.start();
    }

}
