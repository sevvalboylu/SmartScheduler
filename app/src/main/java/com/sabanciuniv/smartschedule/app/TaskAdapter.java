package com.sabanciuniv.smartschedule.app;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskView> {

    private ArrayList<Task> taskArrayList;

    public TaskAdapter(ArrayList<Task> taskArrayList) {
        this.taskArrayList = taskArrayList;
    }

    public class TaskView extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView taskTitle;
        TextView taskTime;
        TextView taskAddr;
        TextView taskImp;

        public TaskView(View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskTime = itemView.findViewById(R.id.taskTime);
            taskAddr = itemView.findViewById(R.id.taskAddress);
            taskImp = itemView.findViewById(R.id.taskImportance);
        }


        @Override
        public void onClick(View view) {

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
        holder.taskImp.setText(row.getLvl());
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
            holder.taskTime.setText(" ");
    }

    @Override
    public int getItemCount() {
        return taskArrayList.size();
    }

}
