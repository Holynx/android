package com.example.applicationv3.Model;

import com.google.firebase.database.PropertyName;

public class User {

    private String name;
    @PropertyName("email")
    private String email;
    @PropertyName("username")
    private String username;
    private String bio;
    private String imageUrl;
    private String id;
    private String defaultAdress;
    private double latitude;
    private double longitude;

    public User() {
    }

    public User(String name, String email, String username, String bio, String imageUrl, String id, String defaultAdress, double latitude, double longitude) {
        this.name = name;
        this.email = email;
        this.username = username;
        this.bio = bio;
        this.imageUrl = imageUrl;
        this.id = id;
        this.defaultAdress = defaultAdress;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDefaultAdress() {
        return defaultAdress;
    }

    public void setDefaultAdress(String defaultAdress) {
        this.defaultAdress = defaultAdress;
    }

    public double getLatitude() {return latitude;}

    public void setLatitude(double latitude) {this.latitude = latitude;}

    public double getLongitude() {return longitude;}

    public void setLongitude(double longitude) {this.longitude = longitude;}
}
