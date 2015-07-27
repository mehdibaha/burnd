package com.insa.burnd.models;

import android.content.Context;

import com.google.gson.annotations.SerializedName;
import com.insa.burnd.utils.SPManager;

/* Class defining a user */
public class User {
    @SerializedName("user_id")
    private String userId;
    @SerializedName("user_name")
    private String name;
    @SerializedName("user_profilepic")
    private String profilePic;

    private String gender;
    private String accessToken;

    public User() { }

    private User(UserBuilder ub) {
        this.userId = ub.userId;
        this.name = ub.name;
        this.profilePic = "https://graph.facebook.com/" + this.userId + "/picture?type=small";
        this.gender = ub.gender;
        this.accessToken = ub.accessToken;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", profilePic='" + profilePic + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public String getGender() {
        return gender;
    }

    public void saveToMemory(Context ctx) {
        SPManager.save(ctx, userId, "USER_ID"); // Saves userId to local memory
        SPManager.save(ctx, name, "NAME");
        SPManager.save(ctx, gender, "GENDER");
        SPManager.save(ctx, accessToken, "ACCESS_TOKEN");
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
}
