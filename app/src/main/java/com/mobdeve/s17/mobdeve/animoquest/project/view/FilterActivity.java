package com.mobdeve.s17.mobdeve.animoquest.project.view;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.mobdeve.s17.mobdeve.animoquest.project.R;

public class FilterActivity extends AppCompatActivity {

    private EditText filterBuilding, filterElevatorNumber;
    private SeekBar filterFloorsSlider, filterWaitTimeSlider, filterCapacitySlider;
    private TextView filterFloorsValue, filterWaitTimeValue, filterCapacityValue;
    private Button applyFilterButton, cancelFilterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        // Initialize views
        filterBuilding = findViewById(R.id.filterBuilding);
        filterElevatorNumber = findViewById(R.id.filterElevatorNumber);
        filterFloorsSlider = findViewById(R.id.filterFloorsSlider);
        filterWaitTimeSlider = findViewById(R.id.filterWaitTimeSlider);
        filterCapacitySlider = findViewById(R.id.filterCapacitySlider);
        filterFloorsValue = findViewById(R.id.filterFloorsValue);
        filterWaitTimeValue = findViewById(R.id.filterWaitTimeValue);
        filterCapacityValue = findViewById(R.id.filterCapacityValue);
        applyFilterButton = findViewById(R.id.applyFilterButton);
        cancelFilterButton = findViewById(R.id.cancelFilterButton);

        // Set the default slider values and listeners
        setSliderListeners();

        // Apply Filter button functionality (close activity on click)
        applyFilterButton.setOnClickListener(v -> finish());

        // Cancel Filter button functionality (close activity on click)
        cancelFilterButton.setOnClickListener(v -> finish());
    }

    // Method to initialize the SeekBar listeners and update the TextView values dynamically
    private void setSliderListeners() {
        // Floors Slider Listener
        filterFloorsSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                filterFloorsValue.setText("Selected: " + progress + " floors");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // No action needed here
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // No action needed here
            }
        });

        // Wait Time Slider Listener
        filterWaitTimeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                filterWaitTimeValue.setText("Selected: " + progress + " sec");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // No action needed here
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // No action needed here
            }
        });

        // Capacity Slider Listener
        filterCapacitySlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                filterCapacityValue.setText("Selected: " + progress + " people");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // No action needed here
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // No action needed here
            }
        });
    }
}
