package com.insa.burnd.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

/* Class defining a commentList, with a constructor from json (server format) */
public class CommentList extends ArrayList<Comment> {

    public CommentList() {
        super();
    }

    public CommentList(int size) {
        super(size);
    }

    public static CommentList fromJson(JsonArray jsonArray) {
        CommentList commentList = new CommentList(jsonArray.size());
        Comment comment;
        User user;

        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject feedObj = (JsonObject) jsonArray.get(i);
            comment = new Comment();
            user = new User.UserBuilder()
                    .setUserId(feedObj.get("comment_uid").getAsString())
                    .setName(feedObj.get("comment_username").getAsString())
                    .build();

            comment.setUser(user);
            comment.setId(feedObj.get("comment_id").getAsInt());
            comment.setStatus(feedObj.get("comment_status").getAsString());

            commentList.add(comment);
        }

        return commentList;
    }
}
