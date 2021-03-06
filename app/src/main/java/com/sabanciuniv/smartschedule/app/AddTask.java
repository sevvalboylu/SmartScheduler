package com.sabanciuniv.smartschedule.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class AddTask extends AppCompatActivity {

    private static final String TAG = "AddTask";
    private static final String REQUIRED = "Required";
    private DatabaseReference mDatabase;
    private EditText mTitleField, mDurationText;
    private TextView mLocationField;
    private Spinner spinner1;
    private Switch mAllDaySwitch;
    private DatePicker mStartDatePicker, mEndDatePicker;
    private TimePicker mStartTimePicker, mEndTimePicker;
    private String lvl, date_n;
    private TextView mStartDateText, mEndDateText, mStartTimeText, mEndTimeText;
    private ArrayList<Profile.Location> locarr = new ArrayList<>();
    private int locpos = -1;
    //private static final Point location = new Point(41.0082, 28.9784); //should not be static, change later
    private final String location = new String();
    private FirebaseAuth mAuth;
    private int startDateTextClickCount = 0, endDateTextClickCount = 0, startTimeTextClickCount = 0, endTimeTextClickCount = 0;
    private boolean reminderEnabled = false;
    double longitude, latitude;
    private FloatingActionButton mdeleteButton, mSubmitButton;

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
        mAllDaySwitch = findViewById(R.id.allDaySwitch);

        mStartDatePicker = findViewById(R.id.datePicker1);
        mStartDatePicker.setVisibility(View.GONE);
        mEndDatePicker = findViewById(R.id.datePicker2);
        mEndDatePicker.setVisibility(View.GONE);

        mStartTimePicker = findViewById(R.id.timePicker1);
        mStartTimePicker.setVisibility(View.GONE);
        mEndTimePicker = findViewById(R.id.timePicker2);
        mEndTimePicker.setVisibility(View.GONE);
        mEndTimePicker.setHour(mStartTimePicker.getHour()+1);
        mDurationText = findViewById(R.id.durationText);

        mAllDaySwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAllDaySwitch.isChecked()) {
                    mDurationText.setEnabled(true);
                    mStartTimeText.setVisibility(View.GONE);
                    mEndTimeText.setVisibility(View.GONE);
                } else if (!mAllDaySwitch.isChecked()) {
                    mDurationText.setEnabled(false);
                    mStartTimeText.setVisibility(View.VISIBLE);
                    mEndTimeText.setVisibility(View.VISIBLE);
                }
            }
        });

        mdeleteButton = findViewById(R.id.floatingActionButton6);
        mdeleteButton.setVisibility(View.GONE);


        mSubmitButton = findViewById(R.id.floatingActionButton5);
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitTask(view);
            }
        });
        //get the spinner from the xml.
        Spinner dropdown = findViewById(R.id.importanceSpinner);
        addListenerOnSpinnerItemSelection();
        //create a list of items for the spinner.

        String[] items = new String[]{"High", "Medium", "Low"};

        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        //set the spinners adapter to the previously created one.
        dropdown.setAdapter(adapter);

        addListenerOnSpinnerItemSelection();
        addListenerOnSpinnerLocSelection();

        final ArrayList<String>[] locs = new ArrayList[]{new ArrayList<>()};
        locs[0].add(0,"Your Places");
        if (isNetworkConnected()) {
            final SharedPreferences.Editor editor = getSharedPreferences("addresses", MODE_PRIVATE).edit();
            Profile.LocLoader tl = new Profile.LocLoader(new DataStatus() {
                @Override
                public void TasksLoaded(List<Task> tasks, List<String> keys) {

                }
                @Override
                public void LocsLoaded(ArrayList<Profile.Location> locations, List<String> keys) {

                    int writeId = 1;
                    for (Profile.Location t : locations) {
                        locarr.add(t);
                        locs[0].add(t.getTitle());
                        Gson gson = new Gson();
                        String json = gson.toJson(t);
                        editor.putString("address" + writeId++, json);
                    }
                    Spinner loc = findViewById(R.id.freqLocationSpinner);
                    ArrayAdapter ladapter = new ArrayAdapter<>(AddTask.this, android.R.layout.simple_spinner_dropdown_item, locs[0]);
                    loc.setAdapter(ladapter);
                    writeId = 1;
                    for (String k : keys) {
                        editor.putString("key" + writeId++, k);
                    }
                    editor.apply();
                }


            }, mAuth.getUid());

        }
        else {
            SharedPreferences s = getSharedPreferences("locations", MODE_PRIVATE);
            Map<String, ?> keys = s.getAll();
            for (Map.Entry<String, ?> entry : keys.entrySet()) {
                Gson gson = new Gson();
                String json = entry.getValue().toString();
                locarr.add(gson.fromJson(json, Profile.Location.class));
                locs[0].add(gson.fromJson(json, Profile.Location.class).getTitle());
            }
            Spinner loc = findViewById(R.id.freqLocationSpinner);
            ArrayAdapter<String> ladapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, locs[0]);
            loc.setAdapter(ladapter);
        }

        //go to map or dropdown list of most frequent places
    }

    public static boolean isNullOrEmpty(String str) {
        if(str != null && !str.trim().isEmpty())
            return false;
        return true;
    }

    public void submitTask(View view) {

        final String title = mTitleField.getText().toString();

        // Title is required
        if (isNullOrEmpty(title)) {
            mTitleField.setError(REQUIRED);
            return;
        }

        // [START single_value_read]
        final String userId = getUid();
        String address = mLocationField.getText().toString();
        Point pnt = new Point(latitude, longitude);
        final Task.Location location;
        if (locpos == 0)
            location = new Task.Location(address, pnt);
        else {
            location = new Task.Location(locarr.get(locpos-1).getAddress(), locarr.get(locpos-1).getCoordinate());
            address = locarr.get(locpos-1).getAddress();
            mLocationField.setText(address);
        }
        Random rand = new Random();
        String taskId = String.valueOf(rand.nextInt(100));
        Task task = null;

        DateTime s = getDateFromDatePicker(mStartDatePicker, mStartTimePicker);
        DateTime e = getDateFromDatePicker(mEndDatePicker, mEndTimePicker);

        if (!mAllDaySwitch.isChecked()) {
            if( btimeComparator(mStartTimePicker.getHour() + ":" +mStartTimePicker.getMinute() , mEndTimePicker.getHour() + ":" +mEndTimePicker.getMinute()))
            task = new com.sabanciuniv.smartschedule.app.Task(userId, taskId, title, location, getDuration(s.toString(), e.toString()), lvl, s.toString(), e.toString(), reminderEnabled);
            else
            { Toast.makeText(this,"Start and end times are not consistent!",Toast.LENGTH_LONG).show();
             return;}
        } else {
            task = new com.sabanciuniv.smartschedule.app.Task(userId, taskId, lvl, Integer.parseInt(mDurationText.getText().toString()), title, location, reminderEnabled);
        }

        if (title.equals("") || address.equals("") || address.equals("Address should appear here"))
        {
            Toast.makeText(this,"Please fill required fields", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(this, "Submitting the task...", Toast.LENGTH_SHORT).show();

            mDatabase.child("tasks").child(userId).child(taskId).setValue(task);

            Intent intent = new Intent(AddTask.this, BasicActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivityForResult(intent, 1);
        }
    }

    public void addListenerOnSpinnerItemSelection() {
        spinner1 = findViewById(R.id.importanceSpinner);
        spinner1.setOnItemSelectedListener(new CustomOnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String s = parent.getItemAtPosition(pos).toString();
                Integer x = 0;
                switch (s) {
                    case "High":
                        x = 3;
                        break;
                    case "Medium":
                        x = 2;
                        break;
                    case "Low":
                        x = 1;
                        break;
                }
                lvl = x.toString();
            }
        });
    }

    public void addListenerOnSpinnerLocSelection() {
        Spinner loc = findViewById(R.id.freqLocationSpinner);
        loc.setOnItemSelectedListener(new CustomOnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                locpos = pos;
            }
        });
    }

    public void goToMap(View view) {
        Intent intent = new Intent(AddTask.this, MapViewActivity.class);
        intent.putExtra("caller", "AddTask.class"); //please don't delete
        startActivity(intent);
    }


    public String getUid() {
        return mAuth.getUid();
    }

    public int getDuration(String s1, String s2) {
        int a = Integer.parseInt(s2.split("T")[1].split(":")[0]);
        int b = Integer.parseInt(s1.split("T")[1].split(":")[0]);
        int c = Integer.parseInt(s2.split("T")[1].split(":")[1]);
        int d = Integer.parseInt(s1.split("T")[1].split(":")[1]);
        int hours = (a - b) * 60;
        int minutes = (c - d);
        return (hours + minutes);
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
        if (startDateTextClickCount % 2 == 1) {
            mStartDatePicker.setVisibility(View.VISIBLE);
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.set(mStartDatePicker.getYear(), mStartDatePicker.getMonth(), mStartDatePicker.getDayOfMonth());
            Date date = calendar.getTime();
            date_n = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date);
            mStartDateText.setText(date_n);
            mStartDatePicker.setVisibility(View.GONE);
        }
    }

    public void onEndDateTextClick(View view) {
        endDateTextClickCount++;
        if (endDateTextClickCount % 2 == 1) {
            mEndDatePicker.setVisibility(View.VISIBLE);
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.set(mEndDatePicker.getYear(), mEndDatePicker.getMonth(), mEndDatePicker.getDayOfMonth());
            Date date = calendar.getTime();
            date_n = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date);
            mEndDateText.setText(date_n);
            mEndDatePicker.setVisibility(View.GONE);
        }
    }

    public void onStartTimeTextClick(View view) {
        startTimeTextClickCount++;
        if (startTimeTextClickCount % 2 == 1) {
            mStartTimePicker.setVisibility(View.VISIBLE);
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.set(mStartDatePicker.getYear(), mStartDatePicker.getMonth(), mStartDatePicker.getDayOfMonth(),
                    mStartTimePicker.getHour(), mStartTimePicker.getMinute());
            DateFormat df = new SimpleDateFormat("hh:mm");
            String date_str = df.format(calendar.getTime());
            mStartTimeText.setText(date_str);
            mStartTimePicker.setVisibility(View.GONE);
        }
    }

    public void onEndTimeTextClick(View view) {
        endTimeTextClickCount++;
        if (endTimeTextClickCount % 2 == 1) {
            mEndTimePicker.setVisibility(View.VISIBLE);
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.set(mEndDatePicker.getYear(), mEndDatePicker.getMonth(), mEndDatePicker.getDayOfMonth(),
                    mEndTimePicker.getHour(), mEndTimePicker.getMinute());
            DateFormat df = new SimpleDateFormat("hh:mm");
            String date_str = df.format(calendar.getTime());
            mEndTimeText.setText(date_str);
            mEndTimePicker.setVisibility(View.GONE);
        }
    }

    public static DateTime getDateFromDatePicker(DatePicker datePicker, TimePicker timePicker) {
        if (datePicker.getDayOfMonth() == 0)
            return null;
        Calendar calendar = Calendar.getInstance();
        calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), timePicker.getHour(), timePicker.getMinute());
        DateTime dt = new DateTime(calendar.getTime());
        return dt;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public boolean btimeComparator(String s, String s1) //returns 1 if left op is sooner
    {
        if (Integer.parseInt(s.split(":")[0]) > Integer.parseInt(s1.split(":")[0])) return false;
        else if (Integer.parseInt(s.split(":")[0]) == Integer.parseInt(s1.split(":")[0]))
            if (Integer.parseInt(s.split(":")[1]) < Integer.parseInt(s1.split(":")[1])) return true;
            else return false;
        else return true;

    }
}