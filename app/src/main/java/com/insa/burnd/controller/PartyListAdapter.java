package com.insa.burnd.controller;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.insa.burnd.R;
import com.insa.burnd.models.PartyList;

public class PartyListAdapter extends BaseAdapter {
    private Context ctx;
    private PartyList partyList;

    public PartyListAdapter(Context ctx, PartyList partyList) {
        super();
        this.ctx = ctx;
        this.partyList = partyList;
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

        String partyName = partyList.get(position).getName();
        String partyPass = partyList.get(position).getAdress();
        if(!TextUtils.isEmpty(partyName) && !TextUtils.isEmpty(partyPass)) {
            holder.partyName.setText(partyName);
            holder.partyLocation.setText(partyPass);
        }

        return v;
    }

    public int getCount() {
        return partyList.size();
    }

    public Object getItem(int arg0) {
        return null;
    }

    public long getItemId(int position) {
        return position;
    }

    public PartyList getPartyList() {
        return partyList;
    }
}