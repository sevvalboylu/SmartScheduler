
package com.sabanciuniv.smartschedule.app;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;


public class BasicActivity extends BaseActivity {

    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);
    private static final String APPLICATION_NAME = "SmartScheduler";
    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String CREDENTIALS_DIRECTORY = ".oauth-credentials";
    private FirebaseAuth mAuth;
    private int eventId = 0;
    private final List<WeekViewEvent> mEvents= new ArrayList<>();
    private List<Task> mTasks = new ArrayList<>();
    private List<Event> gEvents = new ArrayList<>();

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
       // final String uid =  mAuth.getUid();
        TaskLoader tl = new TaskLoader(new DataStatus() {
            @Override
            public void DataIsLoaded(List<Task> tasks, List<String> keys) {
                mTasks = tasks;
                getWeekView().notifyDatasetChanged();
            }
        }
                , "fVujOYBPfIgR1YzpkNwZM3xwhjQ2");

        final int callbackId = 42;
        checkPermissions(callbackId, Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR);

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

        loadFireBaseTasks(newYear,newMonth);
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
        return ab.authorize("fVujOYBPfIgR1YzpkNwZM3xwhjQ2");//change later
    }

    private void checkPermissions(int callbackId, String... permissionsId) {
        boolean permissions = true;
        for (String p : permissionsId) {
            permissions = permissions && ContextCompat.checkSelfPermission(this, p) == PERMISSION_GRANTED;
        }

        if (!permissions) ActivityCompat.requestPermissions(this, permissionsId, callbackId);
    }

    private void loadGoogleEvents(int newYear,int newMonth){

        for (Event e : gEvents) {
            DateTime start = e.getStart().getDateTime();
            java.util.Calendar startTime = java.util.Calendar.getInstance();
            int[] s = DateTimeParser(start.toString());
            startTime.set(java.util.Calendar.HOUR_OF_DAY, 2);
            startTime.set(java.util.Calendar.MINUTE, 30);
            startTime.set(java.util.Calendar.DAY_OF_MONTH, 27);
            startTime.set(java.util.Calendar.MONTH, newMonth-1);
            startTime.set(java.util.Calendar.YEAR, newYear);

            DateTime end = e.getEnd().getDateTime();
            int[] en = DateTimeParser(end.toString());
            java.util.Calendar endTime = (java.util.Calendar) startTime.clone();
            endTime.set(java.util.Calendar.HOUR_OF_DAY, 4);
            endTime.set(java.util.Calendar.MINUTE, 0);
            endTime.set(java.util.Calendar.DAY_OF_MONTH, 27);
            endTime.set(java.util.Calendar.MONTH, newMonth-1);
            endTime.set(java.util.Calendar.YEAR, newYear);
            WeekViewEvent event = new WeekViewEvent(++eventId,"Google Event", startTime, endTime);
            event.setColor(randColor());
            mEvents.add(event);
        }
        getWeekView().notifyDatasetChanged();
    }


  private void loadFireBaseTasks(int newYear,int newMonth)
{

    //mEvents.clear();
    for (Task task : mTasks) { //fill for tasks that are retrieved from firebase
        java.util.Calendar startTime = java.util.Calendar.getInstance();
        if (task.getStartTime() != null) {
            int[] s = DateTimeParser(task.getStartTime());
            startTime.set(java.util.Calendar.HOUR_OF_DAY, s[0]);
            startTime.set(java.util.Calendar.MINUTE, s[1]);
            startTime.set(java.util.Calendar.DAY_OF_MONTH, s[2]);
            startTime.set(java.util.Calendar.MONTH, newMonth-1);
            startTime.set(java.util.Calendar.YEAR, newYear);
        }
        java.util.Calendar endTime = (java.util.Calendar) startTime.clone();
        if (task.getEndTime() != null) {
            int[] en = DateTimeParser(task.getEndTime());
            endTime.set(java.util.Calendar.HOUR_OF_DAY, en[0]);
            endTime.set(java.util.Calendar.MINUTE, en[1]);
            endTime.set(java.util.Calendar.DAY_OF_MONTH, en[2]);
            endTime.set(java.util.Calendar.MONTH, newMonth-1);
            endTime.set(java.util.Calendar.YEAR, newYear);
        }
        WeekViewEvent event = new WeekViewEvent(++eventId, task.getTitle(), startTime, endTime);
        event.setColor(randColor());
        mEvents.add(event);
    }

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



}