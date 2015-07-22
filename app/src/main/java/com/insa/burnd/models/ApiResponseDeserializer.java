package com.insa.burnd.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class ApiResponseDeserializer implements JsonDeserializer<ApiResponse> {

    @Override
    public ApiResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObj = json.getAsJsonObject();
        ApiResponse ar = new ApiResponse();

        if (jsonObj.get("message").getAsString() != null)
            ar.setMessage(jsonObj.get("message").getAsString());

        if (jsonObj.get("error").getAsBoolean())
            ar.setError(jsonObj.get("error").getAsBoolean());

        if(jsonObj.getAsJsonArray("newsfeed") != null)
            ar.setNewsfeed(deserializeNewsfeed(json.getAsJsonObject().getAsJsonArray("newsfeed")));

        if(jsonObj.getAsJsonArray("partylist") != null)
            ar.setPartyList(deserializePartyList(jsonObj.getAsJsonArray("partylist")));
        
        if(jsonObj.getAsJsonArray("meeting") != null)
            ar.setMeetingResponse(deserializeMeetingResponse(jsonObj.getAsJsonArray("meeting")));

        return ar;
    }

    private MeetingResponse deserializeMeetingResponse(JsonArray jsonArray) {
        MeetingResponse mr = new MeetingResponse();
        JsonObject feedObj;

        for (int i = 0; i < jsonArray.size(); i++) {
            feedObj = (JsonObject) jsonArray.get(i);

            mr.setId(feedObj.get("iduser2").getAsString());
            mr.setMatch(feedObj.get("match").getAsString());
            mr.setAge(feedObj.get("age_user2").getAsInt());
            mr.setName(feedObj.get("name").getAsString());
            mr.setStop(feedObj.get("stop").getAsBoolean());
        }

        return mr;
    }

    private Newsfeed deserializeNewsfeed(JsonArray jsonArray) {
        Newsfeed newsfeed = new Newsfeed(jsonArray.size());
        User user;
        FeedItem item;
        JsonObject feedObj;

        for (int i = 0; i < jsonArray.size(); i++) {
            feedObj = (JsonObject) jsonArray.get(i);
            item = new FeedItem();
            user = new User.UserBuilder().setUserId(feedObj.get("user_id").getAsString())
                    .setName(feedObj.get("name").getAsString())
                    .build();

            item.setUser(user);
            item.setId(feedObj.get("post_id").getAsInt());
            item.setImage(feedObj.get("image").getAsString());
            item.setStatus(feedObj.get("status").getAsString());
            item.setTimestamp(feedObj.get("timestamp").getAsLong());
            item.setVotesUp(feedObj.get("vote_up").getAsInt());
            item.setVotesDown(feedObj.get("vote_down").getAsInt());
            item.setVideo(feedObj.get("video").getAsString());
            item.setCommentList(CommentList.fromJson(feedObj.getAsJsonArray("comments")));

            newsfeed.add(item);
        }

        return newsfeed;
    }

    private PartyList deserializePartyList(JsonArray jsonArray) {
        PartyList partyList = new PartyList(jsonArray.size());
        Party party;
        JsonObject feedObj;

        for (int i = 0; i < jsonArray.size(); i++) {
            feedObj = (JsonObject) jsonArray.get(i);
            party = new Party();

            party.setName(feedObj.get("party").getAsString());
            party.setAdress(feedObj.get("location").getAsString());
            partyList.add(party);
        }

        return partyList;
    }
}
