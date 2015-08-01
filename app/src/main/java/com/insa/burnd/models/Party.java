package com.insa.burnd.models;

import com.google.gson.annotations.SerializedName;

public class Party {
    @SerializedName("party_name")
    private String name;
    @SerializedName("party_adress")
    private String adress;

    public Party() {}

    @Override
    public String toString() {
        return "Party{" +
                "name='" + name + '\'' +
                ", adress='" + adress + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public String getAdress() {
        return adress;
    }
}
