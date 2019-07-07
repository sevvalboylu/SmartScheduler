package com.sabanciuniv.smartschedule.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.yandex.mapkit.geometry.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Profile extends AppCompatActivity {


    private static final String TAG = "";

    public static class LocLoader {
        private ArrayList<Profile.Location> locs = new ArrayList<>();

        public LocLoader(final DataStatus dataStatus, String uid) {
            DatabaseReference database = FirebaseDatabase.getInstance().getReference();
            DatabaseReference ref = database.child("addresses").child(uid);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    locs.clear();
                    if (dataSnapshot.exists()) {
                        List<String> keys = new ArrayList<>();
                        for (DataSnapshot keyNode : dataSnapshot.getChildren()) {
                            keys.add(keyNode.getKey());
                            Profile.Location temp = keyNode.getValue(com.sabanciuniv.smartschedule.app.Profile.Location.class);
                            locs.add(temp);
                        }
                        List<Objects> s = new ArrayList<Objects>();
                        dataStatus.LocsLoaded(locs, keys);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }

            });

        }

    }
    private ArrayList<Location> mLocations = new ArrayList<Location>();
    private SharedPreferences s, userdata;
    private RecyclerView mRecyclerView;
    private TextView nameText;
    private String adr = "";
    private Button mDeleteButton;
    private RecyclerView_Loc config = new RecyclerView_Loc();
    private ImageView profilepic;
    private ImageView editbtn;
    private ImageView checkBtn;
    private ImageView cancelBtn;
    private EditText editname;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    private double lon, lat;

    public void editName(View view) {
        editbtn.setVisibility(View.GONE);
        editname.setVisibility(View.VISIBLE);
        nameText.setVisibility(View.GONE);
        checkBtn.setVisibility(View.VISIBLE);
        cancelBtn.setVisibility(View.VISIBLE);
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
    public void checkName(View view) {
        String name = editname.getText().toString();
        if(name.equals(""))
            Toast.makeText(this,"Cannot be blank",Toast.LENGTH_SHORT).show();
        else
        {
            SharedPreferences.Editor editor = userdata.edit();
            editor.putString("userName", name);
            editor.apply();
            nameText.setVisibility(View.VISIBLE);
            nameText.setText(name);
            editbtn.setVisibility(View.VISIBLE);
            editname.setVisibility(View.GONE);
            checkBtn.setVisibility(View.GONE);
            cancelBtn.setVisibility(View.GONE);
        }
    }

    public void cancelName(View view) {
        nameText.setVisibility(View.VISIBLE);
        editbtn.setVisibility(View.VISIBLE);
        editname.setVisibility(View.GONE);
        checkBtn.setVisibility(View.GONE);
        cancelBtn.setVisibility(View.GONE);
    }

    protected static class Location extends Task.Location {
        private String title;

        public Location (){}
        public String getTitle() {
            return title;
        }

        Location(String address, String title, Double lon, Double lat) {
            this.title = title;
            this.address = address;
            this.coordinate = new Point(lat, lon);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        s = this.getSharedPreferences("locations", MODE_PRIVATE);
        userdata = this.getSharedPreferences("userdata", MODE_PRIVATE);

        setContentView(R.layout.activity_profile);
        profilepic = findViewById(R.id.profilepicture);
        profilepic.setImageResource(R.mipmap.ic_launcher);

        ////////////
        editbtn = findViewById(R.id.editnameBtn);
        editname = findViewById(R.id.editName);
        nameText = findViewById(R.id.nameText);
        checkBtn = findViewById(R.id.imgCheck);
        checkBtn.setVisibility(View.GONE);
        cancelBtn = findViewById(R.id.imgCancel);
        ////////////

        if(!isNetworkConnected()) {
            SharedPreferences prefs = getSharedPreferences("addresses", MODE_PRIVATE);
            int readId = 1;
            while (prefs.contains("address" + readId)) {
                Gson gson = new Gson();
                String json = prefs.getString("address" + readId++, "");
                mLocations.add(gson.fromJson(json, Profile.Location.class));
            }
        } else {
            final SharedPreferences.Editor editor = getSharedPreferences("addresses", MODE_PRIVATE).edit();
            LocLoader tl = new LocLoader(new DataStatus() {
                @Override
                public void TasksLoaded(List<Task> tasks, List<String> keys) {

                }

                @Override
                public void LocsLoaded(ArrayList<Location> locs, List<String> keys) {
                    mLocations = locs;
                    int writeId = 1;
                    for (Profile.Location t : mLocations) {
                        Gson gson = new Gson();
                        String json = gson.toJson(t);
                        editor.putString(t.title, json);
                    }
                    writeId = 1;
                    for (String k : keys) {
                        editor.putString("key" + writeId++, k);
                    }
                    editor.apply();
                    config.setConfig(mRecyclerView, Profile.this, mLocations);
                }
            }, FirebaseAuth.getInstance().getUid());
        }



        //////////
        String name = userdata.getString("userName","Your Name");
        nameText.setText(name);

        mRecyclerView = findViewById(R.id.recyclerview_loclist);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final Map<String, ?> keys = s.getAll();
        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            Gson gson = new Gson();
            String json = s.getString(entry.getKey(), "");
            mLocations.add(gson.fromJson(json, Location.class));
        }
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        final SharedPreferences pref = getSharedPreferences("addresses",MODE_PRIVATE);
        final SharedPreferences.Editor sedit = getSharedPreferences("locations", MODE_PRIVATE).edit();
        mDeleteButton = findViewById(R.id.button3);
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Map.Entry<String, Boolean> entry : config.checkedlocs.entrySet()) {
                    if(entry.getValue()){
                        sedit.remove(entry.getKey());
                        Query removeQuery = ref.child("addresses").child(FirebaseAuth.getInstance().getUid()).child(entry.getKey());
                        removeQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot locSnapshot : dataSnapshot.getChildren()) {
                                  locSnapshot.getRef().removeValue();
                                }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                            }

                    });
                sedit.apply();
                Intent myIntent = new Intent(Profile.this, Profile.class);
                Profile.this.startActivity(myIntent);
            }
                }
            }
        });

        if (user != null) {
            // Name, email address, and profile photo Url
            name = user.getDisplayName();
            TextView tv1 = findViewById(R.id.nameText);
            TextView tv2 = findViewById(R.id.emailText);
            if(name != null)
                tv1.setText(name);
            String email = user.getEmail();
            tv2.setText(email);

        }
    }

    public void goToMap(View view) {
        Intent intent = new Intent(Profile.this, MapViewActivity.class);
        intent.putExtra("caller", "Profile.class");
        startActivity(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        adr = intent.getStringExtra("Address");
        lon = intent.getDoubleExtra("Longitude", 0.0);
        lat = intent.getDoubleExtra("Latitude", 0.0);
        TextView addressTxt = findViewById(R.id.address);
        addressTxt.setText(adr);
    }

    public void addLoc(View view) {
        EditText ed = findViewById(R.id.editTitle);
        Profile.Location pl = new Profile.Location(adr,ed.getText().toString(),lon,lat);
        mDatabase.child("addresses").child(FirebaseAuth.getInstance().getUid()).child(ed.getText().toString()).setValue(pl);
        Intent myIntent = new Intent(Profile.this, Profile.class);
        Profile.this.startActivity(myIntent);
    }
}

class RecyclerView_Loc {
    private Context mContext;
    private LocAdapter mLocAdapter;
    public HashMap<String,Boolean> checkedlocs;

    public void setConfig(RecyclerView recyclerView, Context context, List<Profile.Location> locations) {
        mContext = context;
        mLocAdapter = new LocAdapter(locations);
        checkedlocs = new HashMap<String,Boolean>();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(mLocAdapter);
        mLocAdapter.notifyDataSetChanged();
    }

    class LocView extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTitle;
        private TextView mAddress;
        private String key;
        private CheckBox ck;
        ItemClickListener itemClickListener;

        public LocView(ViewGroup parent) {
            super(LayoutInflater.from(mContext).inflate(R.layout.task_list_view, parent, false));
            mTitle = itemView.findViewById(R.id.title_text);
            ck = itemView.findViewById(R.id.checkBox);
            ck.setOnClickListener(this);
            mAddress = itemView.findViewById(R.id.location_text);
        }

        public void bind(Profile.Location loc) {
            mTitle.setText(loc.getTitle());
            mAddress.setText(loc.getAddress());
        }

        public void setItemClickListener(ItemClickListener ic) {
            this.itemClickListener = ic;
        }

        public void onClick(View v) {
            this.itemClickListener.onItemClick(v, getLayoutPosition());
        }
    }

    class LocAdapter extends RecyclerView.Adapter<LocView> {
        private List<Profile.Location> mLoclist;

        public LocAdapter(List<Profile.Location> mLoclist) {
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
            holder.setItemClickListener(new ItemClickListener() {
                @Override
                public void onItemClick(View v, int pos) {
                    CheckBox chk = (CheckBox) v;
                    if (chk.isChecked()) {
                        checkedlocs.put(mLoclist.get(pos).getTitle(), true);
                    } else {
                        checkedlocs.put(mLoclist.get(pos).getTitle(), false);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mLoclist.size();
        }

    }
}
