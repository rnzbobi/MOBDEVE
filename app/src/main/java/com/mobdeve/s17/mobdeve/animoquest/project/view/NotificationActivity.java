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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.jakewharton.threetenabp.AndroidThreeTen;
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
        AndroidThreeTen.init(this);
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
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userNotificationsRef = FirebaseDatabase.getInstance()
                .getReference("userNotifications").child(userId);

        userNotificationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String id = child.getKey();
                    String sender = child.child("sender").getValue(String.class);
                    String subject = child.child("subject").getValue(String.class);
                    String message = child.child("message").getValue(String.class);
                    String timestamp = child.child("timestamp").getValue(String.class);
                    boolean isRead = child.child("isRead").getValue(Boolean.class);

                    // Format the timestamp
                    String formattedTimestamp = formatTimestamp(timestamp);

                    notificationList.add(new NotificationHolder(id, sender, subject, message, formattedTimestamp, isRead));
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

    // Helper method to format the timestamp
    private String formatTimestamp(String isoTimestamp) {
        try {
            // Parse the ISO timestamp
            Instant notificationTime = Instant.parse(isoTimestamp);
            Instant now = Instant.now();

            long minutesDiff = ChronoUnit.MINUTES.between(notificationTime, now);
            long hoursDiff = ChronoUnit.HOURS.between(notificationTime, now);
            long daysDiff = ChronoUnit.DAYS.between(notificationTime, now);

            if (minutesDiff < 60) {
                return minutesDiff + "m"; // e.g., "5m"
            } else if (hoursDiff < 24) {
                return hoursDiff + " hr" + (hoursDiff > 1 ? "s" : ""); // e.g., "1hr", "8hrs"
            } else if (daysDiff <= 7) {
                return daysDiff + "d"; // e.g., "3d"
            } else {
                // Format the date as "MMM. dd, yyyy"
                LocalDateTime localDateTime = LocalDateTime.ofInstant(notificationTime, ZoneId.systemDefault());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM. dd, yyyy");
                return localDateTime.format(formatter);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Invalid Date";
        }
    }
}