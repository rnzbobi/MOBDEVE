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
    private String imageName;

    public FloorsAdapter(List<String> floors, String elevatorName, String imageName) {
        this.floors = floors;
        this.elevatorName = elevatorName;
        this.imageName = imageName;

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
            return Integer.MAX_VALUE; // Handle unexpected non-numeric floor names
        }
    }

    @NonNull
    @Override
    public FloorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_floor, parent, false);
        return new FloorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FloorViewHolder holder, int position) {
        String floorName = floors.get(position);
        holder.floorName.setText(floorName);

        // Set default floor icon
        int resId = holder.itemView.getContext().getResources().getIdentifier(
                imageName, "drawable", holder.itemView.getContext().getPackageName());
        holder.floorIcon.setImageResource(resId);

        // Fetch capacity and waiting time from the Firebase database
        DatabaseReference floorRef = FirebaseDatabase.getInstance()
                .getReference("Elevators")
                .child(elevatorName)
                .child("Floors")
                .child(floorName);

        floorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer capacity = snapshot.child("capacity").getValue(Integer.class);
                Integer waitingTime = snapshot.child("waitingTime").getValue(Integer.class);

                holder.capacity.setText("Capacity: " + (capacity != null ? capacity : "N/A"));
                holder.waitingTime.setText("Waiting Time: " + (waitingTime != null ? waitingTime + " min" : "N/A"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.capacity.setText("Capacity: N/A");
                holder.waitingTime.setText("Waiting Time: N/A");
            }
        });
    }

    @Override
    public int getItemCount() {
        return floors.size();
    }

    static class FloorViewHolder extends RecyclerView.ViewHolder {
        TextView floorName;
        TextView capacity;
        TextView waitingTime;
        ImageView floorIcon;

        public FloorViewHolder(@NonNull View itemView) {
            super(itemView);
            floorName = itemView.findViewById(R.id.floorName);
            capacity = itemView.findViewById(R.id.capacity);
            waitingTime = itemView.findViewById(R.id.waitingTime);
            floorIcon = itemView.findViewById(R.id.floor_icon);
        }
    }
}
