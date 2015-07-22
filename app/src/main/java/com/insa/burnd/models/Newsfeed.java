package com.insa.burnd.models;

import android.content.Context;

import com.insa.burnd.utils.SPManager;

import java.util.ArrayList;

import trikita.log.Log;

/* Class defining a newsfeed, a collection of feed items */
public class Newsfeed extends ArrayList<FeedItem> {

    public Newsfeed() {
        super();
    }

    public Newsfeed(int size) {
        super(size);
    }

    /* Concatenates newsfeeds at a specific index */
    public void concatNewsfeeds(Newsfeed nf, int mergeIndex) {
        int size = nf.size() + this.size() - mergeIndex;
        Newsfeed newsfeed = new Newsfeed(size);

        for (int i = 0; i < nf.size(); i++)
            newsfeed.add(nf.get(i));
        for (int i = mergeIndex; i < this.size(); i++)
            newsfeed.add(this.get(i));

        this.clear();
        this.addAll(newsfeed);
    }

    public void saveLastPostId(Context ctx) {
        String lastPostId = String.valueOf(this.get(0).getId());
        SPManager.save(ctx, lastPostId, "LAST_POST_ID"); // Saves last post id
        Log.v("saving last post id " + lastPostId);
    }

    // Updates the newsfeed from a newsfeed from server
    public void update(Newsfeed nf) {
        if (this.isEmpty()) {
            this.addAll(nf);
        } else {
            int lastIdFromNewsfeed = nf.get(nf.size()-1).getId();

            if (lastIdFromNewsfeed == this.get(0).getId())
                concatNewsfeeds(nf, 1);
            else
                concatNewsfeeds(nf, nf.size());
        }
    }

}
