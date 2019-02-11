package com.sabanciuniv.smartschedule.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yandex.mapkit.geometry.Point;

import java.util.HashMap;
import java.util.Map;

public class AddTask extends AppCompatActivity {

    private static final String TAG = "AddTask";

    private static final String REQUIRED = "Required";
    private DatabaseReference mDatabase;
    private EditText mTitleField;
    private Spinner spinner1, spinner2;
    private Button mSubmitButton;
    private static final Point location = new Point(41.0082, 28.9784);
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //dummy sign in
        mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword("selineyuppglu@gmail.com","123456")
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                        }
                    }
                });
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mTitleField = findViewById(R.id.mytaskTitle);

        mSubmitButton = findViewById(R.id.addTask);

        //get the spinner from the xml.
        Spinner dropdown = findViewById(R.id.impspin);
        addListenerOnSpinnerItemSelection();
        //create a list of items for the spinner.
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitTask();
            }
        });
        String[] items = new String[]{"1", "2", "three"};
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
       //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
       //set the spinners adapter to the previously created one.
        dropdown.setAdapter(adapter);

        //go to map or dropdown list of most frequent places
    }

    private void submitTask() {
        final String title = mTitleField.getText().toString();
       // final Point location =

        // Title is required
        if (TextUtils.isEmpty(title)) {
            mTitleField.setError(REQUIRED);
            return;
        }

        // Disable button so there are no multi-posts
        //setEditingEnabled(false);
        Toast.makeText(this, "Submitting the task...", Toast.LENGTH_SHORT).show();

        // [START single_value_read]
        final String userId = getUid();
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        User user = dataSnapshot.getValue(User.class);

                        // [START_EXCLUDE]
                        if (user == null) {
                            // User is null, error out
                            Log.e(TAG, "User " + userId + " is unexpectedly null");
                            Toast.makeText(AddTask.this,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Write new post
                            addnewTask(userId, user.username, title, location);
                        }

                        // Finish this Activity, back to the stream
                        //setEditingEnabled(true);
                        finish();
                        // [END_EXCLUDE]
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        // [START_EXCLUDE]
                        //setEditingEnabled(true);
                        // [END_EXCLUDE]
                    }
                });
        // [END single_value_read]
    }

    public void addListenerOnSpinnerItemSelection() {
        spinner1 = (Spinner) findViewById(R.id.impspin);
        spinner1.setOnItemSelectedListener(new CustomOnItemSelectedListener());
    }
    private void addnewTask(String userId, String username, String title, Point location) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        String key = mDatabase.child("").push().getKey();
        com.sabanciuniv.smartschedule.app.Task task = new com.sabanciuniv.smartschedule.app.Task(userId, title, location);
        Map<String, Object> postValues = task.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/tasks/" + key, postValues);
        childUpdates.put("/user-tasks/" + userId + "/" + key, postValues);

        mDatabase.updateChildren(childUpdates);
    }

    public void goToMap(View view)
    {
        Intent intent = new Intent(AddTask.this, MapViewActivity.class);
        startActivity(intent);
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

}
