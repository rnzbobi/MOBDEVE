package com.mobdeve.s17.mobdeve.animoquest.project.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mobdeve.s17.mobdeve.animoquest.project.R;

import java.util.List;

public class FloorsAdapter extends RecyclerView.Adapter<FloorsAdapter.FloorViewHolder> {
    private List<String> floors;
    private String elevatorName;
    private String imageName; // Keep this
    private String destinationFloor;
    private String floor;

    // Updated Constructor
    public FloorsAdapter(List<String> floors, String elevatorName, String imageName, String destinationFloor, String floor) {
        this.floors = floors;
        this.elevatorName = elevatorName;
        this.imageName = imageName; // Retain the imageName field
        this.destinationFloor = destinationFloor;
        this.floor = floor;

        // Sort floors numerically
        this.floors.sort((f1, f2) -> {
            int num1 = extractFloorNumber(f1);
            int num2 = extractFloorNumber(f2);
            return Integer.compare(num1, num2);
        });
    }

    private int extractFloorNumber(String floorName) {
        try {
            return Integer.parseInt(floorName.replaceAll("[^\\d]", ""));
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    @NonNull
    @Override
    public FloorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item_floor.xml layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_floor, parent, false);
        return new FloorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FloorViewHolder holder, int position) {
        // Current floor in the list
        String floorName = floors.get(position);

        // Fetch data for the current floor from Firebase
        DatabaseReference floorRef = FirebaseDatabase.getInstance()
                .getReference("Elevators")
                .child(elevatorName)
                .child("Floors")
                .child(floorName);


        // Set Floor Text (specific to this row)
        holder.floorTextView.setText(floor);

        // Destination Text (same for all rows, passed via constructor)
        holder.destinationTextView.setText(destinationFloor);

        // Waiting Time (specific to this row, fetched dynamically from Firebase)
        floorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer waitingTime = snapshot.child("waitingTime").getValue(Integer.class);

                if (waitingTime != null) {
                    holder.waitingTimeTextView.setText(waitingTime + " min/s");
                    holder.waitingTimeTextView.setVisibility(View.VISIBLE);
                } else {
                    holder.waitingTimeTextView.setVisibility(View.GONE); // Hide if no waiting time
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.waitingTimeTextView.setVisibility(View.GONE); // Hide on error
            }
        });
    }



    @Override
    public int getItemCount() {
        return floors.size();
    }

    static class FloorViewHolder extends RecyclerView.ViewHolder {
        TextView destinationTextView;
        TextView waitingTimeTextView;
        TextView floorTextView;

        public FloorViewHolder(@NonNull View itemView) {
            super(itemView);
            destinationTextView = itemView.findViewById(R.id.destinationTextView);
            waitingTimeTextView = itemView.findViewById(R.id.waitingTimeTextView);
            floorTextView = itemView.findViewById(R.id.floorTextView);
        }
    }
}