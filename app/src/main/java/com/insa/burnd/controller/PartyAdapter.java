package com.insa.burnd.controller;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.insa.burnd.R;

import java.util.ArrayList;

public class PartyAdapter extends BaseAdapter {
    private Context ctx;

    private ArrayList<String> partiesList;
    private ArrayList<String> partiesLocation;

    public PartyAdapter(Context ctx, ArrayList<String> partiesList, ArrayList<String> partiesLocation) {
        super();
        this.ctx = ctx;
        this.partiesList = partiesList;
        this.partiesLocation = partiesLocation;
    }

    private static class ViewHolder {
        protected TextView partyName;
        protected TextView partyLocation;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder holder;

        if (v == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(ctx);
            v = inflater.inflate(R.layout.party_item, parent, false);

            holder.partyName = (TextView) v.findViewById(R.id.firstLine);
            holder.partyLocation = (TextView) v.findViewById(R.id.secondLine);

            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        // Sets username in textview
        if(!TextUtils.isEmpty(partiesList.get(position))) {
            holder.partyName.setText(partiesList.get(position));
        }

        // Checking for empty status message
        if(!TextUtils.isEmpty(partiesLocation.get(position))) {
            holder.partyLocation.setText(partiesLocation.get(position));
        }

        return v;
    }

    public int getCount() {
        return partiesList.size();
    }

    public Object getItem(int arg0) {
        return null;
    }

    public long getItemId(int position) {
        return position;
    }

    public ArrayList<String> getPartiesList() {
        return partiesList;
    }

}