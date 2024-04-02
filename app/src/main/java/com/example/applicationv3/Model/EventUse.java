package com.example.applicationv3.Model;

public class EventUse {


    private String title;
    private long startDate;
    private long endDate;
    private String placeId;
    private String titleAdress;
    private Boolean toBeShared;
    private String id;
    private double latitude;
    private double longitude;
    private String ownerId;



    public EventUse(){

    }


    public EventUse(String title, long startDate, long endDate, String placeId, String titleAdress, Boolean toBeShared, String id, String ownerId, double latitude, double longitude) {
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.placeId = placeId;
        this.titleAdress = titleAdress;
        this.toBeShared = toBeShared;
        this.id = id;
        this.ownerId = ownerId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public String getPlaceId() {return placeId;}

    public void setPlaceId(String placeId) {this.placeId = placeId;}

    public String getTitleAdress() {
        return titleAdress;
    }

    public void setTitleAdress(String titleAdress) {
        this.titleAdress = titleAdress;
    }

    public Boolean getToBeShared() {
        return toBeShared;
    }

    public void setToBeShared(Boolean toBeShared) {
        this.toBeShared = toBeShared;
    }

    public String getId() { return id; }

    public void setId(String id) {this.id = id;}

    public String getOwnerId() {return ownerId;}

    public void setOwnerId(String ownerId) {this.ownerId = ownerId;}

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    
}
