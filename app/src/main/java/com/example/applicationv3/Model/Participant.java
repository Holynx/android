package com.example.applicationv3.Model;

public class Participant {

    private String name;
    private String username;
    private String imageUrl;
    private String id;
    private Boolean selected;
    private Boolean isAdmin;

    public Participant() {
    }

    public Participant(String name, String username, String imageUrl, String id) {
        this.name = name;
        this.username = username;
        this.imageUrl = imageUrl;
        this.id = id;
        this.selected = false;
        this.isAdmin = false;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageurl() {
        return imageUrl;
    }

    public void setImageurl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public Boolean getAdmin() {return isAdmin;}

    public void setAdmin(Boolean admin) {isAdmin = admin;}

}
