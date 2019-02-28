package com.sabanciuniv.smartschedule.app;

import com.google.api.client.util.DateTime;
import com.google.firebase.database.IgnoreExtraProperties;

// [START post_class]
@IgnoreExtraProperties
public class Task {

    public String uid;
    public String title;
    public String location;
    public String lvl;
    public String startTime;
    public String endTime;

    public String getTitle() {
        return title;
    }

    public String getLvl() {
        //return Integer.toString(lvl);
        return lvl;
    }

    public String getEndTime() {

        return endTime;
    }
    public String getStartTime() {

        return startTime;
    }
    public String getLocation() {

        return location;
    }

    public Task() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Task(String uid, String lvl, String title, String location) {
        this.uid = uid;
        this.lvl=lvl;
        this.title = title;
        this.location = location;
    }

    public Task(String uid, String lvl, String title, String location, DateTime start, DateTime end) {
        this.uid = uid;
        this.lvl=lvl;
        this.title = title;
        this.location = location;
        this.startTime = start.toString();
        this.endTime = end.toString();
    }
    public Task(String uid, String lvl, String title, String location, DateTime start) {
        this.uid = uid;
        this.lvl=lvl;
        this.title = title;
        this.location = location;
        this.startTime = start.toString();
        this.endTime = "";
    }

}
