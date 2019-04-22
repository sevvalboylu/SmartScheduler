package com.sabanciuniv.smartschedule.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;


public class EditTask extends AppCompatActivity {

    private static final String TAG = "AddTask";
    private static final String REQUIRED = "Required";
    private DatabaseReference mDatabase;
    private EditText mTitleField, mDurationText;
    private TextView mLocationField;
    private Spinner spinner1, freqLocationSpinner;
    private Button mSubmitButton;
    private Button mDeleteButton;
    private Switch mAllDaySwitch;
    private DatePicker mStartDatePicker, mEndDatePicker;
    private TimePicker mStartTimePicker, mEndTimePicker;
    private String lvl, date_s,date_e;
    private TextView mStartDateText, mEndDateText, mStartTimeText, mEndTimeText;
    private ArrayList<Profile.Location> locarr= new ArrayList<>();
    //private static final Point location = new Point(41.0082, 28.9784); //should not be static, change later
    private final String location = new String();
    private FirebaseAuth mAuth;
    private int startDateTextClickCount = 0, endDateTextClickCount = 0, startTimeTextClickCount = 0, endTimeTextClickCount = 0;
    double longitude , latitude;
    private int locpos=-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Bundle extras = getIntent().getExtras();
        String json; Task edit=null;
        if (extras != null) {
            json = extras.getString("clickedEvent");
            Gson gson = new Gson();
            edit = (gson.fromJson(json, Task.class));
        }

        DateFormat df = new SimpleDateFormat("hh:mm");
        setContentView(R.layout.activity_edittask);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        if(edit.getEndTime()!=null){
        Date e = new GregorianCalendar(
                Integer.parseInt(edit.getEndTime().split("T")[0].split("-")[0]),
                Integer.parseInt(edit.getEndTime().split("T")[0].split("-")[1]),
                Integer.parseInt(edit.getEndTime().split("T")[0].split("-")[2])).getTime();
        date_e = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(e);
            mEndDateText = findViewById(R.id.endDateText);
            mEndDateText.setText(date_e);

            mEndTimeText = findViewById(R.id.endTimeText);
            String et = edit.getEndTime().split("T")[1].split(":")[0] +":" + edit.getEndTime().split("T")[1].split(":")[1];
            Date etd= null;
            try {
                etd = df.parse(et);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
            String date_str2 = df.format(etd);
            mEndTimeText.setText(date_str2);

        }

        if(edit.getStartTime()!=null){
        Date s = new GregorianCalendar(
                Integer.parseInt(edit.getStartTime().split("T")[0].split("-")[0]),
                Integer.parseInt(edit.getStartTime().split("T")[0].split("-")[1]),
               Integer.parseInt(edit.getStartTime().split("T")[0].split("-")[2])).getTime();
        date_s = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(s);
            mStartDateText = findViewById(R.id.startDateText);
            mStartDateText.setText(date_s);
        mStartTimeText = findViewById(R.id.startTimeText);
        Calendar cal = Calendar.getInstance();
        String st = edit.getStartTime().split("T")[1].split(":")[0] +":" + edit.getStartTime().split("T")[1].split(":")[1];

        Date std= null;
        try {
            std = df.parse(st);
        } catch (ParseException e1) {
            e1.printStackTrace();
        }

        String date_str = df.format(std);
        mStartTimeText.setText(date_str);
        cal.add(Calendar.HOUR_OF_DAY, 1);
        }

        mTitleField = findViewById(R.id.taskTitleText);
        mTitleField.setText(edit.getTitle());
        mLocationField = findViewById(R.id.address);
        mLocationField.setText(edit.getLocation().getAddress());
        mSubmitButton = findViewById(R.id.editTask);
        mDeleteButton = findViewById(R.id.deleteTask);
        mAllDaySwitch = findViewById(R.id.allDaySwitch);
        mDurationText = findViewById(R.id.durationText);


        mStartDatePicker = findViewById(R.id.datePicker1);
        mStartDatePicker.setVisibility(View.GONE);
        mEndDatePicker = findViewById(R.id.datePicker2);
        mEndDatePicker.setVisibility(View.GONE);

        mStartTimePicker = findViewById(R.id.timePicker1);
        mStartTimePicker.setVisibility(View.GONE);
        mEndTimePicker = findViewById(R.id.timePicker2);
        mEndTimePicker.setVisibility(View.GONE);

        latitude = edit.getLocation().getCoordinate().getLatitude();
        longitude = edit.getLocation().getCoordinate().getLongitude();

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
        final Task finalEdit = edit;
        Spinner dropdown = findViewById(R.id.importanceSpinner);
        String[] items = new String[]{"1", "2", "3"};
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        //set the spinners adapter to the previously created one.
        dropdown.setAdapter(adapter);
        dropdown.setSelection(Integer.valueOf(Integer.parseInt(finalEdit.getLvl())-1));
        addListenerOnSpinnerItemSelection();
        addListenerOnSpinnerLocSelection();

        ArrayList<String> locs = new ArrayList<>();
        SharedPreferences s = getSharedPreferences("locations",MODE_PRIVATE);
        Map<String,?> keys =s.getAll();
        for(Map.Entry<String,?> entry : keys.entrySet()){
            Gson gson = new Gson();
            json = entry.getValue().toString();
            locarr.add(gson.fromJson(json, Profile.Location.class));
            locs.add(gson.fromJson(json, Profile.Location.class).getTitle());
        }

        Spinner loc = findViewById(R.id.freqLocationSpinner);
        ArrayAdapter<String> ladapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, locs);
        loc.setAdapter(ladapter);

//        mDurationText.setText(finalEdit.getDuration());
        //todo: showing the duration as text

        //create a list of items for the spinner.
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTask(finalEdit.getTid()); //change task
            }
        });

        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTask(finalEdit.getTid()); //change task
            }
        });
    }

    public void onStart(){
        super.onStart();
    }

    public void onStop(){
        super.onStop();
    }
    private void editTask(String tid) {
        final String title = mTitleField.getText().toString();

        // Title is required
        if (TextUtils.isEmpty(title)) {
            mTitleField.setError(REQUIRED);
            return;
        }

        // Disable button so there are no multi-posts
        //setEditingEnabled(false);9
        Toast.makeText(this, "Editing the task...", Toast.LENGTH_SHORT).show();

        // [START single_value_read]
        final String userId = getUid();
        final String address = mLocationField.getText().toString();
        Point pnt = new Point(latitude, longitude);
        final Task.Location location;
        //final Task.Location location = new Task.Location(address,pnt);
        if(locpos == -1)
            location  = new Task.Location(address, pnt);
        else location= new Task.Location(locarr.get(locpos).getAddress(),locarr.get(locpos).getCoordinate());

        mDatabase.child("tasks").child(userId).child(tid).removeValue();

        Calendar c = Calendar.getInstance();
        c.set(mStartDatePicker.getYear(),mStartDatePicker.getMonth(),mStartDatePicker.getDayOfMonth(),
                mStartTimePicker.getHour(),mStartTimePicker.getMinute());
        final DateTime s = new DateTime(c.getTime());
        c.set(mEndDatePicker.getYear(),mEndDatePicker.getMonth(),mEndDatePicker.getDayOfMonth(),
                mEndTimePicker.getHour(),mEndTimePicker.getMinute());
        final DateTime e = new DateTime(c.getTime());

        Task task=null;
        if(!mAllDaySwitch.isChecked()){
            task = new com.sabanciuniv.smartschedule.app.Task(userId,tid,title, location, getDuration(s.toString(),e.toString()),lvl, s.toString(),e.toString());
            //todo: duration! it returns wrong and changes end time in the meantime
        }
        else{
            task = new com.sabanciuniv.smartschedule.app.Task(userId, tid, lvl, Integer.parseInt(mDurationText.getText().toString()), title, location);
        }

        //Task task = new com.sabanciuniv.smartschedule.app.Task(userId, tid, lvl, title, location);

        deleteTask(tid);
        mDatabase.child("tasks").child(userId).child(tid).setValue(task);

    }
    private void deleteTask(String tid) {
        Toast.makeText(this, "Deleting the task...", Toast.LENGTH_SHORT).show();
        mDatabase.child("tasks").child(mAuth.getCurrentUser().getUid()).child(tid).removeValue();
        final SharedPreferences.Editor editor = getSharedPreferences("tasks", MODE_PRIVATE).edit();
        editor.remove(tid);
        editor.apply();

    }

    public int getDuration(String s1,String s2){
        return Integer.parseInt(s2.split("T")[1].split(":")[0])-Integer.parseInt(s1.split("T")[1].split(":")[0]);
    }

    public void addListenerOnSpinnerItemSelection() {
        spinner1 = (Spinner) findViewById(R.id.importanceSpinner);
        spinner1.setOnItemSelectedListener(new CustomOnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
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
        Intent intent = new Intent(EditTask.this, MapViewActivity.class);
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
}
