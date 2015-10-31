package com.xorz.didmyshitsell.objects;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Transaction {
    // The canonical form of a transaction is the JSON
    //
    // [{'id':1,'item_id':1,'price':1,'quantity':1,'created':'2015-05-09T17:13:26+00:00',
    //      'purchased':'2015-05-09T17:24:20+00:00'},
    //  {...},...]

    private final static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

    public final long transactionId;
    public final int itemId;
    public final long price;
    public final long quantity;
    public final Date created;
    public final Date purchased;  // might be null!

    public Transaction(JSONObject data) throws JSONException, ParseException {
        transactionId = data.getLong("id");
        itemId = data.getInt("item_id");
        price = data.getLong("price");
        quantity = data.getLong("quantity");

        //parse date formats
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        created = sdf.parse(data.getString("created"));
        if (data.has("purchased")) {
            purchased = sdf.parse(data.getString("purchased"));
        } else {
            purchased = null;
        }
    }


}
