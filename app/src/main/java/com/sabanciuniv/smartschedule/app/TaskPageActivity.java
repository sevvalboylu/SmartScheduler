package com.sabanciuniv.smartschedule.app;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

public class TaskPageActivity extends AppCompatActivity {
    EditTaskPageViewModel viewModel;
    private EditText mTitleField, mDurationText;
    private TextView mLocationField;
    private Spinner spinner1, freqLocationSpinner;
    private Switch mAllDaySwitch;
    private DatePicker mStartDatePicker, mEndDatePicker;
    private TimePicker mStartTimePicker, mEndTimePicker;
    private String lvl, date_n;
    private TextView mStartDateText, mEndDateText, mStartTimeText, mEndTimeText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        viewModel = ViewModelProviders.of(this).get(EditTaskPageViewModel.class);

        attachViews();

        if(savedInstanceState != null){
            viewModel.readFromBundle(savedInstanceState);
        }else{
            /*viewModel.setPriority(getIntent().getIntExtra(MainActivity.PRIORITY_KEY, 0));
            viewModel.setTaskDesc(getIntent().getStringExtra(MainActivity.EVENT_DESC_KEY));
            viewModel.setTaskName(getIntent().getStringExtra(MainActivity.EVENT_NAME_KEY));
            Date dateObj = new Date(getIntent().getLongExtra(MainActivity.DATE_KEY, 0));
            viewModel.setDate(dateObj);*/
        }


    }

    public void attachViews(){
        mStartDateText = findViewById(R.id.startDateText);
        mEndDateText = findViewById(R.id.endDateText);
        mStartTimeText = findViewById(R.id.startTimeText);
        mEndTimeText = findViewById(R.id.endTimeText);
        mTitleField = findViewById(R.id.taskTitleText);
        mLocationField = findViewById(R.id.address);
        mAllDaySwitch = findViewById(R.id.allDaySwitch);
        mStartDatePicker = findViewById(R.id.datePicker1);
        mEndDatePicker = findViewById(R.id.datePicker2);
        mStartTimePicker = findViewById(R.id.timePicker1);
        mEndTimePicker = findViewById(R.id.timePicker2);
        mDurationText = findViewById(R.id.durationText);
    }

}
