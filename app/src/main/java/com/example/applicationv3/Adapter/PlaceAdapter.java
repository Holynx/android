package com.example.applicationv3.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.icu.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.applicationv3.Model.PlaceUse;
import com.example.applicationv3.MyPlaceActivity;
import com.example.applicationv3.R;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder> {

    private List<PlaceUse> placeList;
    private Context context;
    private String profileID;
    private Resources resources;


    public PlaceAdapter(List<PlaceUse> placeList, Context context, String profileID) {
        this.placeList = placeList;
        this.context = context;
        this.profileID = profileID;
        this.resources = context.getResources();
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.place_item, parent, false);
        return new PlaceAdapter.PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        PlaceUse place = placeList.get(position);
        holder.placeName.setText(place.getTitle());
        holder.location.setText(place.getTitleAdress());

        long startTimestamp = place.getStartDate() * 1000; // Convertir les secondes en millisecondes
        String formattedStartDate = formatDate(startTimestamp);
        long endTimestamp = place.getEndDate() * 1000; // Convertir les secondes en millisecondes
        String formattedEndDate = formatDate(endTimestamp);
        if (endTimestamp ==4102441200000L){
            holder.eventPlace.setText(resources.getString(R.string.place));
            holder.date.setVisibility(View.GONE);
        }else {
            holder.eventPlace.setText(resources.getString(R.string.event));
            holder.date.setText(resources.getString(R.string.from) + " " + formattedStartDate + " " +resources.getString(R.string.to) +" " + formattedEndDate);
        }
        int numberOfContactsAllowed = 0;

        // Vérification de nullité de la liste contactsAllowed
        if (place.getContactsAllowed() != null) {
            numberOfContactsAllowed = place.getContactsAllowed().size();
        }

        holder.numberPeople.setText(String.valueOf(numberOfContactsAllowed));

        // Vérifiez si l'URL de l'image est vide ou null
        if (place.getImageUrl() != null && !place.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(place.getImageUrl())
                    .into(holder.imageProfile);
        } else {
            holder.imageProfile.setImageResource(R.drawable.place_bydefault);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String selectedPlaceId = place.getId();
                Intent intent = new Intent(context, MyPlaceActivity.class);
                intent.putExtra("PLACE_ID", selectedPlaceId);
                context.startActivity(intent);
            }
        });

    }

    private String formatDate(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(date);
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    static class PlaceViewHolder extends RecyclerView.ViewHolder {
        public TextView placeName;
        public TextView location;
        public TextView date;
        public TextView eventPlace;
        public TextView numberPeople;
        public CircleImageView imageProfile;



        PlaceViewHolder(View itemView) {
            super(itemView);
            placeName = itemView.findViewById(R.id.title);
            location = itemView.findViewById(R.id.location);
            imageProfile = itemView.findViewById(R.id.image_profile);
            date = itemView.findViewById(R.id.date);
            eventPlace = itemView.findViewById(R.id.eventPlace);
            numberPeople = itemView.findViewById(R.id.numberPeople);

        }
    }
}
