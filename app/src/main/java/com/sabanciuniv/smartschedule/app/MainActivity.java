package com.sabanciuniv.smartschedule.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static RecyclerView_Config config;
    public static RecyclerView_Config getConfig() {
        return config;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //// Get current date & time//////////////
        Calendar cal = Calendar.getInstance();
        DateFormat dt = new SimpleDateFormat("H:mm:ss");
        DateFormat dd = new SimpleDateFormat("yyyy-MM-dd");
        String time_str = dt.format(cal.getTime());
        String date_str = dd.format(cal.getTime());
        ///////////////////////////////////////////

        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                TaskLoader tl = new TaskLoader(new DataStatus() {
                    @Override
                    public void DataIsLoaded(List<Task> tasks, List<String> keys) {
                        List<Task> mTasks = new LinkedList<>();
                        for (Task t :tasks) {
                            if(t.getStartTime() != null)
                            {
                                String startDate = t.getStartTime().split("T")[0];
                                String startTime = t.getStartTime().split("T")[1];
                                if (startDate.equals(date_str) && btimeComparator(time_str, startTime)) {
                                    mTasks.add(t);
                                }
                            }
                            else
                                mTasks.add(t); //free task
                        }
                        config = new RecyclerView_Config();
                        config.setConfig(mRecyclerView, MainActivity.this, mTasks, keys);
                    }
                }, mAuth.getUid());
                pullToRefresh.setRefreshing(false);
            }
        });
        mRecyclerView = findViewById(R.id.recyclerview_tasks);

        final SharedPreferences prefs = getSharedPreferences("tasks", MODE_PRIVATE);
        int readId = 1;
        List<String> keys = new ArrayList<>();
        ArrayList<Task> tasks = new ArrayList<Task>();

        if (prefs.contains("task1") && prefs.getString("task1", "") != "") {
            while (prefs.contains("task" + readId)) {
                Gson gson = new Gson();
                String json = prefs.getString("task" + readId++, "");
                Task temp = gson.fromJson(json, Task.class);
                if(temp.getStartTime() == null)
                    tasks.add(temp);
                else {
                    String startDate = temp.getStartTime().split("T")[0];
                    String startTime = temp.getStartTime().split("T")[1];

                    if (startDate.equals(date_str) && btimeComparator(time_str, startTime)) {
                        tasks.add(temp);
                    }
                }
            }
            readId = 1;
            while (prefs.contains("key" + readId)) {
                String key = prefs.getString("key" + readId++, "");
                keys.add(key);
            }
            config = new RecyclerView_Config();
            config.setConfig(mRecyclerView, MainActivity.this, tasks, keys);
            //todo: check if keys are mixed
        }

    }

    private boolean btimeComparator(String s, String s1) //returns 1 if left op is sooner
    {
        if (Integer.parseInt(s.split(":")[0]) > Integer.parseInt(s1.split(":")[0])) return false;
        else if (Integer.parseInt(s.split(":")[0]) == Integer.parseInt(s1.split(":")[0]))
            if (Integer.parseInt(s.split(":")[1]) < Integer.parseInt(s1.split(":")[1]))
                return true;
            else
                return false;
        else
            return true;

    }

    public void createSchedule(View v) {
        Intent intent = new Intent(MainActivity.this, MapKitRouteActivity.class);
        startActivity(intent);
    }
}

