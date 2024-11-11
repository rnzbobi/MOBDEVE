package com.mobdeve.s17.mobdeve.animoquest.project.model;

public class MarkerInfo {
    private String description;
    private String drawableName;

    public MarkerInfo(String description, String drawableName) {
        this.description = description;
        this.drawableName = drawableName;
    }

    public String getDescription() {
        return description;
    }

    public String getDrawableName() {
        return drawableName;
    }
}
