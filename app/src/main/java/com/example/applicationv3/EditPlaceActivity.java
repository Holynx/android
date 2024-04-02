package com.example.applicationv3;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.applicationv3.Adapter.ParticipantAdapter;
import com.example.applicationv3.Model.Participant;
import com.example.applicationv3.Model.PlaceUse;
import com.example.applicationv3.Model.User;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class EditPlaceActivity extends AppCompatActivity {

    private ImageView close;
    private CircleImageView imageProfile;
    private TextView save;
    private ImageView changePhoto;
    private EditText PlaceName;
    private RecyclerView recyclerViewAttenders;
    private ParticipantAdapter participantAdapter;
    private EditText PlaceDescription;
    private EditText DateBeg;
    private EditText PlaceLocation;
    private EditText DateEnd;
    private List<Participant> participantList;
    private long latitudeExt;
    private long longitudeExt;
    private String newPlaceName;
    private String newPlaceDescription;
    private long newDateBeg;
    private long newDateEnd;
    private String newPlaceLocation;
    private double newPlaceLatitude;
    private double newPlaceLongitude;
    private String newDateBegString;
    private String newDateEndString;
    private List<String> newOwnersId;
    private List<String> newContactsAllowed;
    TextView SwitchText1, SwitchText2;
    Switch DateSwitch;
    private Uri imageUri;
    private String newImageUrl;
    Calendar myCalendar;
    private EditText selectedEditText;
    private FirebaseUser fUser;
    String profileID;

    private Uri mImageUri;
    private StorageTask uploadTask;
    private StorageReference storageRef;
    private FirebaseFirestore db;
    private PlaceUse place;
    private boolean isLocationFocused = false;
    private Map<String, PlaceUse> cachedAuthorizedPlaces;
    private String placeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_place);
        changeStatusBarColor(getResources().getColor(R.color.main_color3));
        place = new PlaceUse();
// Initialize Places API with your API key
        Places.initialize(getApplicationContext(), getString(R.string.GOOGLE_MAPS_API_KEY));

        db = FirebaseFirestore.getInstance();
        close=findViewById(R.id.close);
        imageProfile=findViewById(R.id.image_profile);
        save=findViewById(R.id.save);
        changePhoto = findViewById(R.id.change_photo);
        PlaceName = findViewById(R.id.PlaceName);
        DateSwitch =findViewById(R.id.dateSwitch);
        SwitchText1 = findViewById(R.id.switchText1);
        SwitchText2 = findViewById(R.id.switchText2);
        PlaceDescription=findViewById(R.id.PlaceDescription);
        DateBeg=findViewById(R.id.DateBeg);
        myCalendar=Calendar.getInstance();
        DateEnd=findViewById(R.id.DateEnd);
        PlaceLocation=findViewById(R.id.PlaceLocation);
        cachedAuthorizedPlaces = MasterActivity.getCachedAuthorizedPlaces();

        fUser = FirebaseAuth.getInstance().getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference().child("uploads");
        recyclerViewAttenders = findViewById(R.id.recycler_view_attenders);
        recyclerViewAttenders.setHasFixedSize(true);
        recyclerViewAttenders.setLayoutManager(new LinearLayoutManager(this));
        participantList = new ArrayList<>();
        participantAdapter = new ParticipantAdapter(this, participantList);
        recyclerViewAttenders.setAdapter(participantAdapter);
        placeId = getIntent().getStringExtra("PLACE_ID");
        profileID = MasterActivity.getLoggedInUserId();

        PlaceUse selectedPlace = null;
        for (Map.Entry<String, PlaceUse> entry : cachedAuthorizedPlaces.entrySet()) {
            String currentPlaceId = entry.getValue().getId();
            PlaceUse currentPlace = entry.getValue();
            if (currentPlaceId.equals(placeId)) {
                selectedPlace = currentPlace;
                break;
            }
        }

        newPlaceName = selectedPlace.getTitle();
        newPlaceDescription = selectedPlace.getDescription();
        newDateBeg = selectedPlace.getStartDate();
        newDateEnd = selectedPlace.getEndDate();
        newPlaceLocation = selectedPlace.getTitleAdress();
        newPlaceLatitude = selectedPlace.getLatitude();
        newPlaceLongitude = selectedPlace.getLongitude();
        newContactsAllowed= selectedPlace.getContactsAllowed();
        newOwnersId=selectedPlace.getOwnerId();
        newImageUrl=selectedPlace.getImageUrl();

        PlaceName.setText(selectedPlace.getTitle());
        PlaceDescription.setText(selectedPlace.getDescription());
        PlaceLocation.setText(selectedPlace.getTitleAdress());
        Long DateEndLong = selectedPlace.getEndDate();
        Long DateStartLong = selectedPlace.getStartDate();
        String startDate = convertTimestampToString(DateStartLong);
        String endDate = convertTimestampToString(DateEndLong);

        loadParticipants();
        loadFriendParticipants();

        if (DateEndLong !=4102441200L) {
            DateBeg.setText(startDate);
            DateEnd.setText(endDate);
            DateSwitch.setChecked(true);
            DateBeg.setVisibility(View.VISIBLE);
            DateEnd.setVisibility(View.VISIBLE);
            SwitchText1.setTextColor(getResources().getColor(R.color.second_color2));
            SwitchText2.setTextColor(getResources().getColor(R.color.main_color3));
        }else{
            DateBeg.setVisibility(View.GONE);
            DateEnd.setVisibility(View.GONE);
            SwitchText1.setTextColor(getResources().getColor(R.color.main_color3));
            SwitchText2.setTextColor(getResources().getColor(R.color.second_color2));
        }
        String placeImageURL = selectedPlace.getImageUrl();

        if (placeImageURL != null && !placeImageURL.isEmpty()) {
        Picasso.get()
                .load(placeImageURL)
                .placeholder(R.drawable.place_bydefault) // Image par défaut
                .into(imageProfile);
        } else {
            // Gérez le cas où l'URL de l'image est vide ou null
        }
//DatePicker quand clique sur EditText
        DatePickerDialog.OnDateSetListener date=new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                myCalendar.set(Calendar.YEAR,year);
                myCalendar.set(Calendar.MONTH,month);
                myCalendar.set(Calendar.DAY_OF_MONTH,day);
                updateLabel();

            }
        };
        PlaceLocation.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {

                    PlaceLocation.setText("");
                    List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
                    Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(EditPlaceActivity.this);
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

        DateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Si le switch est activé (sélectionné)
                    DateBeg.setVisibility(View.VISIBLE);
                    DateEnd.setVisibility(View.VISIBLE);
                    SwitchText1.setTextColor(getResources().getColor(R.color.second_color2));
                    SwitchText2.setTextColor(getResources().getColor(R.color.main_color3));
                } else {
                    // Si le switch est désactivé (non sélectionné)
                    DateBeg.setVisibility(View.GONE);
                    DateEnd.setVisibility(View.GONE);
                    SwitchText1.setTextColor(getResources().getColor(R.color.main_color3));
                    SwitchText2.setTextColor(getResources().getColor(R.color.second_color2));
                }
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

                newPlaceName = PlaceName.getText().toString();
                newPlaceDescription = PlaceDescription.getText().toString();
                newPlaceLocation = PlaceLocation.getText().toString();

                if (DateSwitch.isChecked()) {
                    newDateBegString = DateBeg.getText().toString();
                    newDateEndString = DateEnd.getText().toString();
                    newDateBeg = convertStringToSec(newDateBegString);
                    newDateEnd= convertStringToSec(newDateEndString);
                }else {
                    newDateBeg = convertStringToSec("01/01/2024");
                    newDateEnd = convertStringToSec("01/01/2100");
                }
                if (imageUri != null) {
                    updatePlaceImage(imageUri);
                } else {
                    newImageUrl = placeImageURL;
                }

// Mettez à jour ownersId et contactsAllowed
                List<String> updatedOwnersId = new ArrayList<>();
                List<String> updatedContactsAllowed = new ArrayList<>();

                for (Participant participant : participantList) {
                    if (participant.getAdmin()) {
                        updatedOwnersId.add(participant.getId());
                    }
                    if (participant.getSelected()) {
                        updatedContactsAllowed.add(participant.getId());
                    }
                }
                updatedOwnersId.add(profileID);
                updatedContactsAllowed.add(profileID);




                Map<String, Object> updatePlace = new HashMap<>();
                    updatePlace.put("title", newPlaceName);
                    updatePlace.put("description", newPlaceDescription);
                    updatePlace.put("startDate", newDateBeg);
                    updatePlace.put("endDate", newDateEnd);
                    //updatePlace.put("imageUrl",newImageUrl);
                    updatePlace.put("latitude",newPlaceLatitude);
                    updatePlace.put("longitude",newPlaceLongitude);
                    //updatePlace.put("id",placeId);
                    updatePlace.put("titleAdress", newPlaceLocation);
                    updatePlace.put("ownersId", updatedOwnersId);
                    updatePlace.put("contactsAllowed", updatedContactsAllowed);



                db.collection("places")
                        .document(placeId)
                        .update(updatePlace)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Mettez à jour également dans le cache
                                PlaceUse updatedPlace = new PlaceUse();
                                updatedPlace.setId(placeId);
                                updatedPlace.setTitle(newPlaceName);
                                updatedPlace.setDescription(newPlaceDescription);
                                updatedPlace.setTitleAdress(newPlaceLocation);
                                updatedPlace.setStartDate(newDateBeg);
                                updatedPlace.setEndDate(newDateEnd);
                                updatedPlace.setOwnerId(updatedOwnersId);
                                updatedPlace.setContactsAllowed(updatedContactsAllowed);
                                updatedPlace.setLatitude(newPlaceLatitude);
                                updatedPlace.setLongitude(newPlaceLongitude);
                                updatedPlace.setImageUrl(newImageUrl);

                                MasterActivity.addPlaceToCachedAuthorizedPlaces(placeId, updatedPlace);

                            }
                        });

                finish();


            }
            private void updatePlaceImage(Uri imageUri) {
                if (imageUri != null) {
                    StorageReference fileReference = storageRef.child(placeId + System.currentTimeMillis() + ".jpg");

                    uploadTask = fileReference.putFile(imageUri)
                            .addOnSuccessListener(taskSnapshot -> {
                                // Récupérez l'URL de l'image téléchargée
                                fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                    db.collection("places").document(placeId)
                                            .update("imageUrl", uri.toString())
                                            .addOnSuccessListener(aVoid -> {


                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(EditPlaceActivity.this, "Erreur lors de la mise à jour de Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(EditPlaceActivity.this, "Erreur lors du téléchargement vers Firebase Storage: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            }
        });

        DateBeg.setOnClickListener(view -> {
            selectedEditText = DateBeg; // DateBeg a été sélectionné
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    EditPlaceActivity.this,
                    R.style.DatePickerTheme,
                    date,
                    myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
            // Définir la date minimale comme la date d'aujourd'hui
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());

            if (!TextUtils.isEmpty(DateEnd.getText().toString())) {
                long selectedEndDateMillis = myCalendar.getTimeInMillis(); // Obtenez le temps actuel de DateEnd
                datePickerDialog.getDatePicker().setMaxDate(selectedEndDateMillis);
            }
            datePickerDialog.show();
        });

        DateEnd.setOnClickListener(view -> {
            selectedEditText = DateEnd; // DateEnd a été sélectionné
            DatePickerDialog datePickerDialog = new DatePickerDialog(EditPlaceActivity.this,
                    R.style.DatePickerTheme,
                    date,
                    myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
            // Définir la date minimale comme la date d'aujourd'hui
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
            if (!TextUtils.isEmpty(DateBeg.getText().toString())) {
                long selectedStartDateMillis = myCalendar.getTimeInMillis(); // Obtenir le temps actuel de DateBeg
                datePickerDialog.getDatePicker().setMinDate(selectedStartDateMillis);
            }
            datePickerDialog.show();
        });


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Récupérez l'URI de l'image sélectionnée
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(imageProfile);
        }
        if (requestCode ==100 && resultCode == RESULT_OK){
            Place place = Autocomplete.getPlaceFromIntent(data);
            PlaceLocation.setText(place.getAddress());
            newPlaceLatitude = place.getLatLng().latitude;
            newPlaceLongitude = place.getLatLng().longitude;

        } else if(resultCode== AutocompleteActivity.RESULT_ERROR) {
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(getApplicationContext(), status.getStatusMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private Long convertStringToSec(String dateString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            Date parsedDate = dateFormat.parse(dateString);
            if (parsedDate != null) {
                return parsedDate.getTime() / 1000;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
    private String convertTimestampToString(long timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        Date date = new Date(timestamp * 1000); // Convertir les secondes en millisecondes
        return dateFormat.format(date);
    }
    private void updateLabel() {
        String myFormate="dd/MM/yyyy";
        SimpleDateFormat dateFormat=new SimpleDateFormat(myFormate, Locale.US);
        if (selectedEditText != null) {
            selectedEditText.setText(dateFormat.format(myCalendar.getTime()));
        }
    }

    private List<Participant> allParticipantsList = new ArrayList<>();
    private void loadFriendParticipants() {
        Map<String, User> friendInfoMap = MasterActivity.getFriendsInfoMap();
        List<String> friendIds = new ArrayList<>(friendInfoMap.keySet());

        for (String friendId : friendIds) {
            User friendUser = friendInfoMap.get(friendId);

            if (friendUser != null) {
                // Créez un objet Participant à partir des détails de l'utilisateur
                Participant participant = new Participant(
                        friendUser.getName(),
                        friendUser.getUsername(),
                        friendUser.getImageUrl(),
                        friendUser.getId()
                );

                // Ajoutez le participant uniquement s'il n'est pas déjà dans contactsAllowed
                if (!newContactsAllowed.contains(participant.getId())) {
                    participantList.add(participant);
                }
            }
        }

        // Actualisez l'adaptateur après la mise à jour de la liste
        participantAdapter.notifyDataSetChanged();
    }


    private void loadParticipants() {
        // Vérifiez d'abord si la liste d'IDs des propriétaires n'est pas nulle ou vide
        if (newContactsAllowed != null && !newContactsAllowed.isEmpty()) {
            for (String participantId : newContactsAllowed) {
                if (!participantId.equals(profileID)) {
                    // Recherchez l'utilisateur correspondant à l'ID dans Firestore
                    db.collection("users").document(participantId).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    // L'utilisateur existe dans Firestore, créez un objet Participant
                                    User ownerUser = documentSnapshot.toObject(User.class);

                                    if (ownerUser != null) {
                                        boolean isSelected = true;
                                        boolean isAdmin = newOwnersId.contains(ownerUser.getId());

                                        Participant participant = new Participant(
                                                ownerUser.getName(),
                                                ownerUser.getUsername(),
                                                ownerUser.getImageUrl(),
                                                ownerUser.getId()
                                        );

                                        participant.setSelected(isSelected);
                                        participant.setAdmin(isAdmin);
                                        // Ajoutez le participant à la liste
                                        participantList.add(participant);
                                        participantAdapter.notifyDataSetChanged();
                                    }
                                }
                            })
                            .addOnFailureListener(e -> {
                                // Gérez les erreurs de lecture dans Firestore
                                Toast.makeText(EditPlaceActivity.this, "Erreur lors de la lecture des propriétaires: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            }
        }
    }



    private void filterParticipants(String query) {
        List<Participant> filteredList = new ArrayList<>();
        for (Participant participant : allParticipantsList) {
            // Ajoutez ici votre logique de filtrage, par exemple, vérifiez si le nom ou le nom d'utilisateur contient le texte de recherche
            if (participant.getName().toLowerCase().startsWith(query.toLowerCase())
                    || participant.getUsername().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(participant);
            }
        }
        participantAdapter.filterList(filteredList);
    }
    private void changeStatusBarColor(int color) {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(color);
    }
}
