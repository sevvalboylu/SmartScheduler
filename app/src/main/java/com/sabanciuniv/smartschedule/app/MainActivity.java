package com.sabanciuniv.smartschedule.app;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "";
    private RecyclerView mRecyclerView;
    private ArrayList<Task> tasks = new ArrayList<Task>();
    private final String userId= "";
    public interface DataStatus{
    void DataIsLoaded(List<Task> tasks, List<String> keys);

}
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = findViewById(R.id.recyclerview_tasks);
        readTasks(new DataStatus() {
            @Override
            public void DataIsLoaded(List<Task> tasks, List<String> keys) {
                new RecyclerView_Config().setConfig(mRecyclerView,MainActivity.this,tasks,keys);
            }
        });
        //ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.activity_main, tasks);
        //RecyclerView list =  findViewById(R.id.recyclerview_tasks);
        //list.setAdapter(adapter);
    }
    public void readTasks(final DataStatus dataStatus)
    {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref = database.child("tasks");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(tasks != null)
                    tasks.clear();
                if (dataSnapshot.exists()) {
                    List<String> keys = new ArrayList<>();
                    for (DataSnapshot keyNode : dataSnapshot.getChildren()) {
                        keys.add(keyNode.getKey());
                        tasks.add(keyNode.getValue(Task.class));
                    }
                    dataStatus.DataIsLoaded(tasks,keys);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}





