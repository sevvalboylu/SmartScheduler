package com.sabanciuniv.smartschedule.app;

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
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class AllTasks extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    Button scheduleButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private static TaskAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        List<String> keys = new ArrayList<>();
        final List<Task>[] mTasks = new ArrayList[]{new ArrayList<Task>()};
        //mDatabase = FirebaseDatabase.getInstance().getReference();
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
                        adapter = new TaskAdapter(AllTasks.this, (ArrayList<Task>) tasks, false, false, true, true);
                        mRecyclerView.setLayoutManager(new LinearLayoutManager(AllTasks.this));
                        mRecyclerView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void LocsLoaded(ArrayList<Profile.Location> locs, List<String> keys) {
                    }
                }, mAuth.getUid());
                pullToRefresh.setRefreshing(false);
            }
        });
        mRecyclerView = findViewById(R.id.recyclerview_tasks);
        scheduleButton = findViewById(R.id.scheduleBtn);
        scheduleButton.setVisibility(View.GONE); //no need for a button here

        if (isNetworkConnected()) {

            final SharedPreferences.Editor editor = getSharedPreferences("fbEvents", MODE_PRIVATE).edit();
            TaskLoader tl = new TaskLoader(new DataStatus() {
                @Override
                public void TasksLoaded(List<Task> tasks, List<String> keys) {
                    mTasks[0] = tasks;
                    ArrayList<Task> adp_lis = new ArrayList<Task>(mTasks[0]);

                    adapter = new TaskAdapter(AllTasks.this, adp_lis, false, false, true, true);
                    mRecyclerView.setLayoutManager(new LinearLayoutManager( AllTasks.this));
                    mRecyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    int writeId = 1;
                    for (Task t : tasks) {
                        Gson gson = new Gson();
                        String json = gson.toJson(t);
                        editor.putString("task" + writeId++, json);
                    }
                    writeId = 1;
                    for (String k : keys) {
                        editor.putString("key" + writeId++, k);
                    }
                    editor.apply();
                }

                @Override
                public void LocsLoaded(ArrayList<Profile.Location> locs, List<String> keys) {

                }
            }, mAuth.getUid());

        }

        else {
            final SharedPreferences prefs = getSharedPreferences("tasks", MODE_PRIVATE);
            int readId = 1;


            if (prefs.contains("task1") && prefs.getString("task1", "") != "") {
                while (prefs.contains("task" + readId)) {
                    Gson gson = new Gson();
                    String json = prefs.getString("task" + readId++, "");
                    mTasks[0].add(gson.fromJson(json, Task.class));
                }
                readId = 1;
                while (prefs.contains("key" + readId)) {
                    String key = prefs.getString("key" + readId++, "");
                    keys.add(key);
                }
                ArrayList<Task> adp_lis = new ArrayList<Task>(mTasks[0]);

                adapter = new TaskAdapter(AllTasks.this, adp_lis, false, false, true, true);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                mRecyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        }



    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public void createSchedule(View v)
    {
        Intent intent = new Intent(AllTasks.this,MapKitRouteActivity.class);
        startActivity(intent);
    }
}

