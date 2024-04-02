package com.example.applicationv3.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.applicationv3.Model.Participant;
import com.example.applicationv3.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ParticipantAdapter extends RecyclerView.Adapter<ParticipantAdapter.ViewHolder> {

    private Context mContext;
    private List<Participant> mParticipants;

    public ParticipantAdapter(Context mContext, List<Participant> mParticipants) {
        this.mContext = mContext;
        this.mParticipants = mParticipants;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.participant_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Participant participant = mParticipants.get(position);
        holder.username.setText(participant.getUsername());
        holder.fullname.setText(participant.getName());
        if (participant.getImageurl() != null && !participant.getImageurl().isEmpty()) {
            Picasso.get().load(participant.getImageurl()).placeholder(R.drawable.user_bydefault).into(holder.imageProfile);
        }
        // Assurez-vous que participant.getSelected() n'est pas null avant d'appeler la méthode booleanValue()
        Boolean selected = participant.getSelected();
        holder.checkBox.setChecked(selected != null && selected);

        // Gérez le clic sur la case à cocher pour mettre à jour l'état de la propriété selected
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            participant.setSelected(isChecked);
            if (isChecked && participant.getAdmin()) {
                holder.admin.setVisibility(View.VISIBLE);
                holder.notAdmin.setVisibility(View.INVISIBLE);
            } else {
                holder.admin.setVisibility(View.INVISIBLE);
                holder.notAdmin.setVisibility(View.VISIBLE);
            }
        });


        // Initialisez la visibilité de admin et notAdmin en fonction de isAdmin
        if (participant.getAdmin()) {
            holder.admin.setVisibility(View.VISIBLE);
            holder.notAdmin.setVisibility(View.INVISIBLE);
        } else {
            holder.admin.setVisibility(View.INVISIBLE);
            holder.notAdmin.setVisibility(View.VISIBLE);
        }


        // Gérez le clic sur l'icône admin
        holder.admin.setOnClickListener(view -> {
            // Vérifiez si la case à cocher est cochée
            if (holder.checkBox.isChecked()) {
                participant.setAdmin(false);
                holder.admin.setVisibility(View.INVISIBLE);
                holder.notAdmin.setVisibility(View.VISIBLE);
            }

        });
        holder.notAdmin.setOnClickListener(view -> {
            // Vérifiez si la case à cocher est cochée
            if (holder.checkBox.isChecked()) {
                participant.setAdmin(true);
                holder.admin.setVisibility(View.VISIBLE);
                holder.notAdmin.setVisibility(View.INVISIBLE);
            }

        });
    }


        public List<String> getSelectedParticipantsIds() {
            List<String> selectedIds = new ArrayList<>();
            for (Participant participant : mParticipants) {
                if (participant.getSelected() != null && participant.getSelected()) {
                    selectedIds.add(participant.getId());
                }

            }
            return selectedIds;
        }

    public List<String> getSelectedAdminVisibleIds() {
        List<String> adminVisibleIds = new ArrayList<>();
        for (Participant participant : mParticipants) {
            if (participant.getSelected() != null && participant.getSelected() && participant.getAdmin()) {
                adminVisibleIds.add(participant.getId());
            }
        }
        return adminVisibleIds;
    }



    @Override
    public int getItemCount() {
        return mParticipants.size();
    }

    public void filterList(List<Participant> filteredList) {
        mParticipants = filteredList;
        notifyDataSetChanged();
    }
    public void setSelectedParticipantsIds(List<String> selectedIds) {
        for (Participant participant : mParticipants) {
            participant.setSelected(selectedIds.contains(participant.getId()));
        }
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView imageProfile;
        public TextView username;
        public TextView fullname;
        public CheckBox checkBox;
        public LinearLayout admin;
        public LinearLayout notAdmin;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProfile = itemView.findViewById(R.id.image_profile);
            username = itemView.findViewById(R.id.username);
            fullname = itemView.findViewById(R.id.fullname);
            checkBox = itemView.findViewById(R.id.checkBox);
            admin = itemView.findViewById(R.id.admin);
            notAdmin = itemView.findViewById(R.id.notAdmin);
        }
    }

}

