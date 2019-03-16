package com.sabanciuniv.smartschedule.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class RecyclerView_Config_AllTasks {
    private Context mContext;
    private RecyclerView_Config_AllTasks.TaskAdapter mTaskAdapter;
    public ArrayList<Task> checkedTasks = new ArrayList<Task>();

    public void setConfig(RecyclerView recyclerView, Context context, List<Task> tasks, List<String> keys)
    {
        mContext=context;
        mTaskAdapter = new RecyclerView_Config_AllTasks.TaskAdapter(tasks,keys);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(mTaskAdapter);
    }
    class TaskItemView extends RecyclerView.ViewHolder implements View.OnLongClickListener{
        private TextView mTitle;
        private TextView mLocation;
        private TextView mImp;
        private String key;

        ItemClickListener itemClickListener;
        ItemLongClickListener itemLongClickListener;

        public TaskItemView(ViewGroup parent) {
            super(LayoutInflater.from(mContext).inflate(R.layout.task_list_view_nock, parent, false));
            mTitle = itemView.findViewById(R.id.title_text);
            mLocation = itemView.findViewById(R.id.location_text);
            mImp = itemView.findViewById(R.id.imp_text);
            mTitle.setOnLongClickListener(this);
        }

        public void bind(Task task, String key) {
            mTitle.setText(task.getTitle());

            mImp.setText(task.getLvl());

            mLocation.setText(task.getLocation().getAddress());

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

        public void setItemLongClickListener(ItemLongClickListener ic) {
            this.itemLongClickListener = ic;
        }

        @Override
        public boolean onLongClick(View v) {
            this.itemLongClickListener.onItemLongClick(v, getLayoutPosition());
            return true;
        }
    }

    class TaskAdapter extends RecyclerView.Adapter<RecyclerView_Config_AllTasks.TaskItemView>{
        private List<Task> mTasklist;
        private List<String> mKeys;

        public TaskAdapter(List<Task> mTasklist, List<String> mKeys) {
            this.mTasklist = mTasklist;
            this.mKeys = mKeys;
        }

        @NonNull
        @Override
        public RecyclerView_Config_AllTasks.TaskItemView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new RecyclerView_Config_AllTasks.TaskItemView(parent);
        }

        @Override
        public void onBindViewHolder(@NonNull TaskItemView holder, int position) {
            holder.bind(mTasklist.get(position),mKeys.get(position));
            holder.setItemLongClickListener(new ItemLongClickListener() {
                @Override
                public void onItemLongClick(View v, int pos) {
                    Bundle extras;
                    extras = new Bundle();
                    Gson gson = new Gson();
                    String json = gson.toJson(mTasklist.get(pos));
                    extras.putString("clickedEvent", json);

                    Intent in = new Intent(mContext, EditTask.class);
                    in.putExtras(extras);
                    mContext.startActivity(in);
                }
            });
        }


        @Override
        public int getItemCount() {
            return mTasklist.size();
        }
    }

}


