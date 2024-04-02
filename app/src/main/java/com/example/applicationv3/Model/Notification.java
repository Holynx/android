package com.example.applicationv3.Model;

public class Notification {

    private String id;
    private String receiverId;
    private String senderId;
    private Boolean isNew;
    private long createdDate;
    private String eventId;
    private String placeId;
    private String message;
    public Notification(){
    }

    public Notification(String id, String receiverId, String senderId, Boolean isNew, long createdDate, String eventId, String placeId, String message) {
        this.id = id;
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.isNew=isNew;
        this.createdDate = createdDate;
        this.eventId = eventId;
        this.placeId=placeId;
        this.message=message;

    }

    public String getId() {return id;}

    public void setId(String id) {this.id = id;}

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public Boolean getIsNew() {return isNew;}

    public void setIsNew(Boolean aNew) {isNew = aNew;}

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
