
package com.sabanciuniv.smartschedule.app;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;


public class BasicActivity extends BaseActivity{


    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);
    private static final String APPLICATION_NAME = "SmartScheduler";
    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String CREDENTIALS_DIRECTORY = ".oauth-credentials";
    private FirebaseAuth mAuth;
    private List<Task> mTasks = new ArrayList<>();

    @Override
    public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
        final List<WeekViewEvent> mEvents = new ArrayList<>();

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
                        List<Event> items = events.getItems();
                        if (items.isEmpty()) {
                            System.out.println("No upcoming events found.");
                        } else {
                            System.out.println("Upcoming events");

                            for (Event e : items) {
                                DateTime start = e.getStart().getDateTime();
                                java.util.Calendar startTime = java.util.Calendar.getInstance();
                                int[] s = DateTimeParser(start);
                                startTime.set(java.util.Calendar.HOUR_OF_DAY, s[0]);
                                startTime.set(java.util.Calendar.MINUTE, s[1]);
                                startTime.set(java.util.Calendar.DAY_OF_MONTH, s[2]);
                                startTime.set(java.util.Calendar.MONTH, s[3]);
                                startTime.set(java.util.Calendar.YEAR, s[4]);

                                DateTime end = e.getEnd().getDateTime();
                                int[] en = endTimeParser(end);
                                java.util.Calendar endTime = (java.util.Calendar) startTime.clone();
                                endTime.set(java.util.Calendar.HOUR_OF_DAY, s[0]);
                                endTime.set(java.util.Calendar.MINUTE, s[1]);
                                endTime.set(java.util.Calendar.DAY_OF_MONTH, s[2]);
                                endTime.set(java.util.Calendar.MONTH, s[3]);
                                endTime.set(java.util.Calendar.YEAR, s[4]);
                                WeekViewEvent event = new WeekViewEvent(1, getEventTitle(startTime), startTime, endTime);
                                event.setColor(getResources().getColor(R.color.event_color_01));
                                mEvents.add(event);
                            }
                        }


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };

            thread.start();

        for (Task task : mTasks) { //fill for tasks that are retrieved from firebase

            java.util.Calendar startTime = java.util.Calendar.getInstance();
            startTime.set(java.util.Calendar.HOUR_OF_DAY, 3);
            startTime.set(java.util.Calendar.MINUTE, 0);
            startTime.set(java.util.Calendar.MONTH, newMonth - 1);
            startTime.set(java.util.Calendar.YEAR, newYear);
            java.util.Calendar endTime = (java.util.Calendar) startTime.clone();
            endTime.add(java.util.Calendar.HOUR, 1);
            endTime.set(java.util.Calendar.MONTH, newMonth - 1);

        }

        return mEvents;
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

    private int[] DateTimeParser(DateTime d){
        int[] s=new int[5];
        String[] parsed  =  d.toString().split("T");
        s[0]=Integer.parseInt(parsed[1].split(":")[0]);
        s[1]=Integer.parseInt(parsed[1].split(":")[1]);
        s[2]=Integer.parseInt(parsed[0].split("-")[2]);
        s[3]=Integer.parseInt(parsed[0].split("-")[1]);
        s[4]=Integer.parseInt(parsed[0].split("-")[0]);
        return s;
    }
    private int[] endTimeParser(DateTime d){
        int[] s=new int[5];
        String[] parsed  =  d.toString().split("T");
        s[0]=Integer.parseInt(parsed[1].split(":")[0]);
        s[1]=Integer.parseInt(parsed[1].split(":")[1]);
        s[2]=Integer.parseInt(parsed[0].split("-")[2]);
        s[3]=Integer.parseInt(parsed[0].split("-")[1]);
        s[4]=Integer.parseInt(parsed[0].split("-")[0]);
        return s;
    }
    @Override
    public void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        String uid =  mAuth.getCurrentUser().getUid();
        TaskLoader tl = new TaskLoader(new DataStatus() {
            @Override
            public void DataIsLoaded(List<Task> tasks, List<String> keys) {
                mTasks = tasks;
            }
        }, uid);
    }
}