package com.insa.burnd.models;

public class ApiResponse {
    private boolean error;
    private String message;
    private PartyList partyList;
    private Newsfeed newsfeed;
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
