package com.sabanciuniv.smartschedule.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import java.util.ArrayList;


public class ViewSchedule extends AppCompatActivity {

    private RecyclerView RecyclerView;
    FloatingActionButton mapBtn;
    private TaskAdapter taskAdapter;
    protected ArrayList<Task> tasks;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        tasks = MapKitRouteActivity.getTasks();
        // now inflate the recyclerView
        taskAdapter = new TaskAdapter(tasks);
        RecyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView.setAdapter(taskAdapter);

    }


}
