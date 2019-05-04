package com.sabanciuniv.smartschedule.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.amazonaws.http.HttpMethodName;
import com.amazonaws.mobile.api.idwlmmr9yhm9.SmartSchedulerMobileHubClient;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory;
import com.amazonaws.mobileconnectors.apigateway.ApiRequest;
import com.amazonaws.mobileconnectors.apigateway.ApiResponse;
import com.amazonaws.util.IOUtils;
import com.amazonaws.util.StringUtils;
import com.google.gson.Gson;
import com.yandex.mapkit.geometry.Point;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ViewSchedule extends AppCompatActivity {

    private class Scheduler{
        private Task.Location location; //configuration taken from main act.(chosen tasks)
        public Scheduler(Task.Location loc) {
            this.location = loc;
        }

    }
    protected Location location;
    private static int listSize;
    private SmartSchedulerMobileHubClient  apiClient=null;

    private static ArrayList<Task> tasks = new ArrayList<>();
    private Object lock = new Object();

    public static ArrayList<Task> getTasks() {
        return tasks;
    }

    private TaskAdapter adapter;

    private RecyclerView RecyclerView;
    FloatingActionButton mapBtn;
    private TaskAdapter taskAdapter;

    public static ArrayList<distanceMatrix> dm = new ArrayList<>();

    private ProgressBar spinner;

    protected class cacheDM {
        private Point curr;
        private Point dest;
        private int mins;

        public cacheDM(Point curr, Point dest, int mins) {
            this.curr = curr;
            this.dest = dest;
            this.mins = mins;
        }
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ///mAWSAppSyncClient = AWSAppSyncClient.builder()
           ///     .context(getApplicationContext())
              //  .awsConfiguration(new AWSConfiguration(getApplicationContext()))
               // .build();

        adapter = MainActivity.getAdapter();
        listSize = adapter.checkedTasks.size();
        setContentView(R.layout.activity_viewschedule);
        mapBtn = findViewById(R.id.map_fob);
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ViewSchedule.this, MapKitRouteActivity.class);
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

        getDrivingMins();
    }


    public void getDrivingMins() {
        ArrayList<Task> cTasks = adapter.checkedTasks;
        new GetDrivingMinsTask().execute(cTasks);
    }

    public void scheduleTasks() {

        List<Address> address;
        String addressLine = "";
        Point c = new Point(41.0082, 28.9784); //location.getLatitude(),location.getLongitude()
        final Geocoder geocoder = new Geocoder(ViewSchedule.this, Locale.getDefault());
        try {
            address = geocoder.getFromLocation(c.getLatitude(), c.getLongitude(), 1);
            addressLine = address.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Task.Location current = new Task.Location(addressLine, c);

        Scheduler sc = new Scheduler(current);


        // Create the client
       apiClient = new ApiClientFactory()
               .credentialsProvider(AWSMobileClient.getInstance().getCredentialsProvider())
               .apiKey("Agr5aS9Zsh2Lm5TxiBA0Sa7p2zpLuGBo4urXSQKI")
               .endpoint("https://zr8mijb5wi.execute-api.eu-central-1.amazonaws.com/backend/SmartScheduler-sortTasks-mobilehub-1951459660")
               .build(SmartSchedulerMobileHubClient.class);

     doInvokeAPI();

    }


    public void doInvokeAPI() {
        // Create components of api request
        final String method = "GET";

        final String path = "/tasks";

        final String body = "";
        final byte[] content = body.getBytes(StringUtils.UTF8);

        final Map parameters = new HashMap<>();
        parameters.put("lang", "en_US");

        final Map headers = new HashMap<>();

        // Use components to create the api request
        ApiRequest localRequest =
                new ApiRequest(apiClient.getClass().getSimpleName())
                        .withPath(path)
                        .withHttpMethod(HttpMethodName.valueOf(method))
                        .withHeaders(headers)
                        .addHeader("Content-Type", "application/json")
                        .withParameters(parameters);

        // Only set body if it has content.
        if (body.length() > 0) {
            localRequest = localRequest
                    .addHeader("Content-Length", String.valueOf(content.length))
                    .withBody(content);
        }

        final ApiRequest request = localRequest;

        // Make network call on background thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("",
                            "Invoking API w/ Request : " +
                                    request.getHttpMethod() + ":" +
                                    request.getPath());

                    final ApiResponse response = apiClient.execute(request);

                    final InputStream responseContentStream = response.getContent();

                    if (responseContentStream != null) {
                        final String responseData = IOUtils.toString(responseContentStream);
                        //Log.d(LOG_TAG, "Response : " + responseData);
                    }

                   // Log.d(LOG_TAG, response.getStatusCode() + " " + response.getStatusText());

                } catch (final Exception exception) {
                   // Log.e(LOG_TAG, exception.getMessage(), exception);
                    exception.printStackTrace();
                }
            }
        }).start();
    }

    public static class distanceMatrix {
        public int duration;
        public String tid2;
        public String tid1;

        protected distanceMatrix(int duration, String tid1, String tid2) {
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
                    if (t.getTid() != m.getTid()) {
                        if (prefs.contains(Double.toString(t.getLocation().getCoordinate().getLatitude()) + ',' + Double.toString(t.getLocation().getCoordinate().getLongitude()) + ',' + Double.toString(t.getLocation().getCoordinate().getLongitude()) + ',' + Double.toString(t.getLocation().getCoordinate().getLongitude()) + classifyHr(currentHour))) {
                            int mins = 0;
                            prefs.getInt(Double.toString(t.getLocation().getCoordinate().getLatitude()) + ',' + Double.toString(t.getLocation().getCoordinate().getLongitude()) + ',' + Double.toString(t.getLocation().getCoordinate().getLongitude()) + ',' + Double.toString(t.getLocation().getCoordinate().getLongitude()), mins);
                            dm.add(new distanceMatrix(mins, t.getTid(), m.getTid()));
                        } else {
                            String origin = "origins=" + t.getLocation().getCoordinate().getLatitude() + "," + t.getLocation().getCoordinate().getLongitude();
                            String destination = "destinations=" + m.getLocation().getCoordinate().getLatitude() + "," + m.getLocation().getCoordinate().getLongitude();
                            String s_url = "https://dev.virtualearth.net/REST/v1/Routes/DistanceMatrix?" + origin + "&" + destination + "&travelMode=driving&&timeUnit=minute&key=AipJt1t0OydHSoksAhHLJE7c25Bvl-ts3J6MQ-CHypr9UdeUSm9eKgoYZVKWl_eH";

                            HttpURLConnection urlConnection = null;
                            try {
                                String inline = "";
                                InputStream in;
                                URL url = new URL(s_url);

                                urlConnection = (HttpURLConnection) url.openConnection();

                                int status = urlConnection.getResponseCode();
                                if (status != 200)
                                    throw new RuntimeException("HttpResponseCode: " + status);
                                else {
                                    Scanner sc = new Scanner(url.openStream());
                                    while (sc.hasNext()) {
                                        inline += sc.nextLine();
                                    }
                                    System.out.println("\nJSON Response in String format");
                                    System.out.println(inline);
                                    sc.close();
                                }

                                Pattern p = Pattern.compile("\"travelDuration\":(.\\d)+");
                                Matcher mat = p.matcher(inline);
                                if (mat.find()) {
                                    final String k = mat.group(0).replaceAll("\"travelDuration\":", "");
                                    Integer mk = Integer.parseInt(k.split("\\.")[0]);
                                    cacheDM cdm = new cacheDM(t.getLocation().getCoordinate(), m.getLocation().getCoordinate(), mk);
                                    Gson gson = new Gson();
                                    String json = gson.toJson(cdm);
                                    editor.putInt(Double.toString(t.getLocation().getCoordinate().getLatitude()) + ',' + Double.toString(t.getLocation().getCoordinate().getLongitude()) + ',' + Double.toString(t.getLocation().getCoordinate().getLongitude()) + ',' + Double.toString(t.getLocation().getCoordinate().getLongitude()) + classifyHr(currentHour), mk);
                                    distanceMatrix d = new distanceMatrix(mk, t.getTid(), m.getTid());
                                    dm.add(d);
                                    if (dm.size() == (listSize * (listSize - 1)) / 2) {
                                        editor.commit();
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