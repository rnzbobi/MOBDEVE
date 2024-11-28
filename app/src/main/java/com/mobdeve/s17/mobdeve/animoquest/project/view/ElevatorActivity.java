package com.mobdeve.s17.mobdeve.animoquest.project.view;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mobdeve.s17.mobdeve.animoquest.project.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class ElevatorActivity extends AppCompatActivity {

    private ElevatorAdapter adapter;
    private DatabaseReference databaseRef;
    private Button resetFilterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_elevator);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseRef = FirebaseDatabase.getInstance().getReference("Elevators");

        RecyclerView recyclerView = findViewById(R.id.elevatorRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ElevatorAdapter();
        recyclerView.setAdapter(adapter);

        // Initialize the reset filter button
        resetFilterButton = findViewById(R.id.resetFilterButton);
        resetFilterButton.setVisibility(View.GONE);
        resetFilterButton.setOnClickListener(v -> {
            // Reset filters by applying an empty filter
            applyFilters("", 0);
            resetFilterButton.setVisibility(View.GONE); // Hide the reset button
        });

        // Check if "Elevators" node exists
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // Add data if "Elevators" node does not exist
                    addElevatorData();
                }
                // Initialize the adapter after adding data (or confirming data exists)
                adapter = new ElevatorAdapter();
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("DatabaseError", "Failed to check Elevators node: " + error.getMessage());
            }
        });

        ImageView elevatorIcon = findViewById(R.id.elevator_icon);
        elevatorIcon.setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_IN);

        ImageView mapIcon = findViewById(R.id.map_icon);
        mapIcon.setOnClickListener(v -> startActivity(new Intent(ElevatorActivity.this, MainActivity.class)));

        ImageView indoorIcon = findViewById(R.id.indoor_icon);
        indoorIcon.setOnClickListener(v -> startActivity(new Intent(ElevatorActivity.this, IndoorNavigationActivity.class)));

        ImageView notificationIcon = findViewById(R.id.notification_icon);
        notificationIcon.setOnClickListener(v -> startActivity(new Intent(ElevatorActivity.this, NotificationActivity.class)));

        ImageView profileIcon = findViewById(R.id.profile_icon);
        profileIcon.setOnClickListener(v -> startActivity(new Intent(ElevatorActivity.this, ProfileActivity.class)));

        Button filterButton = findViewById(R.id.filterButton);
        filterButton.setOnClickListener(v -> {
            Intent filterIntent = new Intent(ElevatorActivity.this, FilterActivity.class);
            startActivityForResult(filterIntent, 1);
        });

        // Periodically check and update floor data
        Handler handler = new Handler();
        Runnable periodicTask = new Runnable() {
            @Override
            public void run() {
                adapter.checkAndUpdateFloorData(); // Call the method to check and update floor data
                handler.postDelayed(this, 60 * 1000); // Run every 60 seconds
            }
        };
    }


    private void addElevatorData() {
        // Henry Elevators
        addOrUpdateElevator("Henry Elevator 1", generateFloors(15), "henry_photo");
        addOrUpdateElevator("Henry Elevator 2", generateFloors(15), "henry_photo");

        // Yuchengco Elevators
        addOrUpdateElevator("Yuchengco Elevator 1", generateFloors(9), "yuchengco_photo");
        addOrUpdateElevator("Yuchengco Elevator 2", generateFloors(9), "yuchengco_photo");

        // Andrew Elevators
        addOrUpdateElevator("Andrew Elevator 1", generateFloors(22), "andrew_photo");
        addOrUpdateElevator("Andrew Elevator 2", generateFloors(22), "andrew_photo");

        // Razon Elevators
        addOrUpdateElevator("Razon Elevator 1", generateFloors(9), "razon_photo");
        addOrUpdateElevator("Razon Elevator 2", generateFloors(9), "razon_photo");
    }

    private void addOrUpdateElevator(String elevatorName, String[] floors, String imageName) {
        DatabaseReference elevatorRef = databaseRef.child(elevatorName);

        Random random = new Random();
        for (int i = 0; i < floors.length; i++) {
            // Use numeric keys directly
            DatabaseReference floorRef = elevatorRef.child("Floors").child(floors[i]);
            floorRef.child("name").setValue(floors[i]); // Numeric floor value
            floorRef.child("capacity").setValue(random.nextInt(6) + 2); // Random capacity between 1 and 20
            floorRef.child("waitingTime").setValue(random.nextInt(5) + 1); // Random waiting time between 1 and 5 minutes
            // Get the current device time and format it
            String currentTime = getCurrentTime();
            floorRef.child("time").setValue(currentTime); // Set the current device time

        }

        elevatorRef.child("image").setValue(imageName);
    }

    private String getCurrentTime() {
        // Format the current time based on the device's settings
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date());
    }

    private String[] generateFloors(int floorCount) {
        String[] floors = new String[floorCount];
        for (int i = 0; i < floorCount; i++) {
            floors[i] = String.valueOf(i + 1); // Numeric floor names
        }
        return floors;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String buildingName = data.getStringExtra("building_name");
            int floors = data.getIntExtra("floors", 0);
            applyFilters(buildingName, floors);
        }
    }

    private void applyFilters(String buildingName, int floors) {
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<ElevatorAdapter.ElevatorItem> filteredItems = new ArrayList<>();

                for (DataSnapshot elevatorSnapshot : snapshot.getChildren()) {
                    String elevatorName = elevatorSnapshot.getKey();
                    String imageName = elevatorSnapshot.child("image").getValue(String.class);

                    List<String> floorList = new ArrayList<>();
                    for (DataSnapshot floorSnapshot : elevatorSnapshot.child("Floors").getChildren()) {
                        floorList.add(floorSnapshot.getKey());
                    }

                    boolean matchesBuilding = buildingName.isEmpty() || elevatorName.toLowerCase().contains(buildingName.toLowerCase());
                    boolean matchesFloors = floors == 0 || floorList.size() == floors;

                    if (matchesBuilding && matchesFloors) {
                        filteredItems.add(new ElevatorAdapter.ElevatorItem(elevatorName, floorList, imageName));
                    }
                }

                adapter.setElevatorItems(filteredItems);
                // Show the reset button if filters were applied
                if (!buildingName.isEmpty() || floors > 0) {
                    resetFilterButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }
}
