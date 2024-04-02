package com.example.applicationv3;

public interface DataCallback<T> {
    void onDataReceived(T data);
    void onError(Exception e);
}
