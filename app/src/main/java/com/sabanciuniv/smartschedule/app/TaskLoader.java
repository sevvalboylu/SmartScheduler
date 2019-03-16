package com.sabanciuniv.smartschedule.app;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TaskLoader {
    private ArrayList<com.sabanciuniv.smartschedule.app.Task> tasks = new ArrayList<>();


    public TaskLoader(final DataStatus dataStatus, String uid) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref = database.child("tasks").child(uid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tasks.clear();
                if (dataSnapshot.exists()) {
                    List<String> keys = new ArrayList<>();
                    for (DataSnapshot keyNode : dataSnapshot.getChildren()) {
                        keys.add(keyNode.getKey());
                        Task temp = keyNode.getValue(com.sabanciuniv.smartschedule.app.Task.class);
                        tasks.add(temp);
                    }
                    List<Objects> s = new ArrayList<Objects>();
                    dataStatus.DataIsLoaded(tasks, keys);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });

    }

}
