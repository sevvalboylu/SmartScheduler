package com.sabanciuniv.smartschedule.app;

import com.google.api.client.util.DateTime;
import com.google.firebase.database.IgnoreExtraProperties;
import com.yandex.mapkit.geometry.Point;

import java.util.ArrayList;

// [START post_class]
@IgnoreExtraProperties
public class Task {

    private String uid;
    private String tid;
    private String title;
    private Location location;
    private int duration;
    private String lvl;
    private String startTime;
    private String endTime;
    private ArrayList<String> range = new ArrayList<String>(2);

    public void addRange(String start, String end){
        range.add(start);
        range.add(end);
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public ArrayList<String> getRange(){
        return range;
    }

    public Location getLocation() {
        return location;
    }

    public String getEndHour() {
        if(getEndTime() != null)
            return getEndTime().
                    split("T")[1].split(":")[0];
        else return null;
    }
    public String getEndMinute() {
        if(getEndTime()!=null)
            return getEndTime().
                    split("T")[1].split(":")[1];
        else return null;
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
