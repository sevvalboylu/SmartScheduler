
package com.sabanciuniv.smartschedule.app;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
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

import org.apache.commons.codec.StringEncoderComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;


public class MapKitRouteActivity extends Activity implements DrivingSession.DrivingRouteListener {
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

        sortTasks();

        //requestPoints.add(new RequestPoint(currentLocation, arrivalPts,drivingArrivalPts,RequestPointType.WAYPOINT));
       // drivingSession = drivingRouter.requestRoutes(requestPoints, options, this);

    }


    private void sortTasks(){
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

        if(schedTasks.size() >= freeTasks.size()) {
            for (Task fr : freeTasks) {
                Integer i = 0;

                //fr.setStartHour((Integer.parseInt(t.getStartHour()) + Integer.parseInt(schedTasks.get(schedTasks.size()-1).getStartHour()))/2);
                //fr.setStartMinute((Integer.parseInt(t.getStartMinute()) + Integer.parseInt(schedTasks.get(schedTasks.size()-1).getStartMinute()))/2);

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
                if (btimeComparator(timeFormatter(Integer.parseInt(schedTasks.get(distOrder.get(minHeap.peek())).getStartHour()),
                            Integer.parseInt(schedTasks.get(distOrder.get(minHeap.peek())).getStartMinute()+ fr.getDuration() + driving)),
                            schedTasks.get(distOrder.get(minHeap.peek())+1).getStartHour()+":"+schedTasks.get(distOrder.get(minHeap.peek())+1).getStartMinute()))
                    match.put(fr.getTid(),i); //candidate unscheduled tasks to be inserted between ith and i+1th task
                //todo: problem here
            }


        }
        else //all of them are unscheduled, schedule nearest & treat as other case
        {
            if(schedTasks.size() == 0)
            {
                //schedule one
            }


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

        int lastindex = 0;
        Integer max = (Integer) temp.get(temp.size()-1).getValue();
        for(int k = 0; k < max; k++){
            List<HashMap.Entry> candidates = new LinkedList<>();
            int j;
            for(j =lastindex;j<max;j++){
                if((Integer)temp.get(j).getValue() == k)
                    candidates.add(temp.get(j));
                else
                    break;
            } //add all candidates for one interval
            lastindex = j;

            List<Task> candTasks = new LinkedList<>();
            for (HashMap.Entry e:candidates) {
                Task tmp = getTaskById(e.getKey().toString());
                candTasks.add(tmp);
            }

            //now one by one evaluate the candidate tasks
            Collections.sort(candTasks, new Comparator<Task>() {
                @Override
                public int compare(Task task, Task t1) {
                    return -1*task.getLvl().compareTo(t1.getLvl());
                }
            });
            /*
            schedTasks.add(k+1,candTasks.get(0));
            candTasks.remove(0);

            int driving = 20;
            for(int i=0;i<candTasks.size();i++)
            {
                Task fr = candTasks.get(i);
                if (btimeComparator(timeFormatter(Integer.parseInt(schedTasks.get(distOrder.get(minHeap.peek())).getStartHour()),
                        Integer.parseInt(schedTasks.get(distOrder.get(minHeap.peek())).getStartMinute()+ fr.getDuration() + driving)),
                        schedTasks.get(distOrder.get(minHeap.peek())+1).getStartTime()))
                    match.put(fr.getTid(),i); //candidate unscheduled tasks to be inserted between ith and i+1th task

            }*/
            if(candTasks.size()>0)
               attachTasks(k,schedTasks.get(k),schedTasks.get(k+1),candTasks);
            //else
                //todo: sona ya da basa ekle
        }
    }

    public Task getTaskById(String tid){
        for (Task t:config.checkedTasks) {
            if(t.getTid().equals(tid))
                return t;
        }
        return null;
    }


    public void attachTasks(int index, Task t1, Task t2,List<Task> candidateTasks){
        PriorityQueue<Double> minHeap = new PriorityQueue<>();
        HashMap<Double,String> distOrder= new HashMap<>();

        for (Task t: candidateTasks) {
            double d = findDistMid(t1.getLocation().coordinate, t2.getLocation().coordinate, t.getLocation().coordinate);
            minHeap.add(d);
            distOrder.put(d, t.getTid());
        }

        String tid = distOrder.get(minHeap.peek());
        Task tmp = getTaskById(tid);

        int driving = 20;
        if (btimeComparator(timeFormatter(Integer.parseInt(t1.getStartHour()),
                Integer.parseInt(t1.getStartMinute()+ tmp.getDuration() + driving)), t2.getStartTime())) {
            schedTasks.add(index + 1, tmp);
        }
        candidateTasks.remove(tmp);
        if(candidateTasks.size() > 0){
            attachTasks(index,t1,tmp,candidateTasks);
            attachTasks(index+1,tmp,t2,candidateTasks);
        }
    }


    private boolean btimeComparator(String s, String s1) //returns 1 if left op is sooner
    {
        if(Integer.parseInt(s.split(":")[0])  >  Integer.parseInt(s1.split(":")[0]))return false;
        else if(Integer.parseInt(s.split(":")[0]) == Integer.parseInt(s1.split(":")[0]))
            if(Integer.parseInt(s.split(":")[1]) > Integer.parseInt(s1.split(":")[0]))
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
            while(mm1>59)
                mm1 = min - 60;
            hr1+=1;
        }
        if(min < 0){
            while(mm1 < 0){
                hr1 -= 1;
                mm1= 60 + min;
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
