package com.sabanciuniv.smartschedule.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yandex.mapkit.geometry.Point;


import java.util.Random;

public class AddTask extends AppCompatActivity {

    private static final String TAG = "AddTask";
    private static final String REQUIRED = "Required";
    private DatabaseReference mDatabase;
    private EditText mTitleField;
    private TextView mLocationField;
    private Spinner spinner1, freqLocationSpinner;
    private Button mSubmitButton;
    private Switch mPickTimeSwitch, mPickDateSwitch, mAllDaySwitch;
    private DatePicker mDatePicker;
    private TimePicker mTimePicker;
    private String lvl;
    //private static final Point location = new Point(41.0082, 28.9784); //should not be static, change later
    private final String location = new String();
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mTitleField = findViewById(R.id.taskTitleText);

        mLocationField = findViewById(R.id.address);

        mSubmitButton = findViewById(R.id.addTask);

        mPickDateSwitch = findViewById(R.id.pickDateSwitch);

        mPickTimeSwitch = findViewById(R.id.pickTimeSwitch);

        mAllDaySwitch = findViewById(R.id.allDaySwitch);
        mAllDaySwitch.setVisibility(View.GONE);

        mDatePicker = findViewById(R.id.datePicker1);
        mDatePicker.setVisibility(View.GONE);

        mTimePicker = findViewById(R.id.timePicker1);
        mTimePicker.setVisibility(View.GONE);

        mPickDateSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPickDateSwitch.isChecked()) {
                    mDatePicker.setVisibility(View.VISIBLE);
                } else if(!mPickDateSwitch.isChecked()) {
                    mDatePicker.setVisibility(View.GONE);
                }
            }
        });

        mPickTimeSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPickTimeSwitch.isChecked()) {
                    mTimePicker.setVisibility(View.VISIBLE);
                    mAllDaySwitch.setVisibility(View.VISIBLE);
                } else if (!mPickDateSwitch.isChecked()) {
                    mTimePicker.setVisibility(View.GONE);
                    mAllDaySwitch.setVisibility(View.GONE);
                    mAllDaySwitch.setChecked(false);
                }
            }
        });

        mAllDaySwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAllDaySwitch.isChecked()) {
                    mTimePicker.setVisibility(View.GONE);
                } else if(!mAllDaySwitch.isChecked()) {
                    mTimePicker.setVisibility(View.VISIBLE);
                }
            }
        });

        //get the spinner from the xml.
        Spinner dropdown = findViewById(R.id.importanceSpinner);
        addListenerOnSpinnerItemSelection();
        //create a list of items for the spinner.
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitTask();
            }
        });
        String[] items = new String[]{"1", "2", "3"};
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        //set the spinners adapter to the previously created one.
        dropdown.setAdapter(adapter);

        //go to map or dropdown list of most frequent places
    }

    private void submitTask() {
        final String title = mTitleField.getText().toString();

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
        final String location = mLocationField.getText().toString();

        Task task = new com.sabanciuniv.smartschedule.app.Task(userId, lvl, title, location);
        Random rand = new Random();
        String taskId = String.valueOf(rand.nextInt(100));
        mDatabase.child("tasks").child(userId).child(taskId).setValue(task);

    }

    public void addListenerOnSpinnerItemSelection() {
        spinner1 = (Spinner) findViewById(R.id.importanceSpinner);
        spinner1.setOnItemSelectedListener(new CustomOnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
                lvl = parent.getItemAtPosition(pos).toString();
            }
        });
    }

    public void goToMap(View view)
    {
        Intent intent = new Intent(AddTask.this, MapViewActivity.class);
        startActivity(intent);
    }


    public String getUid() {
        return mAuth.getUid();
    }


    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        intent.getStringExtra("Address");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String address = intent.getStringExtra("Address");
        TextView addressTxt = findViewById(R.id.address);
        addressTxt.setText(address);
    }
}