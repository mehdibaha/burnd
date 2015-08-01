package com.insa.burnd.models;

import com.google.gson.annotations.SerializedName;

public class MeetingResponse {
    @SerializedName("iduser2")
    private String id;
    @SerializedName("match")
    private String match;
    @SerializedName("age_user2")
    private int age;
    @SerializedName("name")
    private String name;
    @SerializedName("stop")
    private boolean stop;

    public MeetingResponse() { }

    @Override
    public String toString() {
        return "MeetingResponse{" +
                "id='" + id + '\'' +
                ", match='" + match + '\'' +
                ", age=" + age +
                ", name='" + name + '\'' +
                ", stop=" + stop +
                '}';
    }

    public int getAge() {
        return age;
    }

    public String getId() {
        return id;
    }

    public String getMatch() {
        return match;
    }

    public String getName() {
        return name;
    }

    public boolean isStop() {
        return stop;
    }
}
