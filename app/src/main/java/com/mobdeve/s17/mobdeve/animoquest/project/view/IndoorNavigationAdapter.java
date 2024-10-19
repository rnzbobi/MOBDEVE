package com.mobdeve.s17.mobdeve.animoquest.project.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.mobdeve.s17.mobdeve.animoquest.project.R;
import java.util.ArrayList;
import java.util.List;

// Adapter for the RecyclerView
public class IndoorNavigationAdapter extends RecyclerView.Adapter<IndoorNavigationAdapter.IndoorNavigationViewHolder> {

    // List of building items (each building will be its own card)
    private List<NavigationItem> navigationItems;

    // Constructor for IndoorNavigationAdapter, taking a list of buildings
    public IndoorNavigationAdapter(List<Building> buildingList) {
        navigationItems = new ArrayList<>();
        updateBuildings(buildingList);  // Initialize with provided building list
    }

    @Override
    public IndoorNavigationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_indoor_navigation, parent, false);
        return new IndoorNavigationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(IndoorNavigationViewHolder holder, int position) {
        NavigationItem navigationItem = navigationItems.get(position);

        // Set the building name
        holder.buildingName.setText(navigationItem.getBuildingName());

        // Set the correct image for each building dynamically
        switch (navigationItem.getBuildingName()) {
            case "Henry":
                holder.buildingIcon.setImageResource(R.drawable.henry_photo);
                break;
            case "La Salle Hall":
                holder.buildingIcon.setImageResource(R.drawable.lasallehall_photo);
                break;
            case "Andrew":
                holder.buildingIcon.setImageResource(R.drawable.andrew_photo);
                break;
            case "Razon":
                holder.buildingIcon.setImageResource(R.drawable.razon_photo);
                break;
            case "Goks":
                holder.buildingIcon.setImageResource(R.drawable.gokongwei_photo);
                break;
        }



        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the BottomSheetDialog when clicked
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(holder.itemView.getContext());
                View bottomSheetView = LayoutInflater.from(holder.itemView.getContext())
                        .inflate(R.layout.indoornavigation_popup, null);

                // Set the title, message, and image inside the bottom sheet
                TextView title = bottomSheetView.findViewById(R.id.indoornavigation_title);
                TextView floor = bottomSheetView.findViewById(R.id.floor);
                TextView room = bottomSheetView.findViewById(R.id.room);
                Spinner floorSpinner = bottomSheetView.findViewById(R.id.floorspinner);
                Spinner roomSpinner = bottomSheetView.findViewById(R.id.roomspinner);
                Button closeButton = bottomSheetView.findViewById(R.id.navigate_btn);

                title.setText(navigationItem.getBuildingName());


                // Close button action
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                    }
                });

                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return navigationItems.size();  // Return the number of buildings
    }

    // ViewHolder for holding and reusing views in the RecyclerView
    static class IndoorNavigationViewHolder extends RecyclerView.ViewHolder {

        // UI elements in item_buildingnav.xml
        TextView buildingName;
        ImageView buildingIcon;

        public IndoorNavigationViewHolder(View itemView) {
            super(itemView);
            buildingName = itemView.findViewById(R.id.buildingName);
            buildingIcon = itemView.findViewById(R.id.buildingIcon);
        }
    }

    // Class to represent a single building item
    static class NavigationItem {
        private String buildingName;
        private int floors;

        public NavigationItem(String buildingName, int floors) {
            this.buildingName = buildingName;
            this.floors = floors;
        }

        public String getBuildingName() {
            return buildingName;
        }

        public int getFloors() {
            return floors;
        }
    }

    // Update the list of buildings (only one entry per building)
    public void updateBuildings(List<Building> newBuildingList) {
        this.navigationItems.clear();  // Clear the current list

        // Add each building as a single item (no elevator duplication)
        for (Building building : newBuildingList) {
            navigationItems.add(new NavigationItem(
                    building.getName(),
                    building.getFloors()
            ));
        }

        notifyDataSetChanged();  // Notify RecyclerView that data has changed
    }
}
