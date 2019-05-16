package com.sabanciuniv.smartschedule.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.google.gson.Gson;

import java.util.ArrayList;

public class SavedSchedule extends AppCompatActivity {

    private TaskAdapter adapter;

    private ProgressBar spinner;
    private RecyclerView RecyclerView;
    FloatingActionButton mapBtn;
    private TaskAdapter taskAdapter;
    private static ArrayList<Task> tasks = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_viewschedule);
        mapBtn = findViewById(R.id.map_fob);
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SavedSchedule.this, MapKitRouteActivity.class);
                startActivity(intent);
            }
        });
        RecyclerView = findViewById(R.id.recyclerview_schedule);

        spinner = (ProgressBar) findViewById(R.id.progressBar1);
        spinner.setVisibility(View.INVISIBLE);

        //read task list from cache
        final SharedPreferences editor = getSharedPreferences("lastschedule", MODE_PRIVATE);
        int readId = 1;
        while (editor.contains("scheduledtask" + readId)) {
            Gson gson = new Gson();
            String json = editor.getString("scheduledtask" + readId++, "");
            tasks.add(gson.fromJson(json, Task.class));
        }


        // now inflate the recyclerView
        taskAdapter = new TaskAdapter(SavedSchedule.this, tasks, true, false, false, false);
        RecyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView.setAdapter(taskAdapter);


    }
}

