package com.insa.burnd.models;

/* Class defining a feed item from a newsfeed */
public class FeedItem {

    private int id;
    private String status;
    private String image;
    private String video="";
    private User user;
    private String timeStamp;
    private String votesUp;
    private String votesDown;

    private CommentList commentList;

    public FeedItem() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImge() {
        return image;
    }

    public void setImge(String image) {
        this.image = image;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVotesDown() {
        return votesDown;
    }

    public void setVotesDown(String votesDown) {
        this.votesDown = votesDown;
    }

    public String getVotesUp() {
        return votesUp;
    }

    public void setVotesUp(String votesUp) {
        this.votesUp = votesUp;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public CommentList getCommentList() {
        return commentList;
    }

    public void setCommentList(CommentList commentList) {
        this.commentList = commentList;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
