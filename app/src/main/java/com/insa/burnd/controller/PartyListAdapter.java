package com.insa.burnd.controller;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.insa.burnd.R;
import com.insa.burnd.models.Party;
import com.insa.burnd.models.PartyList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PartyListAdapter extends ArrayAdapter<Party> {
    private Context ctx;

    public PartyListAdapter(Context ctx, PartyList partyList) {
        super(ctx, R.layout.comment_item, partyList);
        this.ctx = ctx;
    }

    public static class ViewHolder {
        @Bind(R.id.firstLine) TextView partyName;
        @Bind(R.id.secondLine) TextView partyLocation;

        public ViewHolder(View v) {
            ButterKnife.bind(this, v);
        }
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        Party party = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder holder;
        if (v == null) {
            v = LayoutInflater.from(ctx).inflate(R.layout.party_item, parent, false);
            holder = new ViewHolder(v);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        String partyName = party.getName();
        String partyPass = party.getAdress();
        if(!TextUtils.isEmpty(partyName) && !TextUtils.isEmpty(partyPass)) {
            holder.partyName.setText(partyName);
            holder.partyLocation.setText(partyPass);
        }

        return v;
    }
}