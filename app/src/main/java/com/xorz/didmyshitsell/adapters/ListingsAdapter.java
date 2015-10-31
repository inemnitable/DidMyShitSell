package com.xorz.didmyshitsell.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.xorz.didmyshitsell.R;
import com.xorz.didmyshitsell.objects.Transaction;
import com.xorz.didmyshitsell.utilities.GW2Items;
import com.xorz.didmyshitsell.utilities.Utilities;

public class ListingsAdapter extends ArrayAdapter<Transaction> {

    public ListingsAdapter(Context ctx) {
        super(ctx, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {

            //inflate a new view
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.listitem_transaction, parent, false);
        }

        TextView nameView = (TextView) convertView.findViewById(R.id.item_name);
        TextView priceView = (TextView) convertView.findViewById(R.id.item_price);

        Transaction item = getItem(position);

        String name = GW2Items.getItemName(item.itemId);
        if (name == null) name = String.valueOf(item.itemId);

        nameView.setText(name);

        priceView.setText(Utilities.formatGold(item.price));

        return convertView;
    }
}
