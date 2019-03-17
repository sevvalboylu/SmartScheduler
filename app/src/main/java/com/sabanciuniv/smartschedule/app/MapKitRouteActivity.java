
package com.sabanciuniv.smartschedule.app;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.map.CameraPosition;
import com.tomtom.online.sdk.map.MapFragment;
import com.tomtom.online.sdk.map.MapView;
import com.tomtom.online.sdk.map.MarkerBuilder;
import com.tomtom.online.sdk.map.OnMapReadyCallback;
import com.tomtom.online.sdk.map.RouteBuilder;
import com.tomtom.online.sdk.map.SimpleMarkerBalloon;
import com.tomtom.online.sdk.map.TomtomMap;
import com.tomtom.online.sdk.routing.OnlineRoutingApi;
import com.tomtom.online.sdk.routing.RoutingApi;
import com.tomtom.online.sdk.routing.data.FullRoute;
import com.tomtom.online.sdk.routing.data.RouteQuery;
import com.tomtom.online.sdk.routing.data.RouteQueryBuilder;
import com.tomtom.online.sdk.routing.data.RouteType;
import com.tomtom.online.sdk.routing.data.TravelMode;
import com.yandex.mapkit.directions.driving.DrivingRouter;
import com.yandex.mapkit.directions.driving.DrivingSession;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.MapObjectCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MapKitRouteActivity extends AppCompatActivity{

    private final String MAPKIT_API_KEY = "e9704f28-2c92-49b7-a560-dd270b81ac8c";
    private final Point TARGET_LOCATION = new Point(41.0082, 28.9784);
    RoutingApi routingApi=null;
    RouteType routeType = RouteType.SHORTEST;
    TravelMode travelMode = TravelMode.CAR;
    TomtomMap map;
    private String provider;
    private LocationManager locationManager;
    private MapView mapView;
    private MapObjectCollection mapObjects;
    private DrivingRouter drivingRouter;
    private DrivingSession drivingSession;
    private RecyclerView_Config config;

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
        //MapKitFactory.setApiKey(MAPKIT_API_KEY);
        //MapKitFactory.initialize(this);
        // Now MapView can be created.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tomtom);
        routingApi = OnlineRoutingApi.create(MapKitRouteActivity.this);
        final MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map_view);
        mapFragment.getAsyncMap(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull TomtomMap tomtomMap) {
                map = tomtomMap;
                LatLng amsterdam = new LatLng(52.37, 4.90);
                SimpleMarkerBalloon balloon = new SimpleMarkerBalloon("Amsterdam");
                tomtomMap.addMarker(new MarkerBuilder(amsterdam).markerBalloon(balloon));
                tomtomMap.centerOn(CameraPosition.builder(amsterdam).zoom(7.0).build());
            }
        });

        submitRequest();
    }

    private void submitRequest() {
        //DrivingOptions options = new DrivingOptions();

       // ArrayList<RequestPoint> requestPoints = new ArrayList<>();
        //Point currentLocation = new Point(location.getLatitude(),location.getLongitude());
        config = MainActivity.getConfig();

        List<LatLng> wayPoints=new ArrayList<LatLng>();
        for (Task temp : config.checkedTasks) {
            LatLng tmp = new LatLng(temp.getLocation().getCoordinate().getLatitude(),temp.getLocation().getCoordinate().getLatitude());
            wayPoints.add(tmp);
        }

        LatLng[] wayPointsArray = wayPoints.toArray(new LatLng[wayPoints.size()]);
        RouteQuery routeQuery = new RouteQueryBuilder(wayPointsArray[0], wayPointsArray[wayPoints.size()-1])
                .withWayPoints(wayPointsArray).withTraffic(true);

        routingApi.planRoute(routeQuery)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(routeResult -> {
                    for (FullRoute fullRoute : routeResult.getRoutes()) {
                        RouteBuilder routeBuilder = new RouteBuilder(
                                fullRoute.getCoordinates());
                        //
                        // map.addRoute(routeBuilder);
                    }
                });
     /*
        ArrayList<Point> arrivalPts = new ArrayList<>();
        ArrayList<DrivingArrivalPoint> drivingArrivalPts = new ArrayList<>();
        int count = 0;
        for (Task temp : config.checkedTasks) {
            Point tmp = temp.getLocation().getCoordinate();
            arrivalPts.add(tmp);
            drivingArrivalPts.add(new DrivingArrivalPoint(tmp, "Point " + count));
            count++;
        }
       */
        List<Address> address;
        String addressLine = "";

       // Point c = new Point(location.getLatitude(), location.getLongitude());
        /*
        final Geocoder geocoder = new Geocoder(MapKitRouteActivity.this, Locale.getDefault());
        try {
            address = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            addressLine = address.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
        //Task.Location current = new Task.Location(addressLine, c);
         //Scheduler sc = new Scheduler(current);
       // ArrayList<Task> tasks = sc.sortTasks();
        //Log.d("submit", "submitRequest:" + tasks.toString());
        //requestPoints.add(new RequestPoint(c, arrivalPts,drivingArrivalPts, RequestPointType.WAYPOINT));
        //drivingSession = drivingRouter.requestRoutes(requestPoints, options, this);

    }

private int getDrivingMins(LatLng pt1,LatLng pt2){
    String k="20";
    RouteQuery routeQuery = new RouteQueryBuilder(pt1, pt2).withTravelMode(travelMode);
    routingApi.planRoute(routeQuery)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(routeResult -> {
                for (FullRoute fullRoute : routeResult.getRoutes()) {
                    String s= fullRoute.toString();
                    Pattern MY_PATTERN = Pattern.compile("travelTimeInSeconds=[0-9]*");
                    Matcher m = MY_PATTERN.matcher(s);
                    if (m.find()) {
                       final String l = m.group(0).replaceAll("travelTimeInSeconds=","");
                    }
                }
            });
    return Integer.parseInt(k)/60;
}

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
    @Override
    public void onStart() {
        super.onStart();
        assignMap();
        submitRequest();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
    public void assignMap() {
        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map_view);
        mapFragment.getAsyncMap(tomtommap -> {
            map = tomtommap;
            LatLng amsterdam = new LatLng(41.0082, 28.9784);
            SimpleMarkerBalloon balloon = new SimpleMarkerBalloon("MyLocation");
            map.addMarker(new MarkerBuilder(amsterdam).markerBalloon(balloon));
            map.centerOn(CameraPosition.builder(amsterdam).zoom(15).build());
            map.getUiSettings().turnOnRasterTrafficIncidents();
            map.getUiSettings().turnOnRasterTrafficFlowTiles();

    });
    }

}
