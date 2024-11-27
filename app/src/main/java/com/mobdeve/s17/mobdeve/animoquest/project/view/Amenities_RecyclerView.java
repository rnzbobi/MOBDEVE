package com.mobdeve.s17.mobdeve.animoquest.project.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mobdeve.s17.mobdeve.animoquest.project.R;

import java.util.List;
import java.util.Map;

public class Amenities_RecyclerView extends RecyclerView.Adapter<Amenities_RecyclerView.MyViewHolder> {
    private String floor;
    private Map<String, List<String>> amenities;

    // Constructor
    public Amenities_RecyclerView(String floor, Map<String, List<String>> amenities) {
        this.floor = floor;
        this.amenities = amenities;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView cardText;
        ImageView cardImage;
        public MyViewHolder(View itemView) {
            super(itemView);
            cardText = itemView.findViewById(R.id.cardText);
            cardImage = itemView.findViewById(R.id.cardImage);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_indoor_amenities, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String currentFloor = floor;

        List<String> floorAmenities = amenities.get(currentFloor);

        if (floorAmenities != null && position < floorAmenities.size()) {
            String amenity = floorAmenities.get(position);
            holder.cardText.setText(amenity);

            int imageResId = getImageResourceForAmenity(amenity);
            if (imageResId != 0) {
                holder.cardImage.setImageResource(imageResId);
            } else {
                holder.cardImage.setImageResource(R.mipmap.ic_launcher_round);
            }
        } else {
            holder.cardText.setText("No amenities available");
            holder.cardImage.setImageResource(R.mipmap.ic_launcher_round);
        }
    }

    private int getImageResourceForAmenity(String amenity) {
        switch (amenity.toLowerCase()) {
            case "seats":
                return R.drawable.seat;
            case "water fountain":
                return R.drawable.waterfountain;
            case "atms":
                return R.drawable.atm;
            case "cafe":
                return R.drawable.cafe;
            case "oas / esh":
                return R.drawable.admission;
            case "discussion rooms":
                return R.drawable.discussion;
            case "vending machines":
                return R.drawable.vendingmachine;
            case "restroom":
                return R.drawable.restroom;
            case "lounge":
                return R.drawable.lounge;
            case "library":
                return R.drawable.library;
            default:
                return 0; // Return 0 if no matching resource is found
        }
    }

    @Override
    public int getItemCount() {
        List<String> floorAmenities = amenities.get(floor);
        return (floorAmenities != null) ? floorAmenities.size() : 0;
    }
}
