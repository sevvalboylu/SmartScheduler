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

import com.google.gson.Gson;

import java.util.ArrayList;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskView> {

    private Context context;

    boolean checkboxPref;
    boolean onclickEnabled;
    boolean onlongclickEnabled;


    private ArrayList<Task> taskArrayList = new ArrayList<Task>();
    public ArrayList<Task> checkedTasks = new ArrayList<Task>();

    public TaskAdapter(Context context, ArrayList<Task> taskList,boolean check, boolean onclick, boolean onlongclick) {
        this.context = context;
        this.taskArrayList = taskList;
        this.checkboxPref = check;
        this.onclickEnabled = onclick;
        this.onlongclickEnabled = onlongclick;

    }

    public class TaskView extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        TextView taskTitle;
        TextView taskTime;
        TextView taskAddr;
        TextView taskImp;
        CheckBox checkbox;

        ItemLongClickListener itemLongClickListener;
        ItemClickListener itemClickListener;

        public TaskView(View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskTime = itemView.findViewById(R.id.taskTime);
            taskAddr = itemView.findViewById(R.id.taskAddress);
            taskImp = itemView.findViewById(R.id.taskImportance);
            checkbox = itemView.findViewById(R.id.checkBox2);
            taskTitle.setOnLongClickListener(this);
            if(!checkboxPref)
                SetNoCheckBox();
            else
                checkbox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Task row = taskArrayList.get(getAdapterPosition());
                        if(checkbox.isChecked()){
                            checkedTasks.add(row);
                        }
                        else{
                            checkedTasks.remove(row);
                        }
                    }
                });
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
        //holder.taskImp.setText(row.getLvl());
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
    }



    @Override
    public int getItemCount() {
        return taskArrayList.size();
    }

}
