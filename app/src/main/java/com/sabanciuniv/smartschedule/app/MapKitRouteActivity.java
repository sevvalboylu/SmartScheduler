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

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.learn.ExplanationForSignedClause;
import org.chocosolver.solver.learn.Implications;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.ValueSortedMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

;


public class MapKitRouteActivity extends Activity implements DrivingSession.DrivingRouteListener {
    private final String MAPKIT_API_KEY = "e9704f28-2c92-49b7-a560-dd270b81ac8c";
    private final Point TARGET_LOCATION = new Point(41.0082, 28.9784);

    private String provider;
    private Model model;
    private LocationManager locationManager;
    private MapView mapView;
    private MapObjectCollection mapObjects;
    private DrivingRouter drivingRouter;
    private DrivingSession drivingSession;

    ArrayList<Task> freeTasks = new ArrayList<>();
    ArrayList<Task> schedTasks= new ArrayList<>();
    protected Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        model = new Model("Smart Scheduler");
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
        Point currentLocation = new Point(location.getLatitude(),location.getLongitude());
        RecyclerView_Config config= MainActivity.getConfig();

        ArrayList<Point> arrivalPts = new ArrayList<>();
        ArrayList<DrivingArrivalPoint> drivingArrivalPts = new ArrayList<>();
        int count = 0;
        for (Task temp: config.checkedTasks) {

            Point tmp = temp.getLocation().getCoordinate();
            arrivalPts.add(tmp);
            drivingArrivalPts.add(new DrivingArrivalPoint(tmp,"Point "+count));
            count ++;
        }
        try {
            sortTasks();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
        //requestPoints.add(new RequestPoint(currentLocation, arrivalPts,drivingArrivalPts,RequestPointType.WAYPOINT));
       // drivingSession = drivingRouter.requestRoutes(requestPoints, options, this);
    }


    private void sortTasks() throws ContradictionException {
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

        if(schedTasks.size() >=1) {
            for (Task fr : freeTasks) {
                Task t = schedTasks.get(0);
                Integer i = 1;

                fr.setStartHour((Integer.parseInt(t.getStartHour()) + Integer.parseInt(schedTasks.get(schedTasks.size()-1).getStartHour()))/2);
                fr.setStartMinute((Integer.parseInt(t.getStartMinute()) + Integer.parseInt(schedTasks.get(schedTasks.size()-1).getStartMinute()))/2);

                while (i < schedTasks.size()) {
                    i++;
                    t = schedTasks.get(i);
                    //task is between ith and i+1th if it exists
                    //get midpoint and get distance
                    if ((i + 1) != schedTasks.size()) {
                        double d = findDistMid(t.getLocation().coordinate, t.getLocation().coordinate, fr.getLocation().coordinate);
                        minHeap.add(d);
                        distOrder.put(d, i);
                    } else {
                        double d = findDist(t.getLocation().coordinate, fr.getLocation().coordinate);
                        minHeap.add(d);
                        distOrder.put(d, i);
                    }
                }
                int driving = 20;
                if (btimeComparator(timeFormatter(Integer.parseInt(schedTasks.get(distOrder.get(minHeap.peek())).getStartHour()),
                            Integer.parseInt(schedTasks.get(distOrder.get(minHeap.peek())).getStartMinute()+ fr.getDuration() + driving)),

                            schedTasks.get(distOrder.get(minHeap.peek())+1).getStartTime()))
                    match.put(fr.getTid(),i); //candidate unscheduled tasks to be inserted between ith and i+1th task
                }
             //
            /*
             TODO:sort match by value so that candidates are sorted
            for each slot:
                get max number of fitting tasks and put them in schedTasks
             */
        }

        else //all of them are unscheduled, use choco
        {
            IntVar vars[] = new IntVar[2*freeTasks.size()];
            int driving = 20;
            //populate map with pairs of taskno and distance to route

            //calculate route and change start time of the task //then append
            for(int q = 0; q < 2*freeTasks.size(); q+=2){
                vars[q] = model.intVar("task_" + q, 1,23); //keep only the start time
                vars[q+1] = model.intVar("task_" + q,0,59);

            }
            for (int i = 0; i < vars.length; i+=2) {
                for (int j = 1; j < vars.length; j += 2) {

                    BoolVar a = timeComparator(timeFormatter(vars[j].getValue(),vars[j+1].getValue()+driving+freeTasks.get(j).getDuration()),timeFormatter(vars[j],vars[j+1]));
                    BoolVar b = timeComparator(timeFormatter(vars[i].getValue(),vars[i+1].getValue()+driving+freeTasks.get(i).getDuration()),timeFormatter(vars[i],vars[i+1]));
                    model.addClauses(LogOp.and(a,b));
                    // no time overlap with other unscheduled ones
                }
            }

            //TODO:calculate the total driving time of the route and try to minimize it
            IntVar OBJ = model.intVar("objective", 0, 999);
            //model.scalar(new IntVar[]{}, new int[]{3,-3}, OBJ).post();
            model.setObjective(Model.MINIMIZE, OBJ);

            Solution solution = model.getSolver().findSolution();
            if (solution != null) {
                System.out.println(solution.toString());
            }
        }
    }


    private boolean btimeComparator(String s, String s1) throws ContradictionException //returns 1 if left op is sooner
    {
        ICause c=new ICause() {
            @Override
            public void explain(ExplanationForSignedClause explanation, ValueSortedMap<IntVar> front, Implications implicationGraph, int pivot) {

            }
        };
        BoolVar b = model.boolVar("b");
        if(Integer.parseInt(s.split(":")[0])  >  Integer.parseInt(s1.split(":")[0]))return false;
        else if(Integer.parseInt(s.split(":")[0]) == Integer.parseInt(s1.split(":")[0]))
            if(Integer.parseInt(s.split(":")[1]) > Integer.parseInt(s1.split(":")[0]))
               return true;
            else
             return false;
        else
        return true;

    }
    private BoolVar timeComparator(String s, String s1) throws ContradictionException //returns 1 if left op is sooner
    {
        ICause c=new ICause() {
            @Override
            public void explain(ExplanationForSignedClause explanation, ValueSortedMap<IntVar> front, Implications implicationGraph, int pivot) {

            }
        };
            BoolVar b = model.boolVar("b");
            if(Integer.parseInt(s.split(":")[0])  >  Integer.parseInt(s1.split(":")[0])){

                b.setToFalse(c);
                return b;
            }
            else if(Integer.parseInt(s.split(":")[0]) == Integer.parseInt(s1.split(":")[0]))
                if(Integer.parseInt(s.split(":")[1]) > Integer.parseInt(s1.split(":")[0])){
                b.setToTrue(c);
                return b;
            }
                else
                {  b.setToFalse(c);
                    return b;}
                else
                { b.setToTrue(c);
                    return b;}

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
    private String timeFormatter(IntVar hr, IntVar min) {
        int mm1=min.getValue();
        int hr1=hr.getValue();
        if(min.getValue() > 59) {
            while(mm1>59)
              mm1 = min.getValue() - 60;
              hr1+=1;
        }
        if(min.getValue()< 0){
            while(mm1 < 0){
                hr1 -= 1;
                mm1= 60 + min.getValue();
            }
        }
       return String.valueOf(hr1)+":"+String.valueOf(mm1);
    }

    public static Comparator<Task> TaskComparator = new Comparator<Task>() {

        @Override
        public int compare(Task t1, Task t2) {
            return t1.getStartTime().compareTo(t2.getStartTime());
        }
    };

    private double findDistMid(Point p1,Point p2,Point p3) // get p3's distance from midpoint of p1 and p2
    {
        Point p = new Point((p1.getLatitude()+p2.getLatitude())/2, (p1.getLongitude()+p2.getLongitude())/2);
        return findDist(p,p3);
    }
    private double findDist(Point p1,Point p2){
      return Math.sqrt(Math.pow(p1.getLatitude()-p2.getLatitude(),2) + Math.pow(p1.getLongitude()-p2.getLongitude(),2));
    }

}
