package com.example.applicationv3;


import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import com.example.applicationv3.Fragments.MapFragment;
import com.example.applicationv3.Fragments.NotificationFragment;
import com.example.applicationv3.Fragments.ProfileFragment;
import com.example.applicationv3.Fragments.SearchFragment;
import com.example.applicationv3.Model.EventUse;
import com.example.applicationv3.Model.Notification;
import com.example.applicationv3.Model.PlaceUse;
import com.example.applicationv3.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.common.reflect.TypeToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class MasterActivity extends AppCompatActivity {
    private static User cachedUser;
    private static Map<String, PlaceUse> cachedAuthorizedPlaces;
    private static Map<String, EventUse> cachedEvents = new HashMap<>();
    private static Map<String, Notification> cachedNotification = new HashMap<>();
    private static Map<String, EventUse> cachedPlaceEvents = new HashMap<>();
    private static Map<String, EventUse> cachedUserEvents = new HashMap<>();
    private static Map<String, User> friendsInfoMap = new HashMap<>();
    public DataManager dataManager;

    private BottomNavigationView bottomNavigationView;
    private Fragment selectorFragment;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();


    public static String getLoggedInUserId() {
        if (cachedUser != null) {
            return cachedUser.getId();
        } else {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = mAuth.getCurrentUser();
            return currentUser.getUid();
        }
    }


    public static User getCachedUser() {
        return cachedUser;
    }

    public static void setCachedUser(User user) {
        cachedUser = user;
    }

    public static Map<String, EventUse> getCachedEvents() {
        return cachedEvents;
    }

    public static Map<String, Notification> getCachedNotification() {
        return cachedNotification;
    }

    public static void setCachedNotifications(Map<String, Notification> notifications) {
        cachedNotification = notifications;
    }

    public static Map<String, EventUse> getCachedPlaceEvents() {
        return cachedPlaceEvents;
    }

    public static Map<String, EventUse> getCachedUserEvents() {
        return cachedUserEvents;
    }

    public static Map<String, User> getFriendsInfoMap() {return friendsInfoMap;}

    public static void setFriendsInfoMap(Map<String, User> map) {friendsInfoMap = map;}


    public static Map<String, PlaceUse> getCachedAuthorizedPlaces() {return cachedAuthorizedPlaces;}

    public static void setCachedAuthorizedPlaces(Map<String, PlaceUse> authorizedPlaces) {cachedAuthorizedPlaces = authorizedPlaces;}

    public static void addEventToCachedUserEvents(String eventId, EventUse event) {
        if (cachedUserEvents == null) {
            cachedUserEvents = new HashMap<>();
        }
        cachedUserEvents.put(eventId, event);
    }
    public static void removeEventFromCachedUserEvents(String eventId) {
        if (cachedUserEvents != null && cachedUserEvents.containsKey(eventId)) {
            cachedUserEvents.remove(eventId);
        }
    }

    public static void updateEventInCachedUserEvents(String eventId, EventUse updatedEvent) {
        if (cachedUserEvents != null && cachedUserEvents.containsKey(eventId)) {
            // Update the event with the new data
            cachedUserEvents.put(eventId, updatedEvent);
        }
    }

    public static void addPlaceToCachedAuthorizedPlaces(String placeId, PlaceUse place) {
        if (cachedAuthorizedPlaces == null) {
            cachedAuthorizedPlaces = new HashMap<>();
        }
        cachedAuthorizedPlaces.put(placeId, place);
    }
    public static void removePlaceFromCachedAuthorizedPlaces(String placeId) {
        if (cachedAuthorizedPlaces != null && cachedAuthorizedPlaces.containsKey(placeId)) {
            cachedAuthorizedPlaces.remove(placeId);
        }
    }
    public void updateAuthorizedPlace(PlaceUse updatedPlace) {

    }
    public static void clearCache() {
        cachedUser = null;
        cachedAuthorizedPlaces = null;
        cachedEvents = new HashMap<>();
        cachedPlaceEvents = new HashMap<>();
        cachedUserEvents = new HashMap<>();
        friendsInfoMap = new HashMap<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master);
// Changer la couleur de la barre de statut
        changeStatusBarColor(getResources().getColor(R.color.main_color2));




        //Récupération du token et stockage dans Firestore:
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (task.isSuccessful()) {
                            String token = task.getResult();
                            storeTokenInFirestore(token);
                        } else {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        }
                    }
                });

        askNotificationPermission();

        dataManager = DataManager.getInstance();
        selectorFragment = new MapFragment();
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.nav_home) {
                    selectorFragment = new MapFragment();
                } else if (itemId == R.id.nav_search) {
                    selectorFragment = new SearchFragment();
                } else if (itemId == R.id.nav_notification) {
                    selectorFragment = new NotificationFragment();
                    DataManager.getInstance().markNotificationsAsRead(DataManager.getInstance().getUserNotifications(), new DataCallback<Void>() {
                        @Override
                        public void onDataReceived(Void data) {
                            // Mettez à jour le badge
                            BadgeDrawable badgeDrawable = bottomNavigationView.getOrCreateBadge(R.id.nav_notification);
                            badgeDrawable.setVisible(false);

                            DataManager.getInstance().refreshCachedData();
                        }

                        @Override
                        public void onError(Exception e) {
                            // Gérez les erreurs ici
                        }
                    });
                } else if (itemId == R.id.nav_profile) {
                    ProfileFragment profileFragment = new ProfileFragment();
                    profileFragment.setCachedUser(cachedUser);
                    selectorFragment = profileFragment;
                }
                if(selectorFragment!=null){
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,selectorFragment).commit();
                }

                return true;
            }
        });

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {

            // Obtenez la référence de la collection "notifications"
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference notificationsRef = db.collection("notifications");

            // Ajoutez un snapshot listener avec une condition "receiverId"
            notificationsRef.whereEqualTo("receiverId", cachedUser.getId())
                    .addSnapshotListener(this, (queryDocumentSnapshots, e) -> {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        if (queryDocumentSnapshots != null) {
                            int unreadCount = 0;
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                Boolean isNew = document.getBoolean("isNew");
                                if (isNew != null && isNew) {
                                    unreadCount++;
                                }
                            }

                            // Mettez à jour le badge
                            updateUnreadNotificationBadge(unreadCount);
                        }
                    });
        }


        Bundle intent = getIntent().getExtras();
        if (intent != null) {
            String selectedFragment = intent.getString("selectedFragment");
            if (selectedFragment != null && selectedFragment.equals("profile")) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
                bottomNavigationView.setSelectedItemId(R.id.nav_profile);
            }
        }else{
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new MapFragment()).commit();
        }

        Log.d("MasterActivity", "Nombre de notifications: " + cachedNotification.size());
    }

    private void saveCachedData() {
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();


        Gson gson = new Gson();
        String cachedAuthorizedPlacesJson = gson.toJson(cachedAuthorizedPlaces);
        editor.putString("cachedAuthorizedPlaces", cachedAuthorizedPlacesJson);
        String cachedPlaceEventsJson = gson.toJson(cachedPlaceEvents);
        editor.putString("cachedEvents", cachedPlaceEventsJson);
        String cachedEventsJson = gson.toJson(cachedEvents);
        editor.putString("cachedEvents", cachedEventsJson);
        String cachedUserJson = gson.toJson(cachedUser);
        editor.putString("cachedUser", cachedUserJson);
        String cachedUserEventsJson = gson.toJson(cachedUserEvents);
        editor.putString("cachedUserEvents", cachedUserEventsJson);
        String friendsInfoMapJson = gson.toJson(friendsInfoMap);
        editor.putString("friendsInfoMap", friendsInfoMapJson);
        editor.apply();
    }


    private void loadCachedData() {
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String cachedAuthorizedPlacesJson = sharedPreferences.getString("cachedAuthorizedPlaces", null);
        if (cachedAuthorizedPlacesJson != null) {
            Type typeAuthorizedPlaces = new TypeToken<Map<String, PlaceUse>>() {}.getType();
            cachedAuthorizedPlaces = gson.fromJson(cachedAuthorizedPlacesJson, typeAuthorizedPlaces);
        }

        String cachedPlaceEventsJson = sharedPreferences.getString("cachedPlaceEvents", null);
        if (cachedPlaceEventsJson != null) {
            Type typePlaceEvents = new TypeToken<Map<String, EventUse>>() {}.getType();
            cachedPlaceEvents = gson.fromJson(cachedPlaceEventsJson, typePlaceEvents);
        }

        String cachedEventsJson = sharedPreferences.getString("cachedEvents", null);
        if (cachedEventsJson != null) {
            Type typeEvents = new TypeToken<Map<String, EventUse>>() {}.getType();
            cachedEvents = gson.fromJson(cachedEventsJson, typeEvents);
        }

        String cachedUserJson = sharedPreferences.getString("cachedUser", null);
        if (cachedUserJson != null) {
            cachedUser = gson.fromJson(cachedUserJson, User.class);
        }

        String cachedUserEventsJson = sharedPreferences.getString("cachedUserEvents", null);
        if (cachedUserEventsJson != null) {
            Type typeUserEvents = new TypeToken<Map<String, EventUse>>() {}.getType();
            cachedUserEvents = gson.fromJson(cachedUserEventsJson, typeUserEvents);
        }

        String friendsInfoMapJson = sharedPreferences.getString("friendsInfoMap", null);
        if (friendsInfoMapJson != null) {
            Type typeFriendsInfoMap = new TypeToken<Map<String, User>>() {}.getType();
            friendsInfoMap = gson.fromJson(friendsInfoMapJson, typeFriendsInfoMap);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //saveCachedData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //loadCachedData();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveCachedData();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        loadCachedData();
    }



    public int dpToPx(Context context, float dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
    private void storeTokenInFirestore(String token) {
        // Vérifiez si l'utilisateur est connecté
        if (cachedUser != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userDocRef = db.collection("tokens").document(cachedUser.getId());

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);

            userDocRef.set(data)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "FCM Token stored in Firestore successfully");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Error storing FCM Token in Firestore: " + e.getMessage(), e);
                        }
                    });
        } else {
            Log.e(TAG, "User not logged in. Unable to store FCM Token in Firestore.");
        }
    }


    private void updateUnreadNotificationBadge(int unreadCount) {
        if (unreadCount > 0) {
            BadgeDrawable badgeDrawable = bottomNavigationView.getOrCreateBadge(R.id.nav_notification);
            badgeDrawable.setVisible(true);
            badgeDrawable.setNumber(unreadCount);
            badgeDrawable.setVerticalOffset(dpToPx(MasterActivity.this, 3));
            badgeDrawable.setBadgeTextColor(getResources().getColor(com.nex3z.notificationbadge.R.color.red));
            badgeDrawable.setBadgeTextColor(getResources().getColor(R.color.white));
        }
    }

    private void changeStatusBarColor(int color) {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(color);
    }


    private void askNotificationPermission() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // Vérifier si l'application a déjà l'autorisation de recevoir des notifications
        boolean areNotificationsEnabled = notificationManager.areNotificationsEnabled();

        // Si les notifications ne sont pas activées, afficher une boîte de dialogue pour demander la permission
        if (!areNotificationsEnabled) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Autoriser les notifications");
            builder.setMessage("Voulez-vous autoriser les notifications ?");
            builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Ouvrir les paramètres de l'application pour permettre à l'utilisateur d'activer les notifications
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, MasterActivity.this.getPackageName());
                    MasterActivity.this.startActivity(intent); // Utilisez YourActivityName.this au lieu de this
                }
            });
            builder.setNegativeButton("Non", null);
            builder.show();
        }
    }

}