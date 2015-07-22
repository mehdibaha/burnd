package com.insa.burnd.models;

public class MeetingResponse {
    private String id;
    private String match;
    private int age;
    private String name;
    private boolean stop;

    public MeetingResponse() {
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMatch() {
        return match;
    }

    public void setMatch(String match) {
        this.match = match;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

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
}
