
package com.sabanciuniv.smartschedule.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.MenuItem;

import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;


public class BasicActivity extends BaseActivity implements WeekView.EventLongPressListener {

    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);
    private static final String APPLICATION_NAME = "SmartScheduler";
    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String CREDENTIALS_DIRECTORY = ".oauth-credentials";
    private FirebaseAuth mAuth;
    private int eventId = 0;
    private final List<WeekViewEvent> mEvents= new ArrayList<>();
    private List<Task> mTasks = new ArrayList<>();
    private boolean gLoaded = false;
    private boolean tLoaded = false;
    private boolean teventloaded= false;
    private List<Event> gEvents = new ArrayList<>();
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        if(!isNetworkConnected()) {
            SharedPreferences prefs = getSharedPreferences("tasks", MODE_PRIVATE);
            int readId = 1;
                while (prefs.contains("task" + readId)) {
                    Gson gson = new Gson();
                    String json = prefs.getString("task" + readId++, "");
                    mTasks.add(gson.fromJson(json, Task.class));
                }
            tLoaded=true;
            } else {
                final SharedPreferences.Editor editor = getSharedPreferences("tasks", MODE_PRIVATE).edit();
                TaskLoader tl = new TaskLoader(new DataStatus() {
                    @Override
                    public void DataIsLoaded(List<Task> tasks, List<String> keys) {
                        mTasks = tasks;
                        int writeId = 1;
                        for (Task t : mTasks) {
                            Gson gson = new Gson();
                            String json = gson.toJson(t);
                            editor.putString("task" + writeId++, json);
                        }
                        writeId = 1;
                        for (String k : keys) {
                            editor.putString("key" + writeId++, k);
                        }
                        getWeekView().notifyDatasetChanged();
                        tLoaded=true;
                        editor.apply();
                    }
                }, mAuth.getUid());
            }

        final int callbackId = 42;
        checkPermissions(callbackId, Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR, Manifest.permission.ACCESS_FINE_LOCATION);

        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();

        Thread thread = new Thread() { //change to async task later
            @Override
            public void run() {
                try {
                    final Credential c = getCredentials(HTTP_TRANSPORT);
                    final Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, c).setApplicationName(APPLICATION_NAME).build();
                    // List the next 10 events from the primary calendar.
                    DateTime now = new DateTime(System.currentTimeMillis());
                    Events events = null;
                    try {
                        events = service.events().list("primary").setMaxResults(10).setTimeMin(now).setOrderBy("startTime").setSingleEvents(true).execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(events!=null) {
                        List<Event> items = events.getItems();
                        if (items.isEmpty()) {
                            System.out.println("No upcoming events found.");
                        } else {
                            gEvents=items;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    @Override
    public List<? extends WeekViewEvent> onMonthChange(final int newYear, final int newMonth) {
        if(teventloaded!=true)
        loadFireBaseTasks(newYear,newMonth);
        if(gLoaded!=true)
        loadGoogleEvents(newYear,newMonth);
        List<WeekViewEvent> matchedEvents = new ArrayList<>();
        for (WeekViewEvent event : mEvents) {
            if (eventMatches(event, newYear, newMonth)) {
                matchedEvents.add(event);
            }
        }
        return matchedEvents;
    }

    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws
            IOException {
        // Load client secrets.
        InputStream in = this.getResources().openRawResource(R.raw.credentials);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        File DATA_STORE_DIR = new File(getFilesDir(), CREDENTIALS_DIRECTORY);
        FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(dataStoreFactory)
                .setAccessType("offline")
                .build();
        //LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8642).build();
        AuthorizationCodeInstalledApp ab = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()){
            protected void onAuthorization(AuthorizationCodeRequestUrl authorizationUrl) throws IOException {
                String url = (authorizationUrl.build());
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }
        };
        return ab.authorize(mAuth.getUid());
    }

    private void checkPermissions(int callbackId, String... permissionsId) {
        boolean permissions = true;
        for (String p : permissionsId) {
            permissions = permissions && ContextCompat.checkSelfPermission(this, p) == PERMISSION_GRANTED;
        }

        if (!permissions) ActivityCompat.requestPermissions(this, permissionsId, callbackId);
    }

    private void loadGoogleEvents(int newYear,int newMonth){
        SharedPreferences prefs = getSharedPreferences("gEvents", MODE_PRIVATE);
        int readId=1;int writeId = 1;
        if(!isNetworkConnected()){
        if(prefs.contains("gEvent1") && prefs.getString("gEvent1","")!=""){
          while(prefs.contains("gEvent"+ readId))
          {
              Gson gson = new Gson();
              String json = prefs.getString("gEvent"+ readId++, "");
              mEvents.add(gson.fromJson(json, WeekViewEvent.class));
          }

      }}//already loaded
      else // load google events
      {
          SharedPreferences.Editor editor = getSharedPreferences("gEvents", MODE_PRIVATE).edit();
          for (Event e : gEvents) {
              DateTime start = e.getStart().getDateTime();
              java.util.Calendar startTime = java.util.Calendar.getInstance();
              int[] s = DateTimeParser(start.toString());
              startTime.set(java.util.Calendar.HOUR_OF_DAY,s[0]);
              startTime.set(java.util.Calendar.MINUTE, s[1]);
              startTime.set(java.util.Calendar.DAY_OF_MONTH, s[2]);
              startTime.set(java.util.Calendar.MONTH, newMonth - 1);
              startTime.set(java.util.Calendar.YEAR, newYear);

              DateTime end = e.getEnd().getDateTime();
              int[] en = DateTimeParser(end.toString());
              java.util.Calendar endTime = (java.util.Calendar) startTime.clone();
              endTime.set(java.util.Calendar.HOUR_OF_DAY, en[0]);
              endTime.set(java.util.Calendar.MINUTE, en[1]);
              endTime.set(java.util.Calendar.DAY_OF_MONTH, en[2]);
              endTime.set(java.util.Calendar.MONTH, newMonth - 1);
              endTime.set(java.util.Calendar.YEAR, newYear);
              WeekViewEvent event = new WeekViewEvent(++eventId, "Google Event", startTime, endTime);
              event.setColor(randColor());
              mEvents.add(event);
              Gson gson = new Gson();
              String json = gson.toJson(event);
              editor.putString("gEvent"+ writeId++,json);
          }

          editor.apply();
      }
        gLoaded=true;
        //getWeekView().notifyDatasetChanged();
    }


  private void loadFireBaseTasks(int newYear,int newMonth)
{
    SharedPreferences prefs = getSharedPreferences("fbEvents", MODE_PRIVATE);
    int readId=1;int writeId = 1;
    if(prefs.contains("event1") && prefs.getString("event1","")!=""){
        while(prefs.contains("event"+ readId))
        {
            Gson gson = new Gson();
            String json = prefs.getString("event"+ readId++, "");
            mEvents.add(gson.fromJson(json, WeekViewEvent.class));
        }
    }
    else {
        SharedPreferences.Editor editor = getSharedPreferences("fbEvents", MODE_PRIVATE).edit();
        for (Task task : mTasks) { //fill for tasks that are retrieved from firebase
            java.util.Calendar startTime = java.util.Calendar.getInstance();
            if (task.getStartTime() != null) {
                int[] s = DateTimeParser(task.getStartTime());
                startTime.set(java.util.Calendar.HOUR_OF_DAY, s[0]);
                startTime.set(java.util.Calendar.MINUTE, s[1]);
                startTime.set(java.util.Calendar.DAY_OF_MONTH, s[2]);
                startTime.set(java.util.Calendar.MONTH, newMonth - 1);
                startTime.set(java.util.Calendar.YEAR, newYear);
            }
            java.util.Calendar endTime = (java.util.Calendar) startTime.clone();
            if (task.getEndTime() != null) {
                int[] en = DateTimeParser(task.getEndTime());
                endTime.set(java.util.Calendar.HOUR_OF_DAY, en[0]);
                endTime.set(java.util.Calendar.MINUTE, en[1]);
                endTime.set(java.util.Calendar.DAY_OF_MONTH, en[2]);
                endTime.set(java.util.Calendar.MONTH, newMonth - 1);
                endTime.set(java.util.Calendar.YEAR, newYear);
            }
            WeekViewEvent event = new WeekViewEvent(++eventId, task.getTitle(), startTime, endTime);
            event.setColor(randColor());
            mEvents.add(event);
            Gson gson = new Gson();
            String json = gson.toJson(event);
            editor.putString("event"+ writeId++,json);
        }
        editor.apply();
    }
    if(tLoaded=true)teventloaded=true;
    getWeekView().notifyDatasetChanged();
}

    private int[] DateTimeParser(String d){
        int[] s=new int[3];
        String[] parsed  =  d.split("T");
        s[0]=Integer.parseInt(parsed[1].split(":")[0]);
        s[1]=Integer.parseInt(parsed[1].split(":")[1]);
        s[2]=Integer.parseInt(parsed[0].split("-")[2]);
        return s;
    }

private int randColor(){

    int[] androidColors = getResources().getIntArray(R.array.androidcolors);
    return androidColors[new Random().nextInt(androidColors.length)];
}

    private boolean eventMatches(WeekViewEvent event, int year, int month) {
        return (event.getStartTime().get(java.util.Calendar.YEAR) == year && event.getStartTime().get(java.util.Calendar.MONTH) == month - 1) || (event.getEndTime().get(java.util.Calendar.YEAR) == year && event.getEndTime().get(java.util.Calendar.MONTH) == month - 1);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @Override
    public void onEmptyViewLongPress(java.util.Calendar time) {

    }

    @Override
    public void onEventClick(WeekViewEvent event, RectF eventRect) {

    }

    @Override
    public void onEventLongPress(WeekViewEvent event, RectF eventRect) {
        Bundle extras = new Bundle();
        Gson gson = new Gson();
        String json = gson.toJson(event);
        extras.putString("clickedEvent", json);

        Intent in = new Intent(this, EditTask.class);
        in.putExtras(extras);
        startActivity(in);
    }

}