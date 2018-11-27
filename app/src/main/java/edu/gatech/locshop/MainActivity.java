package edu.gatech.locshop;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerViewAdapter recyclerViewAdapter;
    private DatabaseReference databaseReference;
    private List<Task> allTask;
    private List<Location> targetLox, publixLox;
    private List<String> highlighted;
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mFusedLocationRquest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        allTask = new ArrayList<Task>();
        targetLox = new ArrayList<>();
        publixLox = new ArrayList<>();
        highlighted = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        recyclerView = (RecyclerView)findViewById(R.id.task_list);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        Button addTaskButton = findViewById(R.id.add_task_button);
        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
                startActivity(intent);
            }
        });

        Button addStoreButton = findViewById(R.id.add_store_button);
        addStoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddStoreActivity.class);
                startActivity(intent);
            }
        });

        databaseReference.child("stores").child("Publix").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                addStoreToList(dataSnapshot, "Walmart");
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        databaseReference.child("stores").child("Target").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                addStoreToList(dataSnapshot, "Target");
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        databaseReference.child("tasks").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                getAllTask(dataSnapshot);
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                getAllTask(dataSnapshot);
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                taskDeletion(dataSnapshot);
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Log.d("Location:", "null");
                    return;
                }
                for (Location location : locationResult.getLocations()) {

                    Log.d("Location:", location.toString());

                    highlighted.clear();

                    for (Location storeLoc: targetLox) {
                        if (location.distanceTo(storeLoc) < 1000) {
                            highlighted.add("Target");
                            break;
                        }
                    }

                    for (Location storeLoc: publixLox) {
                        if (location.distanceTo(storeLoc) < 1000) {
                            highlighted.add("Publix");
                            break;
                        }
                    }
                    updateRecycler();
                }
            }
        };
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        mFusedLocationRquest = new LocationRequest();
        mFusedLocationRquest.setInterval(100);
        mFusedLocationRquest.setFastestInterval(100);
        mFusedLocationRquest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ContextCompat.checkSelfPermission(this , Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d("Location","Coarse Not available" );
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1
                    );
        } else {
            Log.d("Location", "Coarse Available");
        }
        if (ContextCompat.checkSelfPermission(this , Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d("Location","Fine Not available" );
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1
            );
        } else {
            Log.d("Location", "Fine Available");
        }
        mFusedLocationClient.requestLocationUpdates(mFusedLocationRquest,mLocationCallback,null);


    }
    private void addStoreToList(DataSnapshot dataSnapshot, String store){
        Log.d("***", dataSnapshot.toString());
        Store s = dataSnapshot.getValue(Store.class);
        Location loc = new Location("");
        loc.setLongitude(s.getLongitude());
        loc.setLatitude(s.getLatitude());

        if (store.equals("Target"))
            targetLox.add(loc);
        else
            publixLox.add(loc);
    }

    private void updateRecycler() {
        recyclerViewAdapter = new RecyclerViewAdapter(MainActivity.this, allTask, highlighted);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    private void getAllTask(DataSnapshot dataSnapshot){
            Log.d("***", dataSnapshot.toString());
            String taskTitle = dataSnapshot.child("task").getValue(String.class);
            String store = dataSnapshot.child("store").getValue(String.class);
            allTask.add(new Task(taskTitle, store));
            updateRecycler();
    }
    private void taskDeletion(DataSnapshot dataSnapshot){
        String taskTitle = dataSnapshot.child("task").getValue(String.class);
        for(int i = 0; i < allTask.size(); i++){
            if(allTask.get(i).getTask().equals(taskTitle)){
                allTask.remove(i);
            }
        }
        Log.d(TAG, "Task tile " + taskTitle);
        recyclerViewAdapter.notifyDataSetChanged();
        updateRecycler();
    }

}