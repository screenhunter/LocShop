package edu.gatech.locshop;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
    private List<Item> allTask;
    private List<String> highlighted;
    private ArrayList<String> storeList;
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mFusedLocationRquest;
    protected GeoDataClient mGeoDataClient;
    protected PlaceDetectionClient mPlaceDetectionClient;
    private int handelerDelay = 10000;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        allTask = new ArrayList<Item>();
        highlighted = new ArrayList<>();
        storeList = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        recyclerView = (RecyclerView)findViewById(R.id.task_list);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        Button addTaskButton = findViewById(R.id.add_task_button);
        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
                intent.putStringArrayListExtra("storeList", storeList);
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

        databaseReference.child("stores").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                addStoreToList(dataSnapshot);
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

        find();


    }
    private void addStoreToList(DataSnapshot dataSnapshot){
        Log.d("***", dataSnapshot.toString());
        String storeName = dataSnapshot.getValue(String.class);
        storeList.add(storeName);
    }

    private void updateRecycler() {
        recyclerViewAdapter = new RecyclerViewAdapter(MainActivity.this, allTask, highlighted);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    private void getAllTask(DataSnapshot dataSnapshot){
            Log.d("***", dataSnapshot.toString());
            String taskTitle = dataSnapshot.child("task").getValue(String.class);
            String store = dataSnapshot.child("store").getValue(String.class);
            allTask.add(new Item(taskTitle, store));
            updateRecycler();
    }
    private void taskDeletion(DataSnapshot dataSnapshot){
        String taskTitle = dataSnapshot.child("task").getValue(String.class);
        for(int i = 0; i < allTask.size(); i++){
            if(allTask.get(i).getTask().equals(taskTitle)){
                allTask.remove(i);
            }
        }
        Log.d(TAG, "Item tile " + taskTitle);
        recyclerViewAdapter.notifyDataSetChanged();
        updateRecycler();
    }

    private void checkPerm() {
        if (ContextCompat.checkSelfPermission(this , Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d("Location","Fine Not available" );
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1
            );
        } else {
            Log.d("Location", "Fine Available");
        }
    }

    public void find() {

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        mFusedLocationRquest = new LocationRequest();
        mFusedLocationRquest.setInterval(10000);
        mFusedLocationRquest.setFastestInterval(10000);
        mFusedLocationRquest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        checkPerm();
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                mGeoDataClient = Places.getGeoDataClient(getApplicationContext());
                mPlaceDetectionClient = Places.getPlaceDetectionClient(getApplicationContext());

                Task<PlaceLikelihoodBufferResponse> placeResult = mPlaceDetectionClient.getCurrentPlace(null);
                placeResult.addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                        PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();
                        highlighted.clear();
                        for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                            for (String storeName: storeList)
                                if (placeLikelihood.getPlace().getName().toString().toLowerCase().contains(storeName.toLowerCase())) {
                                    addToHighlighted(storeName);
                                }
                            Log.i(TAG, String.format("Place '%s' %s has likelihood: %g",
                                    placeLikelihood.getPlace().getName(),
                                    placeLikelihood.getPlace().getLatLng() + "",
                                    placeLikelihood.getLikelihood()));
                        }
                        updateRecycler();
                        likelyPlaces.release();
                    }
                });
            };
        };
        mFusedLocationClient.requestLocationUpdates(mFusedLocationRquest, mLocationCallback,null);

    }

    public void addToHighlighted(String storeName) {
        if (!highlighted.contains(storeName))
            highlighted.add(storeName);
    }

}