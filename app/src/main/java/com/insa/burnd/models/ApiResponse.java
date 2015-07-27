package com.insa.burnd.models;

import com.google.gson.annotations.SerializedName;

public class ApiResponse {
    @SerializedName("error")
    private boolean error;
    @SerializedName("message")
    private String message;
    @SerializedName("party_list")
    private PartyList partyList;
    @SerializedName("newsfeed")
    private Newsfeed newsfeed;
    @SerializedName("meeting")
    private MeetingResponse meetingResponse;

    public ApiResponse() {}

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public PartyList getPartyList() {
        return partyList;
    }

    public void setPartyList(PartyList partyList) {
        this.partyList = partyList;
    }

    public Newsfeed getNewsfeed() {
        return newsfeed;
    }

    public void setNewsfeed(Newsfeed newsfeed) {
        this.newsfeed = newsfeed;
    }

    public MeetingResponse getMeetingResponse() {
        return meetingResponse;
    }

    public void setMeetingResponse(MeetingResponse meetingResponse) {
        this.meetingResponse = meetingResponse;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "error=" + error +
                ", message='" + message + '\'' +
                ", partyList=" + partyList +
                ", newsfeed=" + newsfeed +
                ", meetingResponse=" + meetingResponse +
                '}';
    }
}
