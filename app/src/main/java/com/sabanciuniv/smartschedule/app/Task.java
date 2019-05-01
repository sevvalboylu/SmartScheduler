package com.sabanciuniv.smartschedule.app;

import com.google.api.client.util.DateTime;
import com.google.firebase.database.IgnoreExtraProperties;
import com.tomtom.online.sdk.common.location.LatLng;
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
    private boolean reminderEnabled;

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

    public static class Location {

        String address;
        Point coordinate;

        public Location(String address, Point coordinate) {
            this.address = address;
            this.coordinate = coordinate;
        }
        public Location(String address, LatLng coordinate) {
            this.address = address;
            this.coordinate= new Point(coordinate.getLatitude(),coordinate.getLongitude());
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

    //used
    public Task(String uid, String tid, String lvl ,int duration, String title, Location location, boolean reminderEnabled) {
        this.uid = uid;
        this.tid = tid;
        this.lvl=lvl;
        this.title = title;
        this.duration=duration;
        this.location = location;
        this.reminderEnabled = reminderEnabled;
    }

    //used
    public Task(String uid, String tid, String title, Location location, int duration, String lvl, String startTime, String endTime, boolean reminderEnabled) {
        this.uid = uid;
        this.tid = tid;
        this.title = title;
        this.location = location;
        this.duration = duration;
        this.lvl = lvl;
        this.startTime = startTime;
        this.endTime = endTime;
        this.reminderEnabled = reminderEnabled;
    }


    public boolean isReminderEnabled() {
        return reminderEnabled;
    }

    public void setReminderEnabled(boolean reminderEnabled) {
        this.reminderEnabled = reminderEnabled;
    }

    public String switchLevelToString(){
        String s = this.getLvl();
        String x = "";
        switch (s) {
            case "3":
                x = "High";
                break;
            case "2":
                x = "Medium";
                break;
            case "1":
                x = "Low";
                break;
        }
        return x;
    }

}
