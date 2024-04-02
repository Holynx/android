package com.example.applicationv3;

import android.content.Intent;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.applicationv3.Adapter.AttenderAdapter;
import com.example.applicationv3.DeleteWindows.DeletePlaceConfirmation;
import com.example.applicationv3.Model.EventUse;
import com.example.applicationv3.Model.PlaceUse;
import com.example.applicationv3.Model.User;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.firebase.Timestamp;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


@OptIn(markerClass = com.google.android.material.badge.ExperimentalBadgeUtils.class)
public class MyPlaceActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AttenderAdapter attenderAdapter;
    private String placeId;
    private TextView month;
    private List<EventUse> allEventUses = new ArrayList<>();
    private List<User> allUsers = new ArrayList<>();
    private Map<String, PlaceUse> cachedAuthorizedPlaces;
    private android.icu.text.SimpleDateFormat dateFormatMonth = new android.icu.text.SimpleDateFormat("MMMM- yyyy", Locale.getDefault());
    CompactCalendarView compactCalendar;
    private Timestamp timestampSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_place);
        changeStatusBarColor(getResources().getColor(R.color.main_color3));

        ImageView back=findViewById(R.id.back);
        ImageView delete=findViewById(R.id.delete);
        ImageView edit = findViewById(R.id.edit);
        month = findViewById(R.id.month);
        compactCalendar =  findViewById(R.id.compactcalendar_view);
        compactCalendar.setUseThreeLetterAbbreviation(true);
        attenderAdapter = new AttenderAdapter(new ArrayList<>(), this);
        // Ajoutez le code suivant pour obtenir le mois actuel et l'afficher dans le TextView month
        Calendar currentCalendar = Calendar.getInstance();
        String currentMonth = formatMonth(currentCalendar.getTime());
        currentCalendar.setTime(new Date());
        month.setText(currentMonth);
        cachedAuthorizedPlaces = MasterActivity.getCachedAuthorizedPlaces();

        timestampSelected = new Timestamp(Calendar.getInstance().getTime());

        // Récupérez le titre de la place et l'URL de l'image depuis l'Intent
        String profileID = MasterActivity.getLoggedInUserId();
        placeId = getIntent().getStringExtra("PLACE_ID");
        PlaceUse selectedPlace = null;

        for (Map.Entry<String, PlaceUse> entry : cachedAuthorizedPlaces.entrySet()) {
            String currentPlaceId = entry.getValue().getId();
            PlaceUse currentPlace = entry.getValue();

            Log.d("MyPlaceActivity", "Comparing placeId: " + placeId + " with currentPlaceId: " + currentPlaceId);

            // Vérifiez si l'ID de la place actuelle correspond à placeId
            if (currentPlaceId.equals(placeId)) {
                // Si c'est le cas, cette place est la selectedPlace
                selectedPlace = currentPlace;
                break;
            }
        }

        // Vous pouvez maintenant extraire les informations nécessaires de selectedPlace
        String placeTitle = selectedPlace.getTitle();
        String placeDescription = selectedPlace.getDescription();
        String placeAdress = selectedPlace.getTitleAdress();
        String placeImageURL = selectedPlace.getImageUrl();
        long startDate = selectedPlace.getStartDate();
        long endDate = selectedPlace.getEndDate();
        List<String> ownerIds = selectedPlace.getOwnerId();
        List<String> ContactsAllowed = selectedPlace.getContactsAllowed();
        int contactsAllowedCount = ContactsAllowed.size();


        // Assurez-vous d'avoir les vues correspondantes dans votre layout activity_my_place.xml
        TextView titleTextView = findViewById(R.id.title);
        TextView descriptionTextView = findViewById(R.id.description);
        TextView dateStartTextView = findViewById(R.id.dateStart);
        TextView dateEndTextView = findViewById(R.id.dateEnd);
        TextView locationTextView = findViewById(R.id.location);
        LinearLayout temporal = findViewById(R.id.temporal);
        TextView permanent = findViewById(R.id.permanent);
        TextView number = findViewById(R.id.number);
        CircleImageView imageProfile = findViewById(R.id.image_profile);

        // Remplissage Titre
        titleTextView.setText(placeTitle);
        descriptionTextView.setText(placeDescription);
        locationTextView.setText(placeAdress);
        number.setText(String.valueOf(ContactsAllowed.size()));

        // Remplissage Image
        if (placeImageURL != null && !placeImageURL.isEmpty()) {
            Picasso.get().load(placeImageURL).placeholder(R.drawable.place_bydefault).into(imageProfile);
        } else {
            imageProfile.setImageResource(R.drawable.place_bydefault);
        }


        fetchDataForPlace(placeId);

        if (endDate == 4102441200L) {
            temporal.setVisibility(View.INVISIBLE);
            permanent.setVisibility(View.VISIBLE);
        } else {
            temporal.setVisibility(View.VISIBLE);
            permanent.setVisibility(View.INVISIBLE);
        }
        // Remplissage Dates

        String startDateFormated = formatDate(startDate*1000);
        String endDateFormated = formatDate(endDate*1000);
        dateStartTextView.setText(startDateFormated);
        dateEndTextView.setText(endDateFormated);

        // Initialisez le RecyclerView
        recyclerView = findViewById(R.id.recycler_view_attenders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Calendar calendar = Calendar.getInstance();
        long currentDateInMillis = calendar.getTimeInMillis();
        compactCalendar.setCurrentDate(new Date());


        compactCalendar.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                timestampSelected = new Timestamp(dateClicked);
                fetchAttendersForDate(timestampSelected);

            }
            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                month.setText(dateFormatMonth.format(firstDayOfNewMonth));
                timestampSelected = new Timestamp(firstDayOfNewMonth);
                fetchAttendersForDate(timestampSelected);

            }
        });
        timestampSelected = new Timestamp(Calendar.getInstance().getTime());
        fetchAttendersForDate(timestampSelected);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });

        // Vérifiez si profileID est inclus dans la liste OWNER_ID
        if (ownerIds != null && ownerIds.contains(profileID)) {
            // Si profileID est présent dans la liste, affichez le bouton de suppression
            delete.setVisibility(View.VISIBLE);
            edit.setVisibility(View.VISIBLE);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Logique de suppression
                }
            });
        } else {
            // Si profileID n'est pas présent dans la liste, masquez le bouton de suppression
            delete.setVisibility(View.GONE);
            edit.setVisibility(View.GONE);
        }

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyPlaceActivity.this, EditPlaceActivity.class);
                intent.putExtra("PLACE_ID", placeId);
                startActivity(intent);
                finish();
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Créez une instance du dialog et affichez-le lorsque l'icône de suppression est cliquée
                DeletePlaceConfirmation dialog = new DeletePlaceConfirmation(MyPlaceActivity.this);
                dialog.setPlaceIdToDelete(placeId);
                dialog.setMyPlaceActivityReference(MyPlaceActivity.this);
                dialog.show();
            }
        });

    }
    private void fetchDataForPlace(String placeId) {
        DataManager.getInstance().fetchEventsForPlace(placeId, new DataCallback<List<EventUse>>() {
            @Override
            public void onDataReceived(List<EventUse> eventUses) {
                if (eventUses != null && !eventUses.isEmpty()) {
                    allEventUses.clear();
                    allEventUses.addAll(eventUses);

                    // Fetch ownerIds from the retrieved events
                    List<String> ownerIds = new ArrayList<>();
                    for (EventUse eventUse : eventUses) {
                        ownerIds.add(eventUse.getOwnerId());
                    }

                    // Fetch users for the retrieved ownerIds
                    fetchUsersForOwnerIds(ownerIds);
                } else {
                    // Handle case where no events are associated with this place
                    Log.d("MyPlaceActivity", "No events found for this place.");
                    // You might reset the adapter or display a message to the user here if needed.
                }
            }

            @Override
            public void onError(Exception e) {
                // Handle errors if necessary
            }
        });
    }

    private void fetchUsersForOwnerIds(List<String> ownerIds) {
        DataManager.getInstance().fetchUsersForOwnerIds(ownerIds, new DataCallback<List<User>>() {
            @Override
            public void onDataReceived(List<User> users) {
                if (users != null && !users.isEmpty()) {
                    allUsers.clear();
                    allUsers.addAll(users);


                } else {
                    Log.d("MyPlaceActivity", "No users found for these events.");

                }
            }

            @Override
            public void onError(Exception e) {
                // Handle errors if necessary
            }
        });
    }


    private void fetchAttendersForDate(Timestamp timestampSelected) {
        if (!allEventUses.isEmpty()) {
            List<String> ownerIds = new ArrayList<>();
            Map<String, String> userEventMap = new HashMap<>();

            for (EventUse eventUse : allEventUses) {
                long startTimestampSeconds = eventUse.getStartDate();
                long endTimestampSeconds = eventUse.getEndDate();
                long startTimestampMillis = startTimestampSeconds * 1000;
                long endTimestampMillis = endTimestampSeconds * 1000;
                Date startDate = new Date(startTimestampMillis);
                Date endDate = new Date(endTimestampMillis);
                Timestamp startDateTime = new Timestamp(startDate);
                Timestamp endDateTime = new Timestamp(endDate);
                long timestampSelectedMillis = timestampSelected.getSeconds() * 1000;
                Date selectedDate = new Date(timestampSelectedMillis);

                // Vérifier si la date sélectionnée est entre StartDate et EndDate de l'événement
                Timestamp timestampForComparison = new Timestamp(selectedDate);

                if (startDateTime.compareTo(timestampForComparison) <= 0 && endDateTime.compareTo(timestampForComparison) >= 0) {
                    ownerIds.add(eventUse.getOwnerId());
                    // Obtenez le nom de l'événement
                    String eventName = eventUse.getTitle();

                }
            }


            if (!ownerIds.isEmpty()) {
                List<User> usersForDate = new ArrayList<>();
                for (User user : allUsers) {
                    if (ownerIds.contains(user.getId())) {
                        // Ajoutez les utilisateurs correspondant aux ownerIds à la liste usersForDate
                        usersForDate.add(user);
                    }
                }
                recyclerView.setAdapter(null);
                attenderAdapter = new AttenderAdapter(usersForDate, MyPlaceActivity.this);
                recyclerView.setAdapter(attenderAdapter);
            } else {
                // La liste des événements est vide, aucune action nécessaire pour les utilisateurs
                Log.d("MyPlaceActivity", "No events found for this date.");
                // Vous pourriez également réinitialiser l'adaptateur ou effacer les données précédentes ici si nécessaire.
            }

        } else {
            // Gérer le cas où aucun événement n'est associé à cet emplacement
            Log.d("MyPlaceActivity", "No events found for this place.");
            // Vous pourriez réinitialiser l'adaptateur ou afficher un message à l'utilisateur ici si nécessaire.
        }

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            // Mise à jour des données à partir des données renvoyées par EditPlaceActivity

        }

    }





    private String formatMonth(Date date) {
        // Utilisez un format personnalisé pour obtenir le mois avec une majuscule à la première lettre
        android.icu.text.SimpleDateFormat customFormat = new android.icu.text.SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        return customFormat.format(date);
    }
    private String formatDate(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(date);
    }

    private void addBadgesToCalendar(List<Calendar> daysWithParticipants) {
        for (Calendar day : daysWithParticipants) {
            compactCalendar.addEvent(new Event(Color.RED, day.getTimeInMillis()));
        }
    }


    private void changeStatusBarColor(int color) {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(color);
    }

}