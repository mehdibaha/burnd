package com.insa.burnd.models;

import com.google.gson.annotations.SerializedName;

/* Class defining a comment from a feed item*/
public class Comment {
    @SerializedName("comment_id")
    private int id;
    @SerializedName("comment_status")
    private String status;
    @SerializedName("comment_user")
    private User user;

    public Comment() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "user=" + user +
                ", status='" + status + '\'' +
                '}';
    }
}