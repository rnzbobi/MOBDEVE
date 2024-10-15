package com.mobdeve.s17.mobdeve.animoquest.project.view;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
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

import com.mobdeve.s17.mobdeve.animoquest.project.R;

import java.util.Arrays;
import java.util.List;

public class ElevatorActivity extends AppCompatActivity {


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

        // Add this inside the existing onCreate() method

// Find the RecyclerView in the layout
        RecyclerView recyclerView = findViewById(R.id.elevatorRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // Ensure LinearLayoutManager is set


        // Sample data for the buildings and their elevator waiting times (in seconds), floors, and capacities
        List<Building> buildings = Arrays.asList(
                new Building("Henry", Arrays.asList(5, 7), 10, Arrays.asList(2, 7)), // Different capacities for each elevator
                new Building("Yuchengco", Arrays.asList(10, 8), 8, Arrays.asList(5, 6)),
                new Building("Andrew", Arrays.asList(4, 6), 12, Arrays.asList(4, 3)),
                new Building("Razon", Arrays.asList(12, 9), 15, Arrays.asList(8, 10))
        );

        // Set up the adapter for the RecyclerView
        ElevatorAdapter adapter = new ElevatorAdapter(buildings);
        recyclerView.setAdapter(adapter);

        // Set the indoor_icon to green
        ImageView elevatorIcon = findViewById(R.id.elevator_icon);
        elevatorIcon.setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_IN);

        // Set up click listener for indoor_icon
        ImageView mapIcon = findViewById(R.id.map_icon);
        mapIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to IndoorNavigationActivity
                Intent intent = new Intent(ElevatorActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Set up click listener for elevator_icon
        ImageView indoorIcon = findViewById(R.id.indoor_icon);
        indoorIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to ElevatorActivity
                Intent intent = new Intent(ElevatorActivity.this, IndoorNavigationActivity.class);
                startActivity(intent);
            }
        });

        // Set up click listener for elevator_icon
        ImageView notificationIcon = findViewById(R.id.notification_icon);
        notificationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to ElevatorActivity
                Intent intent = new Intent(ElevatorActivity.this, NotificationActivity.class);
                startActivity(intent);
            }
        });

        // Set up click listener for elevator_icon
        ImageView profileIcon = findViewById(R.id.profile_icon);
        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to ElevatorActivity
                Intent intent = new Intent(ElevatorActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        // Filter Button functionality
        Button filterButton = findViewById(R.id.filterButton);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the FilterActivity when the button is clicked
                Intent filterIntent = new Intent(ElevatorActivity.this, FilterActivity.class);
                startActivity(filterIntent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Start the LoginActivity when back button is pressed
        super.onBackPressed();
        Intent intent = new Intent(ElevatorActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Optional: Call finish to remove this activity from the stack
    }
}