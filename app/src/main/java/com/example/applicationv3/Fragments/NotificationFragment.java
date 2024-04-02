package com.example.applicationv3.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.applicationv3.Adapter.NotificationAdapter;
import com.example.applicationv3.DataManager;
import com.example.applicationv3.MasterActivity;
import com.example.applicationv3.Model.Notification;
import com.example.applicationv3.R;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


// Dans votre fragment de notification
public class NotificationFragment extends Fragment {
    private RecyclerView recyclerView;
    private NotificationAdapter notificationAdapter;
    private FirebaseFirestore db;
    private Map<String, Notification> cachedNotifications;
    private List<String> cachedUsernames = new ArrayList<>();
    private FirebaseUser fUser;
    private DataManager dataManager;
    private List<Notification> notifications;
    String profileID;
    private boolean isDataCached = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_notifications);
        dataManager = DataManager.getInstance();

        // Utilisez l'ID de l'utilisateur depuis le DataManager
        String profileID = MasterActivity.getLoggedInUserId();


        cachedNotifications = MasterActivity.getCachedNotification();
        Log.d("NotificationFragment", "Nombre de notifications: " + cachedNotifications.size());
        if (cachedNotifications != null) {

            List<Notification> notificationList = new ArrayList<>(cachedNotifications.values());
            // Tri de la liste de notifications par createdDate (du plus récent au plus ancien)
            Collections.sort(notificationList, (notification1, notification2) ->
                    Long.compare(notification2.getCreatedDate(), notification1.getCreatedDate()));

            // Mettez à jour l'adaptateur avec les nouvelles données triées
            updateAdapter(notificationList);
        }
        //ICI


        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    private void updateAdapter(List<Notification> notificationList) {
        notificationAdapter = new NotificationAdapter(notificationList, getContext());
        recyclerView.setAdapter(notificationAdapter);
    }


}