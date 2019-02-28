package com.sabanciuniv.smartschedule.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "";
    private RecyclerView mRecyclerView;
    private String userId;

    private FirebaseAuth mAuth=FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mRecyclerView = findViewById(R.id.recyclerview_tasks);

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

}





