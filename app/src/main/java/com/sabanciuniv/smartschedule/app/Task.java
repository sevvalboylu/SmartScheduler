package com.sabanciuniv.smartschedule.app;

import com.google.api.client.util.DateTime;
import com.google.firebase.database.IgnoreExtraProperties;

// [START post_class]
@IgnoreExtraProperties
public class Task {

    public String uid;
    public String tid;
    public String title;
    public Location location;
    public String lvl;
    public String startTime;
    public String endTime;

    protected static class Location {

        String address;
        Point coordinate;

        public Location(String address, Point coordinate) {
            this.address = address;
            this.coordinate = coordinate;
        }

        public Location() {
            this.address ="";
            this.coordinate = new Point(0,0);
        }

        public String  getAddress() {
            return address;
        }

        public Point getCoordinate() {
            return coordinate;
        }
    }

    public String getTitle() {
        return title;
    }

    public String getLvl() {
        //return Integer.toString(lvl);
        return lvl;
    }
    public String getTid() {
        //return Integer.toString(lvl);
        return tid;
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

    public Task(String uid, String tid, String lvl, String title, String location) {
        this.uid = uid;
        this.tid = tid;
        this.lvl=lvl;
        this.title = title;
        this.location = location;
    }

    public Task(String uid, String tid, String lvl, String title, String location, DateTime start, DateTime end) {
        this.uid = uid;
        this.tid = tid;
        this.lvl=lvl;
        this.title = title;
        this.location = location;
        this.startTime = start.toString();
        this.endTime = end.toString();
    }
    public Task(String uid, String tid, String lvl, String title, String location, DateTime start) {
        this.uid = uid;
        this.lvl=lvl;
        this.tid = tid;
        this.title = title;
        this.location = location;
        this.startTime = start.toString();
        this.endTime = "";
    }
    public Task(String uid, String tid, String lvl, String title, Location location) {
        this.uid = uid;
        this.tid = tid;
        this.lvl=lvl;
        this.title = title;
        this.location = location;
    }

}
