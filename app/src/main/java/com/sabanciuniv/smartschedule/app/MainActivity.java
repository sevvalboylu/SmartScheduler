package com.sabanciuniv.smartschedule.app;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.annotation.Nullable;


public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static TaskAdapter adapter;

    public static TaskAdapter getAdapter() {
        return adapter;
    }

    ArrayList<Task> mTasks;

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
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
                    public void TasksLoaded(List<Task> tasks, List<String> keys) {
                        mTasks = new ArrayList<>();
                        for (Task t : tasks) {
                            if (t.getStartTime() != null) {
                                String startDate = t.getStartTime().split("T")[0];
                                String startTime = t.getStartTime().split("T")[1];
                                if (startDate.equals(date_str) && btimeComparator(time_str, startTime) && !t.isDone() ) {
                                    mTasks.add(t);
                                }
                            } else
                                mTasks.add(t); //free task
                        }
                        adapter = new TaskAdapter(MainActivity.this, mTasks, true, true, false, false);
                        mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                        mRecyclerView.setAdapter(adapter);
                    }

                    @Override
                    public void LocsLoaded(ArrayList<Profile.Location> locs, List<String> keys) {

                    }
                }, mAuth.getUid());
                pullToRefresh.setRefreshing(false);
            }
        });
        mRecyclerView = findViewById(R.id.recyclerview_tasks);

        final SharedPreferences prefs = getSharedPreferences("fbEvents", MODE_PRIVATE);
        int readId = 1;
        List<String> keys = new ArrayList<>();
        ArrayList<Task> tasks = new ArrayList<Task>();

        if (prefs.contains("task1") && prefs.getString("task1", "") != "") {
            while (prefs.contains("task" + readId)) {
                Gson gson = new Gson();
                String json = prefs.getString("task" + readId++, "");
                Task temp = gson.fromJson(json, Task.class);
                if (temp.getStartTime() == null)
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
            adapter = new TaskAdapter(MainActivity.this, tasks, true, true, false, false);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerView.setAdapter(adapter);
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

    public boolean checkclashes(ArrayList<Task> ct) {
        for (Task t1 : ct)
            for (Task t2 : ct) {
                if (t1 != t2)
                    if (t1.getStartTime() != null && t2.getStartTime() != null) {
                        if (t2.getStartTime().compareTo(t1.getStartTime()) >= 0 && t2.getStartTime().compareTo(t1.getEndTime()) <= 0)
                            return false;
                        if (t2.getStartTime().compareTo(t1.getStartTime()) <= 0 && t1.getStartTime().compareTo(t2.getEndTime()) <= 0)
                            return false;

                    }

            }
        return true;
    }

    public void createSchedule(View v) {

        if (adapter.checkedTasks.size() > 0) {
            if (checkclashes(adapter.checkedTasks)) {
                //Prompt user for the latest ending time
                TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        String endTime = timePicker.getHour() + ":" + timePicker.getMinute();
                        Intent intent = new Intent(MainActivity.this, ViewSchedule.class);
                        intent.putExtra("endTime", endTime);
                        startActivity(intent);
                    }
                },12,0,true);
                timePickerDialog.setMessage("Select ending time for schedule");
                timePickerDialog.show();

            } else {
                Toast.makeText(this, "Some of the tasks overlap!", Toast.LENGTH_SHORT).show();
            }
        } else
            Toast.makeText(this, "Please select tasks!", Toast.LENGTH_SHORT).show();
    }
}

