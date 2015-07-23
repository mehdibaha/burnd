package com.insa.burnd.models;

import com.google.gson.annotations.SerializedName;

public class Party {
    @SerializedName("party_name")
    private String name;
    @SerializedName("party_adress")
    private String adress;

    public Party() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAdress() {
        return adress;
    }

    public void setAdress(String adress) {
        this.adress = adress;
    }

    @Override
    public String toString() {
        return "Party{" +
                "name='" + name + '\'' +
                ", adress='" + adress + '\'' +
                '}';
    }
}
