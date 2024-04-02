package com.example.applicationv3.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.applicationv3.Model.EventUse;
import com.example.applicationv3.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FriendEventAdapter extends RecyclerView.Adapter<FriendEventAdapter.EventViewHolder> {
    private List<EventUse> eventUseList;
    private String profileID;
    private String userID;
    private long selectedMillis;
    private String UserName;

    public FriendEventAdapter(List<EventUse> eventUseList, String profileID, String userID, long selectedMillis, String UserName) {
        this.eventUseList = eventUseList;
        this.profileID = profileID;
        this.userID = userID;
        this.selectedMillis = selectedMillis;
        this.UserName = UserName;

    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_event_item, parent, false);
        return new EventViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventUse eventUse = eventUseList.get(position);
        holder.titleTextView.setText(eventUse.getTitle());
        holder.adressTextView.setText(eventUse.getTitleAdress());

        long startTimestamp = (eventUse.getStartDate()) * 1000; // Convertir les secondes en millisecondes
        String formattedStartDate = formatDate(startTimestamp);
        long endTimestamp = (eventUse.getEndDate()) * 1000; // Convertir les secondes en millisecondes
        String formattedEndDate = formatDate(endTimestamp);
        holder.dateTextView.setText("From " + formattedStartDate + " To " + formattedEndDate);


        holder.poke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date currentDate1 = new Date();
                long timestampSeconds = currentDate1.getTime() / 1000;

                // Convertir selectedMillis en Date
                Date selectedDate = new Date(selectedMillis);
                java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String formattedDate = dateFormat.format(selectedDate);

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String message = UserName + " veut vous voir à " + eventUse.getTitleAdress() + " le " + formattedDate ;
                Map<String, Object> notificationData = new HashMap<>();
                notificationData.put("senderId", userID);
                notificationData.put("receiverId", profileID);
                notificationData.put("isNew", true);
                notificationData.put("createdDate", timestampSeconds);
                notificationData.put("eventId", eventUse.getId());
                notificationData.put("placeId", "");
                notificationData.put("message", message);
                db.collection("notifications")
                        .add(notificationData)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                String notificationId = documentReference.getId();
                                db.collection("notifications").document(notificationId).update("id", notificationId);
                                Toast.makeText(holder.itemView.getContext(), "Poke sent", Toast.LENGTH_SHORT).show();

                            }
                        });
            }
        });

    }
    private String formatDate(long timestamp) {
        Date date = new Date(timestamp);
        android.icu.text.SimpleDateFormat sdf = new android.icu.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(date);
    }

    @Override
    public int getItemCount() {
        return eventUseList.size();
    }



    public class EventViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView adressTextView;
        TextView dateTextView;
        LinearLayout poke;


        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.eventname);
            adressTextView = itemView.findViewById(R.id.eventplace);
            dateTextView = itemView.findViewById(R.id.eventdate);
            poke = itemView.findViewById(R.id.poke);

        }
    }
}