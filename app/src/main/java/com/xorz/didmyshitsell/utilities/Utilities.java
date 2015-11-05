package com.xorz.didmyshitsell.utilities;

import com.xorz.didmyshitsell.objects.Transaction;
import com.xorz.didmyshitsell.objects.Wallet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Utilities {

    // TODO: update if we decide to care about currencies other than gold
    public static Wallet parseWallet(String walletJson) throws JSONException {
        JSONArray data = new JSONArray(walletJson);
        Wallet wallet = new Wallet();

        for (int i = 0; i < data.length(); i++) {
            JSONObject obj = data.getJSONObject(i);
            int type = obj.getInt("id");
            if (type == 1) {
                wallet.gold = obj.getLong("value");
            }
        }

        return wallet;
    }

    public static List<Transaction> parseTransactions(String transactionJson) throws JSONException, ParseException {
        JSONArray data = new JSONArray(transactionJson);
        List<Transaction> transactions = new ArrayList<>();

        for (int i = 0; i < data.length(); i++) {
            JSONObject obj = data.getJSONObject(i);
            Transaction t = new Transaction(obj);
            transactions.add(t);
        }

        return transactions;
    }

    public static Map<Integer, String> parseItems(String json) throws JSONException {
        JSONArray data = new JSONArray(json);
        Map<Integer, String> names = new HashMap<>();

        for (int i = 0; i < data.length(); i++) {
            JSONObject obj = data.getJSONObject(i);
            int id = obj.getInt("id");
            String name = obj.getString("name");
            names.put(id, name);
        }

        return names;
    }

    public static String formatGold(long amount) {
        long gold = amount / 10000;
        long silver = (amount / 100) % 100;
        long copper = amount % 100;

        return String.format(Locale.US, "%dg%ds%dc", gold, silver, copper);
    }

    public static String join(Iterable<Integer> list, String delim) {
        StringBuilder sb = new StringBuilder();
        for (Integer i : list) {
            sb.append(i);
            sb.append(delim);
        }
        if (sb.length() == 0) return "";
        sb.replace(sb.length() - delim.length(), sb.length(), "");
        return sb.toString();
    }
}
