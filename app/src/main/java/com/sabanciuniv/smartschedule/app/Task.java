package com.sabanciuniv.smartschedule.app;

import com.google.firebase.database.IgnoreExtraProperties;
import com.yandex.mapkit.geometry.Point;

// [START post_class]
@IgnoreExtraProperties
public class Task {

    public String uid;
    public String title;
    public Point location;
    public int lvl;

    public String getTitle() {
        return title;
    }

    public String getLvl() {
        return Integer.toString(lvl);
    }

    public Point getLocation() {

        return location;
    }

    public Task() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Task(String uid, int lvl, String title, Point location) {
        this.uid = uid;
        this.lvl=lvl;
        this.title = title;
        this.location = location;
    }



}
