package com.mobdeve.s17.mobdeve.animoquest.project.view;


import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mobdeve.s17.mobdeve.animoquest.project.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IndoorNavigationResultActivity extends AppCompatActivity {

    private final List<String> titlesList = new ArrayList<>();
    private final List<Integer> imagesList = new ArrayList<>();
    private final List<String> navigationList = new ArrayList<>();
    private final Map<String,List<String>> floorAmenitiesList = new LinkedHashMap<>();
    int buildingFloorCount;
    String buildingName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indoornavigationresult);

        buildingFloorCount = getIntent().getIntExtra("building_floorcount", 0);
        buildingName = getIntent().getStringExtra("building_name");

        postToList();
        System.out.println("Floor Amenities List: " + floorAmenitiesList);
        System.out.println("Amenities Map: " + navigationList);

        updateViews("Ground Floor");

        Widget_RecyclerView adapter1 = new Widget_RecyclerView(titlesList);
        RecyclerView recyclerView1 = findViewById(R.id.indoornavfloors);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView1.setLayoutManager(layoutManager1);
        recyclerView1.setAdapter(adapter1);

        Amenities_RecyclerView adapter2 = new Amenities_RecyclerView("Ground Floor", floorAmenitiesList);
        RecyclerView recyclerView2 = findViewById(R.id.indoornavamenities);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView2.setLayoutManager(layoutManager2);
        recyclerView2.setAdapter(adapter2);

        ImageView backBtn = findViewById(R.id.indoornavigationbackbtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Finish the current activity and return to the previous one
            }
        });
    }

    public void updateViews (String floor) {
        String navigationInfo = getNavigationInfo(floor);
        int floorNo = floor.equals("Ground Floor") ? 0 :
                floor.equals("2nd Floor") ? 1 :
                floor.equals("3rd Floor") ? 2 :
                floor.equals("4th Floor") ? 3 :
                floor.equals("5th Floor") ? 4 :
                floor.equals("6th Floor") ? 5 :
                floor.equals("7th Floor") ? 6 :
                floor.equals("8th Floor") ? 7 :
                floor.equals("9th Floor") ? 8 :
                floor.equals("10th Floor") ? 9 :
                floor.equals("11th Floor") ? 10 :
                floor.equals("12th Floor") ? 11 :
                floor.equals("14th Floor") ? 12 : 0;

        TextView indoornavdetails = findViewById(R.id.indoornavdetails);
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvTitle2 = findViewById(R.id.tvTitle2);
        ImageView indoornavimage = findViewById(R.id.indoornavimage);
        TextView flooramenitiestext = findViewById(R.id.flooramenitiestext);
        TextView detailstitle = findViewById(R.id.detailstitle);

        indoornavdetails.setText(navigationInfo);
        tvTitle.setText(floor);
        tvTitle2.setText("Henry Sy Sr. Hall");
        indoornavimage.setImageResource(imagesList.get(floorNo));
        flooramenitiestext.setText("Floor Amenities");
        detailstitle.setText("How to go there?");

        Amenities_RecyclerView adapter2 = new Amenities_RecyclerView(floor, floorAmenitiesList);
        RecyclerView recyclerView2 = findViewById(R.id.indoornavamenities);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView2.setLayoutManager(layoutManager2);
        recyclerView2.setAdapter(adapter2);
    }

    private void addToList(String title, int image, String navs, Map<String,List<String>> amenities) {
        titlesList.add(title);
        imagesList.add(image);
        navigationList.add(navs);
        floorAmenitiesList.putAll(amenities);
    }

    private void postToList() {

        for (int i = 1; i <= buildingFloorCount; i++) {
            if (i != 13) {
                Integer imageResId = getBuildingFloorMaps(buildingName).get(buildingName.toLowerCase() + i);
                String floorName = (i == 1 ? "Ground" : i == 2 ? i + "nd"
                                    : i == 3 ? i + "rd" : i + "th") + " Floor";
                String navs = getNavigationInfo(floorName);
                Map<String,List<String>> amenities = getFloorAmenities(floorName);
                if (imageResId != null) {
                    addToList(floorName, imageResId, navs, amenities);
                } else {
                    addToList(floorName, R.mipmap.ic_launcher_round, "null", amenities);
                }
            }
        }
    }

    private Map<String, Integer> getBuildingFloorMaps (String buildingName) {
        Map<String, Integer> resourceMap = new HashMap<>();

        switch (buildingName) {
            case "Henry":
                resourceMap.put("henry1", R.drawable.henry1);
                resourceMap.put("henry2", R.drawable.henry2);
                resourceMap.put("henry3", R.drawable.henry3);
                resourceMap.put("henry4", R.drawable.henry4);
                resourceMap.put("henry5", R.drawable.henry5);
                resourceMap.put("henry6", R.drawable.henry6);
                resourceMap.put("henry7", R.drawable.henry7);
                resourceMap.put("henry8", R.drawable.henry8);
                resourceMap.put("henry9", R.drawable.henry9);
                resourceMap.put("henry10", R.drawable.henry10);
                resourceMap.put("henry11", R.drawable.henry11);
                resourceMap.put("henry12", R.drawable.henry12);
                resourceMap.put("henry14", R.drawable.henry14);
            break;
            case "Andrew":
                break;
            default:

            break;
        }

        return resourceMap;
    }

    private Map<String, List<String>> getFloorAmenities(String floor) {
        Map<String, List<String>> amenitiesMap = new HashMap<>();
        List<String> amenities = new ArrayList<>();
        switch (floor) {
            case "Ground Floor":
                amenities.add("Seats");
                amenities.add("Restroom");
                amenities.add("Water Fountain");
                amenities.add("ATMs");
                amenities.add("Vending Machines");
                amenitiesMap.put("Ground Floor", amenities);
                break;
            case "2nd Floor":
                amenities.add("Seats");
                amenities.add("OAS / ESH");
                amenitiesMap.put("2nd Floor", amenities);
                break;
            case "3rd Floor":
                amenities.add("Seats");
                amenitiesMap.put("3rd Floor", amenities);
                break;
            case "4th Floor":
                amenities.add("Seats");
                amenitiesMap.put("4th Floor", amenities);
                break;
            case "5th Floor":
                amenities.add("Seats");
                amenitiesMap.put("5th Floor", amenities);
                break;
            case "6th Floor":
                amenities.add("Seats");
                amenities.add("Restroom");
                amenities.add("Water Fountain");
                amenities.add("Lounge");
                amenities.add("Cafe");
                amenitiesMap.put("6th Floor", amenities);
                break;
            case "7th Floor":
                amenities.add("Seats");
                amenities.add("Restroom");
                amenities.add("Water Fountain");
                amenities.add("Library");
                amenities.add("Discussion Rooms");
                amenitiesMap.put("7th Floor", amenities);
                break;
            case "8th Floor":
                amenities.add("Seats");
                amenities.add("Restroom");
                amenities.add("Water Fountain");
                amenities.add("Library");
                amenities.add("Discussion Rooms");
                amenitiesMap.put("8th Floor", amenities);
                break;
            case "9th Floor":
                amenities.add("Seats");
                amenities.add("Restroom");
                amenities.add("Water Fountain");
                amenities.add("Library");
                amenities.add("Discussion Rooms");
                amenitiesMap.put("9th Floor", amenities);
                break;
            case "10th Floor":
                amenities.add("Seats");
                amenities.add("Restroom");
                amenities.add("Water Fountain");
                amenities.add("Library");
                amenities.add("Discussion Rooms");
                amenitiesMap.put("10th Floor", amenities);
                break;
            case "11th Floor":
                amenities.add("Seats");
                amenities.add("Restroom");
                amenities.add("Water Fountain");
                amenities.add("Library");
                amenitiesMap.put("11th Floor", amenities);
                break;
            case "12th Floor":
                amenities.add("Seats");
                amenities.add("Restroom");
                amenities.add("Water Fountain");
                amenities.add("Library");
                amenitiesMap.put("12th Floor", amenities);
                break;
            case "14th Floor":
                amenities.add("Seats");
                amenities.add("Restroom");
                amenities.add("Water Fountain");
                amenities.add("Library");
                amenitiesMap.put("14th Floor", amenities);
                break;
            default:
                break;
        }
        return amenitiesMap;
    }

    private String getNavigationInfo(String floor) {
        String navInfo = "";

        switch (floor) {
            case "Ground Floor": navInfo = "Look for a big square-shaped building and you should be fine";
                break;
            case "2nd Floor": navInfo = "Go to the elevator or ride the escalator until you reach the floor. Floor levels are indicated in each floor when you use the escalator.";
                break;
            case "3rd Floor": navInfo = "Go to the elevator or ride the escalator until you reach the floor. Floor levels are indicated in each floor when you use the escalator.";
                break;
            case "4th Floor": navInfo = "Go to the elevator or ride the escalator until you reach the floor. Floor levels are indicated in each floor when you use the escalator.";
                break;
            case "5th Floor": navInfo = "Go to the elevator or ride the escalator until you reach the floor. Floor levels are indicated in each floor when you use the escalator.";
                break;
            case "6th Floor": navInfo = "Go to the elevator or ride the escalator until you reach the floor. Floor levels are indicated in each floor when you use the escalator.";
                break;
            case "7th Floor": navInfo = "After going to the 6th floor, go inside the lounge and use the elevator or the escalator to go up to the 7th floor.";
                break;
            case "8th Floor": navInfo = "After going to the 6th floor, go inside the lounge and use the elevator or the escalator to go up to the 8th floor.";
                break;
            case "9th Floor": navInfo = "After going to the 6th floor, go inside the lounge and use the elevator or the escalator to go up to the 9th floor.";
                break;
            case "10th Floor": navInfo = "After going to the 6th floor, go inside the lounge and use the elevator or the escalator to go up to the 10th floor.";
                break;
            case "11th Floor": navInfo = "After going to the 6th floor, go inside the lounge and use the elevator or the escalator to go up to the 11th floor.";
                break;
            case "12th Floor": navInfo = "After going to the 6th floor, go inside the lounge and use the elevator or the escalator to go up to the 12th floor.";
                break;
            case "14th Floor": navInfo = "After going to the 6th floor, go inside the lounge and use the elevator or the escalator to go up to the 14th floor.";
                break;
            default:
                break;
        }

        return navInfo;
    }
}


