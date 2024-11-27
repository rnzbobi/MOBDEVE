package com.mobdeve.s17.mobdeve.animoquest.project.view;

import java.util.List;

public class Building {
    private String name;
    private List<Integer> elevators;
    private int noOfFloors;
    private List<Integer> capacities;  // Updated: List of capacities for each elevator

    public Building(String name, List<Integer> elevators, int noOfFloors, List<Integer> capacities) {
        this.name = name;
        this.elevators = elevators;
        this.noOfFloors = noOfFloors;
        this.capacities = capacities;
    }

    public String getName() {
        return name;
    }

    public List<Integer> getElevators() {
        return elevators;
    }

    public int getFloors() {
        return noOfFloors;
    }

    public List<Integer> getCapacities() {
        return capacities;
    }

    public int getCapacityForElevator(int elevatorIndex) {
        return capacities.get(elevatorIndex);
    }
}
