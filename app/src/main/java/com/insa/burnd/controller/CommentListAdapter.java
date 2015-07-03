package com.insa.burnd.controller;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.insa.burnd.R;
import com.insa.burnd.models.Comment;
import com.insa.burnd.models.CommentList;
import com.insa.burnd.network.VolleySingleton;

/* Class to bind the commentList to its listView */
public class CommentListAdapter extends ArrayAdapter<Comment> {
    private ImageLoader imageLoader = VolleySingleton.getInstance().getImageLoader();

    private static class ViewHolder {
        protected TextView username;
        protected TextView statusMsg;
        protected NetworkImageView profilePic;
    }

    public CommentListAdapter(Context context, CommentList commentList) {
        super(context, R.layout.feed_item, commentList);
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        // Get the data item for this position
        Comment comment = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view

        ViewHolder holder;

        if (v == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            v = inflater.inflate(R.layout.comment_item, parent, false);

            holder.username = (TextView) v.findViewById(R.id.comment_username);
            holder.statusMsg = (TextView) v.findViewById(R.id.comment_status);
            holder.profilePic = (NetworkImageView) v.findViewById(R.id.comment_profilePic);

            v.setTag(holder);

        } else {
            holder = (ViewHolder) v.getTag();
        }


        // Sets username in textview
        if(!TextUtils.isEmpty(comment.getUser().getName())) {
            holder.username.setText(comment.getUser().getName());
        }

        // Checking for empty status message
        if (!TextUtils.isEmpty(comment.getStatus())) {
            holder.statusMsg.setText(comment.getStatus());
            holder.statusMsg.setVisibility(View.VISIBLE);
        } else {
            // status is empty, remove from view
            holder.statusMsg.setVisibility(View.GONE);
        }

        // user profile pic
        if(!TextUtils.isEmpty(comment.getUser().getProfilePic())) {
            holder.profilePic.setImageUrl(comment.getUser().getProfilePic(), imageLoader);
        }

        return v;
    }
}