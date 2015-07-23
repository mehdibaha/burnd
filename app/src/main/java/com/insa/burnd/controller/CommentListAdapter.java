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

import butterknife.Bind;
import butterknife.ButterKnife;

/* Class to bind the commentList to its listView */
public class CommentListAdapter extends ArrayAdapter<Comment> {
    private ImageLoader imageLoader = VolleySingleton.getInstance().getImageLoader();
    private Context ctx;

    public static class ViewHolder {
        @Bind(R.id.comment_username) TextView username;
        @Bind(R.id.comment_status) TextView statusMsg;
        @Bind(R.id.comment_profilePic) NetworkImageView profilePic;

        public ViewHolder(View v) {
            ButterKnife.bind(this, v);
        }
    }

    public CommentListAdapter(Context ctx, CommentList commentList) {
        super(ctx, R.layout.feed_item, commentList);
        this.ctx = ctx;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        // Get the data item for this position
        Comment comment = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder holder;
        if (v == null) {
            v = LayoutInflater.from(ctx).inflate(R.layout.comment_item, parent, false);
            holder = new ViewHolder(v);
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