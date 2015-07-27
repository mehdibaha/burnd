package com.insa.burnd.models;

import com.google.gson.annotations.SerializedName;

/* Class defining a feed item from a newsfeed */
public class FeedItem {
    @SerializedName("post_id")
    private int id;
    @SerializedName("timestamp")
    private String timestamp;
    @SerializedName("votes_up")
    private int votesUp;
    @SerializedName("votes_down")
    private int votesDown;
    @SerializedName("status")
    private String status;
    @SerializedName("image")
    private String image;
    @SerializedName("video")
    private String video;
    @SerializedName("post_user")
    private User user;
    @SerializedName("comments")
    private CommentList commentList;

    public FeedItem() {
    }

    @Override
    public String toString() {
        return "FeedItem{" +
                "commentList=" + commentList +
                '}';
    }

    public int getId() {
        return this.id;
    }

    public String getStatus() {
        return status;
    }

    public User getUser() {
        return user;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getImage() {
        return image;
    }

    public String getVideo() {
        return video;
    }

    public int getVotesUp() {
        return votesUp;
    }

    public int getVotesDown() {
        return votesDown;
    }

    public CommentList getCommentList() {
        return commentList;
    }
}
