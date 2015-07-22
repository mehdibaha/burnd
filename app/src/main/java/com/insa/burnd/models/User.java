package com.insa.burnd.models;

import android.content.Context;

import com.insa.burnd.utils.SPManager;

/* Class defining a user */
public class User {
    private String userId;
    private String name;

    private String profilePic;
    private String gender;
    private String accessToken;

    private User(UserBuilder ub) {
        this.userId = ub.userId;
        this.name = ub.name;
        this.profilePic = "https://graph.facebook.com/" + this.userId + "/picture?type=small";
        this.gender = ub.gender;
        this.accessToken = ub.accessToken;
    }

    public static class UserBuilder {
        private String userId;
        private String name;
        private String gender;
        private String accessToken;

        public UserBuilder setUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public UserBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public UserBuilder setGender(String gender) {
            this.gender = gender;
            return this;
        }

        public UserBuilder setAccessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }

    public void saveToMemory(Context ctx) {
        SPManager.save(ctx, userId, "USER_ID"); // Saves userId to local memory
        SPManager.save(ctx, name, "NAME");
        SPManager.save(ctx, gender, "GENDER");
        SPManager.save(ctx, accessToken, "ACCESS_TOKEN");
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
                " name='" + name + '\'' +
                '}';
    }
}
