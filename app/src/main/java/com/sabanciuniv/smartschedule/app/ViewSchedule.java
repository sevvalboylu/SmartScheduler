package com.sabanciuniv.smartschedule.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.yandex.mapkit.geometry.Point;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ViewSchedule extends AppCompatActivity {

    private String scheduleEnd;
    protected Task.Location location;
    private static int listSize;
    private static ArrayList<Task> tasks = new ArrayList<>();
    private Object lock = new Object();
    public static ArrayList<Task> getTasks() {
        return tasks;
    }

    private TaskAdapter adapter;

    private RecyclerView RecyclerView;
    FloatingActionButton mapBtn;
    private TaskAdapter taskAdapter;
    private Button markAsDone;

    public static ArrayList<distanceMatrix> dm = new ArrayList<>();

    private ProgressBar spinner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = MainActivity.getAdapter();
        listSize = adapter.checkedTasks.size();
        setContentView(R.layout.activity_viewschedule);
        mapBtn = findViewById(R.id.map_fob);
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ViewSchedule.this, MapKitRouteActivity.class);
                intent.putExtra("Caller", "ViewSchedule");
                startActivity(intent);
            }
        });
        RecyclerView = findViewById(R.id.recyclerview_schedule);
        spinner = (ProgressBar) findViewById(R.id.progressBar1);
        // now inflate the recyclerView
        taskAdapter = new TaskAdapter(ViewSchedule.this, tasks, false, false, false, false);
        RecyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView.setAdapter(taskAdapter);
        spinner.setVisibility(View.VISIBLE);

        markAsDone = findViewById(R.id.markasdoneBtn);
        markAsDone.setVisibility(View.GONE);

        scheduleEnd = getIntent().getStringExtra("endTime"); //ending time will be used in scheduler

        //todo: dummy location??? not safe.
        /*
        List<Address> address;
        String addressLine = "";
        Point c = new Point(40.892152, 29.378957); //location.getLatitude(),location.getLongitude()
        final Geocoder geocoder = new Geocoder(ViewSchedule.this, Locale.getDefault());
        try {
            address = geocoder.getFromLocation(c.getLatitude(), c.getLongitude(), 1);
            addressLine = address.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        location = new Task.Location(addressLine, c);*/
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
    }


    public void getDrivingMins() {
        ArrayList<Task> cTasks = new ArrayList<>(adapter.checkedTasks);
        Task.Location xd = new Task.Location("", new Point(location.coordinate.getLatitude(),location.coordinate.getLongitude()));
        Task dummy = new Task();
        dummy.setLocation(xd);
        cTasks.add(dummy);
        dm.clear();
        new GetDrivingMinsTask().execute(cTasks);
    }

    public void scheduleTasks() {
        Scheduler sc = new Scheduler(location, scheduleEnd);
        tasks = sc.sortTasks(dm, scheduleEnd);

        if(!sc.isScheduledAll())
        {
            // Some tasks could not be scheduled.
            Toast.makeText(this, "Some tasks could not be scheduled.", Toast.LENGTH_LONG).show();
        }

        //write to sharedPrefs,
        final SharedPreferences.Editor editor = getSharedPreferences("lastschedule", MODE_PRIVATE).edit();
        editor.clear();
        int writeId = 1;
        for (Task t : tasks) {
            Gson gson = new Gson();
            String json = gson.toJson(t);
            editor.putString("scheduledtask" + writeId++, json);
        }
        editor.apply();
    }

    public static class distanceMatrix {
        int duration;
        String tid2;
        String tid1;

        distanceMatrix(int duration, String tid1, String tid2) {
            this.duration = duration;
            this.tid2 = tid2;
            this.tid1 = tid1;
        }
    }

    private class GetDrivingMinsTask extends AsyncTask<ArrayList<Task>, Boolean, Boolean> {
        @Override
        protected Boolean doInBackground(ArrayList<Task>... arrayLists) {
            SharedPreferences prefs = getSharedPreferences("Distance Matrices", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            Calendar rightNow = Calendar.getInstance();
            int currentHour = rightNow.get(Calendar.HOUR_OF_DAY);
            for (Task t : arrayLists[0])
                for (Task m : arrayLists[0]) {
                    if (!t.getTid().equals(m.getTid())) {
                        if (prefs.contains(Double.toString(t.getLocation().getCoordinate().getLatitude()) + ',' + Double.toString(t.getLocation().getCoordinate().getLongitude()) + ',' + Double.toString(t.getLocation().getCoordinate().getLongitude()) + ',' + Double.toString(t.getLocation().getCoordinate().getLongitude()) + classifyHr(currentHour))) {
                            int mins = prefs.getInt(Double.toString(t.getLocation().getCoordinate().getLatitude()) + ',' + Double.toString(t.getLocation().getCoordinate().getLongitude()) + ',' + Double.toString(t.getLocation().getCoordinate().getLongitude()) + ',' + Double.toString(t.getLocation().getCoordinate().getLongitude())+ classifyHr(currentHour), 0);
                            dm.add(new distanceMatrix(mins, t.getTid(), m.getTid()));

                        }
                        else if (t.getLocation().getAddress().equals(m.getLocation().getAddress())) {
                            int mins = 0;
                            dm.add(new distanceMatrix(mins, t.getTid(), m.getTid()));
                        }
                        else {
                            String origin = "origins=" + t.getLocation().getCoordinate().getLatitude() + "," + t.getLocation().getCoordinate().getLongitude();
                            String destination = "destinations=" + m.getLocation().getCoordinate().getLatitude() + "," + m.getLocation().getCoordinate().getLongitude();
                            String s_url = "https://dev.virtualearth.net/REST/v1/Routes/DistanceMatrix?" + origin + "&" + destination + "&travelMode=driving&&timeUnit=minute&key=AipJt1t0OydHSoksAhHLJE7c25Bvl-ts3J6MQ-CHypr9UdeUSm9eKgoYZVKWl_eH";

                            HttpURLConnection urlConnection = null;
                            try {
                                StringBuilder inline = new StringBuilder();
                                InputStream in;
                                URL url = new URL(s_url);

                                urlConnection = (HttpURLConnection) url.openConnection();

                                int status = urlConnection.getResponseCode();
                                if (status != 200)
                                    throw new RuntimeException("HttpResponseCode: " + status);
                                else {
                                    Scanner sc = new Scanner(url.openStream());
                                    while (sc.hasNext()) {
                                        inline.append(sc.nextLine());
                                    }
                                    System.out.println("\nJSONt Response in String format");
                                    System.out.println(inline);
                                    sc.close();
                                }

                                Pattern p = Pattern.compile("\"travelDuration\":(.\\d)+");
                                Matcher mat = p.matcher(inline.toString());
                                if (mat.find()) {
                                    final String k = mat.group(0).replaceAll("\"travelDuration\":", "");
                                    int mk = Integer.parseInt(k.split("\\.")[0]);
                                    editor.putInt(Double.toString(t.getLocation().getCoordinate().getLatitude()) + ',' + Double.toString(t.getLocation().getCoordinate().getLongitude()) + ',' + Double.toString(t.getLocation().getCoordinate().getLongitude()) + ',' + Double.toString(t.getLocation().getCoordinate().getLongitude()) + classifyHr(currentHour), mk);
                                    distanceMatrix d = new distanceMatrix(mk, t.getTid(), m.getTid());
                                    dm.add(d);
                                    if (dm.size() == (listSize * (listSize - 1)) / 2) {
                                        editor.apply();
                                        return true;
                                    }
                                }

                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            return false;
        }

        protected void onPostExecute(Boolean result) {
            scheduleTasks();
            taskAdapter = new TaskAdapter(ViewSchedule.this, tasks, false, false, false, false);
            RecyclerView.setLayoutManager(new LinearLayoutManager(ViewSchedule.this));
            RecyclerView.setAdapter(taskAdapter);
            spinner.setVisibility(View.GONE);
        }
    }

    public String classifyHr(int hr) {

        if (hr >= 0 && hr <= 10) return "morning";
        if (hr > 10 && hr <= 16) {
            return "noon";
        }
        if (hr > 16 && hr <= 20) {
            return "evening";
        }
        if (hr > 20) {
            return "night";
        }
        return "";
    }

}