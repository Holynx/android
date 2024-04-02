package com.example.applicationv3.Adapter;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.applicationv3.MasterActivity;
import com.example.applicationv3.Model.User;
import com.example.applicationv3.ProfileActivity;
import com.example.applicationv3.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class Attender2Adapter extends RecyclerView.Adapter<Attender2Adapter.AttenderViewHolder> {
    private Context mContext;
    private List<User> userList;
    private String profileID;

    public Attender2Adapter(List<User> userList, Context mContext) {
        this.mContext = mContext;
        this.userList = userList;
        profileID = MasterActivity.getLoggedInUserId();
    }

    public void updateData(List<User> newData) {
        userList.clear();
        userList.addAll(newData);
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public AttenderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.attender2_item, parent, false);
        return new AttenderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttenderViewHolder holder, int position) {
        User user = userList.get(position);

        holder.username.setText(user.getUsername());

        // Utilisez Picasso pour charger l'image depuis l'URL
        if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
            Picasso.get().load(user.getImageUrl())
                    .placeholder(R.drawable.user_bydefault)
                    .into(holder.imageProfile);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user.getId().equals(profileID)){
                    //ICI ouvrir le ProfileFragment.
                } else {
                    Intent intent = new Intent(mContext, ProfileActivity.class);
                    intent.putExtra("clickedID", user.getId());
                    mContext.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class AttenderViewHolder extends RecyclerView.ViewHolder {
        TextView username;
        CircleImageView imageProfile;

        public AttenderViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            imageProfile = itemView.findViewById(R.id.image_profile);
        }
    }
}

