package com.sabanciuniv.smartschedule.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Comparator;

public class SavedSchedule extends AppCompatActivity {

    private TaskAdapter adapter;

    private ProgressBar spinner;
    private RecyclerView RecyclerView;
    private  FloatingActionButton mapBtn;
    private Button markasDone;
    private TaskAdapter taskAdapter;
    private static int listSize;
    private static ArrayList<Task> tasks = new ArrayList<>();
    private static ArrayList<Task> doneTasks = new ArrayList<>();

    public static ArrayList<Task> getTasks() {
        return tasks;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_viewschedule);
        mapBtn = findViewById(R.id.map_fob);
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SavedSchedule.this, MapKitRouteActivity.class);
                intent.putExtra("Caller", "SavedSchedule");
                startActivity(intent);
            }
        });
        RecyclerView = findViewById(R.id.recyclerview_schedule);

        spinner = (ProgressBar) findViewById(R.id.progressBar1);
        spinner.setVisibility(View.INVISIBLE);

        tasks.clear();
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
        markasDone = findViewById(R.id.markasdoneBtn);
        markasDone.setVisibility(View.VISIBLE);
        markasDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //delete from database OR keep a separate list
                for (Task temp :taskAdapter.checkedTasks) {
                    temp.setDone(true);
                    //todo: Also maybe update in firebase(might be a bad idea, just asking)???
                    //todo: UPDATE IN SHAREDPREFERENCES TOO!!!
                }
                taskAdapter.checkedTasks.sort(new Comparator<Task>() {
                    @Override
                    public int compare(Task task, Task t1) {
                        if(task.isDone() && t1.isDone())
                            return 0;
                        else if(task.isDone() && !t1.isDone())
                            return 1;
                        else
                            return -1;
                    }
                });
                taskAdapter.notifyDataSetChanged();
            }
        });

    }
}

