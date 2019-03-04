package com.sabanciuniv.smartschedule.app;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "";
    private RecyclerView mRecyclerView;
    private Button newScheduleBtn;
    private ArrayList<Task> tasks = new ArrayList<>();
    private String userId;

    ////////// Checked Task List ////////////////
    private static RecyclerView_Config config;

    public static RecyclerView_Config getConfig() {
        return config;
    }
    /////////////////////////////////////////////

    public interface DataStatus{
        void DataIsLoaded(List<Task> tasks, List<String> keys);
    }


    private RecyclerView mRecyclerView;
    private FirebaseAuth mAuth=FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mRecyclerView = findViewById(R.id.recyclerview_tasks);

        readTasks(new DataStatus() {
            @Override
            public void DataIsLoaded(List<Task> tasks, List<String> keys) {
                config = new RecyclerView_Config();
                config.setConfig(mRecyclerView,MainActivity.this,tasks,keys);
            }
        });
    }

    public void readTasks(final DataStatus dataStatus)
    {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        userId = mAuth.getUid();
        DatabaseReference ref = database.child("tasks").child(userId);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    tasks.clear();
                if (dataSnapshot.exists()) {
                    List<String> keys = new ArrayList<>();
                    for (DataSnapshot keyNode : dataSnapshot.getChildren()) {
                        keys.add(keyNode.getKey());
                        Task temp = keyNode.getValue(Task.class);
                        tasks.add(temp);
                    }
                    dataStatus.DataIsLoaded(tasks,keys);
                }
            }

        final SharedPreferences prefs = getSharedPreferences("tasks", MODE_PRIVATE);
        int readId=1;
        List<String> keys=new ArrayList<>();
        ArrayList<Task> tasks = new ArrayList<Task>();

        if(prefs.contains("task1") && prefs.getString("task1","")!=""){
            while(prefs.contains("task"+ readId))
            {
                Gson gson = new Gson();
                String json = prefs.getString("task"+ readId++, "");
                tasks.add(gson.fromJson(json, Task.class));
            }

            readId=1;
            while(prefs.contains("key"+ readId))
            {
                String key = prefs.getString("key"+ readId++, "");
                keys.add(key);
            }
            new RecyclerView_Config().setConfig(mRecyclerView, MainActivity.this, tasks, keys);
        }
        else {
            TaskLoader tl = new TaskLoader(new DataStatus() {
                @Override
                public void DataIsLoaded(List<Task> tasks, List<String> keys) {
                    new RecyclerView_Config().setConfig(mRecyclerView, MainActivity.this, tasks, keys);
                }
            }, mAuth.getUid());
        }

    }

    public void createSchedule(View view) {
        Intent intent = new Intent(MainActivity.this, MapKitRouteActivity.class);
        startActivity(intent);

    }
}





