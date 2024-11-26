package com.mobdeve.s17.mobdeve.animoquest.project.view;

import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mobdeve.s17.mobdeve.animoquest.project.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ElevatorAdapter extends RecyclerView.Adapter<ElevatorAdapter.ElevatorViewHolder> {

    private List<ElevatorItem> elevatorItems = new ArrayList<>();

    public ElevatorAdapter() {
        fetchElevatorsFromDatabase();
    }

    private void fetchElevatorsFromDatabase() {
        DatabaseReference elevatorsRef = FirebaseDatabase.getInstance().getReference("Elevators");
        elevatorsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                elevatorItems.clear();
                for (DataSnapshot elevatorSnapshot : snapshot.getChildren()) {
                    String elevatorName = elevatorSnapshot.getKey();
                    String imageName = elevatorSnapshot.child("image").getValue(String.class);

                    List<String> floors = new ArrayList<>();
                    for (DataSnapshot floorSnapshot : elevatorSnapshot.child("Floors").getChildren()) {
                        floors.add(floorSnapshot.getKey());
                    }

                    // Sort floors numerically
                    floors.sort((f1, f2) -> {
                        int num1 = extractFloorNumber(f1);
                        int num2 = extractFloorNumber(f2);
                        return Integer.compare(num1, num2);
                    });

                    elevatorItems.add(new ElevatorItem(elevatorName, floors, imageName));
                }
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }


    public void checkAndUpdateFloorData() {
        DatabaseReference elevatorsRef = FirebaseDatabase.getInstance().getReference("Elevators");

        elevatorsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot elevatorSnapshot : snapshot.getChildren()) {
                    String elevatorName = elevatorSnapshot.getKey();
                    DatabaseReference floorsRef = elevatorsRef.child(elevatorName).child("Floors");

                    for (DataSnapshot floorSnapshot : elevatorSnapshot.child("Floors").getChildren()) {
                        String floor = floorSnapshot.getKey();
                        String time = floorSnapshot.child("time").getValue(String.class);

                        if (time != null && hasTimeExceededOneMinute(time)) {
                            // Update capacity to 0 and time to current time
                            DatabaseReference floorRef = floorsRef.child(floor);
                            floorRef.child("capacity").setValue(0);
                            floorRef.child("time").setValue(getCurrentTime());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
            }
        });
    }

    // Helper method to check if the time has exceeded one minute
    private boolean hasTimeExceededOneMinute(String floorTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            Date floorDate = sdf.parse(floorTime);
            Date currentDate = new Date();

            // Calculate time difference in milliseconds
            long differenceInMillis = currentDate.getTime() - floorDate.getTime();
            return differenceInMillis >= 1 * 60 * 1000; // Check if difference is greater than or equal to 1 minute
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private int extractFloorNumber(String floorName) {
        try {
            return Integer.parseInt(floorName.replaceAll("[^\\d]", ""));
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE; // Handle unexpected non-numeric floor names
        }
    }

    @NonNull
    @Override
    public ElevatorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_elevator, parent, false);
        return new ElevatorViewHolder(view);
    }


    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void refreshFloorData(String elevatorName) {
        DatabaseReference elevatorRef = FirebaseDatabase.getInstance()
                .getReference("Elevators")
                .child(elevatorName)
                .child("Floors");

        // Add a slight delay before refreshing to avoid conflict with new input
        new Handler().postDelayed(() -> {
            elevatorRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String currentTime = getCurrentTime();
                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());

                    for (DataSnapshot floorSnapshot : snapshot.getChildren()) {
                        String floor = floorSnapshot.getKey();
                        String floorTime = floorSnapshot.child("time").getValue(String.class);

                        if (floorTime != null && !floorTime.isEmpty()) {
                            try {
                                Date floorDate = sdf.parse(floorTime);
                                Date currentDate = sdf.parse(currentTime);

                                if (floorDate != null && currentDate != null) {
                                    long timeDifference = currentDate.getTime() - floorDate.getTime();
                                    long minutesDifference = timeDifference / (60 * 1000);

                                    // Skip floors updated in the same session (time difference < 2 minutes)
                                    if (minutesDifference < 2) {
                                        continue;
                                    }

                                    if (minutesDifference >= 2) {
                                        // Reset capacity to 0 and update the time to the current time
                                        elevatorRef.child(floor).child("capacity").setValue(0);
                                        elevatorRef.child(floor).child("time").setValue(currentTime);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace(); // Handle parsing errors
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error
                }
            });
        }, 500); // Slight delay to ensure the new input is processed
    }



    @Override
    public void onBindViewHolder(@NonNull ElevatorViewHolder holder, int position) {
        ElevatorItem item = elevatorItems.get(position);

        holder.buildingName.setText(item.getElevatorName());
        int resId = holder.itemView.getContext().getResources().getIdentifier(
                item.getImageName(), "drawable", holder.itemView.getContext().getPackageName());
        holder.buildingIcon.setImageResource(resId);

        // Set the number of floors
        holder.floorCount.setText("Floors: " + item.getFloors().size());

        holder.itemView.setOnClickListener(v -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(holder.itemView.getContext());
            View bottomSheetView = LayoutInflater.from(holder.itemView.getContext())
                    .inflate(R.layout.bottom_sheet_elevator, null);
            bottomSheetDialog.setContentView(bottomSheetView);

            TextView title = bottomSheetView.findViewById(R.id.elevator_title);
            title.setText(item.getElevatorName());

            EditText inputCapacity = bottomSheetView.findViewById(R.id.input_capacity);
            EditText inputFloor = bottomSheetView.findViewById(R.id.input_floor);
            Button confirmButton = bottomSheetView.findViewById(R.id.btn_confirm);
            Button closeButton = bottomSheetView.findViewById(R.id.btn_close_elevator);

            confirmButton.setOnClickListener(view -> {
                String capacityInput = inputCapacity.getText().toString().trim();
                String floorInput = inputFloor.getText().toString().trim();


                // New destination input
                EditText inputDestination = bottomSheetView.findViewById(R.id.input_destination);
                String destinationInput = inputDestination.getText().toString().trim();

                if (!capacityInput.isEmpty() && !floorInput.isEmpty() && !destinationInput.isEmpty()) {

                    if (floorInput.equals(destinationInput)) {
                        // Show error if floor and destination are the same
                        Toast.makeText(holder.itemView.getContext(),
                                "Floor and destination cannot be the same. Please enter different values.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!item.getFloors().contains(floorInput) || !item.getFloors().contains(destinationInput)) {
                        // Show error if floor or destination is not valid
                        Toast.makeText(holder.itemView.getContext(),
                                "Invalid floor. Please enter a valid floor for this elevator.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int capacity = Integer.parseInt(capacityInput);

                    // Get the current device time
                    String currentTime = getCurrentTime();

                    // Refresh the capacity and time for all floors
                    refreshFloorData(item.getElevatorName());

                    // Update the capacity and time in the Firebase database
                    DatabaseReference floorRef = FirebaseDatabase.getInstance()
                            .getReference("Elevators")
                            .child(item.getElevatorName())
                            .child("Floors")
                            .child(floorInput);

                    floorRef.child("capacity").setValue(capacity).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Update the time in the database
                            floorRef.child("time").setValue(currentTime).addOnCompleteListener(timeTask -> {
                                if (timeTask.isSuccessful()) {
                                    Toast.makeText(
                                            holder.itemView.getContext(),
                                            "Capacity and time updated successfully",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    // Update waiting times for all floors
                                    updateWaitingTimes(item.getElevatorName(), Integer.parseInt(floorInput), 5, 3);

                                    // Proceed to the next activity
                                    Intent intent = new Intent(holder.itemView.getContext(), ElevatorDetailsActivity.class);
                                    intent.putStringArrayListExtra("floors", new ArrayList<>(item.getFloors()));
                                    intent.putExtra("elevator_name", item.getElevatorName());
                                    intent.putExtra("image_name", item.getImageName());
                                    intent.putExtra("capacity", capacity);
                                    intent.putExtra("floor", floorInput);
                                    intent.putExtra("destination", destinationInput); // Pass destination floor

                                    holder.itemView.getContext().startActivity(intent);
                                    bottomSheetDialog.dismiss();
                                } else {
                                    Toast.makeText(
                                            holder.itemView.getContext(),
                                            "Failed to update time",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            });
                        } else {
                            Toast.makeText(
                                    holder.itemView.getContext(),
                                    "Failed to update capacity",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                }
            });


            closeButton.setOnClickListener(view -> bottomSheetDialog.dismiss());
            bottomSheetDialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return elevatorItems.size();
    }

    private void updateWaitingTimes(String elevatorName, int userFloor, int baseTime, int travelTimePerFloor) {
        DatabaseReference elevatorRef = FirebaseDatabase.getInstance()
                .getReference("Elevators")
                .child(elevatorName)
                .child("Floors");

        // Define elevator capacity range
        final int elevatorCapacityMin = 6;
        final int elevatorCapacityMax = 9;
        final int averageElevatorCapacity = (elevatorCapacityMin + elevatorCapacityMax) / 2;

        elevatorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalCapacity = 0;
                List<String> floors = new ArrayList<>();
                List<Integer> capacities = new ArrayList<>();

                for (DataSnapshot floorSnapshot : snapshot.getChildren()) {
                    String floor = floorSnapshot.getKey();
                    Integer capacity = floorSnapshot.child("capacity").getValue(Integer.class);
                    if (floor != null && capacity != null) {
                        floors.add(floor);
                        capacities.add(capacity);
                        totalCapacity += capacity;
                    }
                }

                // Log the total capacity
                Log.d("ElevatorCapacity", "Total capacity: " + totalCapacity);

                // Ensure a minimum total capacity to avoid division by zero
                int effectiveTotalCapacity = Math.max(totalCapacity, 1);

                // Adjust waiting times dynamically based on total and individual capacities
                for (int i = 0; i < floors.size(); i++) {
                    String floor = floors.get(i);
                    int capacity = capacities.get(i);

                    // Skip the user's floor to hide its waiting time
                    int floorNumber = extractFloorNumber(floor);
                    if (floorNumber == userFloor) {
                        elevatorRef.child(floor).child("waitingTime").setValue(null); // Remove waiting time
                        continue;
                    }

                    // Calculate distance from user's floor
                    int distance = Math.abs(floorNumber - userFloor);

                    // Calculate trips required for the floor's capacity
                    int tripsRequired = (int) Math.ceil((double) capacity / averageElevatorCapacity);

                    // Adjust waiting time based on trips required and distance
                    double capacityFactor = (double) capacity / effectiveTotalCapacity;
                    int waitingTime = (int) ((baseTime
                            + (distance * travelTimePerFloor) * tripsRequired // Adjust by trips
                            + (capacityFactor * 5))); // Floor-specific capacity adjustment

                    // Cap the waiting time to 30 minutes
                    waitingTime = Math.min(waitingTime, 30);

                    // Update the waiting time in the database
                    elevatorRef.child(floor).child("waitingTime").setValue(waitingTime);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }





    static class ElevatorViewHolder extends RecyclerView.ViewHolder {
        TextView buildingName;
        TextView floorCount;
        ImageView buildingIcon;

        public ElevatorViewHolder(@NonNull View itemView) {
            super(itemView);
            buildingName = itemView.findViewById(R.id.buildingName);
            floorCount = itemView.findViewById(R.id.floorCount);
            buildingIcon = itemView.findViewById(R.id.building_icon);
        }
    }

    static class ElevatorItem {
        private String elevatorName;
        private List<String> floors;
        private String imageName;

        public ElevatorItem(String elevatorName, List<String> floors, String imageName) {
            this.elevatorName = elevatorName;
            this.floors = floors;
            this.imageName = imageName;
        }

        public String getElevatorName() {
            return elevatorName;
        }

        public List<String> getFloors() {
            return floors;
        }

        public String getImageName() {
            return imageName;
        }
    }

    public void setElevatorItems(List<ElevatorItem> newItems) {
        this.elevatorItems.clear();
        this.elevatorItems.addAll(newItems);
        notifyDataSetChanged();
    }
}