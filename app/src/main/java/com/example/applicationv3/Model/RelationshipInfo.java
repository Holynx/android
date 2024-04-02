package com.example.applicationv3.Model;

import java.util.List;

public class RelationshipInfo {
    private List<String> friends;
    private List<String> pending;
    private List<String> asked;

    public RelationshipInfo() {
    }

    public RelationshipInfo(String status, String buttonText) {
        if ("friends".equals(status)) {
            this.friends = friends;
        } else if ("pending".equals(status)) {
            this.pending = pending;
        } else if ("asked".equals(status)) {
            this.asked = asked;
        }
    }

    public List<String> getFriends() {
        return friends;
    }

    public void setFriends(List<String> friends) {
        this.friends = friends;
    }

    public List<String> getPending() {
        return pending;
    }

    public void setPending(List<String> pending) {
        this.pending = pending;
    }

    public List<String> getAsked() {
        return asked;
    }

    public void setAsked(List<String> asked) {
        this.asked = asked;
    }
}