package com.insa.burnd.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/* Class defining a commentList, with a constructor from json (server format) */
public class CommentList extends ArrayList<Comment> {

    public CommentList(JSONArray jsonArray) throws JSONException {

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject feedObj = (JSONObject) jsonArray.get(i);
            User user = new User(feedObj.getString("comment_uid"), feedObj.getString("comment_username"), "small");

            Comment comment = new Comment();
            comment.setId(feedObj.getInt("comment_id"));
            comment.setStatus(feedObj.getString("comment_status"));
            comment.setUser(user);

            this.add(comment);
        }
    }
}
