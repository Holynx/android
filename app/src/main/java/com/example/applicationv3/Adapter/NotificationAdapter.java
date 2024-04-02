package com.example.applicationv3.Adapter;


import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.applicationv3.Model.Notification;
import com.example.applicationv3.MyPlaceActivity;
import com.example.applicationv3.ProfileActivity;
import com.example.applicationv3.R;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private List<Notification> notificationList;
    private Context context;


    public NotificationAdapter(List<Notification> notificationList, Context context) {
        this.notificationList = notificationList;
        this.context = context;
        }
    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item, parent, false);
        return new NotificationViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);

        if (notification != null) {
            holder.commentTextView.setText(notification.getMessage());

            if (notification.getEventId().isEmpty() && !notification.getPlaceId().isEmpty()) {
                holder.imageNotification.setImageResource(R.drawable.ic_notif_place);
                // Vérifiez si le type de la notification est "new friend"
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Ouvrez le ProfileActivity avec l'ID du sender
                        Intent intent = new Intent(context, MyPlaceActivity.class);
                        intent.putExtra("PLACE_ID", notification.getPlaceId());
                        context.startActivity(intent);
                    }
                });
            }
            if (notification.getPlaceId().isEmpty() && !notification.getEventId().isEmpty()) {
                holder.imageNotification.setImageResource(R.drawable.ic_notif_poke);
                // Vérifiez si le type de la notification est "new friend"
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ProfileActivity.class);
                        intent.putExtra("clickedID", notification.getSenderId());
                        context.startActivity(intent);
                    }
                });
            }
            if (notification.getPlaceId().isEmpty() && notification.getEventId().isEmpty()) {
                holder.imageNotification.setImageResource(R.drawable.ic_notif_friend);
                // Vérifiez si le type de la notification est "new friend"
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Ouvrez le ProfileActivity avec l'ID du sender
                        Intent intent = new Intent(context, ProfileActivity.class);
                        intent.putExtra("clickedID", notification.getSenderId());
                        context.startActivity(intent);
                    }
                });
            }


            long createdDateMillis = notification.getCreatedDate()*1000;
            String formattedElapsedTime = getFormattedElapsedTime(createdDateMillis);
            holder.notifDate.setText(formattedElapsedTime);
            Log.d("NotificationAdapter", "Notification Message: " + notification.getMessage());
            Log.d("NotificationAdapter", "Notification isNew: " + notification.getIsNew());

            if (notification.getIsNew() != null && notification.getIsNew()) {
                holder.notificationItemLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.second_color3));
            } else {
                holder.notificationItemLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            }

        }

    }


    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder {
        private TextView usernameTextView;
        private TextView commentTextView;
        private ImageView imageNotification;
        private LinearLayout notificationItemLayout;
        private TextView notifDate;

        public NotificationViewHolder(View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.username);
            commentTextView = itemView.findViewById(R.id.comment);
            imageNotification = itemView.findViewById(R.id.image_notification);
            notificationItemLayout = itemView.findViewById(R.id.notification_item_layout);
            notifDate = itemView.findViewById(R.id.notifDate);

        }
    }
    private String getFormattedElapsedTime(long createdDateMillis) {
        long currentTimeMillis = System.currentTimeMillis();
        long elapsedTimeMillis = currentTimeMillis - createdDateMillis;

        if (elapsedTimeMillis < DateUtils.MINUTE_IN_MILLIS) {
            // Moins d'une minute
            return "Il y a " + (elapsedTimeMillis / DateUtils.SECOND_IN_MILLIS) + " secondes";
        } else if (elapsedTimeMillis < DateUtils.HOUR_IN_MILLIS) {
            // Moins d'une heure
            return "Il y a " + (elapsedTimeMillis / DateUtils.MINUTE_IN_MILLIS) + " minutes";
        } else if (elapsedTimeMillis < DateUtils.DAY_IN_MILLIS) {
            // Moins de 24 heures
            return "Il y a " + (elapsedTimeMillis / DateUtils.HOUR_IN_MILLIS) + " heures";
        } else if (elapsedTimeMillis < DateUtils.DAY_IN_MILLIS * 30) {
            // Moins d'un mois
            return "Il y a " + (elapsedTimeMillis / DateUtils.DAY_IN_MILLIS) + " jours";
        } else {
            // Plus d'un mois
            return DateUtils.formatDateTime(context, createdDateMillis, DateUtils.FORMAT_SHOW_DATE);
        }
    }
}
