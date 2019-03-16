
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

        List<Address> address;
        String addressLine = "";
        Point c = new Point(location.getLatitude(),location.getLongitude());
        final Geocoder geocoder = new Geocoder(MapKitRouteActivity.this, Locale.getDefault());
        try {
            address = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(), 1);
            addressLine = address.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Task.Location current = new Task.Location(addressLine,c);
        Scheduler sc = new Scheduler(current);
        ArrayList<Task> tasks = sc.sortTasks();
        Log.d("", "submitRequest: returned");
        //requestPoints.add(new RequestPoint(currentLocation, arrivalPts,drivingArrivalPts,RequestPointType.WAYPOINT));
       // drivingSession = drivingRouter.requestRoutes(requestPoints, options, this);

    }

}
