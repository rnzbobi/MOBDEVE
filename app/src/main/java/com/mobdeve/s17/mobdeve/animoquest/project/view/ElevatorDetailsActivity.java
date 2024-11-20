package com.mobdeve.s17.mobdeve.animoquest.project.view;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mobdeve.s17.mobdeve.animoquest.project.R;

import java.util.List;

public class ElevatorDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elevator_details);

        String elevatorName = getIntent().getStringExtra("elevator_name");
        List<String> floors = getIntent().getStringArrayListExtra("floors");
        String imageName = getIntent().getStringExtra("image_name");
        int capacity = getIntent().getIntExtra("capacity", -1);
        String floor = getIntent().getStringExtra("floor"); // Handle as String

        TextView elevatorNameTextView = findViewById(R.id.elevatorNameTextView);
        elevatorNameTextView.setText(
                elevatorName + " Floor: " + floor + ", Capacity: " + capacity);

        RecyclerView floorsRecyclerView = findViewById(R.id.floorsRecyclerView);
        floorsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        floorsRecyclerView.setAdapter(new FloorsAdapter(floors, elevatorName, imageName));
    }
}
