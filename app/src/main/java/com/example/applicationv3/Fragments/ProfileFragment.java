package com.example.applicationv3.Fragments;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.applicationv3.Adapter.EventAdapter;
import com.example.applicationv3.Adapter.PlaceAdapter;
import com.example.applicationv3.DataManager;
import com.example.applicationv3.EditProfileActivity;
import com.example.applicationv3.EventActivity;
import com.example.applicationv3.FeedbackActivity;
import com.example.applicationv3.FriendsListActivity;
import com.example.applicationv3.MainActivity;
import com.example.applicationv3.MasterActivity;
import com.example.applicationv3.Model.EventUse;
import com.example.applicationv3.Model.PlaceUse;
import com.example.applicationv3.Model.User;
import com.example.applicationv3.R;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileFragment extends Fragment {

    private CircleImageView imageProfile;
    ImageView menu;
    private DrawerLayout drawerLayout;
    private LinearLayout menuDrawer;
    private GestureDetector gestureDetector;
    private TextView eventsBtn;
    private TextView placesBtn;
    private TextView friends;
    TextView friendstext;
    private TextView fullname;
    private TextView bio;
    private TextView username;
    private TextView location;
    private TextView month;
    ImageView addEvent;
    private RecyclerView recyclerViewEvents;
    private RecyclerView recyclerViewPlaces;
    private Map<String, EventUse> cachedUserEvents;
    private Map<String, PlaceUse> cachedAuthorizedPlaces;
    private Map<String, EventUse> cachedPlaceEvents;
    private List<EventUse> eventUseList;
    private EventAdapter eventAdapter;
    private LinearLayout eventView;
    private LinearLayout placesView;
    private LinearLayout logout;
    private LinearLayout feedback;
    PlaceAdapter placeAdapter;
    private DataManager dataManager;
    private User cachedUser; // Pour stocker les données de l'utilisateur en cache
    CompactCalendarView compactCalendar;
    private Timestamp timestampSelected;
    private SimpleDateFormat dateFormatMonth = new SimpleDateFormat("MMMM- yyyy", Locale.getDefault());

    LinearLayout editProfile;
    String profileID;

    public void setCachedUser(User cachedUser) {
        this.cachedUser = cachedUser;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        dataManager = DataManager.getInstance();
        MasterActivity masterActivity = (MasterActivity) getActivity();
        cachedUserEvents = masterActivity.getCachedUserEvents();



        profileID = MasterActivity.getLoggedInUserId();
        imageProfile = view.findViewById(R.id.image_profile);
        menu = view.findViewById(R.id.menu);
        drawerLayout = view.findViewById(R.id.drawer_layout);
        menuDrawer = view.findViewById(R.id.menu_drawer);
        friends = view.findViewById(R.id.friends);
        friendstext=view.findViewById(R.id.friendstext);
        fullname = view.findViewById(R.id.fullname);
        bio = view.findViewById(R.id.bio);
        month = view.findViewById(R.id.month);
        location = view.findViewById(R.id.location);
        username = view.findViewById(R.id.username);
        editProfile = view.findViewById(R.id.edit_profile);
        addEvent = view.findViewById(R.id.addEvent);
        eventView = view.findViewById(R.id.eventView);
        placesView=view.findViewById(R.id.placesView);
        logout = view.findViewById(R.id.logout);
        feedback = view.findViewById(R.id.feedback);
        recyclerViewEvents = view.findViewById(R.id.recycler_view_events);
        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewPlaces = view.findViewById(R.id.recycler_view_places);
        recyclerViewPlaces.setLayoutManager(new LinearLayoutManager(getContext()));
        eventUseList = new ArrayList<>();
        eventAdapter = new EventAdapter(eventUseList, getContext());
        eventAdapter.setProfileFragment(this);
        recyclerViewEvents.setAdapter(eventAdapter);
        eventsBtn = view.findViewById(R.id.eventsBtn);
        placesBtn = view.findViewById(R.id.placesBtn);
        compactCalendar =  view.findViewById(R.id.compactcalendar_view);
        compactCalendar.setUseThreeLetterAbbreviation(true);
        // Ajoutez le code suivant pour obtenir le mois actuel et l'afficher dans le TextView month
        Calendar currentCalendar = Calendar.getInstance();
        String currentMonth = formatMonth(currentCalendar.getTime());
        currentCalendar.setTime(new Date());
        month.setText(currentMonth);
        //eventsBtn.setSelected(true);
        //placesBtn.setSelected(false);

        if (cachedUser != null) {
            fillUserInfo(cachedUser);
        } else {
            // Si les données de l'utilisateur ne sont pas encore définies, vous pouvez les récupérer ici ou définir les champs comme vides pour le moment
        }

        int numberOfFriends = MasterActivity.getFriendsInfoMap().size();
        friends.setText(String.valueOf(numberOfFriends));

        ItemTouchHelper.Callback callback = new EventAdapter.SwipeToDeleteCallback(eventAdapter, getContext());
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        EventAdapter.attachSwipeToDelete(itemTouchHelper, recyclerViewEvents);
        //ICI

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), EditProfileActivity.class));
            }
        });
        addEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), EventActivity.class);
                intent.putExtra("selectedDate", timestampSelected.toDate());
                startActivity(intent);
            }
        });
        friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FriendsListActivity.class);
                startActivity(intent);
            }
        });
        friendstext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ouvrez l'activity_friends_list.xml lorsque "friendstext" est cliqué
                Intent intent = new Intent(getContext(), FriendsListActivity.class);
                startActivity(intent);
            }
        });


        cachedAuthorizedPlaces = masterActivity.getCachedAuthorizedPlaces();


        eventsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventView.setVisibility(View.VISIBLE);
                placesView.setVisibility(View.GONE);

                eventsBtn.setTextColor(getResources().getColor(R.color.main_color3));
                placesBtn.setTextColor(getResources().getColor(R.color.second_color3));
            }
        });

        placesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                placesView.setVisibility(View.VISIBLE);
                eventView.setVisibility(View.GONE);

                eventsBtn.setTextColor(getResources().getColor(R.color.second_color3));
                placesBtn.setTextColor(getResources().getColor(R.color.main_color3));
            }
        });
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    drawerLayout.closeDrawer(GravityCompat.END);
                } else {
                    drawerLayout.openDrawer(GravityCompat.END);
                }
            }
        });

        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                final int SWIPE_MIN_DISTANCE = 120;
                final int SWIPE_MAX_OFF_PATH = 250;
                final int SWIPE_THRESHOLD_VELOCITY = 200;

                try {
                    if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MAX_OFF_PATH)
                        return false;
                    if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                        // Faire quelque chose lorsque le glissement vers la gauche est détecté
                        drawerLayout.closeDrawer(GravityCompat.END);
                        return true;
                    }
                } catch (Exception e) {
                    // nothing
                }
                return false;
            }
        });

        menuDrawer.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });
        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), FeedbackActivity.class));
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearCache();
                FirebaseAuth.getInstance().signOut();

                getActivity().finishAffinity();

                Intent intent = new Intent(getContext(), MainActivity.class);
                startActivity(intent);
            }
        });


        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), EditProfileActivity.class);
                startActivityForResult(intent, 1001); // Utilisation de startActivityForResult
            }
        });

        updateCompactCalendarView(cachedUserEvents);

        compactCalendar.setCurrentDate(new Date());
        compactCalendar.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                timestampSelected = new Timestamp(dateClicked);
                Log.d("CalendarDebug", "TimeStampSelected onDayClick: " + timestampSelected.toString());
                fetchEventsForDate(timestampSelected);
            }
            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                month.setText(dateFormatMonth.format(firstDayOfNewMonth));
            }
        });


        return view;
    }
    @Override
    public void onPause() {
        super.onPause();
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        }
    }

    // Ajoutez cette méthode pour récupérer les événements en cache
    private List<EventUse> getCachedUserEvents() {
        return eventUseList;
    }


    // Ajoutez cette méthode pour mettre à jour le RecyclerView avec les informations des places autorisées
    private void updatePlacesRecyclerView(List<PlaceUse> authorizedPlaces) {
        if (cachedAuthorizedPlaces != null && !cachedAuthorizedPlaces.isEmpty()) {
            placeAdapter = new PlaceAdapter(new ArrayList<>(cachedAuthorizedPlaces.values()), getContext(), profileID);
            recyclerViewPlaces.setAdapter(placeAdapter);
        }
    }

    private void fillUserInfo(User user) {
        if (user != null) {
            username.setText(user.getUsername());
            fullname.setText(user.getName());
            bio.setText(user.getBio());
            String locationText = user.getDefaultAdress();
            location.setText(locationText);
            // Utilisation de Picasso avec placeholder pour charger une image par défaut
            if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
                Picasso.get()
                        .load(user.getImageUrl())
                        .placeholder(R.drawable.user_bydefault) // Image par défaut
                        .into(imageProfile);
            }
        }
    }

    private void fetchEventsForDate(Timestamp selectedTimestamp) {
        long selectedMillis = selectedTimestamp.toDate().getTime();

        // Filtrer les événements en fonction de la date sélectionnée
        List<EventUse> filteredEvents = new ArrayList<>();
        for (EventUse eventUse : cachedUserEvents.values()) {
            long eventStartMillis = (eventUse.getStartDate()) * 1000;
            long eventEndMillis = (eventUse.getEndDate()) * 1000;

            if (eventStartMillis <= selectedMillis && eventEndMillis >= selectedMillis) {
                filteredEvents.add(eventUse);
            }
        }

        updateRecyclerView(filteredEvents);
    }

    // Nouvelle méthode pour mettre à jour le RecyclerView avec la liste d'événements
    private void updateRecyclerView(List<EventUse> eventUses) {
        if (getView() != null && recyclerViewEvents != null) {
            // Instanciez votre adaptateur d'événements avec la liste d'événements et configurez le RecyclerView
            if (eventAdapter == null) {
                eventAdapter = new EventAdapter(eventUses, getContext());
                recyclerViewEvents.setAdapter(eventAdapter);
            } else {
                eventAdapter.updateEventsList(eventUses);
            }
        }
    }


    private void clearCache() {
        MasterActivity.clearCache();
    }


    @Override
    public void onResume() {
        super.onResume();
        // Mettez à jour les informations de l'utilisateur à partir du cache
        cachedUser = MasterActivity.getCachedUser();
        cachedUserEvents = MasterActivity.getCachedUserEvents();
        fillUserInfo(cachedUser);

        cachedAuthorizedPlaces = MasterActivity.getCachedAuthorizedPlaces();
        if (cachedAuthorizedPlaces != null && !cachedAuthorizedPlaces.isEmpty()) {
            updatePlacesRecyclerView(new ArrayList<>(cachedAuthorizedPlaces.values()));
        }
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();
        timestampSelected = new Timestamp(currentDate.getTime() / 1000, 0);

        fetchEventsForDate(timestampSelected);

    }

    public void updateCompactCalendarView(Map<String, EventUse> cachedEventUses) {
        for (EventUse eventUse : cachedEventUses.values()) {
            long startMillis = (eventUse.getStartDate())*1000; // Already in milliseconds
            long endMillis = (eventUse.getEndDate())*1000; // Already in milliseconds
            int mainColor2 = ContextCompat.getColor(requireContext(), R.color.main_color2);

            long currentDateInMillis = startMillis;
            while (currentDateInMillis <= endMillis) {

                Event event = new Event(mainColor2, currentDateInMillis, eventUse.getTitle());
                compactCalendar.addEvent(event);

                currentDateInMillis += 24 * 60 * 60 * 1000; // Add one day in milliseconds
            }
        }
    }

    public void removeFromCompactCalendarView(EventUse eventUse) {
        long startMillis = (eventUse.getStartDate()) * 1000; // Already in milliseconds
        long endMillis = (eventUse.getEndDate()) * 1000; // Already in milliseconds
        int mainColor2 = ContextCompat.getColor(requireContext(), R.color.main_color2);

        long currentDateInMillis = startMillis;
        while (currentDateInMillis <= endMillis) {
            Event event = new Event(mainColor2, currentDateInMillis, eventUse.getTitle());
            compactCalendar.removeEvent(event);

            currentDateInMillis += 24 * 60 * 60 * 1000; // Add one day in milliseconds
        }
    }


    private String formatMonth(Date date) {
        SimpleDateFormat customFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        return customFormat.format(date);
    }
}