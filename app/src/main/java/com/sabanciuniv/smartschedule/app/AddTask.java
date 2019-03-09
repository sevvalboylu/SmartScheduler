package com.sabanciuniv.smartschedule.app;

import android.content.Intent;
import android.content.SharedPreferences;
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

import com.google.api.client.util.DateTime;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.yandex.mapkit.geometry.Point;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class AddTask extends AppCompatActivity  {

    private static final String TAG = "AddTask";
    private static final String REQUIRED = "Required";
    private DatabaseReference mDatabase;
    private EditText mTitleField;
    private TextView mLocationField;
    private Spinner spinner1, freqLocationSpinner;
    private Button mSubmitButton;
    private Switch mAllDaySwitch;
    private DatePicker mStartDatePicker, mEndDatePicker;
    private TimePicker mStartTimePicker, mEndTimePicker;
    private String lvl, date_n;
    private TextView mStartDateText, mEndDateText, mStartTimeText, mEndTimeText;
    private ArrayList<Profile.Location> locarr= new ArrayList<>();
    private int locpos=-1;
    //private static final Point location = new Point(41.0082, 28.9784); //should not be static, change later
    private final String location = new String();
    private FirebaseAuth mAuth;
    private int startDateTextClickCount = 0, endDateTextClickCount = 0, startTimeTextClickCount = 0, endTimeTextClickCount = 0;

    double longitude , latitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        date_n = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());
        mStartDateText = findViewById(R.id.startDateText);
        mStartDateText.setText(date_n);
        mEndDateText = findViewById(R.id.endDateText);
        mEndDateText.setText(date_n);

        mStartTimeText = findViewById(R.id.startTimeText);
        mEndTimeText = findViewById(R.id.endTimeText);
        Calendar cal = Calendar.getInstance();
        DateFormat df = new SimpleDateFormat("hh:mm");
        String date_str = df.format(cal.getTime());
        mStartTimeText.setText(date_str);
        cal.add(Calendar.HOUR_OF_DAY, 1);
        String date_str2 = df.format(cal.getTime());
        mEndTimeText.setText(date_str2);

        mTitleField = findViewById(R.id.taskTitleText);
        mLocationField = findViewById(R.id.address);
        mSubmitButton = findViewById(R.id.addTask);
        mAllDaySwitch = findViewById(R.id.allDaySwitch);

        mStartDatePicker = findViewById(R.id.datePicker1);
        mStartDatePicker.setVisibility(View.GONE);
        mEndDatePicker = findViewById(R.id.datePicker2);
        mEndDatePicker.setVisibility(View.GONE);

        mStartTimePicker = findViewById(R.id.timePicker1);
        mStartTimePicker.setVisibility(View.GONE);
        mEndTimePicker = findViewById(R.id.timePicker2);
        mEndTimePicker.setVisibility(View.GONE);

        mAllDaySwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAllDaySwitch.isChecked()) {
                    mStartTimeText.setVisibility(View.GONE);
                    mEndTimeText.setVisibility(View.GONE);
                } else if(!mAllDaySwitch.isChecked()) {
                    mStartTimeText.setVisibility(View.VISIBLE);
                    mEndTimeText.setVisibility(View.VISIBLE);
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

        //TODO: make importance levels user-friendly: change from integer to string ("High", "Moderate", "Low")
        String[] items = new String[]{"1", "2", "3"};
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        //set the spinners adapter to the previously created one.
        dropdown.setAdapter(adapter);

        addListenerOnSpinnerItemSelection();
        addListenerOnSpinnerLocSelection();

        ArrayList<String> locs = new ArrayList<>();
        SharedPreferences s = getSharedPreferences("locations",MODE_PRIVATE);
        Map<String,?> keys =s.getAll();
        for(Map.Entry<String,?> entry : keys.entrySet()){
            Gson gson = new Gson();
            String json = entry.getValue().toString();
            locarr.add(gson.fromJson(json, Profile.Location.class));
            locs.add(gson.fromJson(json, Profile.Location.class).getTitle());
        }

        Spinner loc = findViewById(R.id.freqLocationSpinner);
        ArrayAdapter<String> ladapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, locs);
        loc.setAdapter(ladapter);
        //go to map or dropdown list of most frequent places
    }

    private void submitTask() {
        final String title = mTitleField.getText().toString();
        Intent intent = this.getIntent();
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
        final String address = mLocationField.getText().toString();
        Point pnt = new Point(latitude,longitude);
        final Task.Location location;
        if(locpos == -1)
        location  = new Task.Location(address, pnt);
        else location= new Task.Location(locarr.get(locpos).getAddress(),locarr.get(locpos).getCoordinate());
        Random rand = new Random();
        String taskId = String.valueOf(rand.nextInt(100));
        Task task=null;
        DateTime s = getDateFromDatePicker(mStartDatePicker,mStartTimePicker);
        DateTime e = getDateFromDatePicker(mEndDatePicker,mEndTimePicker);
        if(s!=null && e==null)
        task = new com.sabanciuniv.smartschedule.app.Task(userId, taskId, lvl, title, location, s);
        else if(s!=null && e!=null){task = new com.sabanciuniv.smartschedule.app.Task(userId, taskId, lvl, title, location, s,e); }
        else if(s==null && e == null){
            task = new com.sabanciuniv.smartschedule.app.Task(userId, taskId, lvl, title, location, s);
        }
        mDatabase.child("tasks").child(userId).child(taskId).setValue(task);
    }

    public void addListenerOnSpinnerItemSelection() {
        spinner1 = findViewById(R.id.importanceSpinner);
        spinner1.setOnItemSelectedListener(new CustomOnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
                lvl = parent.getItemAtPosition(pos).toString();
            }
        });
    }

    public void addListenerOnSpinnerLocSelection() {
        Spinner loc = findViewById(R.id.freqLocationSpinner);
        loc.setOnItemSelectedListener(new CustomOnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
                locpos=pos;
            }
        });
    }

    public void goToMap(View view)
    {
        Intent intent = new Intent(AddTask.this, MapViewActivity.class);
        intent.putExtra("caller","AddTask.class"); //please don't delete
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
        longitude = intent.getDoubleExtra("Longitude", 0);
        latitude = intent.getDoubleExtra("Latitude", 0);
        TextView addressTxt = findViewById(R.id.address);
        addressTxt.setText(address);
    }

    public void onStartDateTextClick(View view) {
        startDateTextClickCount++;
        if (startDateTextClickCount%2 == 1) {
            mStartDatePicker.setVisibility(View.VISIBLE);
        }
        else {
            mStartDatePicker.setVisibility(View.GONE);
        }
    }

    public void onEndDateTextClick(View view) {
        endDateTextClickCount++;
        if (endDateTextClickCount%2 == 1) {
            mEndDatePicker.setVisibility(View.VISIBLE);
        }
        else {
            mEndDatePicker.setVisibility(View.GONE);
        }
    }

    public void onStartTimeTextClick(View view) {
        startTimeTextClickCount++;
        if (startTimeTextClickCount%2 == 1) {
            mStartTimePicker.setVisibility(View.VISIBLE);
        }
        else {
            mStartTimePicker.setVisibility(View.GONE);
        }
    }

    public void onEndTimeTextClick(View view) {
        endTimeTextClickCount++;
        if (endTimeTextClickCount%2 == 1) {
            mEndTimePicker.setVisibility(View.VISIBLE);
        }
        else {
            mEndTimePicker.setVisibility(View.GONE);
        }
    }
    public static DateTime getDateFromDatePicker(DatePicker datePicker,TimePicker timePicker){
        if(datePicker.getDayOfMonth()==0)
            return null;
        Calendar calendar = Calendar.getInstance();
        calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), timePicker.getHour(),timePicker.getMinute());
        DateTime dt = new DateTime(calendar.getTime());
        return dt;
    }
}