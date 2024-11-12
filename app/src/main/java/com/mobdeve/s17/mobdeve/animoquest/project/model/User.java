package com.mobdeve.s17.mobdeve.animoquest.project.model;

public class User {
    public String firstName;
    public String lastName;
    public String idNumber;
    public String email;


    public User() {
    }

    public User(String firstName, String lastName, String idNumber, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.idNumber = idNumber;
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getIdNumber() {
        return idNumber;
    }
}

