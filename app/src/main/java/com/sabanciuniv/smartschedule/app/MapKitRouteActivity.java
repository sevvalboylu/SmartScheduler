
package com.sabanciuniv.smartschedule.app;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Toast;

import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.routing.OnlineRoutingApi;
import com.tomtom.online.sdk.routing.RoutingApi;
import com.tomtom.online.sdk.routing.data.FullRoute;
import com.tomtom.online.sdk.routing.data.RouteQuery;
import com.tomtom.online.sdk.routing.data.RouteQueryBuilder;
import com.tomtom.online.sdk.routing.data.RouteType;
import com.tomtom.online.sdk.routing.data.TravelMode;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.directions.Directions;
import com.yandex.mapkit.directions.DirectionsFactory;
import com.yandex.mapkit.directions.driving.DrivingRouter;
import com.yandex.mapkit.directions.driving.DrivingOptions;
import com.yandex.mapkit.directions.driving.DrivingSession;
import com.yandex.mapkit.directions.driving.DrivingRoute;
import com.yandex.mapkit.RequestPoint;
import com.yandex.mapkit.RequestPointType;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.runtime.Error;
import com.yandex.runtime.network.NetworkError;
import com.yandex.runtime.network.RemoteError;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.runtime.image.ImageProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MapKitRouteActivity extends Activity implements DrivingSession.DrivingRouteListener {

    private static ArrayList<Task> tasks;

    public static ArrayList<Task> getTasks() {
        return tasks;
    }

    protected Location location;
    int listSize;
    RoutingApi routingApi = null;
    TravelMode travelMode = TravelMode.CAR;
    ArrayList<distanceMatrix> dm = new ArrayList<distanceMatrix>();
    private String provider;
    private LocationManager locationManager;
    private RecyclerView_Config config;

    LatLng userLocation;
    FloatingActionButton gotolist;

    private final String MAPKIT_API_KEY = "e9704f28-2c92-49b7-a560-dd270b81ac8c";
    private final Point TARGET_LOCATION = new Point(41.0082, 28.9784);

    private MapView mapView;
    private MapObjectCollection mapObjects;
    private DrivingRouter drivingRouter;
    private DrivingSession drivingSession;

    private String url, response;


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

        routingApi = OnlineRoutingApi.create(MapKitRouteActivity.this);
        location = locationManager.getLastKnownLocation(provider);

        super.onCreate(savedInstanceState);
        MapKitFactory.setApiKey(MAPKIT_API_KEY);
        MapKitFactory.initialize(this);
        DirectionsFactory.initialize(this);

        setContentView(R.layout.activity_route);
        gotolist = findViewById(R.id.list_fob);


        mapView = findViewById(R.id.routeview);
        mapObjects = mapView.getMap().getMapObjects().addCollection();
        mapView.getMap().move(new CameraPosition(
                TARGET_LOCATION, 5, 0, 0));
        Directions factory = DirectionsFactory.getInstance();
        drivingRouter = factory.createDrivingRouter();
        mapObjects = mapView.getMap().getMapObjects().addCollection();
        try {
            getDrivingMins();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        List<Address> address;
        String addressLine = "";

        Point c = new Point(userLocation.getLatitude(), userLocation.getLongitude());
        final Geocoder geocoder = new Geocoder(MapKitRouteActivity.this, Locale.getDefault());
        try {
            address = geocoder.getFromLocation(userLocation.getLatitude(), userLocation.getLongitude(), 1);
            addressLine = address.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Task.Location current = new Task.Location(addressLine, c);
        Scheduler sc = new Scheduler(current);
        tasks = sc.sortTasks(dm);

        List<Point> wayPoints = new ArrayList<>();
        for (Task temp : tasks) {
            Point tmp = new Point(temp.getLocation().getCoordinate().getLatitude(), temp.getLocation().getCoordinate().getLongitude());
            wayPoints.add(0, tmp);
        }

        final PlacemarkMapObject mark = mapObjects.addPlacemark(wayPoints.get(0));
        mark.setIcon(ImageProvider.fromResource(this, R.drawable.search_layer_pin_selected_default));

        LatLng[] wayPointsArray = wayPoints.toArray(new LatLng[wayPoints.size()]);

        DrivingOptions options = new DrivingOptions();
        options.setAlternativeCount(1); // todo: somehow we may reach the fastest route possible.
        ArrayList<RequestPoint> requestPoints = new ArrayList<>();

        for (Point p : wayPoints) {
            requestPoints.add(new RequestPoint(p, RequestPointType.WAYPOINT, null));
            final PlacemarkMapObject tmp = mapObjects.addPlacemark(p);
            tmp.setIcon(ImageProvider.fromResource(this, R.drawable.search_layer_pin_selected_default));
        }

        drivingSession = drivingRouter.requestRoutes(requestPoints, options, this);

        mapView.getMap().move(
                new CameraPosition(wayPoints.get(0), 12.0f, 0.0f, 0.0f),
                new Animation(Animation.Type.SMOOTH, 5),
                null);
    }


    public void getDrivingMins() throws IOException {
        for (Task t : config.checkedTasks)
            for (Task m : config.checkedTasks) {
                if (t.getTid() != m.getTid()) {
                    LatLng pt1 = new LatLng(t.getLocation().getCoordinate().getLatitude(), t.getLocation().getCoordinate().getLongitude());
                    LatLng pt2 = new LatLng(m.getLocation().getCoordinate().getLatitude(), m.getLocation().getCoordinate().getLongitude());
                    RouteQuery routeQuery = new RouteQueryBuilder(pt1, pt2).withTravelMode(travelMode);
                    routingApi.planRoute(routeQuery).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(routeResult -> {
                        for (FullRoute fullRoute : routeResult.getRoutes()) {
                            String s = fullRoute.toString();
                            Pattern MY_PATTERN = Pattern.compile("travelTimeInSeconds=[0-9]*");
                            Matcher mat = MY_PATTERN.matcher(s);
                            if (mat.find()) {
                                final String k = mat.group(0).replaceAll("travelTimeInSeconds=", "");
                                dm.add(new distanceMatrix(Integer.parseInt(k) / 60, t.getTid(), m.getTid()));
                                if (dm.size() == (listSize * (listSize - 1)) / 2) {
                                    submitRequest();
                                    return;
                                }
                            }
                        }
                    });

                }
            }
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