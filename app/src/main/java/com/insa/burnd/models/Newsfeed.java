package com.insa.burnd.models;

import android.text.TextUtils;

import com.insa.burnd.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/* Class defining a newsfeed, a collection of feed items */
public class Newsfeed extends ArrayList<FeedItem> {

    private JSONArray feedArray = new JSONArray();

    public Newsfeed() {
    }

    public Newsfeed(JSONArray feedArray) throws JSONException {
        /*  if not null create the newsfeed */
        if(feedArray.length()!=0) {
            this.feedArray = feedArray;

            for (int i = 0; i < feedArray.length(); i++) {
                JSONObject feedObj = (JSONObject) feedArray.get(i);

                FeedItem item = new FeedItem();
                User user = new User(feedObj.getString("user_id"), feedObj.getString("name"), "small");

                item.setId(feedObj.getInt("post_id"));
                item.setUser(user);
                String image = feedObj.isNull("image") ? null : feedObj // Image might be null sometimes
                        .getString("image");
                item.setImge(image);
                item.setStatus(feedObj.getString("status"));
                item.setTimeStamp(feedObj.getString("timestamp"));
                item.setVotesUp(feedObj.getString("vote_up"));
                item.setVotesDown(feedObj.getString("vote_down"));
                String video = feedObj.isNull("video") ? null : feedObj // Image might be null sometimes
                        .getString("video");
                item.setVideo(video);
                item.setCommentList(new CommentList(feedObj.getJSONArray("comments")));

                this.add(item);
            }
        }
    }

    /* Checks if newsfeed is empty */
    public boolean isNull() {
        return feedArray.length() == 0;
    }

    /* Updates the newsfeed from a json */
    public void update(JSONArray feedArrayFromUpdate) throws JSONException {
        /* Calculating the index at which the arrays should be merged */
        int lastIdFromUpdate = ((JSONObject) feedArrayFromUpdate.get(feedArrayFromUpdate.length()-1)).getInt("post_id");
        int firstId = this.size()==0 ? lastIdFromUpdate : this.get(0).getId();
        int mergeIndex;

        if(firstId == lastIdFromUpdate)
            mergeIndex = 1;
        else
            mergeIndex = feedArrayFromUpdate.length();

        feedArray = Utils.concatArray(feedArrayFromUpdate, feedArray, mergeIndex);

        this.clear();
        this.addAll(new Newsfeed(feedArray));
    }

    public String toJson() {
        return feedArray.toString();
    }

    public static JSONArray jsonToFeedArray(String json) throws JSONException {
        if(!TextUtils.isEmpty(json)) {
            if(json.startsWith("[")) { // json from cache
                return new JSONArray(json);
            }
            else { // json from new connexion
                JSONObject jsonObject = new JSONObject(json);
                return jsonObject.getJSONArray("newsfeed");
            }
        }
        else {
            return new JSONArray();
        }
    }

}
