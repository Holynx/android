package com.example.applicationv3;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.applicationv3.Adapter.ParticipantAdapter;
import com.example.applicationv3.Model.Participant;
import com.example.applicationv3.Model.PlaceUse;
import com.example.applicationv3.Model.User;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

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

public class PlaceActivity extends AppCompatActivity {
    private RecyclerView recyclerViewAttenders;
    private List<Participant> participantList;
    private ParticipantAdapter participantAdapter;
    private String uid;
    private CircleImageView imageProfile;
    private EditText selectedEditText;
    private SearchView SearchAttenders;
    private StorageTask uploadTask;
    private String placeId;
    private FirebaseFirestore db;
    ImageView close;
    private Uri imageUri;
    private StorageReference storageRef;
    private ImageView changePhoto;
    FirebaseDatabase firebaseDatabase;
    EditText PlaceName, PlaceDescription, DateBeg, DateEnd, PlaceLocation;
    TextView SwitchText1, SwitchText2;
    Switch DateSwitch;
    Button placeBtn;
    FirebaseAuth mAuth;
    Calendar myCalendar;
    double latitudeExt;
    double longitudeExt;

    String profileID;
    String profileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);
        User cachedUser = MasterActivity.getCachedUser();
        changeStatusBarColor(getResources().getColor(R.color.main_color3));

        androidx.appcompat.widget.SearchView searchView = findViewById(R.id.search_bar_attenders);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterParticipants(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterParticipants(newText);
                return true;
            }
        });

        profileID = cachedUser.getId();
        profileName = cachedUser.getName();

        close=findViewById(R.id.close);
        placeBtn = findViewById(R.id.placeBtn);
        PlaceName = findViewById(R.id.PlaceName);
        PlaceDescription = findViewById(R.id.PlaceDescription);
        DateBeg = findViewById(R.id.DateBeg);
        DateEnd = findViewById(R.id.DateEnd);
        DateSwitch =findViewById(R.id.dateSwitch);
        imageProfile=findViewById(R.id.image_profile);
        SwitchText1 = findViewById(R.id.switchText1);
        SwitchText2 = findViewById(R.id.switchText2);
        myCalendar=Calendar.getInstance();
        changePhoto = findViewById(R.id.change_photo);
        PlaceLocation = findViewById(R.id.PlaceLocation);
        SearchAttenders = findViewById(R.id.search_bar_attenders);
        recyclerViewAttenders = findViewById(R.id.recycler_view_attenders);
        recyclerViewAttenders.setHasFixedSize(true);
        recyclerViewAttenders.setLayoutManager(new LinearLayoutManager(this));
        participantList = new ArrayList<>();
        participantAdapter = new ParticipantAdapter(this, participantList);
        recyclerViewAttenders.setAdapter(participantAdapter);
        mAuth= FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference().child("uploads");

        loadFriendParticipants();

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

        //Initialize Place
        Places.initialize(getApplicationContext(), getString(R.string.GOOGLE_MAPS_API_KEY));
        //Set EditText non focusable
        PlaceLocation.setFocusable(false);
        PlaceLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS,Place.Field.LAT_LNG,Place.Field.NAME);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY,fieldList).build(PlaceActivity.this);
                startActivityForResult(intent,100);
            }
        });

        DateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Si le switch est activé (sélectionné)
                    DateBeg.setVisibility(View.VISIBLE);
                    DateEnd.setVisibility(View.VISIBLE);
                    SwitchText1.setTextColor(getResources().getColor(R.color.main_color1));
                    SwitchText2.setTextColor(getResources().getColor(R.color.main_color2));
                } else {
                    // Si le switch est désactivé (non sélectionné)
                    DateBeg.setVisibility(View.GONE);
                    DateEnd.setVisibility(View.GONE);
                    SwitchText1.setTextColor(getResources().getColor(R.color.main_color2));
                    SwitchText2.setTextColor(getResources().getColor(R.color.main_color1));
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

        placeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                    uid = MasterActivity.getLoggedInUserId();
                    String PlaceLoc = PlaceLocation.getText().toString();
                    String PlaceBeg = DateBeg.getText().toString();
                    String PlaceEnd = DateEnd.getText().toString();
                    String PlaceTitle = PlaceName.getText().toString();
                    String PlaceDesc = PlaceDescription.getText().toString();

                    // Vérifiez si le lieu n'est pas vide
                    if (!TextUtils.isEmpty(PlaceLoc)&&!TextUtils.isEmpty(PlaceTitle)) {
                        if(DateSwitch.isChecked() &&TextUtils.isEmpty(PlaceBeg)&&TextUtils.isEmpty(PlaceEnd)) {
                            Toast.makeText(PlaceActivity.this, "Fill the date or create an Permanent Placeq ", Toast.LENGTH_SHORT).show();
                        }else{
                            db = FirebaseFirestore.getInstance();

                            // Convertissez les chaînes de dates en nombre de secondes depuis le 1er janvier 1970
                            long timestampBeg;
                            long timestampEnd;
                            if (DateSwitch.isChecked()) {
                                // Si le switch est activé (sélectionné)
                                timestampBeg = convertStringToSec(PlaceBeg);
                                timestampEnd = convertStringToSec(PlaceEnd);
                            } else {
                                // Si le switch n'est pas activé, utilisez les valeurs par défaut
                                timestampBeg = convertStringToSec("01/01/2024");
                                timestampEnd = convertStringToSec("01/01/2100");
                            }

                            List<String> selectedUserIds = participantAdapter.getSelectedParticipantsIds();
                            List<String> adminIds = participantAdapter.getSelectedAdminVisibleIds();
                            adminIds.add(uid);

                            selectedUserIds.add(uid);

                            // Créez un nouvel événement avec le champ "Lieu" rempli
                            Map<String, Object> place = new HashMap<>();
                            place.put("title", PlaceTitle);
                            place.put("titleAdress", PlaceLoc);
                            place.put("startDate", timestampBeg);
                            place.put("endDate", timestampEnd);
                            place.put("description", PlaceDesc);
                            place.put("ownersId", adminIds);
                            place.put("latitude", latitudeExt);
                            place.put("longitude", longitudeExt);
                            place.put("contactsAllowed", selectedUserIds);
                            place.put("imageUrl", "");


                            // Ajoutez cet événement à la collection "Place" sous l'UID de l'utilisateur actuel
                            db.collection("places")
                                    .add(place)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            placeId = documentReference.getId();
                                            if (imageUri != null) {
                                                updatePlaceImage(imageUri);
                                            }

                                            db.collection("places").document(placeId).update("id", placeId);


                                            createNotifications(selectedUserIds, uid, placeId, PlaceTitle);

                                            Toast.makeText(PlaceActivity.this, "Place created", Toast.LENGTH_SHORT).show();
                                            PlaceName.setText("");
                                            PlaceLocation.setText("");
                                            PlaceDescription.setText("");
                                            DateBeg.setText("");
                                            DateEnd.setText("");

                                            PlaceUse newPlaceUse = new PlaceUse();
                                            newPlaceUse.setId(placeId);
                                            newPlaceUse.setTitle(PlaceTitle);
                                            newPlaceUse.setTitleAdress(PlaceLoc);
                                            newPlaceUse.setStartDate(timestampBeg);
                                            newPlaceUse.setEndDate(timestampEnd);
                                            newPlaceUse.setDescription(PlaceDesc);
                                            newPlaceUse.setOwnerId(Arrays.asList(uid));
                                            newPlaceUse.setLatitude(latitudeExt);
                                            newPlaceUse.setLongitude(longitudeExt);
                                            newPlaceUse.setContactsAllowed(selectedUserIds);


                                            MasterActivity masterActivity = new MasterActivity();
                                            masterActivity.addPlaceToCachedAuthorizedPlaces(placeId, newPlaceUse);
                                            finish();
                                        }

                                        private void createNotifications(List<String> receiverIds, String senderId, String placeId, String PlaceTitle) {
                                            Date currentDate1 = new Date();
                                            long timestampSeconds = currentDate1.getTime() / 1000;
                                            FirebaseFirestore db = FirebaseFirestore.getInstance();

                                            for (String receiverId : receiverIds) {
                                                if (!receiverId.equals(senderId)) {
                                                    String message = profileName + " vous a ajouté dans la place " + PlaceTitle;
                                                    Map<String, Object> notificationData = new HashMap<>();
                                                    notificationData.put("senderId", senderId);
                                                    notificationData.put("receiverId", receiverId);
                                                    notificationData.put("isNew", true);
                                                    notificationData.put("createdDate", timestampSeconds);
                                                    notificationData.put("eventId", "");
                                                    notificationData.put("placeId", placeId);
                                                    notificationData.put("message", message);

                                                    db.collection("notifications")
                                                            .add(notificationData)
                                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                @Override
                                                                public void onSuccess(DocumentReference documentReference) {
                                                                    String notificationId = documentReference.getId();
                                                                    db.collection("notifications").document(notificationId).update("id", notificationId);
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                }
                                                            });
                                                }
                                            }
                                        }

                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Gestion de l'échec de l'ajout de l'événement
                                            Toast.makeText(PlaceActivity.this, "Erreur lors de l'ajout de l'événement : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(PlaceActivity.this, "Remplissez tous les champs", Toast.LENGTH_SHORT).show();
                    }

            }


        });


        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        DateBeg.setOnClickListener(view -> {
            selectedEditText = DateBeg; // DateBeg a été sélectionné
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    PlaceActivity.this,
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
            DatePickerDialog datePickerDialog = new DatePickerDialog(PlaceActivity.this,
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
    private void updateLabel() {
        String myFormate="dd/MM/yyyy";
        SimpleDateFormat dateFormat=new SimpleDateFormat(myFormate, Locale.US);
        if (selectedEditText != null) {
            selectedEditText.setText(dateFormat.format(myCalendar.getTime()));
        }
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
                                        Toast.makeText(PlaceActivity.this, "Erreur lors de la mise à jour de Firestore: ", Toast.LENGTH_SHORT).show();
                                    });
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(PlaceActivity.this, "Erreur lors du téléchargement vers Firebase Storage: ", Toast.LENGTH_SHORT).show();
                    });
        }
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
            latitudeExt = place.getLatLng().latitude;
            longitudeExt = place.getLatLng().longitude;

        } else if(resultCode== AutocompleteActivity.RESULT_ERROR) {
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(getApplicationContext(), status.getStatusMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private List<Participant> allParticipantsList = new ArrayList<>();
    private void loadFriendParticipants() {
        Map<String, User> friendInfoMap = MasterActivity.getFriendsInfoMap();
        List<String> friendIds = new ArrayList<>(friendInfoMap.keySet());
        for (String friendId : friendIds) {
            User friendUser = friendInfoMap.get(friendId);

            if (friendUser != null) {
                // Create a Participant object using the User details
                Participant participant = new Participant(
                        friendUser.getName(),
                        friendUser.getUsername(),
                        friendUser.getImageUrl(),
                        friendUser.getId()
                );

                // Add the Participant to the list
                participantList.add(participant);
                allParticipantsList.add(participant);
            }
        }
        participantAdapter.notifyDataSetChanged();


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