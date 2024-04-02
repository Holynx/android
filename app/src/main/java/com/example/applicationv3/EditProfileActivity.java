package com.example.applicationv3;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.applicationv3.Model.User;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class EditProfileActivity extends AppCompatActivity {

    private ImageView close;
    private CircleImageView imageProfile;
    private TextView save;
    private ImageView changePhoto;
    private EditText fullname;
    private EditText username;
    private EditText bio;
    private EditText location;
    private EditText email;
    double latitudeExt;
    double longitudeExt;
    private Uri imageUri;
    private String newImageUrl;

    private FirebaseUser fUser;
    private Uri mImageUri;
    private StorageTask uploadTask;
    private StorageReference storageRef;
    private FirebaseFirestore db;
    private User user;
    private boolean isLocationFocused = false;
    private String previousLocationText = "";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
// Initialize Places API with your API key
        Places.initialize(getApplicationContext(), getString(R.string.GOOGLE_MAPS_API_KEY));
        changeStatusBarColor(getResources().getColor(R.color.main_color3));

        db = FirebaseFirestore.getInstance();
        close=findViewById(R.id.close);
        imageProfile=findViewById(R.id.image_profile);
        save=findViewById(R.id.save);
        changePhoto = findViewById(R.id.change_photo);
        fullname = findViewById(R.id.fullname);
        username=findViewById(R.id.username);
        email=findViewById(R.id.email);
        bio=findViewById(R.id.bio);
        location=findViewById(R.id.location);

        fUser = FirebaseAuth.getInstance().getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference().child("uploads");
        String profileId = MasterActivity.getLoggedInUserId();

        location.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    previousLocationText = location.getText().toString();
                    location.setText(""); // Efface le texte quand l'EditText obtient le focus
                    List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
                    Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(EditProfileActivity.this);
                    startActivityForResult(intent, 101);
                }
            }
        });

        changePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ajoutez le code pour permettre à l'utilisateur de sélectionner ou de prendre une nouvelle photo
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        // Utilisez la DataManager pour récupérer les informations de l'utilisateur
        DataManager.getInstance().fetchUserProfileInfo(new DataCallback<User>() {
            @Override
            public void onDataReceived(User userData) {
                user = userData;
                // Remplissez les TextView avec les informations de l'utilisateur
                fullname.setText(user.getName());
                username.setText(user.getUsername());
                email.setText(user.getEmail());
                bio.setText(user.getBio());
                location.setText(user.getDefaultAdress());

                Picasso.get()
                        .load(user.getImageUrl())
                        .placeholder(R.drawable.user_bydefault) // Image par défaut
                        .into(imageProfile);
            }
            @Override
            public void onError(Exception e) {
                // Gérez les erreurs ici
            }
        });



        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Rajouter condition si changement
                updateUserImage(imageUri);

                // Récupérer les nouvelles valeurs des champs
                String newUsername = username.getText().toString();
                String newName = fullname.getText().toString();
                String newEmail = email.getText().toString();
                String newLocation = location.getText().toString();
                String newBio = bio.getText().toString();


                // Créer une carte avec les champs à mettre à jour dans Firestore
                Map<String, Object> updateData = new HashMap<>();



                    updateData.put("username", newUsername);
                    updateData.put("name", newName);
                    updateData.put("email", newEmail);
                if (!newLocation.equals(user.getDefaultAdress())) {
                    updateData.put("defaultAdress", newLocation);
                    updateData.put("latitude", latitudeExt);
                    updateData.put("longitude", longitudeExt);
                }
                    updateData.put("bio", newBio);



                // Mettre à jour les données dans Firestore uniquement si des changements ont été détectés
                if (!updateData.isEmpty()) {
                    db.collection("users").document(profileId)
                            .update(updateData)
                            .addOnSuccessListener(aVoid -> {
                                // Mettre à jour les champs locaux dans l'objet User
                                user.setUsername(newUsername);
                                user.setName(newName);
                                user.setEmail(newEmail);
                                if (!newLocation.equals(user.getDefaultAdress())) {
                                    user.setDefaultAdress(newLocation);
                                    user.setLatitude(latitudeExt);
                                    user.setLongitude(longitudeExt);
                                }
                                user.setBio(newBio);
                                user.setImageUrl(newImageUrl);

                                // Mise à jour du cache
                                MasterActivity.setCachedUser(user);


                            })
                            .addOnFailureListener(e -> {
                                // Gérez les erreurs ici
                            });
                }
                // Fermez l'activité
                finish();
            }


            private void updateUserImage(Uri imageUri) {
                if (imageUri != null) {
                    StorageReference fileReference = storageRef.child(profileId + System.currentTimeMillis() + ".jpg");

                    uploadTask = fileReference.putFile(imageUri)
                            .addOnSuccessListener(taskSnapshot -> {
                                // Récupérez l'URL de l'image téléchargée
                                fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                    // Mettez à jour le champ 'imageurl' dans Firestore avec l'URL de la nouvelle image
                                    db.collection("users").document(profileId)
                                            .update("imageUrl", uri.toString())
                                            .addOnSuccessListener(aVoid -> {
                                                // Mise à jour réussie, mettez à jour localement l'URL de l'image
                                                user.setImageUrl(uri.toString());
                                                newImageUrl = uri.toString();

                                                MasterActivity.setCachedUser(user);
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(EditProfileActivity.this, "Erreur lors de la mise à jour de Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(EditProfileActivity.this, "Erreur lors du téléchargement vers Firebase Storage: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    // Gérer le cas où l'URI est null
                }
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Récupérez l'URI de l'image sélectionnée
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(imageProfile);
        }

        if (requestCode == 101 && resultCode == RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            location.setText(place.getAddress());
            latitudeExt = place.getLatLng().latitude;
            longitudeExt = place.getLatLng().longitude;
            location.setEnabled(true);
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(this, status.getStatusMessage(), Toast.LENGTH_SHORT).show();
        }
        if (location.getText().toString().isEmpty()) {
            location.setText(previousLocationText);
        }
    }
    private void changeStatusBarColor(int color) {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(color);
    }
}