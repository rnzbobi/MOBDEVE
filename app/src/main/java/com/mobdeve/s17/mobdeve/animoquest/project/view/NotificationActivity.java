package com.mobdeve.s17.mobdeve.animoquest.project.view;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mobdeve.s17.mobdeve.animoquest.project.R;
import com.mobdeve.s17.mobdeve.animoquest.project.model.NotificationHolder;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter notificationAdapter;
    private List<NotificationHolder> notificationList;
    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up the RecyclerView
        recyclerView = findViewById(R.id.recycler_view_notifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize notification list
        notificationList = new ArrayList<>();

        // Set up adapter
        notificationAdapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(notificationAdapter);

        databaseReference = FirebaseDatabase.getInstance()
                .getReference("notifications");

        loadNotificationsFromFirebase();

        // Set the indoor_icon to green
        ImageView notificationIcon = findViewById(R.id.notification_icon);
        notificationIcon.setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_IN);

        // Set up click listener for indoor_icon
        ImageView indoorIcon = findViewById(R.id.indoor_icon);
        indoorIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to IndoorNavigationActivity
                Intent intent = new Intent(NotificationActivity.this, IndoorNavigationActivity.class);
                startActivity(intent);
            }
        });

        // Set up click listener for elevator_icon
        ImageView elevatorIcon = findViewById(R.id.elevator_icon);
        elevatorIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to ElevatorActivity
                Intent intent = new Intent(NotificationActivity.this, ElevatorActivity.class);
                startActivity(intent);
            }
        });

        // Set up click listener for elevator_icon
        ImageView mapIcon = findViewById(R.id.map_icon);
        mapIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to ElevatorActivity
                Intent intent = new Intent(NotificationActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Set up click listener for elevator_icon
        ImageView profileIcon = findViewById(R.id.profile_icon);
        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to ElevatorActivity
                Intent intent = new Intent(NotificationActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Start the LoginActivity when back button is pressed
        super.onBackPressed();
        Intent intent = new Intent(NotificationActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Optional: Call finish to remove this activity from the stack
    }

    private void loadNotificationsFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationList.clear(); // Clear the list before adding new data
                for (DataSnapshot child : snapshot.getChildren()) {
                    String sender = child.child("sender").getValue(String.class);
                    String message = child.child("message").getValue(String.class);
                    String subject = child.child("subject").getValue(String.class);
                    String timestamp = child.child("timestamp").getValue(String.class);

                    NotificationHolder notification = new NotificationHolder(
                            sender != null ? sender : "Unknown Sender",
                            subject != null ? subject : "No Title",
                            message != null ? message : "No Message",
                            timestamp != null ? timestamp : "No Timestamp"
                    );

                    notificationList.add(notification);
                }

                List<NotificationHolder> reversedList = new ArrayList<>(notificationList);
                Collections.reverse(reversedList);

                notificationAdapter.updateData(reversedList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NotificationActivity.this, "Failed to load notifications", Toast.LENGTH_SHORT).show();
            }
        });
    }
}