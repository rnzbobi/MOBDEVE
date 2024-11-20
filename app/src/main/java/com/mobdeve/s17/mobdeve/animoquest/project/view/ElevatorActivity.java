package com.mobdeve.s17.mobdeve.animoquest.project.view;

import android.content.Intent;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

public class ElevatorActivity extends AppCompatActivity {

    private ElevatorAdapter adapter;
    private DatabaseReference databaseRef;

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
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }
}
