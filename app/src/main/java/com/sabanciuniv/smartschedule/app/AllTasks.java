package com.sabanciuniv.smartschedule.app;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.firebase.database.FirebaseDatabase;
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
        mDatabase = FirebaseDatabase.getInstance().getReference();
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
                        adapter = new TaskAdapter(AllTasks.this,(ArrayList<Task>) tasks,false,false,true);
                        mRecyclerView.setLayoutManager(new LinearLayoutManager(AllTasks.this));
                        mRecyclerView.setAdapter(adapter);
                    }
                }, mAuth.getUid());
                pullToRefresh.setRefreshing(false);
            }
        });
        mRecyclerView = findViewById(R.id.recyclerview_tasks);
        scheduleButton = findViewById(R.id.scheduleBtn);
        scheduleButton.setVisibility(View.GONE); //no need for a button here

        final SharedPreferences prefs = getSharedPreferences("tasks", MODE_PRIVATE);
        int readId = 1;
        List<String> keys = new ArrayList<>();
        ArrayList<Task> tasks = new ArrayList<Task>();

        if (prefs.contains("task1") && prefs.getString("task1", "") != "") {
            while (prefs.contains("task" + readId)) {
                Gson gson = new Gson();
                String json = prefs.getString("task" + readId++, "");
                tasks.add(gson.fromJson(json, Task.class));
            }
            readId = 1;
            while (prefs.contains("key" + readId)) {
                String key = prefs.getString("key" + readId++, "");
                keys.add(key);
            }
            adapter = new TaskAdapter(AllTasks.this,tasks,false,false,true);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerView.setAdapter(adapter);


        }
    }

    /*public void setReminder(boolean value){
        String userId = mAuth.getUid();
        String tid =
        mDatabase.child("tasks").child(userId).child(tid).child("reminderEnabled").setValue(value);
    }*/

    public void createSchedule(View v)
    {
        Intent intent = new Intent(AllTasks.this,MapKitRouteActivity.class);
        startActivity(intent);
    }
}

