package com.example.applicationv3.Model;

import java.util.List;

public class PlaceUse {
    private String id;
    private String title;
    private String titleAdress;
    private String description;
    private double latitude;
    private double longitude;
    private long endDate;
    private long startDate;
    private List<String> ownersId;
    private String imageUrl;
    private List<String> contactsAllowed;

    public PlaceUse(){

    }
    public PlaceUse(String id, String title,String description, String titleAdress, double latitude, double longitude, long endDate, long startDate, List<String> ownersId,List<String> contactsAllowed, String imageUrl) {
        this.id = id;
        this.title = title;
        this.titleAdress = titleAdress;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.endDate = endDate;
        this.startDate = startDate;
        this.ownersId = ownersId;
        this.contactsAllowed = contactsAllowed;
        this.imageUrl = imageUrl;
    }

    public String getId() {return id;}

    public void setId(String id) {this.id = id;}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {return description;}

    public void setDescription(String description) {this.description = description;}

    public String getTitleAdress() {
        return titleAdress;
    }

    public void setTitleAdress(String titleAdress) {
        this.titleAdress = titleAdress;
    }

    public double getLatitude() {return latitude;}

    public void setLatitude(double latitude) {this.latitude = latitude;}

    public double getLongitude() {return longitude;}

    public void setLongitude(double longitude) {this.longitude = longitude;}

    public long getEndDate() {return endDate;}

    public void setEndDate(long endDate) {this.endDate = endDate;}

    public long getStartDate() {return startDate;}

    public void setStartDate(long startDate) {this.startDate = startDate;}

    public List<String> getOwnerId() {return ownersId;}

    public void setOwnerId(List<String> ownersId) {this.ownersId = ownersId;}

    public List<String> getContactsAllowed() {return contactsAllowed;}

    public void setContactsAllowed(List<String> contactsAllowed) {this.contactsAllowed = contactsAllowed;}

    public String getImageUrl() {return imageUrl;}

    public void setImageUrl(String imageurl) {this.imageUrl = imageurl;}

    @Override
    public String toString() {
        return title; // Utilisé pour afficher les titres dans le Spinner
    }
}
