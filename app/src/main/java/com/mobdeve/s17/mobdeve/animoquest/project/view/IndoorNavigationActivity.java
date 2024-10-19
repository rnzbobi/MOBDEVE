package com.mobdeve.s17.mobdeve.animoquest.project.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.mobdeve.s17.mobdeve.animoquest.project.R;

import java.util.Arrays;
import java.util.List;

public class IndoorNavigationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_indoor_navigation);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RecyclerView recyclerView = findViewById(R.id.navRecyclerView);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        List<Building> buildings = Arrays.asList(
                new Building("Henry", Arrays.asList(5, 7), 15, Arrays.asList(2, 7)), // Different capacities for each elevator
                new Building("La Salle Hall", Arrays.asList(10, 8), 3, Arrays.asList(5, 6)),
                new Building("Andrew", Arrays.asList(4, 6), 12, Arrays.asList(4, 3)),
                new Building("Razon", Arrays.asList(12, 9), 12, Arrays.asList(8, 10)),
                new Building("Goks", Arrays.asList(0, 0), 4, Arrays.asList(0, 0))
        );

        IndoorNavigationAdapter adapter = new IndoorNavigationAdapter(buildings);
        recyclerView.setAdapter(adapter);
        // Set the indoor_icon to green
        ImageView indoorIcon = findViewById(R.id.indoor_icon);
        indoorIcon.setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_IN);

        // Set up click listener for indoor_icon
        ImageView mapIcon = findViewById(R.id.map_icon);
        mapIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to IndoorNavigationActivity
                Intent intent = new Intent(IndoorNavigationActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Set up click listener for elevator_icon
        ImageView elevatorIcon = findViewById(R.id.elevator_icon);
        elevatorIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to ElevatorActivity
                Intent intent = new Intent(IndoorNavigationActivity.this, ElevatorActivity.class);
                startActivity(intent);
            }
        });

        // Set up click listener for elevator_icon
        ImageView notificationIcon = findViewById(R.id.notification_icon);
        notificationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to ElevatorActivity
                Intent intent = new Intent(IndoorNavigationActivity.this, NotificationActivity.class);
                startActivity(intent);
            }
        });

        // Set up click listener for elevator_icon
        ImageView profileIcon = findViewById(R.id.profile_icon);
        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to ElevatorActivity
                Intent intent = new Intent(IndoorNavigationActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Start the LoginActivity when back button is pressed
        super.onBackPressed();
        Intent intent = new Intent(IndoorNavigationActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Optional: Call finish to remove this activity from the stack
    }
}
