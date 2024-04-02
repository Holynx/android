package com.example.applicationv3.Model;

import java.util.List;

public class FriendIds {

    private List<String> friends;

    public FriendIds() {
    }

    public FriendIds(List<String> friends) {
        this.friends = friends;
    }

    public List<String> getFriends() {
        return friends;
    }

    public void setFriends(List<String> friends) {
        this.friends = friends;
    }
}
