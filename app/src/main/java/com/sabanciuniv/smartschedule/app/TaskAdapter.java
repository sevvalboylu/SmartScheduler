package com.sabanciuniv.smartschedule.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.ArrayList;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskView> {

    private Context context;

    boolean checkboxPref;
    boolean onclickEnabled;
    boolean onlongclickEnabled;
    boolean reminderEnabled;


    private ArrayList<Task> taskArrayList = new ArrayList<Task>();
    public ArrayList<Task> checkedTasks = new ArrayList<>();

    public TaskAdapter(Context context, ArrayList<Task> taskList,boolean check, boolean onclick, boolean onlongclick, boolean reminderEnable) {
        this.context = context;
        this.taskArrayList = taskList;
        this.checkboxPref = check;
        this.onclickEnabled = onclick;
        this.onlongclickEnabled = onlongclick;
        this.reminderEnabled = reminderEnable;

    }

    public class TaskView extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        TextView taskTitle;
        TextView taskTime;
        TextView taskAddr;
        TextView taskImp;
        CheckBox checkbox;
        ToggleButton reminder;

        ItemLongClickListener itemLongClickListener;
        ItemClickListener itemClickListener;

        public TaskView(View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskTime = itemView.findViewById(R.id.taskTime);
            taskAddr = itemView.findViewById(R.id.taskAddress);
            taskImp = itemView.findViewById(R.id.taskImportance);
            checkbox = itemView.findViewById(R.id.checkBox2);
            reminder = itemView.findViewById(R.id.chkState);
            taskTitle.setOnLongClickListener(this);
            if (!checkboxPref)
                SetNoCheckBox();
            else
            {
                checkbox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Task row = taskArrayList.get(getAdapterPosition());
                        if (checkbox.isChecked()) {
                            checkedTasks.add(row);
                        } else {
                            checkedTasks.remove(row);
                        }
                    }
                });
            }
            if(!reminderEnabled){
                setNoReminder();
            }
            else{
                reminder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Task row = taskArrayList.get(getAdapterPosition());
                        if(reminder.isChecked()) {
                            row.setReminderEnabled(true);
                            setReminder(row, true);
                        }
                        else{
                            row.setReminderEnabled(false);
                            setReminder(row, false);
                        }
                    }
                });
            }
        }


        public void setReminder(Task t, boolean value){
            String tid = t.getTid();
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
            String userId = mAuth.getUid();
            mDatabase.child("tasks").child(userId).child(tid).child("reminderEnabled").setValue(value);
        }

        @Override
        public void onClick(View v) {
            if(onclickEnabled)
            {
                final Task row = taskArrayList.get(getAdapterPosition());
                if(checkbox.isChecked()){
                    checkedTasks.add(row);
                }
                else{
                    checkedTasks.remove(row);
                }
            }
        }

        public void setItemLongClickListener(ItemLongClickListener ic) {
            if(onlongclickEnabled)
                this.itemLongClickListener = ic;
            else
                this.itemClickListener = null;
        }

        public void setItemClickListener(ItemClickListener ic) {
            if(onclickEnabled)
                this.itemClickListener = ic;
            else
                this.itemClickListener = null;
        }

        @Override
        public boolean onLongClick(View v) {
            if (onlongclickEnabled) {
                Bundle extras;
                extras = new Bundle();
                Gson gson = new Gson();
                String json = gson.toJson(taskArrayList.get(getAdapterPosition()));
                extras.putString("clickedEvent", json);

                Intent in = new Intent(context, EditTask.class);
                in.putExtras(extras);
                context.startActivity(in);
                return true;
            }
            else
                return false;
        }

        //method to remove checkboxes.
        public void SetNoCheckBox() {
            checkbox.setVisibility(itemView.GONE);
        }

        public void setNoReminder(){
            reminder.setVisibility(itemView.GONE);
        }
    }


    @NonNull
    @Override
    public TaskView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_schedule, parent, false);
        return new TaskView(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskView holder, int position) {
        final Task row = taskArrayList.get(holder.getAdapterPosition());

        holder.taskTitle.setText(row.getTitle());
        holder.taskAddr.setText(row.getLocation().getAddress());
        holder.taskImp.setText(row.switchLevelToString());
        if(row.getLvl().equals("1"))
            holder.taskImp.setTextColor(Color.rgb(58,148,1));
        else if(row.getLvl().equals("2"))
            holder.taskImp.setTextColor(Color.rgb(254,172,0));
        else
            holder.taskImp.setTextColor(Color.rgb(182,1,59));

        if(row.getStartTime() != null){
            holder.taskTime.setText(""+ row.getStartHour()+":"+row.getStartMinute() + " - "
                    + row.getEndHour() + ":" + row. getEndMinute());
        }
        else
            holder.taskTime.setText(row.getDuration() +"mins");

        if(row.isReminderEnabled()){
            holder.reminder.setChecked(true);
        }
        else {
            holder.reminder.setChecked(false);
        }
    }

    @Override
    public int getItemCount() {
        return taskArrayList.size();
    }

}
