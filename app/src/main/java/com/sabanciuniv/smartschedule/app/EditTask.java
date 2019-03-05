package com.sabanciuniv.smartschedule.app;

import android.content.Intent;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;


public class EditTask extends AppCompatActivity {

    private static final String TAG = "AddTask";
    private static final String REQUIRED = "Required";
    private DatabaseReference mDatabase;
    private EditText mTitleField;
    private TextView mLocationField;
    private Spinner spinner1, freqLocationSpinner;
    private Button mSubmitButton;
    private Button mDeleteButton;
    private Switch mAllDaySwitch;
    private DatePicker mStartDatePicker, mEndDatePicker;
    private TimePicker mStartTimePicker, mEndTimePicker;
    private String lvl, date_s,date_e;
    private TextView mStartDateText, mEndDateText, mStartTimeText, mEndTimeText;
    //private static final Point location = new Point(41.0082, 28.9784); //should not be static, change later
    private final String location = new String();
    private FirebaseAuth mAuth;
    private int startDateTextClickCount = 0, endDateTextClickCount = 0, startTimeTextClickCount = 0, endTimeTextClickCount = 0;

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
        final String location = mLocationField.getText().toString();

        Task task = new com.sabanciuniv.smartschedule.app.Task(userId, tid, lvl, title, location);
        mDatabase.child("tasks").child(mAuth.getCurrentUser().getUid()).child(tid).setValue(task);

    }
    private void deleteTask(String tid) {
        Toast.makeText(this, "Deleting the task...", Toast.LENGTH_SHORT).show();
        mDatabase.child("tasks").child(mAuth.getCurrentUser().getUid()).child(tid).removeValue();

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
