
package com.sabanciuniv.smartschedule.app;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapKitRouteActivity extends Activity implements DrivingSession.DrivingRouteListener {

    protected Task.Location location;

    private String provider;
    private LocationManager locationManager;
    private TaskAdapter adapter;

    private final String MAPKIT_API_KEY = "e9704f28-2c92-49b7-a560-dd270b81ac8c";
    private final Point TARGET_LOCATION = new Point(41.0082, 28.9784);

    private MapView mapView;
    private MapObjectCollection mapObjects;
    private DrivingRouter drivingRouter;
    private DrivingSession drivingSession;

    private ArrayList<Task> tasks;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        adapter = MainActivity.getAdapter();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider requesting the permissions again
        }


        //location = locationManager.getLastKnownLocation(provider);

             /*
        final Location[] curr = new Location[1];
        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location loc) {
                // Called when a new location is found by the network location provider.
                if(curr[0]==null) {
                    curr[0] = loc;
                    location = new Task.Location("", new Point(curr[0].getLatitude(), curr[0].getLongitude()));
                    getDrivingMins();
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
   */
            List<Address> address;
            String addressLine = "";
            Point c = new Point(40.892152, 29.378957); //location.getLatitude(),location.getLongitude()
            final Geocoder geocoder = new Geocoder(MapKitRouteActivity.this, Locale.getDefault());
            try {
                address = geocoder.getFromLocation(c.getLatitude(), c.getLongitude(), 1);
                addressLine = address.get(0).getAddressLine(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            location = new Task.Location(addressLine, c);

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

        String caller = getIntent().getStringExtra("Caller");

        do {
            if(caller.equals("ViewSchedule"))
                tasks = ViewSchedule.getTasks();
            else
                tasks = SavedSchedule.getTasks();
        } while (tasks == null);
        //todo: does not seem so safe.

        drawRoutes();
    }

    @Override
    public void onDrivingRoutes(@NonNull List<DrivingRoute> list) {
        for (DrivingRoute route : list) {
            mapObjects.addPolyline(route.getGeometry());
        }
    }

    protected void drawRoutes() {

        DrivingOptions options = new DrivingOptions();
        options.setAlternativeCount(1);
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


   public void goToYandexApp(View v){
        // Map point based on address
       String routeQuery = "?rtext=";
       routeQuery += location.coordinate.getLatitude() + ","  + location.coordinate.getLongitude() + "~";
       for(Task t: tasks ){
          routeQuery+= t.getLocation().coordinate.getLatitude() + ",";
          routeQuery+= t.getLocation().coordinate.getLongitude() + "~";
       }
       routeQuery= routeQuery.substring(0, routeQuery.length() - 1);
       routeQuery += "&rtt=auto";
       Uri uri = Uri.parse("yandexmaps://maps.yandex.ru/"+ routeQuery);
       Intent routeIntent = new Intent(Intent.ACTION_VIEW, uri);
       PackageManager packageManager = getPackageManager();
       List<ResolveInfo> activities = packageManager.queryIntentActivities(routeIntent, 0);
       boolean isIntentSafe = activities.size() > 0;
       // Start an activity if it's safe
       if (isIntentSafe) { startActivity(routeIntent);
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

}