package com.example.applicationv3.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.applicationv3.EditEventActivity;
import com.example.applicationv3.Fragments.ProfileFragment;
import com.example.applicationv3.MasterActivity;
import com.example.applicationv3.Model.EventUse;
import com.example.applicationv3.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<EventUse> eventUseList;
    private Context context;
    private EventAdapterListener listener;
    private ProfileFragment profileFragment;

    // Ajout d'une interface pour la suppression d'éléments
    public interface ItemTouchHelperAdapter {
        void onItemDismiss(int position);
    }



    public EventAdapter(List<EventUse> eventUseList, Context context) {
        this.eventUseList = eventUseList;
        this.context = context;
        this.profileFragment = profileFragment;


    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.event_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventUse eventUse = eventUseList.get(position);
        holder.titleTextView.setText(eventUse.getTitle());



        long startTimestamp = (eventUse.getStartDate()) * 1000; // Convertir les secondes en millisecondes
        String formattedStartDate = formatDate(startTimestamp);
        long endTimestamp = (eventUse.getEndDate()) * 1000; // Convertir les secondes en millisecondes
        String formattedEndDate = formatDate(endTimestamp);
        holder.DateTextView.setText("From " + formattedStartDate + " To " + formattedEndDate);

        holder.LocTextView.setText(eventUse.getTitleAdress());


        if (eventUse.getToBeShared()) {
            holder.privateEvent.setVisibility(View.GONE);
            holder.publicEvent.setVisibility(View.VISIBLE);

        } else {
            holder.privateEvent.setVisibility(View.VISIBLE);
            holder.publicEvent.setVisibility(View.GONE);
        }

        holder.privateEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.privateEvent.setVisibility(View.GONE);
                holder.publicEvent.setVisibility(View.VISIBLE);
                eventUse.setToBeShared(true);
                FirebaseFirestore.getInstance().collection("events").document(eventUse.getId())
                        .update("toBeShared", true)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Mise à jour réussie dans Firestore
                                MasterActivity.updateEventInCachedUserEvents(eventUse.getId(),eventUse);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("EventAdapter", "Error updating event in Firestore: " + e.getMessage());
                            }
                        });
            }
        });
        holder.publicEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.privateEvent.setVisibility(View.VISIBLE);
                holder.publicEvent.setVisibility(View.GONE);
                eventUse.setToBeShared(false);
                FirebaseFirestore.getInstance().collection("events").document(eventUse.getId())
                        .update("toBeShared", false)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                MasterActivity.updateEventInCachedUserEvents(eventUse.getId(),eventUse);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("EventAdapter", "Error updating event in Firestore: " + e.getMessage());
                            }
                        });
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedEventId = eventUse.getId();
                long selectedEventBeg = eventUse.getStartDate();
                long selectedEventEnd = eventUse.getEndDate();
                String selectedEventPlace = eventUse.getPlaceId();
                String selectedEventTitle = eventUse.getTitle();
                String selectedEventAdress = eventUse.getTitleAdress();
                double selectedEventLat = eventUse.getLatitude();
                double selectedEventLong = eventUse.getLongitude();

                Intent intent = new Intent(context, EditEventActivity.class);
                intent.putExtra("EVENT_ID", selectedEventId);
                intent.putExtra("EVENT_BEG", selectedEventBeg);
                intent.putExtra("EVENT_END", selectedEventEnd);
                intent.putExtra("EVENT_PLACE", selectedEventPlace);
                intent.putExtra("EVENT_TITLE", selectedEventTitle);
                intent.putExtra("EVENT_ADDRESS", selectedEventAdress);
                intent.putExtra("EVENT_LATITUDE", selectedEventLat);
                intent.putExtra("EVENT_LONGITUDE", selectedEventLong);
                context.startActivity(intent);
            }
        });

    }
    private String formatDate(long timestamp) {
        Date date = new Date(timestamp);
        android.icu.text.SimpleDateFormat sdf = new android.icu.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(date);
    }

    // Nouvelle méthode pour mettre à jour la liste d'événements après le glissement
    public void updateEventsList(List<EventUse> eventUses) {
        this.eventUseList = eventUses;
        notifyDataSetChanged();
    }



    // Ajout d'un ItemTouchHelper
    public static class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

        private final EventAdapter mAdapter;
        private Drawable background;
        private final ColorDrawable deleteBackgroundColor;
        private Drawable deleteIcon;

        public SwipeToDeleteCallback(EventAdapter adapter, Context context) {
            super(0, ItemTouchHelper.RIGHT);
            mAdapter = adapter;
            deleteBackgroundColor = new ColorDrawable(ContextCompat.getColor(context, R.color.red));
            deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete);
            int deleteColor = ContextCompat.getColor(context, R.color.main_color1);
            deleteIcon.setColorFilter(deleteColor, PorterDuff.Mode.SRC_ATOP);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            // Supprimer l'élément lors du swipe
            int position = viewHolder.getAdapterPosition();
            mAdapter.onItemDismiss(position);
        }
        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            View itemView = viewHolder.itemView;
            int backgroundCornerOffset = 20; // Adjust this value as needed for the corner radius of your background
            if (dX > 0) {
                deleteBackgroundColor.setBounds(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + ((int) dX) + backgroundCornerOffset, itemView.getBottom());
            } else {
                deleteBackgroundColor.setBounds(0, 0, 0, 0);
            }
            deleteBackgroundColor.draw(c);
            if (dX > 0) {
                int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                int iconTop = itemView.getTop() + (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                int iconBottom = iconTop + deleteIcon.getIntrinsicHeight();
                int iconLeft = itemView.getLeft() + iconMargin;
                int iconRight = itemView.getLeft() + iconMargin + deleteIcon.getIntrinsicWidth();
                deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                deleteIcon.draw(c);
            }

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }

    public void onItemDismiss(int position) {
        EventUse eventUse = eventUseList.get(position);
        Log.d("EventActivity", "EventId2: " + eventUse.getId());
        eventUseList.remove(position);
        notifyItemRemoved(position);
        deleteEventFromFirestore(eventUse);

        //Map<String, EventUse> cachedUserEvents = MasterActivity.getCachedUserEvents();
        profileFragment.removeFromCompactCalendarView(eventUse);

    }

    private void deleteEventFromFirestore(EventUse eventUse) {

        FirebaseFirestore.getInstance().collection("events").document(eventUse.getId()).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        MasterActivity.removeEventFromCachedUserEvents(eventUse.getId());
                    }
                });
        //ICI
    }

    // Ajout d'une méthode pour attacher l'ItemTouchHelper au RecyclerView
    public static void attachSwipeToDelete(ItemTouchHelper itemTouchHelper, RecyclerView recyclerView) {
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public static void attachSwipeToEdit(ItemTouchHelper itemTouchHelper, RecyclerView recyclerView) {
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }


    @Override
    public int getItemCount() {
        return eventUseList.size();
    }

    public class EventViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView startDateTextView;
        TextView DateTextView;
        TextView LocTextView;
        ImageView deleteEvent;
        ImageView editEvent;
        LinearLayout publicEvent;
        LinearLayout privateEvent;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.eventname);
            DateTextView = itemView.findViewById(R.id.eventdate);
            LocTextView = itemView.findViewById(R.id.eventplace);
            publicEvent = itemView.findViewById(R.id.publicEvent);
            privateEvent = itemView.findViewById(R.id.privateEvent);


        }
    }

    public interface EventAdapterListener {
        void onItemDismiss(int position);
        void removeEventsFromCompactCalendar(EventUse eventUse);
        void onItemClick(EventUse eventUse);
    }
    public void setProfileFragment(ProfileFragment profileFragment) {
        this.profileFragment = profileFragment;
    }

}
