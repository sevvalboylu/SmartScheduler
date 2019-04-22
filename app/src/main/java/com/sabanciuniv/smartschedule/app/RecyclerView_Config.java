package com.sabanciuniv.smartschedule.app;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

public class RecyclerView_Config {
    private Context mContext;
    private TaskAdapter mTaskAdapter;
    public ArrayList<Task> checkedTasks = new ArrayList<Task>();

    public void setConfig(RecyclerView recyclerView, Context context, ArrayList<Task> tasks) {
        mContext = context;
        mTaskAdapter = new TaskAdapter(tasks, true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(mTaskAdapter);
    }

}