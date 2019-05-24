package com.sabanciuniv.smartschedule.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class EditTask extends AppCompatActivity {

    private static final String TAG = "AddTask";
    private static final String REQUIRED = "Required";
    private DatabaseReference mDatabase;
    private EditText mTitleField, mDurationText;
    private TextView mLocationField;
    private Spinner spinner1;
    private FloatingActionButton mSubmitButton;
    private FloatingActionButton mDeleteButton;

    private Switch mAllDaySwitch;
    private DatePicker mStartDatePicker, mEndDatePicker;
    private TimePicker mStartTimePicker, mEndTimePicker;
    private String lvl, date_s, date_e;
    private TextView mStartDateText, mEndDateText, mStartTimeText, mEndTimeText;
    private ArrayList<Profile.Location> locarr = new ArrayList<>();
    //private static final Point location = new Point(41.0082, 28.9784); //should not be static, change later
    private final String location = new String();
    private FirebaseAuth mAuth;
    private int startDateTextClickCount = 0, endDateTextClickCount = 0, startTimeTextClickCount = 0, endTimeTextClickCount = 0;
    double longitude, latitude;
    private int locpos = -1;
    private boolean reminderEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Bundle extras = getIntent().getExtras();
        String json;
        Task edit = null;
        if (extras != null) {
            json = extras.getString("clickedEvent");
            Gson gson = new Gson();
            edit = (gson.fromJson(json, Task.class));
        }

        DateFormat df = new SimpleDateFormat("hh:mm");
        setContentView(R.layout.activity_add_task);

        mStartTimePicker = findViewById(R.id.timePicker1);
        mStartDatePicker = findViewById(R.id.datePicker1);

        mEndDatePicker = findViewById(R.id.datePicker2);
        mEndTimePicker = findViewById(R.id.timePicker2);
        mEndDateText = findViewById(R.id.endDateText);
        mEndDateText.setVisibility(View.VISIBLE);
        mStartDateText = findViewById(R.id.startDateText);
        mStartDateText.setVisibility(View.VISIBLE);
        mEndTimeText = findViewById(R.id.endTimeText);
        mStartTimeText = findViewById(R.id.startTimeText);
        mAllDaySwitch = findViewById(R.id.allDaySwitch);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        //set text view of end
        if (edit.getEndTime() != null) {
            int year = Integer.parseInt(edit.getEndTime().split("T")[0].split("-")[0]);
            int month = Integer.parseInt(edit.getEndTime().split("T")[0].split("-")[1])-1;
            int day = Integer.parseInt(edit.getEndTime().split("T")[0].split("-")[2]);
            Date e = new GregorianCalendar(
                    year,
                    month,
                    day).getTime();
            date_e = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(e);

            mEndDateText.setText(date_e);

            String et1 = edit.getEndTime().split("T")[1].split(":")[0];
            String et2 = edit.getEndTime().split("T")[1].split(":")[1];
            String et = et1 + ":" + et2;
            Date etd = null;
            try {
                etd = df.parse(et);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
            String date_str2 = df.format(etd);
            mEndTimeText.setText(date_str2);
            mEndTimePicker.setHour(Integer.parseInt(et1));
            mEndTimePicker.setMinute(Integer.parseInt(et2));

            mEndDatePicker.updateDate(year, month, day);

        }
        //set text view of start
        if (edit.getStartTime() != null) {
            int year = Integer.parseInt(edit.getStartTime().split("T")[0].split("-")[0]);
            int month = Integer.parseInt(edit.getStartTime().split("T")[0].split("-")[1])-1;
            int day = Integer.parseInt(edit.getStartTime().split("T")[0].split("-")[2]);
            Date s = new GregorianCalendar(
                    year, month, day).getTime();
            date_s = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(s);

            mStartDateText.setText(date_s);
            Calendar cal = Calendar.getInstance();
            String st1 = edit.getStartTime().split("T")[1].split(":")[0];
            String st2 = edit.getStartTime().split("T")[1].split(":")[1];
            String st = st1 + ":" + st2;

            Date std = null;
            try {
                std = df.parse(st);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }

            String date_str = df.format(std);
            mStartTimeText.setText(date_str);
            cal.add(Calendar.HOUR_OF_DAY, 1);

            mStartTimePicker.setHour(Integer.parseInt(st1));
            mStartTimePicker.setMinute(Integer.parseInt(st2));

            mStartDatePicker.init(year, month, day, null);
        }
        else {
          mAllDaySwitch.setChecked(true);

        }
        mEndTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int i, int i1) {
                int minute = timePicker.getMinute();
                int hour = timePicker.getHour();
                String et_upd = hour + ":" + minute;
                Date eee = null;
                try {
                    eee = df.parse(et_upd);
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
                String endTimeInp = df.format(eee);
                mEndTimeText.setText(endTimeInp);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mEndDatePicker.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
                @Override
                public void onDateChanged(DatePicker datePicker, int i, int i1, int i2) {
                    int year_dp = datePicker.getYear();
                    int month_dp = datePicker.getMonth();
                    int day_dp = datePicker.getDayOfMonth();
                    Date e_dp = new GregorianCalendar(
                            year_dp, month_dp, day_dp).getTime();
                    String date_e_dp = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(e_dp);
                    mEndDateText.setText(date_e_dp);
                }
            });
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mStartDatePicker.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
                @Override
                public void onDateChanged(DatePicker datePicker, int i, int i1, int i2) {
                    int year_dp = datePicker.getYear();
                    int month_dp = datePicker.getMonth();
                    int day_dp = datePicker.getDayOfMonth();
                    Date s_dp = new GregorianCalendar(
                            year_dp, month_dp, day_dp).getTime();
                    String date_s_dp = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(s_dp);
                    mStartDateText.setText(date_s_dp);
                }
            });
        }
        mStartTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int i, int i1) {
                int minute = timePicker.getMinute();
                int hour = timePicker.getHour();
                String et_upd = hour + ":" + minute;
                Date eee = null;
                try {
                    eee = df.parse(et_upd);
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
                String endTimeInp = df.format(eee);
                mStartTimeText.setText(endTimeInp);
            }
        });

        mTitleField = findViewById(R.id.taskTitleText);
        mTitleField.setText(edit.getTitle());
        mLocationField = findViewById(R.id.address);
        mLocationField.setText(edit.getLocation().getAddress());
        mSubmitButton = findViewById(R.id.floatingActionButton5);
        mDeleteButton = findViewById(R.id.floatingActionButton6);
        mDeleteButton.setVisibility(View.VISIBLE);

        mDurationText = findViewById(R.id.durationText);

        mStartDatePicker.setVisibility(View.GONE);
        mEndDatePicker.setVisibility(View.GONE);
        mStartTimePicker.setVisibility(View.GONE);
        mEndTimePicker.setVisibility(View.GONE);

        Integer dur = edit.getDuration();
        final String duration = dur.toString();

        mAllDaySwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAllDaySwitch.isChecked()) {
                    mDurationText.setText(duration);
                    mDurationText.setEnabled(true);
                    mStartTimeText.setVisibility(View.GONE);
                    mEndTimeText.setVisibility(View.GONE);

                    mEndDateText.setVisibility(View.GONE);
                    mStartDateText.setVisibility(View.GONE);
                    //mEndDateText.setText("");
                    //mStartDateText.setText("");
                }
                else if (!mAllDaySwitch.isChecked()) {
                    mDurationText.setEnabled(false);

                    date_e = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());
                    mEndDateText.setText(date_e);
                    date_s = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());
                    mStartDateText.setText(date_s);

                    Calendar calendar = Calendar.getInstance();
                    calendar.set(mEndDatePicker.getYear(), mEndDatePicker.getMonth(), mEndDatePicker.getDayOfMonth(),
                            mEndTimePicker.getHour(), mEndTimePicker.getMinute());
                    DateFormat df = new SimpleDateFormat("hh:mm");
                    mStartTimePicker.setHour(Calendar.HOUR_OF_DAY);
                    mStartTimePicker.setMinute(Calendar.MINUTE);

                    String date_str = df.format(calendar.getTime());
                    mStartTimeText.setText(date_str);
                    mEndTimePicker.setHour(Calendar.HOUR_OF_DAY+1);
                    mEndTimePicker.setMinute(Calendar.MINUTE);

                    calendar.set(mEndDatePicker.getYear(), mEndDatePicker.getMonth(), mEndDatePicker.getDayOfMonth(),
                            mEndTimePicker.getHour(), mEndTimePicker.getMinute());
                    String date_e = df.format(calendar.getTime());
                    mEndTimeText.setText(date_e);

                    mStartTimeText.setVisibility(View.VISIBLE);
                    mEndTimeText.setVisibility(View.VISIBLE);


                    mEndDateText.setVisibility(View.VISIBLE);
                    mStartDateText.setVisibility(View.VISIBLE);
                }
            }
        });

        latitude = edit.getLocation().getCoordinate().getLatitude();
        longitude = edit.getLocation().getCoordinate().getLongitude();

        //get the spinner from the xml.
        final Task finalEdit = edit;
        Spinner dropdown = findViewById(R.id.importanceSpinner);
        String[] items = new String[]{"Low", "Medium", "High"};

        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        //set the spinners adapter to the previously created one.
        dropdown.setAdapter(adapter);
        dropdown.setSelection(Integer.valueOf(Integer.parseInt(finalEdit.getLvl()) - 1));
        addListenerOnSpinnerItemSelection(dropdown);
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
                    ArrayAdapter ladapter = new ArrayAdapter<>(EditTask.this, android.R.layout.simple_spinner_dropdown_item, locs[0]);
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
                String js = entry.getValue().toString();
                locarr.add(gson.fromJson(js, Profile.Location.class));
                locs[0].add(gson.fromJson(js, Profile.Location.class).getTitle());
            }
            Spinner loc = findViewById(R.id.freqLocationSpinner);
            ArrayAdapter<String> ladapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, locs[0]);
            loc.setAdapter(ladapter);
        }


        //create a list of items for the spinner.
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTask(finalEdit.getTid(), "Editing the task..."); //change task
            }
        });

        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTask(finalEdit.getTid(), "Deleting the task..."); //change task
            }
        });
    }

    public void onStart() {
        super.onStart();
    }

    public void onStop() {
        super.onStop();
    }

    public static boolean isNullOrEmpty(String str) {
        if (str != null && !str.trim().isEmpty())
            return false;
        return true;
    }

    private void editTask(String tid, String message) {
        final String title = mTitleField.getText().toString();

        // Title is required
        if (isNullOrEmpty(title)) {
            mTitleField.setError(REQUIRED);
            return;
        }

        // Disable button so there are no multi-posts
        //setEditingEnabled(false);9
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        // [START single_value_read]
        final String userId = getUid();
        final String address = mLocationField.getText().toString();
        Point pnt = new Point(latitude, longitude);
        final Task.Location location;
        //final Task.Location location = new Task.Location(address,pnt);
        if (locpos == -1)
            location = new Task.Location(address, pnt);
        else
            location = new Task.Location(locarr.get(locpos).getAddress(), locarr.get(locpos).getCoordinate());

        mDatabase.child("tasks").child(userId).child(tid).removeValue();

        Calendar c = Calendar.getInstance();
        c.set(mStartDatePicker.getYear(), mStartDatePicker.getMonth(), mStartDatePicker.getDayOfMonth(),
                mStartTimePicker.getHour(), mStartTimePicker.getMinute());
        final DateTime s = new DateTime(c.getTime());
        c.set(mEndDatePicker.getYear(), mEndDatePicker.getMonth(), mEndDatePicker.getDayOfMonth(),
                mEndTimePicker.getHour(), mEndTimePicker.getMinute());
        final DateTime e = new DateTime(c.getTime());

        Task task = null;


        if (!mAllDaySwitch.isChecked()) {
            if( btimeComparator(mStartTimePicker.getHour() + ":" +mStartTimePicker.getMinute() , mEndTimePicker.getHour() + ":" +mEndTimePicker.getMinute()))
                task = new com.sabanciuniv.smartschedule.app.Task(userId, tid, title, location, getDuration(s.toString(), e.toString()), lvl, s.toString(), e.toString(), reminderEnabled);
            else
            { Toast.makeText(this,"Start and end times are not consistent!",Toast.LENGTH_LONG).show();
                return;}} else {
            String dur = mDurationText.getText().toString();
            if (isNullOrEmpty(dur)) {
                mDurationText.setError(REQUIRED);
                return;
            }
            else
                task = new com.sabanciuniv.smartschedule.app.Task(userId, tid, lvl, Integer.parseInt(dur), title, location, reminderEnabled);
        }

        deleteTask(tid, message);
        mDatabase.child("tasks").child(userId).child(tid).setValue(task);

        //return to previous actiivty page
        Intent intent = new Intent(EditTask.this, AllTasks.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivityForResult(intent, 1);
    }

    private void deleteTask(String tid, String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        mDatabase.child("tasks").child(mAuth.getCurrentUser().getUid()).child(tid).removeValue();
        final SharedPreferences.Editor editor = getSharedPreferences("tasks", MODE_PRIVATE).edit();
        editor.remove(tid);
        editor.apply();

        //return to previous actiivty page
        Intent intent = new Intent(EditTask.this, AllTasks.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivityForResult(intent, 1);
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

    public void addListenerOnSpinnerItemSelection(Spinner spin) {
        spin.setOnItemSelectedListener(new CustomOnItemSelectedListener() {
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
        Intent intent = new Intent(EditTask.this, MapViewActivity.class);
        intent.putExtra("caller", "EditTask.class"); //please don't delete
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
        if (startDateTextClickCount % 2 == 1) {
            mStartDatePicker.setVisibility(View.VISIBLE);
        } else {
            mStartDatePicker.setVisibility(View.GONE);
        }
    }

    public void onEndDateTextClick(View view) {
        endDateTextClickCount++;
        if (endDateTextClickCount % 2 == 1) {
            mEndDatePicker.setVisibility(View.VISIBLE);
        } else {
            mEndDatePicker.setVisibility(View.GONE);
        }
    }

    public void onStartTimeTextClick(View view) {
        startTimeTextClickCount++;
        if (startTimeTextClickCount % 2 == 1) {
            mStartTimePicker.setVisibility(View.VISIBLE);
        } else {
            mStartTimePicker.setVisibility(View.GONE);
        }
    }
    public boolean btimeComparator(String s, String s1) //returns 1 if left op is sooner
    {
        if (Integer.parseInt(s.split(":")[0]) > Integer.parseInt(s1.split(":")[0])) return false;
        else if (Integer.parseInt(s.split(":")[0]) == Integer.parseInt(s1.split(":")[0]))
            if (Integer.parseInt(s.split(":")[1]) < Integer.parseInt(s1.split(":")[1])) return true;
            else return false;
        else return true;

    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
        public void onEndTimeTextClick(View view) {
        endTimeTextClickCount++;
        if (endTimeTextClickCount % 2 == 1) {
            mEndTimePicker.setVisibility(View.VISIBLE);
        } else {
            mEndTimePicker.setVisibility(View.GONE);
        }
    }
}
