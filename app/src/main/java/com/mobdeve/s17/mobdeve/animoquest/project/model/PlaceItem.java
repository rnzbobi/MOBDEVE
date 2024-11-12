// PlaceItem.java
package com.mobdeve.s17.mobdeve.animoquest.project.model;

public class PlaceItem {
    private String name;
    private String address;
    private double latitude;
    private double longitude;

    // Required empty constructor for Firebase
    public PlaceItem() {}

    public PlaceItem(String name, String address, double latitude, double longitude) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and setters
    public String getName() { return name; }

    public String getAddress() { return address; }

    public double getLatitude() { return latitude; }

    public double getLongitude() { return longitude; }
}
