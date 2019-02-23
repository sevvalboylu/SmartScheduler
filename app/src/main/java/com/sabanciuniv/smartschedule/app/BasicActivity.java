
package com.sabanciuniv.smartschedule.app;

import android.Manifest;
import android.content.ContentResolver;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.alamkanak.weekview.WeekViewEvent;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.Value;
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

import static android.content.pm.PackageManager.PERMISSION_GRANTED;


public class BasicActivity extends BaseActivity{


    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);
    private static final String APPLICATION_NAME = "SmartScheduler";
    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String CREDENTIALS_DIRECTORY = ".oauth-credentials";
    private FirebaseAuth mAuth;
    private List<Task> mTasks = new ArrayList<>();
    @Value("${local.server.port}")
    private String serverPort;

    @Override
    public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
        List<WeekViewEvent> mEvents = new ArrayList<>();
        ContentResolver cr = getContentResolver();

        final int callbackId = 42;
        checkPermissions(callbackId, Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR);

        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    final Credential c = getCredentials(HTTP_TRANSPORT);
                    final Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, c)
                            .setApplicationName(APPLICATION_NAME)
                            .build();
                    // List the next 10 events from the primary calendar.
                    DateTime now = new DateTime(System.currentTimeMillis());
                    Events events = null;
                    try {
                        events = service.events().list("primary")
                                .setMaxResults(10)
                                .setTimeMin(now)
                                .setOrderBy("startTime")
                                .setSingleEvents(true)
                                .execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    List<Event> items = events.getItems();
                    if (items.isEmpty()) {
                        System.out.println("No upcoming events found.");
                    } else {
                        System.out.println("Upcoming events");
                        for (Event event : items) {
                            DateTime start = event.getStart().getDateTime();
                            if (start == null) {
                                start = event.getStart().getDate();
                            }
                            System.out.printf("%s (%s)\n", event.getSummary(), start);
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
 //convert to java.util.list before returning
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
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8642).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize(mAuth.getUid());
    }

    private void checkPermissions(int callbackId, String... permissionsId) {
        boolean permissions = true;
        for (String p : permissionsId) {
            permissions = permissions && ContextCompat.checkSelfPermission(this, p) == PERMISSION_GRANTED;
        }

        if (!permissions) ActivityCompat.requestPermissions(this, permissionsId, callbackId);
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