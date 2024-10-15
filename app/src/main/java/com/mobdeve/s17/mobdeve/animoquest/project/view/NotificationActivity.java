package com.mobdeve.s17.mobdeve.animoquest.project.view;

import android.app.Notification;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.mobdeve.s17.mobdeve.animoquest.project.R;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter notificationAdapter;
    private List<NotificationHolder> notificationList;

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
        notificationList.add(new NotificationHolder("DLSU", "UAAP S87 Pep Rally", "Join us at the UAAP S87 Pep Rally and show your school spirit!", "2 hours ago", R.drawable.logo_dlsu, R.drawable.uaap_poster));
        notificationList.add(new NotificationHolder("DLSU", "Annual Recruitment Week Starts Now!", "Explore various student organizations and find your passion!", "3 hours ago", R.drawable.logo_dlsu, 0));
        notificationList.add(new NotificationHolder("DLSU", "Free Food at CADS", "Stop by CADS today for some delicious free food. Don't miss out!", "5 hours ago", R.drawable.logo_dlsu, 0));
        notificationList.add(new NotificationHolder("DLSU", "Goks 24/7 Starts Again Tomorrow", "Goks is back! Study any time of day starting tomorrow.", "Oct 12, 2024", R.drawable.logo_dlsu, 0));
        notificationList.add(new NotificationHolder("DLSU", "Animusika Concert Coming Soon!", "Get ready for an amazing night of music at Animusika. Stay tuned!", "Oct 10, 2024", R.drawable.logo_dlsu, R.drawable.animusika_poster));
        notificationList.add(new NotificationHolder("DLSU", "DLSU Tryouts for Basketball at Razon", "Join the tryouts for the DLSU basketball team at Razon. See you there!", "Oct 8, 2024", R.drawable.logo_dlsu, 0));


        // Set up adapter
        notificationAdapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(notificationAdapter);

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
}