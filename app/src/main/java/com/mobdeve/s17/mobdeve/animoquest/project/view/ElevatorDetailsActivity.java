package com.mobdeve.s17.mobdeve.animoquest.project.view;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mobdeve.s17.mobdeve.animoquest.project.R;

import java.util.ArrayList;
import java.util.List;

public class ElevatorDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elevator_details);


        // Get data from intent
        String elevatorName = getIntent().getStringExtra("elevator_name");
        List<String> floors = getIntent().getStringArrayListExtra("floors");
        String imageName = getIntent().getStringExtra("image_name");
        int capacity = getIntent().getIntExtra("capacity", -1);
        String floor = getIntent().getStringExtra("floor");
        String destination = getIntent().getStringExtra("destination");

        // Set formatted text for elevator details
        TextView elevatorNameTextView = findViewById(R.id.elevatorNameTextView);
        String formattedText = String.format(
                "Capacity: %d\nBuilding: %s",
                capacity, elevatorName
        );
        elevatorNameTextView.setText(formattedText);

        // Set elevator image
        ImageView elevatorImageView = findViewById(R.id.elevatorImageView);
        int resId = getResources().getIdentifier(imageName, "drawable", getPackageName());
        if (resId != 0) {
            elevatorImageView.setImageResource(resId);
            elevatorImageView.setVisibility(View.VISIBLE);
        } else {
            elevatorImageView.setVisibility(View.GONE); // Hide the ImageView if image is not found
        }
        // Filter the floors to only show the destination if provided
        if (destination != null && !destination.isEmpty()) {
            floors = new ArrayList<>();
            floors.add(destination);
        }

        // Initialize RecyclerView
        RecyclerView floorsRecyclerView = findViewById(R.id.floorsRecyclerView);
        floorsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Pass destination to the adapter
        floorsRecyclerView.setAdapter(new FloorsAdapter(floors, elevatorName, imageName, destination, floor));
    }

}
