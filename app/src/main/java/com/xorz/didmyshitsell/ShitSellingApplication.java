package com.xorz.didmyshitsell;

import android.app.Application;
import android.content.SharedPreferences;

import com.squareup.okhttp.OkHttpClient;

/**
 * Created by Adam on 10/30/2015.
 */
public class ShitSellingApplication extends Application {

    private String userAPIKey = "";
    private OkHttpClient httpClient;

    private final static String PREFERENCE_KEY = "com.xorz.didmyshitsell";

    private final static String API_KEY_PREFERENCE = "api_key";

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences prefs = getSharedPreferences(PREFERENCE_KEY, MODE_PRIVATE);
        userAPIKey = prefs.getString(API_KEY_PREFERENCE, "");
    }

    public void setUserAPIKey(String key) {
        if (key != null) {
            // set the key in memory
            userAPIKey = key;

            // and in persistent storage
            SharedPreferences.Editor prefs = getSharedPreferences(PREFERENCE_KEY, MODE_PRIVATE).edit();
            prefs.putString(API_KEY_PREFERENCE, key);
            prefs.commit();
        }
    }

    public String getUserAPIKey() {
        return userAPIKey;
    }

    public OkHttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = new OkHttpClient();
        }

        return httpClient;
    }

    // Not for use from user-interfacing code
    private void clearAPIKey() {
        getSharedPreferences(PREFERENCE_KEY, MODE_PRIVATE).edit().putString(API_KEY_PREFERENCE, "");
    }
}
