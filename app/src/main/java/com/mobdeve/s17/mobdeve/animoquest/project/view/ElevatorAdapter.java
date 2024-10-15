package com.mobdeve.s17.mobdeve.animoquest.project.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.mobdeve.s17.mobdeve.animoquest.project.R;
import java.util.ArrayList;
import java.util.List;

// Adapter for the RecyclerView
public class ElevatorAdapter extends RecyclerView.Adapter<ElevatorAdapter.ElevatorViewHolder> {

    // List of elevator items (each elevator will be its own card)
    private List<ElevatorItem> elevatorItems;

    // Constructor for ElevatorAdapter, taking a list of buildings
    public ElevatorAdapter(List<Building> buildingList) {
        elevatorItems = new ArrayList<>();

        // Loop through each building and add its elevators as separate items
        for (Building building : buildingList) {
            for (int i = 0; i < building.getElevators().size(); i++) {
                elevatorItems.add(new ElevatorItem(
                        building.getName(),
                        building.getElevators().get(i),
                        i + 1,
                        building.getFloors(),
                        building.getCapacityForElevator(i) // Get the capacity for each elevator
                ));
            }
        }
    }

    @Override
    public ElevatorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_elevator, parent, false);
        return new ElevatorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ElevatorViewHolder holder, int position) {
        ElevatorItem elevatorItem = elevatorItems.get(position);

        // Set the building name and elevator waiting time
        holder.buildingName.setText(elevatorItem.getBuildingName() + " Elevator " + elevatorItem.getElevatorNumber());
        holder.elevatorTime.setText("Waiting Time: " + elevatorItem.getWaitingTime() + " sec");
        holder.floors.setText("Floors: " + elevatorItem.getFloors());
        holder.capacity.setText("Capacity: " + elevatorItem.getCapacity());

        // Set the correct image for each building dynamically
        switch (elevatorItem.getBuildingName()) {
            case "Henry":
                holder.buildingIcon.setImageResource(R.drawable.henry_photo); // Image for Henry building
                break;
            case "Yuchengco":
                holder.buildingIcon.setImageResource(R.drawable.yuchengco_photo); // Image for Yuchengco building
                break;
            case "Andrew":
                holder.buildingIcon.setImageResource(R.drawable.andrew_photo); // Image for Andrew building
                break;
            case "Razon":
                holder.buildingIcon.setImageResource(R.drawable.razon_photo); // Image for Razon building
                break;
        }
    }

    @Override
    public int getItemCount() {
        return elevatorItems.size(); // Return the number of elevator items
    }

    // ViewHolder for holding and reusing views in the RecyclerView
    static class ElevatorViewHolder extends RecyclerView.ViewHolder {

        // UI elements in item_elevator.xml
        TextView buildingName;
        TextView elevatorTime;
        TextView floors;
        TextView capacity;
        ImageView buildingIcon;

        public ElevatorViewHolder(View itemView) {
            super(itemView);
            buildingName = itemView.findViewById(R.id.buildingName);
            elevatorTime = itemView.findViewById(R.id.elevatorTime);
            floors = itemView.findViewById(R.id.floors);
            capacity = itemView.findViewById(R.id.capacity);
            buildingIcon = itemView.findViewById(R.id.building_icon);
        }
    }

    // Class to represent a single elevator item
    static class ElevatorItem {
        private String buildingName;
        private int waitingTime;
        private int elevatorNumber;
        private int floors;
        private int capacity;

        public ElevatorItem(String buildingName, int waitingTime, int elevatorNumber, int floors, int capacity) {
            this.buildingName = buildingName;
            this.waitingTime = waitingTime;
            this.elevatorNumber = elevatorNumber;
            this.floors = floors;
            this.capacity = capacity;
        }

        public String getBuildingName() {
            return buildingName;
        }

        public int getWaitingTime() {
            return waitingTime;
        }

        public int getElevatorNumber() {
            return elevatorNumber;
        }

        public int getFloors() {
            return floors;
        }

        public int getCapacity() {
            return capacity;
        }
    }
    public void updateBuildings(List<Building> newBuildingList) {
        this.elevatorItems.clear(); // Clear the current list

        // Rebuild the elevator items list with the new buildings
        for (Building building : newBuildingList) {
            for (int i = 0; i < building.getElevators().size(); i++) {
                elevatorItems.add(new ElevatorItem(
                        building.getName(),
                        building.getElevators().get(i),
                        i + 1,
                        building.getFloors(),
                        building.getCapacityForElevator(i)
                ));
            }
        }

        notifyDataSetChanged(); // Notify RecyclerView that data has changed
    }

}