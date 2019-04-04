
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Toast;

import com.evernote.android.job.Job;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.RequestPoint;
import com.yandex.mapkit.RequestPointType;
import com.yandex.mapkit.directions.Directions;
import com.yandex.mapkit.directions.DirectionsFactory;
import com.yandex.mapkit.directions.driving.DrivingOptions;
import com.yandex.mapkit.directions.driving.DrivingRoute;
import com.yandex.mapkit.directions.driving.DrivingRouter;
import com.yandex.mapkit.directions.driving.DrivingSession;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.runtime.Error;
import com.yandex.runtime.image.ImageProvider;
import com.yandex.runtime.network.NetworkError;
import com.yandex.runtime.network.RemoteError;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapKitRouteActivity extends Activity implements DrivingSession.DrivingRouteListener {

    protected Location location;

    private String provider;
    private LocationManager locationManager;
    private RecyclerView_Config config;

    private final String MAPKIT_API_KEY = "e9704f28-2c92-49b7-a560-dd270b81ac8c";
    private final Point TARGET_LOCATION = new Point(41.0082, 28.9784);

    private MapView mapView;
    private MapObjectCollection mapObjects;
    private DrivingRouter drivingRouter;
    private DrivingSession drivingSession;

    private ArrayList<Task> tasks;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        config = MainActivity.getConfig();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider requesting the permissions again
        }


        location = locationManager.getLastKnownLocation(provider);

        super.onCreate(savedInstanceState);
        MapKitFactory.setApiKey(MAPKIT_API_KEY);
        MapKitFactory.initialize(this);
        DirectionsFactory.initialize(this);

        setContentView(R.layout.activity_route);


        mapView = findViewById(R.id.routeview);
        mapObjects = mapView.getMap().getMapObjects().addCollection();
        mapView.getMap().move(new CameraPosition(TARGET_LOCATION, 5, 0, 0));
        Directions factory = DirectionsFactory.getInstance();
        drivingRouter = factory.createDrivingRouter();
        mapObjects = mapView.getMap().getMapObjects().addCollection();

        do {
            tasks = ViewSchedule.getTasks();
        }while(tasks == null);

        drawRoutes();
    }

    @Override
    public void onDrivingRoutes(@NonNull List<DrivingRoute> list) {
        for (DrivingRoute route : list) {
            mapObjects.addPolyline(route.getGeometry());
        }
    }

    protected void drawRoutes(){

        DrivingOptions options = new DrivingOptions();
        options.setAlternativeCount(1); // todo: somehow we may reach the fastest route possible.
        ArrayList<RequestPoint> requestPoints = new ArrayList<>();

        List<Point> wayPoints = new ArrayList<>();

        for (Task temp : tasks) {
            Point tmp = new Point(temp.getLocation().getCoordinate().getLatitude(), temp.getLocation().getCoordinate().getLongitude());
            wayPoints.add(0, tmp);
        }

        /*
        final PlacemarkMapObject mark = mapObjects.addPlacemark(wayPoints.get(0));
        mark.setIcon(ImageProvider.fromResource(this, R.drawable.search_layer_pin_selected_default));

        requestPoints.add(new RequestPoint(wayPoints.get(0), RequestPointType.WAYPOINT, null));
        */

        for (Point p : wayPoints) {
            requestPoints.add(new RequestPoint(p, RequestPointType.WAYPOINT, null));
            final PlacemarkMapObject tmp = mapObjects.addPlacemark(p);
            tmp.setIcon(ImageProvider.fromResource(this, R.drawable.search_layer_pin_selected_default));
        }

        drivingSession = drivingRouter.requestRoutes(requestPoints, options, this);
        mapView.getMap().move(new CameraPosition(wayPoints.get(0), 12.0f, 0.0f, 0.0f), new Animation(Animation.Type.SMOOTH, 5), null);

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

}