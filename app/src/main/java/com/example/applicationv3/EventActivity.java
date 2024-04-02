package com.example.applicationv3;

//import android.app.DatePickerDialog;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.applicationv3.Model.EventUse;
import com.example.applicationv3.Model.PlaceUse;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

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


public class EventActivity extends AppCompatActivity {

    private static final int RC_WRITE_CALENDAR = 123;
    private String uid;
    private DatabaseReference databaseReference;
    private PlacesClient placesClient;
    private String selectedTitle;
    FirebaseDatabase firebaseDatabase;
    EditText EventName, DateBeg, DateEnd, EventLocation;
    EditText selectedEditText;
    TextView LongLat;
    Switch SharedToFriends;
    Button eventBtn;
    ImageView close;
    FirebaseAuth mAuth;
    Calendar myCalendar;
    double latitudeExt;
    double longitudeExt;
    long minStartDateAfterGivenDate;
    private boolean isPlaceSelected = false;
    String placeId;
    Spinner MyPlacesList;
    private Map<String, PlaceUse> cachedAuthorizedPlaces;
    private Map<String, EventUse> cachedUserEvents;
    private Map<String, String> titleToAddressMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        changeStatusBarColor(getResources().getColor(R.color.main_color3));

        Places.initialize(getApplicationContext(), getString(R.string.GOOGLE_MAPS_API_KEY));
        FirebaseApp.initializeApp(this);
        placesClient = Places.createClient(this);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        mAuth= FirebaseAuth.getInstance();
        close=findViewById(R.id.close);
        eventBtn = findViewById(R.id.eventBtn);
        EventName = findViewById(R.id.EventName);
        DateBeg = findViewById(R.id.DateBeg);
        DateEnd = findViewById(R.id.DateEnd);
        myCalendar=Calendar.getInstance();
        SharedToFriends = findViewById(R.id.SharedToFriends);
        EventLocation = findViewById(R.id.EventLocation);
        MyPlacesList = findViewById(R.id.MyPlacesList);
        Date selectedDate = (Date) getIntent().getSerializableExtra("selectedDate");

        cachedAuthorizedPlaces = MasterActivity.getCachedAuthorizedPlaces();
        cachedUserEvents = MasterActivity.getCachedUserEvents();

        populateSpinnerWithAuthorizedPlaces();

        if (selectedDate != null) {
            Calendar todayCalendar = Calendar.getInstance();
            todayCalendar.setTime(new Date());

            Calendar selectedDateCalendar = Calendar.getInstance();
            selectedDateCalendar.setTime(selectedDate);

            // Préremplir les EditText Date si une date séléctionnée autre qu'aujourd'hui.
            if (todayCalendar.get(Calendar.YEAR) != selectedDateCalendar.get(Calendar.YEAR)
                    || todayCalendar.get(Calendar.MONTH) != selectedDateCalendar.get(Calendar.MONTH)
                    || todayCalendar.get(Calendar.DAY_OF_MONTH) != selectedDateCalendar.get(Calendar.DAY_OF_MONTH)) {

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                String formattedSelectedDate = dateFormat.format(selectedDate);
                boolean eventExists = doesEventExistOnDate(selectedDate);

                // Si aucun événement n'existe à la date sélectionnée, remplissez la DateBeg
                if (!eventExists) {
                        DateBeg.setText(formattedSelectedDate);
                }
            }
        }


        com.wdullaer.materialdatetimepicker.date.DatePickerDialog.OnDateSetListener date =
                new com.wdullaer.materialdatetimepicker.date.DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(com.wdullaer.materialdatetimepicker.date.DatePickerDialog view,
                                          int year, int monthOfYear, int dayOfMonth) {
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        updateLabel();
                    }
        };



        MyPlacesList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                DateEnd.setText("");
                DateBeg.setText("");
                // Récupérez le titre de la place sélectionnée dans le spinner
                String selectedPlaceTitle = MyPlacesList.getSelectedItem().toString();
                PlaceUse selectedPlace = findPlaceByTitle(selectedPlaceTitle);

                    if (selectedPlace != null) {
                        isPlaceSelected = true;
                        EventLocation.setText(selectedPlace.getTitleAdress());
                        latitudeExt = selectedPlace.getLatitude();
                        longitudeExt = selectedPlace.getLongitude();
                        placeId = selectedPlace.getId();


                    }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                isPlaceSelected = false;
            }
        });


        //Initialize Place
        Places.initialize(getApplicationContext(), getString(R.string.GOOGLE_MAPS_API_KEY));
        //Set EditText non focusable
        EventLocation.setFocusable(false);
        EventLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Initialize place field list
                List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS,Place.Field.LAT_LNG,Place.Field.NAME);
                //Create intent
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY,fieldList).build(EventActivity.this);
                //Start activity result
                startActivityForResult(intent,100);
            }
        });
        eventBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String EventLoc = EventLocation.getText().toString();
                String EventBeg = DateBeg.getText().toString();
                String EventEnd = DateEnd.getText().toString();
                String EventTitle = EventName.getText().toString();
                boolean toBeShared = SharedToFriends.isChecked();

                // Vérifiez si les champs ne sont pas vides
                if (!TextUtils.isEmpty(EventTitle) &&!TextUtils.isEmpty(EventLoc) && !TextUtils.isEmpty(EventBeg) && !TextUtils.isEmpty(EventEnd)) {
                    uid = MasterActivity.getLoggedInUserId();
                    if (!TextUtils.isEmpty(EventLoc)) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        // Convertissez les chaînes de dates en nombre de seconde depuis le 1 Janv 1970
                        long timestampBeg = convertStringToSec(EventBeg);
                        long timestampEnd = convertStringToSec(EventEnd);

                        // Créez un nouvel événement avec le champ "Lieu" rempli
                        Map<String, Object> event = new HashMap<>();
                        event.put("title", EventTitle);
                        event.put("titleAdress", EventLoc);
                        event.put("startDate", timestampBeg);
                        event.put("endDate", timestampEnd);
                        event.put("ownerId", uid);
                        event.put("toBeShared",toBeShared);
                        event.put("latitude",latitudeExt);
                        event.put("longitude",longitudeExt);
                        event.put("placeId",placeId);


                        // Ajoutez cet événement à la collection "Event" sous l'UID de l'utilisateur actuel
                        db.collection("events")
                                .add(event)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        String eventId = documentReference.getId();
                                        db.collection("events").document(eventId).update("id", eventId);
                                        Toast.makeText(EventActivity.this, "Event created", Toast.LENGTH_SHORT).show();
                                        EventName.setText("");
                                        EventLocation.setText("");
                                        DateBeg.setText("");
                                        DateEnd.setText("");
                                        Intent intent = new Intent(EventActivity.this, MasterActivity.class);
                                        intent.putExtra("selectedFragment", "profile");
                                        startActivity(intent);
                                        finish();
                                        Log.d("EventActivity", "EventId1: " + eventId);
//Récupérer l'ID qui vient d'etre créer dans la Firestore
                                        EventUse newEventUse = new EventUse();
                                        newEventUse.setTitle(EventTitle);
                                        newEventUse.setTitleAdress(EventLoc);
                                        newEventUse.setStartDate(timestampBeg);
                                        newEventUse.setEndDate(timestampEnd);
                                        newEventUse.setOwnerId(uid);
                                        newEventUse.setId(eventId);
                                        newEventUse.setToBeShared(toBeShared);
                                        newEventUse.setLatitude(latitudeExt);
                                        newEventUse.setLongitude(longitudeExt);
                                        newEventUse.setPlaceId(placeId);

                                        //Ajouter l'event dans le cache
                                        MasterActivity.addEventToCachedUserEvents(eventId, newEventUse);

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(EventActivity.this, "Erreur lors de l'ajout de l'événement : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(EventActivity.this, "Saisissez un lieu.", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    // Affichez un message d'erreur si l'un des champs est vide
                    Toast.makeText(EventActivity.this, "Remplissez tous les champs", Toast.LENGTH_SHORT).show();
                }

            }

        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventActivity.this, MasterActivity.class);
                intent.putExtra("selectedFragment", "profile");
                startActivity(intent);
                finish();
            }
        });

        DateBeg.setOnClickListener(view -> {
            selectedEditText = DateBeg;
            com.wdullaer.materialdatetimepicker.date.DatePickerDialog datePickerDialog =
                    com.wdullaer.materialdatetimepicker.date.DatePickerDialog.newInstance(
                            date,
                            myCalendar.get(Calendar.YEAR),
                            myCalendar.get(Calendar.MONTH),
                            myCalendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.setAccentColor(getResources().getColor(R.color.main_color2));
            datePickerDialog.setOkColor(getResources().getColor(R.color.main_color3));
            datePickerDialog.setCancelColor(getResources().getColor(R.color.main_color2));
            datePickerDialog.setMinDate(Calendar.getInstance());

            // Obtenez les jours désactivés
            Calendar[] disabledDays = getDisabledDays();
            datePickerDialog.setDisabledDays(disabledDays);

            String selectedPlaceTitle = MyPlacesList.getSelectedItem().toString();
            PlaceUse selectedPlace = findPlaceByTitle(selectedPlaceTitle);

            if (selectedPlace != null ) {
                //ICI//Mettre ici la date mini du Date picker égale à selectedPlace.getStartDate (sauf si la date d'aujourd'hui est supérieure) et le max à selectedPlace.getEndDate.
                Calendar minDate = Calendar.getInstance();
                minDate.setTimeInMillis(selectedPlace.getStartDate() * 1000);
                if (minDate.after(Calendar.getInstance())) {
                    datePickerDialog.setMinDate(minDate);
                }

                Calendar maxDate = Calendar.getInstance();
                maxDate.setTimeInMillis(selectedPlace.getEndDate() * 1000);
                datePickerDialog.setMaxDate(maxDate);
            }


            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            datePickerDialog.show(ft, "DatePickerDialog");
        });
        DateBeg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                DateEnd.setText("");
            }
        });

        DateEnd.setOnClickListener(view -> {
            if (TextUtils.isEmpty(DateBeg.getText().toString())) {
                // Afficher un message demandant de sélectionner d'abord une date de début
                Toast.makeText(EventActivity.this, "Veuillez d'abord sélectionner une date de début.", Toast.LENGTH_SHORT).show();
            } else {
                selectedEditText = DateEnd;
                com.wdullaer.materialdatetimepicker.date.DatePickerDialog datePickerDialog =
                        com.wdullaer.materialdatetimepicker.date.DatePickerDialog.newInstance(
                                date,
                                myCalendar.get(Calendar.YEAR),
                                myCalendar.get(Calendar.MONTH),
                                myCalendar.get(Calendar.DAY_OF_MONTH));

                datePickerDialog.setAccentColor(getResources().getColor(R.color.main_color2));
                datePickerDialog.setOkColor(getResources().getColor(R.color.main_color3));
                datePickerDialog.setCancelColor(getResources().getColor(R.color.main_color2));
                datePickerDialog.setMinDate(Calendar.getInstance());

                Calendar[] disabledDays = getDisabledDays();
                datePickerDialog.setDisabledDays(disabledDays);


                    String selectedPlaceTitle = MyPlacesList.getSelectedItem().toString();
                    PlaceUse selectedPlace = findPlaceByTitle(selectedPlaceTitle);

                    minStartDateAfterGivenDate = getMinStartDateAfterGivenDate(DateBeg.getText().toString());

                if (minStartDateAfterGivenDate != Long.MAX_VALUE) {
                    Calendar maxDateCalendar = Calendar.getInstance();
                    maxDateCalendar.setTimeInMillis(minStartDateAfterGivenDate * 1000);

                    if (selectedPlace != null) {
                        // Use a Calendar object to set the minimum date
                        Calendar minDate = Calendar.getInstance();
                        minDate.setTimeInMillis(selectedPlace.getStartDate() * 1000);
                        datePickerDialog.setMinDate(minDate);

                        // Use a Calendar object to set the maximum date
                        Calendar maxDate = Calendar.getInstance();
                        maxDate.setTimeInMillis(selectedPlace.getEndDate() * 1000);

                        // Determine the minimum between maxDateCalendar and maxDate
                        Calendar minMaxDate = Calendar.getInstance();
                        minMaxDate.setTimeInMillis(Math.min(maxDateCalendar.getTimeInMillis(), maxDate.getTimeInMillis()));

                        datePickerDialog.setMaxDate(minMaxDate);
                    } else {
                        datePickerDialog.setMaxDate(maxDateCalendar);
                    }
                }


                if (!TextUtils.isEmpty(DateBeg.getText().toString())) {
                    Calendar selectedStartDate = Calendar.getInstance();
                    selectedStartDate.setTimeInMillis(myCalendar.getTimeInMillis());
                    datePickerDialog.setMinDate(selectedStartDate);
                }

                datePickerDialog.show(getSupportFragmentManager(), "DatePickerDialog");
            }
        });


    }

    private void populateSpinnerWithAuthorizedPlaces() {
        List<String> placeTitles = new ArrayList<>();
        for (PlaceUse place : cachedAuthorizedPlaces.values()) {
            placeTitles.add(place.getTitle());
        }
        Typeface customFont = ResourcesCompat.getFont(this, R.font.font2);
        placeTitles.add(0, ""); // Ajoutez une première ligne vide
        ArrayAdapter<String> adapter = new ArrayAdapter<>(EventActivity.this, android.R.layout.simple_spinner_item, placeTitles);

        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        MyPlacesList.setAdapter(adapter);

        if (selectedTitle != null && !selectedTitle.isEmpty()) {
            int selectedIndex = placeTitles.indexOf("test3");
            MyPlacesList.setSelection(selectedIndex);
        }

    }
    private String convertSecToString(long timestampSec) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            Date date = new Date(timestampSec * 1000);
            return dateFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
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

    private Calendar[] getDisabledDays() {
        List<Calendar> disabledDaysList = new ArrayList<>();

        for (EventUse event : cachedUserEvents.values()) {
            Calendar eventStartDate = Calendar.getInstance();
            eventStartDate.setTimeInMillis(event.getStartDate() * 1000);

            Calendar eventEndDate = Calendar.getInstance();
            eventEndDate.setTimeInMillis(event.getEndDate() * 1000);

            // Ajoutez toutes les dates comprises entre la startDate et la endDate
            while (!eventStartDate.after(eventEndDate)) {
                Calendar disabledDay = Calendar.getInstance();
                disabledDay.setTimeInMillis(eventStartDate.getTimeInMillis());
                disabledDaysList.add(disabledDay);
                eventStartDate.add(Calendar.DATE, 1);
            }
        }

        // Convertissez la liste en tableau
        return disabledDaysList.toArray(new Calendar[0]);
    }



    private void updateLabel() {
        String myFormate="dd/MM/yyyy";
        SimpleDateFormat dateFormat=new SimpleDateFormat(myFormate, Locale.US);
        if (selectedEditText != null) {
            selectedEditText.setText(dateFormat.format(myCalendar.getTime()));
        }
    }

    private long getMinStartDateAfterGivenDate(String givenDateString) {
        long minStartDate = Long.MAX_VALUE;

        // Convertissez la chaîne de date en nombre de secondes depuis le 1er janvier 1970
        long givenDateSec = convertStringToSec(givenDateString);

        for (EventUse event : cachedUserEvents.values()) {
            if (event.getStartDate() > givenDateSec && event.getStartDate() < minStartDate) {
                minStartDate = event.getStartDate();
            }
        }

        return minStartDate;
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode ==100 && resultCode == RESULT_OK){
            Place place = Autocomplete.getPlaceFromIntent(data);
            //Set adress on EditText
            EventLocation.setText(place.getAddress());
            // Set latitude and longitude in your Firestore document
            latitudeExt = place.getLatLng().latitude;
            longitudeExt = place.getLatLng().longitude;

        } else if(resultCode==AutocompleteActivity.RESULT_ERROR) {
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(getApplicationContext(), status.getStatusMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void changeStatusBarColor(int color) {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(color);
    }

    private PlaceUse findPlaceByTitle(String title) {
        for (PlaceUse place : cachedAuthorizedPlaces.values()) {
            if (place.getTitle().equals(title)) {
                return place;
            }
        }
        return null;
    }

    private boolean doesEventExistOnDate(Date selectedDate) {
        long selectedDateSec = selectedDate.getTime();

        for (EventUse event : cachedUserEvents.values()) {
            long eventStartDateSec = event.getStartDate();

            // Comparez les dates pour voir si un événement existe déjà à la date sélectionnée
            if (selectedDateSec == eventStartDateSec) {
                return true; // Un événement existe déjà à cette date
            }
        }

        return false;
    }
}