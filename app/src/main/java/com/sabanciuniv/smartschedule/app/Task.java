package com.sabanciuniv.smartschedule.app;

import com.yandex.mapkit.geometry.Point;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

// [START post_class]
@IgnoreExtraProperties
public class Task {

    public String uid;
    public String title;
    public Point location;
    public int lvl;

    public Task() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Task(String uid, int lvl, String title, Point location) {
        this.uid = uid;
        this.lvl=lvl;
        this.title = title;
        this.location = location;
    }

    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("title", title);
        result.put("location", location);

        return result;
    }

}

