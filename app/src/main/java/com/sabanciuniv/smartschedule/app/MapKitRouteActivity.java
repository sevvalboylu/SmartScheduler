
package com.sabanciuniv.smartschedule.app;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.directions.DirectionsFactory;
import com.yandex.mapkit.directions.driving.DrivingArrivalPoint;
import com.yandex.mapkit.directions.driving.DrivingOptions;
import com.yandex.mapkit.directions.driving.DrivingRoute;
import com.yandex.mapkit.directions.driving.DrivingRouter;
import com.yandex.mapkit.directions.driving.DrivingSession;
import com.yandex.mapkit.directions.driving.RequestPoint;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.runtime.Error;
import com.yandex.runtime.network.NetworkError;
import com.yandex.runtime.network.RemoteError;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;

public class MapKitRouteActivity extends Activity implements DrivingSession.DrivingRouteListener {

    private class mixedArray{
        double distance;
        String importance;
        String tid;

        public mixedArray(double distance, String importance, String tid) {
            this.distance = distance;
            this.importance = importance;
            this.tid = tid;
        }
    }

    private final String MAPKIT_API_KEY = "e9704f28-2c92-49b7-a560-dd270b81ac8c";
    private final Point TARGET_LOCATION = new Point(41.0082, 28.9784);

    private String provider;
    private LocationManager locationManager;
    private MapView mapView;
    private MapObjectCollection mapObjects;
    private DrivingRouter drivingRouter;
    private DrivingSession drivingSession;
    private RecyclerView_Config config;
    ArrayList<Task> freeTasks = new ArrayList<>();
    ArrayList<Task> schedTasks= new ArrayList<>();
    protected Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider requesting the permissions again
        }

        location = locationManager.getLastKnownLocation(provider);
        MapKitFactory.setApiKey(MAPKIT_API_KEY);
        MapKitFactory.initialize(this);
        // Now MapView can be created.
        setContentView(R.layout.activity_route);
        super.onCreate(savedInstanceState);

        MapKitFactory.initialize(this);
        DirectionsFactory.initialize(this);
        mapView = findViewById(R.id.routeview);
        mapObjects = mapView.getMap().getMapObjects().addCollection();

        mapView.getMap().move(new CameraPosition(
                TARGET_LOCATION, 5, 0, 0));
        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter();
        mapObjects = mapView.getMap().getMapObjects().addCollection();

        submitRequest();
    }

    @Override
    public void onDrivingRoutes(@NonNull List<DrivingRoute> list) {
        for (DrivingRoute route : list) {
            mapObjects.addPolyline(route.getGeometry());
        }
    }

    @Override
    public void onDrivingRoutesError(@NonNull Error error) {
        String errorMessage = getString(R.string.unknown_error_message);
        if (error instanceof RemoteError) {
            errorMessage = getString(R.string.remote_error_message);
        } else if (error instanceof NetworkError) {
            errorMessage = getString(R.string.network_error_message);
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }

    private void submitRequest() {
        DrivingOptions options = new DrivingOptions();
        ArrayList<RequestPoint> requestPoints = new ArrayList<>();
        //Point currentLocation = new Point(location.getLatitude(),location.getLongitude());
        config= MainActivity.getConfig();

        ArrayList<Point> arrivalPts = new ArrayList<>();
        ArrayList<DrivingArrivalPoint> drivingArrivalPts = new ArrayList<>();
        int count = 0;
        for (Task temp: config.checkedTasks) {

            Point tmp = temp.getLocation().getCoordinate();
            arrivalPts.add(tmp);
            drivingArrivalPts.add(new DrivingArrivalPoint(tmp,"Point "+count));
            count ++;
        }

        ArrayList<Task> tasks = sortTasks();
        Log.d("", "submitRequest: returned");
        //requestPoints.add(new RequestPoint(currentLocation, arrivalPts,drivingArrivalPts,RequestPointType.WAYPOINT));
       // drivingSession = drivingRouter.requestRoutes(requestPoints, options, this);

    }


    private ArrayList<Task> sortTasks(){
        //tasks with given time are already assigned
        //others can not overlap
        RecyclerView_Config config = MainActivity.getConfig();
        //eliminate the ones with fixed slot

        for (Task t : config.checkedTasks)
            if (t.getStartTime() == null && t.getEndTime() == null) freeTasks.add(t);
        for (Task t : config.checkedTasks)
            if (t.getStartTime() != null && t.getEndTime() != null) schedTasks.add(t);
        HashMap<Double,Integer> distOrder= new HashMap<>();
        HashMap<String,Integer> match= new HashMap<>();
        PriorityQueue<Double> minHeap = new PriorityQueue<>();

        Collections.sort(schedTasks,TaskComparator);

        if(schedTasks.size() >= 1) {
            for (Task fr : freeTasks) {
                Integer i = 0;

                while (i < schedTasks.size()) {
                    Task t = schedTasks.get(i);

                    //task is between ith and i+1th if it exists
                    //get midpoint and get distance
                    if ((i + 1) != schedTasks.size()) {
                        Task p = schedTasks.get(i+1);
                        double d = findDistMid(t.getLocation().coordinate, p.getLocation().coordinate, fr.getLocation().coordinate);
                        minHeap.add(d);
                        distOrder.put(d, i);
                    }
                    else {
                        double d = findDist(t.getLocation().coordinate, fr.getLocation().coordinate);
                        minHeap.add(d);
                        distOrder.put(d, i);
                    }
                    i++;
                }
                int driving = 20; //todo: to go to the free task & also to go back on route

                Double index = minHeap.peek();
                int order = distOrder.get(index);

                int hr = Integer.parseInt(schedTasks.get(order).getEndHour());
                int min = Integer.parseInt(schedTasks.get(distOrder.get(minHeap.peek())).getEndMinute());
                min += fr.getDuration() + driving;
                String s = timeFormatter(hr,min);
                String s1 = schedTasks.get(distOrder.get(minHeap.peek())+1).getStartHour()+":"+schedTasks.get(distOrder.get(minHeap.peek())+1).getStartMinute();
                if (btimeComparator(s, s1))
                    match.put(fr.getTid(),distOrder.get(minHeap.peek())); //candidate unscheduled tasks to be inserted between ith and i+1th task

            }
            //sort HashMap by value

            List<HashMap.Entry> temp = new LinkedList<>(match.entrySet());

            Collections.sort(temp, new Comparator<HashMap.Entry>() {
                @Override
                public int compare(Map.Entry entry, Map.Entry t1) {
                    Integer i = (Integer) entry.getValue();
                    Integer j = (Integer) entry.getValue();
                    return i.compareTo(j);
                }

            });

            //now evaluate the candidate tasks in match

            if(temp.size() > 0)
            {
                int lastindex = 0;
                Integer max = (Integer) temp.get(temp.size()-1).getValue();
                for(int k = 0; k <= max; k++) {
                    List<HashMap.Entry> candidates = new LinkedList<>();
                    int j;
                    for (j = lastindex; j <temp.size(); j++) {
                        if ((Integer) temp.get(j).getValue() == k)
                            candidates.add(temp.get(j));
                        else
                            break;
                    } //add all candidates for one interval
                    lastindex = j;

                    List<Task> candTasks = new LinkedList<>();
                    for (HashMap.Entry e : candidates) {
                        Task tmp = getTaskById(e.getKey().toString());
                        candTasks.add(tmp);
                    }

                    //now one by one evaluate the candidate tasks
                    Collections.sort(candTasks, new Comparator<Task>() {
                        @Override
                        public int compare(Task task, Task t1) {
                            return -1 * task.getLvl().compareTo(t1.getLvl());
                        }
                    });

                    if (candTasks.size() > 0)
                        attachTasks(k, schedTasks.get(k), schedTasks.get(k + 1), candTasks);
                    else {
                        Task first = schedTasks.get(0);
                        Task last = schedTasks.get(schedTasks.size() - 1);

                        for (Task fr : freeTasks) {
                            Double distFirst = findDist(first.getLocation().coordinate, fr.getLocation().coordinate);
                            Double distLast = findDist(last.getLocation().coordinate, fr.getLocation().coordinate);
                            if (distFirst > distLast)
                                schedTasks.add(fr);
                            else
                                schedTasks.add(0, fr);
                            //todo: may need to check our logic
                        }

                    }
                }
            }
            else
            {
                Task first = schedTasks.get(0); //todo: first will be updated in for loop
                Task last = schedTasks.get(schedTasks.size()-1);

                for (Task fr :freeTasks) {
                    Double distFirst = findDist(first.getLocation().coordinate, fr.getLocation().coordinate);
                    Double distLast = findDist(last.getLocation().coordinate, fr.getLocation().coordinate);
                    if(distFirst>distLast)
                        schedTasks.add(fr);
                    else
                        schedTasks.add(0,fr);
                    //todo: may need to check our logic
                }

            }

        }
        else //all of them are unscheduled, schedule nearest & treat as other case
        {
            // use location = current location
            Point current = new Point(location.getLatitude(),location.getLongitude());
            ArrayList<mixedArray> mArray = new ArrayList<>();
            for (Task t : freeTasks) {
                double d = findDist(current, t.getLocation().coordinate);
                mixedArray m = new mixedArray(d,t.getLvl(),t.getTid());
                mArray.add(m);
            }

            Collections.sort(mArray, new Comparator<mixedArray>() {
                @Override
                public int compare(mixedArray m1, mixedArray m2) {
                    if(Integer.parseInt(m1.importance)>Integer.parseInt(m2.importance))
                        if(m1.distance< m2.distance)
                            return 1;
                        else if(m1.distance== m2.distance)
                            return 0;
                        else
                            return -1;
                    else
                        return -1;
                }
            });

            int driving = 20;
            Calendar cal = Calendar.getInstance();
            DateFormat df = new SimpleDateFormat("H:mm");
            String date_n = new SimpleDateFormat("dd-mm-yyyy", Locale.getDefault()).format(new Date());
            String date_str = df.format(cal.getTime());

            String earliestStart = timeFormatter(Integer.parseInt(date_str.split(":")[0]),Integer.parseInt(date_str.split(":")[1])+ driving + 30);
            String latestEnd = timeFormatter(Integer.parseInt(earliestStart.split(":")[0]),Integer.parseInt(earliestStart.split(":")[1]));
            Task tmp = getTaskById(mArray.get(0).tid);
            freeTasks.remove(tmp);
            tmp.addRange(earliestStart,latestEnd);
            schedTasks.add(tmp);

            //check if we can attach another task now
            Task dummy = new Task();
            List<Address> address = null;
            String addressLine = "";
            final Geocoder geocoder = new Geocoder(MapKitRouteActivity.this, Locale.getDefault());
            try {
                address = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                addressLine = address.get(0).getAddressLine(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Task.Location curLocation = new Task.Location(addressLine,new Point(location.getLatitude(),location.getLongitude()));
            dummy.setLocation(curLocation);

            //dummy.setStartTime();
        }

        return schedTasks;
    }

    public Task getTaskById(String tid){
        for (Task t:config.checkedTasks) {
            if(t.getTid().equals(tid))
                return t;
        }
        return null;
    }


    public void attachTasks(int index, Task t1, Task t2,List<Task> candidateTasks){
        if(candidateTasks.size() == 0)
            return;

        PriorityQueue<Double> minHeap = new PriorityQueue<>();
        HashMap<Double,String> distOrder= new HashMap<>();

        for (Task t: candidateTasks) {
            double d = findDistMid(t1.getLocation().coordinate, t2.getLocation().coordinate, t.getLocation().coordinate);
            minHeap.add(d);
            distOrder.put(d, t.getTid());
        }

        String tid = distOrder.get(minHeap.peek());
        Task tmp = getTaskById(tid);

        String[] timerange1 = null, timerange2 = null;
        int hr=0,min = 0,hr2=0,min2=0;
        int driving = 20;
        if(t1.getStartTime() == null)
            timerange1 = t1.getRange();
        else
        {
            hr = Integer.parseInt(t1.getEndHour());
            min = Integer.parseInt(t1.getEndMinute());
        }
        if(t2.getStartTime()==null)
            timerange2 = t2.getRange();
        else {
            hr2 = Integer.parseInt(t2.getStartHour());
            min2 = Integer.parseInt(t2.getStartMinute());
        }

        if(timerange1==null && timerange2 != null)
        {
            //t1 scheduled, t2 free
            String time1 = timeFormatter(hr,min + driving + tmp.getDuration());
            String time2 = timeFormatter(Integer.parseInt(timerange2[1].split(":")[0]),
                    Integer.parseInt(timerange2[1].split(":")[1])-t2.getDuration()-driving);
            if (btimeComparator(time1, time2))
            {
                String start = timeFormatter(hr,min + driving);
                hr2 = Integer.parseInt(timerange2[1].split(":")[0]);
                min2 = Integer.parseInt(timerange2[1].split(":")[1]);
                String end = timeFormatter(hr2,min2-driving-t2.getDuration());
                tmp.addRange(start,end);

                schedTasks.remove(t2);
                t2.addRange(timeFormatter(Integer.parseInt(end.split(":")[0]),
                        Integer.parseInt(end.split(":")[1])+driving),t2.getRange()[1]);
                schedTasks.add(index+1,t2);
                schedTasks.add(index+1, tmp);
                candidateTasks.remove(tmp);
            }

        }
        else if(timerange1 != null && timerange2 == null){
            //t1 free, t2 scheduled
            //todo: here
            hr  = Integer.parseInt(timerange1[0].split(":")[0]);
            min = Integer.parseInt(timerange1[0].split(":")[1]) + tmp.getDuration() + driving + t1.getDuration();

            if (btimeComparator(timeFormatter(hr,min),timeFormatter(hr2,min2))) {
                String start = timeFormatter(hr,min-tmp.getDuration());
                String end = timeFormatter(hr2,min2-driving);
                tmp.addRange(start,end);

                schedTasks.remove(t1);
                t1.addRange(t1.getRange()[0],timeFormatter(Integer.parseInt(tmp.getRange()[0].split(":")[0]),
                        Integer.parseInt(tmp.getRange()[0].split(":")[1])-driving));
                schedTasks.add(index,t1);
                schedTasks.add(index + 1, tmp);
            }
            candidateTasks.remove(tmp);
        }
        else if(timerange1 != null && timerange2 != null){
            //t1 free, t2 free
            //todo: here
            hr  = Integer.parseInt(timerange1[0].split(":")[0]);
            min = Integer.parseInt(timerange1[0].split(":")[1]) + tmp.getDuration() + driving + t1.getDuration();

            hr2 =  Integer.parseInt(timerange2[1].split(":")[0]);
            min2 = Integer.parseInt(timerange2[1].split(":")[1]) - t2.getDuration() - driving;

             if (btimeComparator(timeFormatter(hr,min),timeFormatter(hr2,min2))) {
                String start = timeFormatter(hr,min-tmp.getDuration());
                String end = timeFormatter(hr2,min2);
                tmp.addRange(start,end);

                schedTasks.remove(t1);
                t1.addRange(t1.getRange()[0],timeFormatter(Integer.parseInt(start.split(":")[0]),
                        Integer.parseInt(start.split(":")[1])-driving));
                schedTasks.add(index,t1);

                 schedTasks.remove(t2);
                 t2.addRange(timeFormatter(Integer.parseInt(end.split(":")[0]),
                         Integer.parseInt(end.split(":")[1])+driving),t2.getRange()[1]);
                 schedTasks.add(index+1,t2);

                schedTasks.add(index + 1, tmp);
            }
            candidateTasks.remove(tmp);
        }
        else
        {
            //when we have two driving durations, also replace min2 with min2-driving2
            if (btimeComparator(timeFormatter(hr,min + driving + tmp.getDuration()), timeFormatter(hr2, min2-driving))) {
                String start = timeFormatter(hr, min + driving);
                String end = timeFormatter(hr2, min2 -driving);
                tmp.addRange(start, end);
                schedTasks.add(index + 1, tmp);
                candidateTasks.remove(tmp);
            }
        }

        if(candidateTasks.size() > 0){
            if(schedTasks.size()>index+1)
                attachTasks(index,schedTasks.get(index),schedTasks.get(index+1),candidateTasks);
            if(schedTasks.size()>index+2)
                attachTasks(index+1,schedTasks.get(index+1),schedTasks.get(index+2),candidateTasks);
        }

    }


    private boolean btimeComparator(String s, String s1) //returns 1 if left op is sooner
    {
        if(Integer.parseInt(s.split(":")[0])  >  Integer.parseInt(s1.split(":")[0]))return false;
        else if(Integer.parseInt(s.split(":")[0]) == Integer.parseInt(s1.split(":")[0]))
            if(Integer.parseInt(s.split(":")[1]) < Integer.parseInt(s1.split(":")[0]))
               return true;
            else
                return false;
        else
            return true;

    }
   private String timeFormatter(int hr, int min) {
        int mm1=min;
        int hr1=hr;
        if(min > 59) {
            while(mm1>59) {
                mm1 = mm1 - 60;
                hr1 += 1;
            }
        }
        if(min < 0){ //todo
            while(mm1 < 0){
                hr1 -= 1;
                mm1= 60 + mm1;
            }
        }
        return String.valueOf(hr1)+":"+String.valueOf(mm1) ;
    }
    public static Comparator<Task> TaskComparator = new Comparator<Task>() {

        @Override
        public int compare(Task t1, Task t2) {
            return t1.getStartTime().compareTo(t2.getStartTime());
        }
    };

    //todo: şevval get route distance from yandex
    private double findDistMid(Point p1,Point p2,Point p3) // get p3's distance from midpoint of p1 and p2
    {
        Point p = new Point((p1.getLatitude()+p2.getLatitude())/2, (p1.getLongitude()+p2.getLongitude())/2);
        return findDist(p,p3);
    }

    private double findDist(Point p1,Point p2){
      return Math.sqrt(Math.pow(p1.getLatitude()-p2.getLatitude(),2) + Math.pow(p1.getLongitude()-p2.getLongitude(),2));
    }

}
