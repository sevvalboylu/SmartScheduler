
package com.sabanciuniv.smartschedule.app;

import android.Manifest;
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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.map.CameraPosition;
import com.tomtom.online.sdk.map.Icon;
import com.tomtom.online.sdk.map.MapFragment;
import com.tomtom.online.sdk.map.MarkerBuilder;
import com.tomtom.online.sdk.map.OnMapReadyCallback;
import com.tomtom.online.sdk.map.Route;
import com.tomtom.online.sdk.map.RouteBuilder;
import com.tomtom.online.sdk.map.SimpleMarkerBalloon;
import com.tomtom.online.sdk.map.TomtomMap;
import com.tomtom.online.sdk.routing.OnlineRoutingApi;
import com.tomtom.online.sdk.routing.RoutingApi;
import com.tomtom.online.sdk.routing.data.FullRoute;
import com.tomtom.online.sdk.routing.data.RouteQuery;
import com.tomtom.online.sdk.routing.data.RouteQueryBuilder;
import com.tomtom.online.sdk.routing.data.RouteResult;
import com.tomtom.online.sdk.routing.data.RouteType;
import com.tomtom.online.sdk.routing.data.TravelMode;
import com.yandex.mapkit.geometry.Point;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class MapKitRouteActivity extends AppCompatActivity {

    protected Location location;
    RoutingApi routingApi = null;
    RouteType routeType = RouteType.SHORTEST;
    TravelMode travelMode = TravelMode.CAR;
    TomtomMap map;
    int listSize;
    private Route route;

    public ArrayList<distanceMatrix> getDm() {
        return dm;
    }

    ArrayList<distanceMatrix> dm = new ArrayList<distanceMatrix>();
    private String provider;
    private LocationManager locationManager;
    private RecyclerView_Config config;
    private MapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        config = MainActivity.getConfig();
        listSize = config.checkedTasks.size();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider requesting the permissions again
        }

        location = locationManager.getLastKnownLocation(provider);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tomtom);
        routingApi = OnlineRoutingApi.create(MapKitRouteActivity.this);
        mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map_view);
        mapFragment.getAsyncMap(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull TomtomMap tomtomMap) {
                    map = tomtomMap;
                    map.setMyLocationEnabled(true);
                    //LatLng amsterdam = new LatLng(52.37, 4.90);
                    Location myLocation = map.getUserLocation();
                    LatLng userLocation;
                    if(myLocation != null)
                        userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    else
                        userLocation = new LatLng(41.0082, 28.9784);
                    SimpleMarkerBalloon balloon = new SimpleMarkerBalloon("My Location");
                    map.addMarker(new MarkerBuilder(userLocation).markerBalloon(balloon));
                    map.centerOn(CameraPosition.builder(userLocation).zoom(2.0).build());
                    getDrivingMins();

                    List<Address> address;
                    String addressLine = "";

                    Point c = new Point(location.getLatitude(), location.getLongitude());
                    final Geocoder geocoder = new Geocoder(MapKitRouteActivity.this, Locale.getDefault());
                    try {
                    address = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    addressLine = address.get(0).getAddressLine(0);
                    } catch (IOException e) {
                    e.printStackTrace();
                    }
                    Task.Location current = new Task.Location(addressLine, c);
                    Scheduler sc = new Scheduler(current);
                    ArrayList<Task> tasks = sc.sortTasks(dm);
                    //submitRequest(tasks);
            }
        });
    }

    private void submitRequest(ArrayList<Task> tasks) {


        List<LatLng> wayPoints = new ArrayList<>();
        for (Task temp : tasks) {
            LatLng tmp = new LatLng(temp.getLocation().getCoordinate().getLatitude(), temp.getLocation().getCoordinate().getLongitude());
            wayPoints.add(0,tmp);
        }


        LatLng[] wayPointsArray = wayPoints.toArray(new LatLng[wayPoints.size()]);

       // RouteQuery routeQuery = new RouteQueryBuilder(wayPointsArray[0], wayPointsArray[wayPoints.size() - 1]).withWayPoints(wayPointsArray).withTraffic(true);
        Icon startIcon = Icon.Factory.fromResources(mapFragment.getContext(), R.drawable.ic_map_route_departure);
        Icon endIcon = Icon.Factory.fromResources(mapFragment.getContext(), R.drawable.ic_map_route_destination);
        RouteQuery routeQuery = createRouteQuery(wayPointsArray[0],wayPointsArray[-1], wayPointsArray);
        //showDialogInProgress();
        routingApi.planRoute(routeQuery)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<RouteResult>() {

                    @Override
                    public void onSuccess(RouteResult routeResult) {
                        //dismissDialogInProgress();
                        displayRoutes(routeResult.getRoutes());
                        map.displayRoutesOverview();
                    }

                    private void displayRoutes(List<FullRoute> routes) {
                        for (FullRoute fullRoute : routes) {
                            route = map.addRoute(new RouteBuilder(
                                    fullRoute.getCoordinates()).startIcon(startIcon).endIcon(endIcon).isActive(true));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                       Log.d("","Route display failed");
                    }
                });
    }

    private RouteQuery createRouteQuery(LatLng start, LatLng stop, LatLng[] wayPoints) {
        return (wayPoints != null) ?
                new RouteQueryBuilder(start, stop).withWayPoints(wayPoints).withRouteType(RouteType.FASTEST) :
                new RouteQueryBuilder(start, stop).withRouteType(RouteType.FASTEST);
/*

        for(int i = 0; i<tasks.size()-1; i++)
        {
            RouteQuery routeQuery = new RouteQueryBuilder(wayPointsArray[i], wayPointsArray[i+1]).withWayPoints(wayPointsArray).withTraffic(true);
            int finalI = i;
            routingApi.planRoute(routeQuery).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(routeResult -> {
                for (FullRoute fullRoute : routeResult.getRoutes()) {
                    RouteBuilder routeBuilder = new RouteBuilder(fullRoute.getCoordinates());
                    if(finalI == 0)
                    {
                        Icon startIcon = Icon.Factory.fromResources(mapFragment.getContext(), R.drawable.ic_map_route_departure);
                        routeBuilder.startIcon(startIcon);
                    }
                    else
                    {
                        Icon endIcon  = Icon.Factory.fromResources(mapFragment.getContext(), R.drawable.ic_map_route_destination);
                        routeBuilder.endIcon(endIcon);
                    }
                    map.addRoute(routeBuilder);
                }
            });
        }
*/
    }

    public void getDrivingMins() {
        for (Task t : config.checkedTasks)
            for (Task m : config.checkedTasks) {
                if (t.getTid() != m.getTid()) {
                    LatLng pt1 = new LatLng(t.getLocation().getCoordinate().getLatitude(),t.getLocation().getCoordinate().getLongitude());
                    LatLng pt2 = new LatLng(m.getLocation().getCoordinate().getLatitude(),m.getLocation().getCoordinate().getLongitude());
                    RouteQuery routeQuery = new RouteQueryBuilder(pt1, pt2).withTravelMode(travelMode);
                    routingApi.planRoute(routeQuery).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(routeResult -> {
                        for (FullRoute fullRoute : routeResult.getRoutes()) {
                            String s = fullRoute.toString();
                            Pattern MY_PATTERN = Pattern.compile("travelTimeInSeconds=[0-9]*");
                            Matcher mat = MY_PATTERN.matcher(s);
                            if (mat.find()) {
                                final String k = mat.group(0).replaceAll("travelTimeInSeconds=", "");
                                dm.add(new distanceMatrix(Integer.parseInt(k) / 60, t.getTid(), m.getTid()));
                                if(dm.size()==(listSize*(listSize-1))/2)
                                {
                                    System.out.print(dm);
                                    return;
                                }
                            }
                        }
                    });
                }
            }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        map.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public class distanceMatrix {
        int distance;
        String tid2;
        String tid1;

        protected distanceMatrix(int distance, String tid1, String tid2) {
            this.distance = distance;
            this.tid2 = tid2;
            this.tid1 = tid1;
        }
    }
}