package com.insa.burnd.models;

import android.content.Context;

import com.facebook.model.GraphUser;
import com.insa.burnd.utils.SPManager;
import com.insa.burnd.utils.Utils;

/* Class defining a user */
public class User {

    private String userId;
    private String name;
    private String profilePic;
    private String gender;
    private String accessToken;

    public User(GraphUser user, String accessToken) {
        this.userId = user.getId();
        this.name = user.getName();
        this.profilePic = "https://graph.facebook.com/" + user.getId() + "/picture?type=small";
        this.gender = user.asMap().get("gender").toString();
        this.accessToken = accessToken;
    }

    public User(String userId, String name, String profilePicType) {
        this.userId = userId;
        this.name = name;
        this.profilePic = "https://graph.facebook.com/" + userId + "/picture?type=" + profilePicType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", profilePic='" + profilePic + '\'' +
                ", gender='" + gender + '\'' +
                ", accessToken='" + Utils.makeReadableString(accessToken, 10) + '\'' +
                '}'; // Only giving preview of accesstoken
    }

    public void saveToMemory(Context ctx) {
        SPManager.save(ctx, getUserId(), "USER_ID"); // Saves userId to local memory
        SPManager.save(ctx, getName(), "NAME");
        SPManager.save(ctx, getGender(), "GENDER");
        SPManager.save(ctx, getAccessToken(), "ACCESS_TOKEN");
    }
}
