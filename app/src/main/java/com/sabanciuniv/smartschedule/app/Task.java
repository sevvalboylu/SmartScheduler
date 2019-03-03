package com.sabanciuniv.smartschedule.app;

import com.google.firebase.database.IgnoreExtraProperties;
import com.yandex.mapkit.geometry.Point;

// [START post_class]
@IgnoreExtraProperties

public class Task {

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

    public String uid;
    public String title;
    public String lvl;
    public Location location;


    public String getTitle() {
        return title;
    }

    public String getLvl() {
        //return Integer.toString(lvl);
        return lvl;
    }

    public Location getLocation() {
        return location;
    }

    public Task() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Task(String uid, String lvl, String title, Location location) {
        this.uid = uid;
        this.lvl=lvl;
        this.title = title;
        this.location = location;
    }
}
