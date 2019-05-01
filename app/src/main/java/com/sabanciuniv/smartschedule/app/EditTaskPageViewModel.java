package com.sabanciuniv.smartschedule.app;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class EditTaskPageViewModel extends ViewModel {

    private static final DatabaseReference HOT_STOCK_REF =
            FirebaseDatabase.getInstance().getReference();

    //private final FirebaseLiveData liveData = new FirebaseLiveData(HOT_STOCK_REF);

    private MutableLiveData<String> taskLevel;
    private MutableLiveData<String> taskName;
    private MutableLiveData<Task.Location> taskLoc;
    private MutableLiveData<String> taskStartDate;
    private MutableLiveData<String> taskEndDate;

    public EditTaskPageViewModel(){
        taskLevel = new MutableLiveData<>();
        taskName = new MutableLiveData<>();
        taskStartDate = new MutableLiveData<>();
        taskEndDate = new MutableLiveData<>();
        taskLoc = new MutableLiveData<>();
        taskLevel.setValue("");
        taskName.setValue("");
        taskEndDate.setValue("");
        taskStartDate.setValue("");
        //taskLoc.setValue("");

    }

    /*public LiveData<DataSnapshot> getDataSnapshotLiveData() {
        return liveData;
    }*/


    public void readFromBundle(Bundle savedInstanceState) {

    }
}
