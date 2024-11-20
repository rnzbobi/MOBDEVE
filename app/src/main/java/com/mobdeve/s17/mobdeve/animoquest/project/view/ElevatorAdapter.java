package com.mobdeve.s17.mobdeve.animoquest.project.view;

import android.content.Intent;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

                if (!capacityInput.isEmpty() && !floorInput.isEmpty()) {
                    int capacity = Integer.parseInt(capacityInput);
                    String floor = floorInput;

                    // Update the capacity in the Firebase database
                    DatabaseReference floorRef = FirebaseDatabase.getInstance()
                            .getReference("Elevators")
                            .child(item.getElevatorName())
                            .child("Floors")
                            .child(floor);

                    floorRef.child("capacity").setValue(capacity).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(
                                    holder.itemView.getContext(),
                                    "Capacity updated successfully",
                                    Toast.LENGTH_SHORT
                            ).show();

                            // Proceed to the next activity
                            Intent intent = new Intent(holder.itemView.getContext(), ElevatorDetailsActivity.class);
                            intent.putStringArrayListExtra("floors", new ArrayList<>(item.getFloors()));
                            intent.putExtra("elevator_name", item.getElevatorName());
                            intent.putExtra("image_name", item.getImageName());
                            intent.putExtra("capacity", capacity);
                            intent.putExtra("floor", floor);

                            holder.itemView.getContext().startActivity(intent);
                            bottomSheetDialog.dismiss();
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
