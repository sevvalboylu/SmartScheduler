package com.sabanciuniv.smartschedule.app;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

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
    class TaskItemView extends RecyclerView.ViewHolder {
        private TextView mTitle;
        private TextView mLocation;
        private TextView mImp;
        private CheckBox ck;
        private String key;

        public TaskItemView(ViewGroup parent) {
            super(LayoutInflater.from(mContext).inflate(R.layout.task_list_view, parent, false));
            mTitle = itemView.findViewById(R.id.title_text);
            mLocation = itemView.findViewById(R.id.location_text);
            mImp = itemView.findViewById(R.id.imp_text);
            ck = itemView.findViewById(R.id.checkBox);
        }

        public void bind(Task task, String key) {
            mTitle.setText(task.getTitle());
            mLocation.setText(Double.toString(task.getLocation().getLatitude())+ Double.toString(task.getLocation().getLongitude()));
            mImp.setText(task.getLvl());
            this.key = key;
        }
    }
    class TaskAdapter extends RecyclerView.Adapter<TaskItemView>{
    private List<Task> mTasklist;
    private List<String> mKeys;

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
        }

        @Override
        public int getItemCount() {
            return mTasklist.size();
        }
    }

    }