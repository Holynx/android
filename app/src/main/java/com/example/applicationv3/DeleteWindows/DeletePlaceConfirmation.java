package com.example.applicationv3.DeleteWindows;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.applicationv3.MasterActivity;
import com.example.applicationv3.Model.PlaceUse;
import com.example.applicationv3.MyPlaceActivity;
import com.example.applicationv3.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;


public class DeletePlaceConfirmation extends Dialog implements View.OnClickListener {

    private Activity activity;
    private TextView yesButton, noButton;
    private String placeIdToDelete;
    private Map<String, PlaceUse> cachedAuthorizedPlaces;
    private MyPlaceActivity myPlaceActivity;

    public DeletePlaceConfirmation(Activity activity) {

        super(activity);
        this.activity = activity;
        setContentView(R.layout.delete_place_confirmation);

        yesButton = findViewById(R.id.yes_button);
        noButton = findViewById(R.id.no_button);

        yesButton.setOnClickListener(this);
        noButton.setOnClickListener(this);
    }

    // Méthode pour définir l'ID de la place à supprimer
    public void setPlaceIdToDelete(String placeId) {
        this.placeIdToDelete = placeId;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.yes_button) {
            // Logique de suppression de la place si le bouton YES est cliqué
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Supprimer le document correspondant à placeIdToDelete
            if (placeIdToDelete != null && !placeIdToDelete.isEmpty()) {
                db.collection("places")
                        .document(placeIdToDelete)
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                MasterActivity.removePlaceFromCachedAuthorizedPlaces(placeIdToDelete);
                                dismiss();
                                myPlaceActivity.finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Gérer l'échec de la suppression
                                dismiss();
                            }
                        });
            }
        } else if (v.getId() == R.id.no_button) {
            // Si le bouton NO est cliqué, fermez simplement le dialog
            dismiss();
        }
    }
    public void updateCachedAuthorizedPlaces(Map<String, PlaceUse> cachedAuthorizedPlaces) {
        this.cachedAuthorizedPlaces = cachedAuthorizedPlaces;
    }

    public void setMyPlaceActivityReference(MyPlaceActivity myPlaceActivity) {
        this.myPlaceActivity = myPlaceActivity;
    }

}
