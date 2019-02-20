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

    public void setConfig(RecyclerView recyclerView,Context context, List<Task> tasks, List<String> keys)
    {
        mContext=context;
        mTaskAdapter = new TaskAdapter(tasks,keys);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(mTaskAdapter);
    }
    class TaskItemView extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView mTitle;
        private TextView mLocation;
        private TextView mImp;
        private CheckBox ck;
        private String key;

        ItemClickListener itemClickListener;


        public TaskItemView(ViewGroup parent) {
            super(LayoutInflater.from(mContext).inflate(R.layout.task_list_view, parent, false));
            mTitle = itemView.findViewById(R.id.title_text);
            mLocation = itemView.findViewById(R.id.location_text);
            mImp = itemView.findViewById(R.id.imp_text);
            ck = itemView.findViewById(R.id.checkBox);
            ck.setOnClickListener(this);
        }

        public void bind(Task task, String key) {
            mTitle.setText(task.getTitle());

            mImp.setText(task.getLvl());

            mLocation.setText(task.getLocation());

            if("3".equals(task.getLvl()))
            {
                mImp.setTextColor(Color.rgb(182,1,59));
            }
            else if("2".equals(task.getLvl()))
            {
                mImp.setTextColor(Color.rgb(254,172,0));
            }
            else
                mImp.setTextColor(Color.rgb(58,148,1));
            this.key = key;
        }

        public void  setItemClickListener(ItemClickListener ic){
          this.itemClickListener = ic;
        }
        @Override
        public void onClick(View v) {
            this.itemClickListener.onItemClick(v,getLayoutPosition());

        }
    }
    class TaskAdapter extends RecyclerView.Adapter<TaskItemView>{
    private List<Task> mTasklist;
    private List<String> mKeys;
    private ArrayList<Task> checkedTasks = new ArrayList<Task>();

        public TaskAdapter(List<Task> mTasklist, List<String> mKeys) {
            this.mTasklist = mTasklist;
            this.mKeys = mKeys;
        }

        @NonNull
        @Override
        public TaskItemView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new TaskItemView(parent);
        }

        @Override
        public void onBindViewHolder(@NonNull TaskItemView holder, int position) {
           holder.bind(mTasklist.get(position),mKeys.get(position));
           holder.setItemClickListener(new ItemClickListener() {
               @Override
               public void onItemClick(View v, int pos) {
                   CheckBox chk = (CheckBox) v;
                   if(chk.isChecked()){
                       checkedTasks.add(mTasklist.get(pos));
                   }
                   else{
                       checkedTasks.remove(mTasklist.get(pos));
                   }
               }
           });
        }

        @Override
        public int getItemCount() {
            return mTasklist.size();
        }
    }

    }