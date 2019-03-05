package com.sabanciuniv.smartschedule.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;


class RecyclerView_Loc {
    private Context mContext;
    private LocAdapter mLocAdapter;

    public void setConfig(RecyclerView recyclerView, Context context, List<Location> locations)
    {
        mContext=context;
        mLocAdapter = new LocAdapter(locations);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(mLocAdapter);
    }
    class LocView extends RecyclerView.ViewHolder{
        private TextView mTitle;
        private TextView mAddress;
        private String key;


        public LocView(ViewGroup parent) {
            super(LayoutInflater.from(mContext).inflate(R.layout.task_list_view, parent, false));
            mTitle = itemView.findViewById(R.id.title_text);
            mAddress = itemView.findViewById(R.id.location_text);
        }

        public void bind(Location loc) {
            mTitle.setText(loc.getTitle());
            mAddress.setText(loc.getAddress());
        }
    }
    class LocAdapter extends RecyclerView.Adapter<LocView>{
        private List<Location> mLoclist;

        public LocAdapter(List<Location> mLoclist) {
            this.mLoclist = mLoclist;
        }

        @NonNull
        @Override
        public LocView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new LocView(parent);
        }

        @Override
        public void onBindViewHolder(@NonNull LocView holder, int position) {
            holder.bind(mLoclist.get(position));
        }

        @Override
        public int getItemCount() {
            return mLoclist.size();
        }
    }

}

class Location extends Task.Location{
    private String title;
    public String getTitle(){return title;}
        }

public class Profile extends AppCompatActivity {

    private ArrayList<Location> mLocations;
    private ArrayList<String> titles = new ArrayList<String>();
    private RecyclerView mRecyclerView = findViewById(R.id.recyclerview_loclist);

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences s = getSharedPreferences("locations", MODE_PRIVATE);
        int readId = 1;
        while (s.contains("location" + readId)) {
            Gson gson = new Gson();
            String json = s.getString("location" + readId++, "");
            mLocations.add(gson.fromJson(json, Location.class));
        }

        if (user != null) {
            // Name, email address, and profile photo Url
            String name = user.getDisplayName();
            TextView tv1 = (TextView)findViewById(R.id.nameText);
            TextView tv2 = (TextView)findViewById(R.id.nameText);
            tv1.setText(name);
            String email = user.getEmail();
            tv2.setText(email);
            RecyclerView_Loc config = new RecyclerView_Loc();
            config.setConfig(mRecyclerView, Profile.this, mLocations);
        }
    }

    public void goToMap(View view)
    {
        Intent intent = new Intent(Profile.this, MapViewActivity.class);
        startActivity(intent); //todo:sevval can you return the selected address?
    }
}
