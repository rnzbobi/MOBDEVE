package com.mobdeve.s17.mobdeve.animoquest.project.model;

public class User {
    private String firstName;
    private String lastName;
    private String idNumber;
    private String email;
    private String hashedPassword;

    public User() {
    }

    public User(String firstName, String lastName, String idNumber, String email, String hashedPassword) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.idNumber = idNumber;
        this.email = email;
        this.hashedPassword = hashedPassword;
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

    public String getHashedPassword() {
        return hashedPassword;
    }
}

