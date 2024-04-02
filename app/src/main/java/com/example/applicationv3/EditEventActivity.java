package com.example.applicationv3;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import java.util.TimeZone;

import pub.devrel.easypermissions.EasyPermissions;


public class EditEventActivity extends AppCompatActivity {

    private static final int RC_WRITE_CALENDAR = 123;
    private String uid;
    private DatabaseReference databaseReference;
    private PlacesClient placesClient;
    private boolean isInitializing = true;
    String placeTitle;
    FirebaseDatabase firebaseDatabase;
    EditText EventName, DateBeg, DateEnd, EventLocation;
    EditText selectedEditText;
    TextView LongLat;
    Switch SharedToFriends;
    long minStartDateAfterGivenDate;
    Button eventBtn;
    ImageView close;
    FirebaseAuth mAuth;
    Calendar myCalendar;
    double latitudeExt;
    double longitudeExt;
    String placeId;
    Spinner MyPlacesList;
    private Map<String, PlaceUse> cachedAuthorizedPlaces;
    private Map<String, EventUse> cachedUserEvents;
    private Map<String, String> titleToAddressMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

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


        String eventId = getIntent().getStringExtra("EVENT_ID");


        cachedAuthorizedPlaces = MasterActivity.getCachedAuthorizedPlaces();
        cachedUserEvents = MasterActivity.getCachedUserEvents();


        EventUse eventDetails = getEventDetailsFromCache(eventId);

        long eventBeg = eventDetails.getStartDate();
        long eventEnd = eventDetails.getEndDate();
        String eventTitle = eventDetails.getTitle();
        String eventPlaceId = eventDetails.getPlaceId();
        String eventAdress = eventDetails.getTitleAdress();
        double eventLatitude = eventDetails.getLatitude();
        double eventLongitude = eventDetails.getLongitude();


            PlaceUse placeDetails = cachedAuthorizedPlaces.get(eventPlaceId);
            if (placeDetails != null) {
                placeTitle = placeDetails.getTitle();
            }


        EventName.setText(eventTitle);
        EventLocation.setText(eventAdress);

        Date eventBegDate = new Date(eventBeg * 1000);
        Date eventEndDate = new Date(eventEnd * 1000);
        String formattedDateBeg = formatDate(eventBegDate);
        String formattedDateEnd = formatDate(eventEndDate);

        populateSpinnerWithAuthorizedPlaces(placeTitle);

        DateBeg.setText(formattedDateBeg);
        DateEnd.setText(formattedDateEnd);


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

                        //DateEnd.setText("");
                        //DateBeg.setText("");

                        String selectedPlaceTitle = MyPlacesList.getSelectedItem().toString();
                        PlaceUse selectedPlace = findPlaceByTitle(selectedPlaceTitle);
                        if (selectedPlace != null) {
                            // Remplissez EventLocation avec l'adresse associée à la place
                            EventLocation.setText(selectedPlace.getTitleAdress());
                            latitudeExt = selectedPlace.getLatitude();
                            longitudeExt = selectedPlace.getLongitude();
                            placeId = selectedPlace.getId();
                        }
                }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        Places.initialize(getApplicationContext(), getString(R.string.GOOGLE_MAPS_API_KEY));
        //Set EditText non focusable
        EventLocation.setFocusable(false);
        EventLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Initialize place field list
                List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS,Place.Field.LAT_LNG,Place.Field.NAME);
                //Create intent
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY,fieldList).build(EditEventActivity.this);
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
                if (!TextUtils.isEmpty(EventLoc) && !TextUtils.isEmpty(EventBeg) && !TextUtils.isEmpty(EventEnd)) {


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
                        event.put("id",eventId);


                        // Ajoutez cet événement à la collection "Event" sous l'UID de l'utilisateur actuel
                        db.collection("events")
                                .document(eventId) // Utilisez l'ID de l'événement existant
                                .set(event)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(EditEventActivity.this, "Event updated", Toast.LENGTH_SHORT).show();
                                        // Mettez à jour également dans le cache
                                        EventUse updatedEvent = new EventUse();
                                        updatedEvent.setId(eventId);
                                        updatedEvent.setTitle(EventTitle);
                                        updatedEvent.setTitleAdress(EventLoc);
                                        updatedEvent.setStartDate(timestampBeg);
                                        updatedEvent.setEndDate(timestampEnd);
                                        updatedEvent.setOwnerId(uid);
                                        updatedEvent.setToBeShared(toBeShared);
                                        updatedEvent.setLatitude(latitudeExt);
                                        updatedEvent.setLongitude(longitudeExt);
                                        updatedEvent.setPlaceId(placeId);

                                        MasterActivity.addEventToCachedUserEvents(eventId, updatedEvent);

                                        //Ajouter l'event dans le calendrier
                                        String[] perms = {Manifest.permission.WRITE_CALENDAR};
                                        if (EasyPermissions.hasPermissions(EditEventActivity.this, perms)) {
                                            // L'autorisation a déjà été accordée, écrivez dans le calendrier
                                            ContentResolver cr = getContentResolver();
                                            ContentValues values = new ContentValues();
                                            values.put(CalendarContract.Events.DTSTART, (timestampBeg)*1000); // Début de l'événement en millisecondes
                                            values.put(CalendarContract.Events.DTEND, (timestampEnd)*1000); // Fin de l'événement en millisecondes
                                            values.put(CalendarContract.Events.TITLE, EventTitle);
                                            values.put(CalendarContract.Events.DESCRIPTION, EventLoc);
                                            //values.put(CalendarContract.Events.CALENDAR_ID, calendarId); // ID du calendrier, consultez le calendrier par défaut
                                            values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

                                            // Ajoutez l'événement au calendrier
                                            Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

                                            if (uri != null) {
                                                // L'URI renvoyé peut être utilisé pour récupérer l'ID de l'événement si nécessaire
                                                //long eventId = Long.parseLong(uri.getLastPathSegment());
                                                Toast.makeText(EditEventActivity.this, "Event added to calendar", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(EditEventActivity.this, "Failed to add event to calendar", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            // L'autorisation n'a pas encore été accordée, demandez-la à l'utilisateur
                                            EasyPermissions.requestPermissions(EditEventActivity.this, "Permission nécessaire pour écrire dans le calendrier", RC_WRITE_CALENDAR, perms);
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(EditEventActivity.this, "Erreur lors de l'ajout de l'événement : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                        } else {
                        Toast.makeText(EditEventActivity.this, "Saisissez un lieu.", Toast.LENGTH_SHORT).show();
                        }

                } else {
                    // Affichez un message d'erreur si l'un des champs est vide
                    Toast.makeText(EditEventActivity.this, "Remplissez tous les champs", Toast.LENGTH_SHORT).show();
                }

            }

        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditEventActivity.this, MasterActivity.class);
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
            Calendar[] disabledDays = getDisabledDays(eventId);
            datePickerDialog.setDisabledDays(disabledDays);

            String selectedPlaceTitle = MyPlacesList.getSelectedItem().toString();
            PlaceUse selectedPlace = findPlaceByTitle(selectedPlaceTitle);


            if (selectedPlace != null) {
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
                Toast.makeText(EditEventActivity.this, "Veuillez d'abord sélectionner une date de début.", Toast.LENGTH_SHORT).show();
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

                Calendar[] disabledDays = getDisabledDays(eventId);
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



    private void populateSpinnerWithAuthorizedPlaces(String SelectedPlaceTitle) {
        List<String> placeTitles = new ArrayList<>();
        for (PlaceUse place : cachedAuthorizedPlaces.values()) {
            placeTitles.add(place.getTitle());
        }
        Typeface customFont = ResourcesCompat.getFont(this, R.font.font2);
                placeTitles.add(0, "");
                ArrayAdapter<String> adapter = new ArrayAdapter<>(EditEventActivity.this, android.R.layout.simple_spinner_item, placeTitles);

                adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                MyPlacesList.setAdapter(adapter);

        if (SelectedPlaceTitle != null) {
            int selectedIndex = placeTitles.indexOf(SelectedPlaceTitle);
            MyPlacesList.setSelection(selectedIndex);

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

    private void updateLabel() {
        String myFormate="dd/MM/yyyy";
        SimpleDateFormat dateFormat=new SimpleDateFormat(myFormate, Locale.US);
        if (selectedEditText != null) {
            selectedEditText.setText(dateFormat.format(myCalendar.getTime()));
        }
    }

    private Calendar[] getDisabledDays(String excludedEventID) {
        List<Calendar> disabledDaysList = new ArrayList<>();

        for (EventUse event : cachedUserEvents.values()) {
            // Vérifiez si l'événement actuel correspond à l'ID que vous souhaitez exclure
            if (!event.getId().equals(excludedEventID)) {
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
        }

        // Convertissez la liste en tableau
        return disabledDaysList.toArray(new Calendar[0]);
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
            latitudeExt = place.getLatLng().latitude;
            longitudeExt = place.getLatLng().longitude;

        } else if(resultCode==AutocompleteActivity.RESULT_ERROR) {
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(getApplicationContext(), status.getStatusMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private String formatDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        return dateFormat.format(date);
    }

    private void changeStatusBarColor(int color) {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(color);
    }

    private EventUse getEventDetailsFromCache(String eventId) {
        if (cachedUserEvents.containsKey(eventId)) {
            return cachedUserEvents.get(eventId);
        }
        return null;
    }
    private PlaceUse findPlaceByTitle(String title) {
        for (PlaceUse place : cachedAuthorizedPlaces.values()) {
            if (place.getTitle().equals(title)) {
                return place;
            }
        }
        return null;
    }

}