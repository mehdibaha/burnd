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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getVotesUp() {
        return votesUp;
    }

    public void setVotesUp(int votesUp) {
        this.votesUp = votesUp;
    }

    public int getVotesDown() {
        return votesDown;
    }

    public void setVotesDown(int votesDown) {
        this.votesDown = votesDown;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public CommentList getCommentList() {
        return commentList;
    }

    public void setCommentList(CommentList commentList) {
        this.commentList = commentList;
    }

    @Override
    public String toString() {
        return "FeedItem{" +
                "commentList=" + commentList +
                '}';
    }
}
