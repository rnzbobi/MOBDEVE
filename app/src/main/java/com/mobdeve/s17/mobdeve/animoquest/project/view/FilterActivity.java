package com.mobdeve.s17.mobdeve.animoquest.project.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.mobdeve.s17.mobdeve.animoquest.project.R;

public class FilterActivity extends AppCompatActivity {

    private SeekBar filterFloorsSlider;
    private EditText filterBuilding;
    private TextView filterFloorsValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        // Initialize input fields
        filterBuilding = findViewById(R.id.filterBuilding);

        // Initialize slider and its respective text value
        filterFloorsSlider = findViewById(R.id.filterFloorsSlider);
        filterFloorsValue = findViewById(R.id.filterFloorsValue);

        // Initialize buttons
        Button applyFilterButton = findViewById(R.id.applyFilterButton);
        Button cancelFilterButton = findViewById(R.id.cancelFilterButton);

        // Set up button click listeners
        applyFilterButton.setOnClickListener(v -> applyFilters());
        cancelFilterButton.setOnClickListener(v -> finish());

        // Set up seek bar listener to update text view with current progress
        setupSeekBarListener();
    }

    private void setupSeekBarListener() {
        filterFloorsSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                filterFloorsValue.setText("Selected: " + progress + " floors");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void applyFilters() {
        String buildingName = filterBuilding.getText().toString().trim();
        int floors = filterFloorsSlider.getProgress();

        Intent intent = new Intent();
        intent.putExtra("building_name", buildingName);
        intent.putExtra("floors", floors);
        setResult(RESULT_OK, intent);
        finish();
    }
}
