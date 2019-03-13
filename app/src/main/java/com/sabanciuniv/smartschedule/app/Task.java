package com.sabanciuniv.smartschedule.app;

import com.google.api.client.util.DateTime;
import com.google.firebase.database.IgnoreExtraProperties;
import com.yandex.mapkit.geometry.Point;

// [START post_class]
@IgnoreExtraProperties
public class Task {

    public String uid;
    public String tid;
    public String title;
    public Location location;
    public int duration;
    public String lvl;
    public String startTime;
    public String endTime;

    public Location getLocation() {
        return location;
    }

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

    public String getStartHour() {
        if(getStartTime() != null)
        return getStartTime().
                split("T")[1].split(":")[0];
        else return null;
    }
    public String getStartMinute() {
        if(getStartTime()!=null)
        return getStartTime().
                split("T")[1].split(":")[1];
        else return null;
    }

    public int getDuration(){ //int minutes
        return duration;
    }
    public Task(){}


    public Task(String uid, String tid, String lvl, String title, String location) {

    }
    public Task(String uid, String tid, String lvl, String title, Location location,int duration) {
        this.uid = uid;
        this.tid = tid;
        this.lvl=lvl;
        this.title = title;
        this.location = location;
        this.duration = duration;
    }

    public Task(String uid, String tid, String title, Location location, int duration, String lvl, String startTime, String endTime) {
        this.uid = uid;
        this.tid = tid;
        this.title = title;
        this.location = location;
        this.duration = duration;
        this.lvl = lvl;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Task(String uid, String tid, String lvl, String title, Location location, DateTime start, DateTime end) {
        this.uid = uid;
        this.tid = tid;
        this.lvl=lvl;
        this.title = title;
        this.location = location;
        this.startTime = start.toString();
        this.endTime = end.toString();
    }
    public Task(String uid, String tid, String lvl, String title, Location location, DateTime start) {
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
