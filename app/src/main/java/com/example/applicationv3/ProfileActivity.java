package com.example.applicationv3;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.applicationv3.Adapter.FriendEventAdapter;
import com.example.applicationv3.Model.EventUse;
import com.example.applicationv3.Model.RelationshipInfo;
import com.example.applicationv3.Model.User;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.WriteBatch;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView imageProfile;
    private TextView fullname;
    private TextView bio;
    private TextView username;
    private TextView location;
    private DataManager dataManager;
    private Boolean isFriend;
    CompactCalendarView compactCalendar;

    private Button AddRemove;
    private Button acceptBtn;
    private Button refuseBtn;
    private ImageView close;
    private TextView month;
    private LinearLayout friendFrame;
    String profileID;
    private ListenerRegistration userListener;
    private String UserID;
    private String UserName;
    private RecyclerView recyclerViewEvents;
    private android.icu.text.SimpleDateFormat dateFormatMonth = new android.icu.text.SimpleDateFormat("MMMM- yyyy", Locale.getDefault());
    private FriendEventAdapter friendEventAdapter;
    Map<String, EventUse> friendsEventsMap;
    Map<String, EventUse> cachedProfileEvents;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        imageProfile = findViewById(R.id.image_profile);
        fullname = findViewById(R.id.fullname);
        bio = findViewById(R.id.bio);
        location = findViewById(R.id.location);
        username = findViewById(R.id.username);
        AddRemove = findViewById(R.id.add_remove);
        month = findViewById(R.id.month);
        acceptBtn = findViewById(R.id.acceptBtn);
        refuseBtn = findViewById(R.id.refuseBtn);
        close = findViewById(R.id.close);
        friendFrame = findViewById(R.id.friend_frame);
        dataManager = DataManager.getInstance();
        recyclerViewEvents = findViewById(R.id.recycler_view_events);
        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(this));
        compactCalendar =  findViewById(R.id.compactcalendar_view);
        compactCalendar.setUseThreeLetterAbbreviation(true);
        // Ajoutez le code suivant pour obtenir le mois actuel et l'afficher dans le TextView month
        Calendar currentCalendar = Calendar.getInstance();
        String currentMonth = formatMonth(currentCalendar.getTime());
        currentCalendar.setTime(new Date());
        month.setText(currentMonth);

        changeStatusBarColor(getResources().getColor(R.color.main_color3));

        Map<String, User> friendsInfoMap = MasterActivity.getFriendsInfoMap();
        friendsEventsMap = MasterActivity.getCachedEvents();
        Log.d("ProfileActivity", "Number of events total: " + friendsEventsMap.size());
        profileID = getIntent().getStringExtra("clickedID");
        cachedProfileEvents = getCachedProfileEvents(friendsEventsMap, profileID);
        Log.d("ProfileActivity", "Number of events input: " + cachedProfileEvents.size());
        updateCompactCalendarView(cachedProfileEvents);

        User ConnectedUser = MasterActivity.getCachedUser();
        UserID = ConnectedUser.getId();
        UserName = ConnectedUser.getName();

        if (profileID != null) {
            if (friendsInfoMap.containsKey(profileID)) {
                // Si les informations de l'ami existent dans la Map, utilisez-les pour remplir les TextView
                User friend = friendsInfoMap.get(profileID);
                if (friend.getImageUrl() != null && !friend.getImageUrl().isEmpty()) {
                    Picasso.get()
                            .load(friend.getImageUrl())
                            .placeholder(R.drawable.user_bydefault) // Image par défaut
                            .into(imageProfile);
                }
                username.setText(friend.getUsername());
                fullname.setText(friend.getName());
                bio.setText(friend.getBio());
                String locationText = friend.getDefaultAdress();
                location.setText(locationText);

            } else {
                userListener = FirebaseFirestore.getInstance().collection("users").document(profileID)
                        .addSnapshotListener((documentSnapshot, e) -> {
                            if (e != null) {
                                // Gérez les erreurs ici
                                return;
                            }

                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                // Extrayez les valeurs de champs nécessaires du document
                                User user = documentSnapshot.toObject(User.class);
                                if (user != null) {
                                    // Utilisez ces valeurs pour remplir les EditText de votre ProfileActivity
                                    if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
                                        Picasso.get()
                                                .load(user.getImageUrl())
                                                .placeholder(R.drawable.user_bydefault) // Image par défaut
                                                .into(imageProfile);
                                    }
                                    username.setText(user.getUsername());
                                    fullname.setText(user.getName());
                                    bio.setText(user.getBio());
                                    String locationText = user.getDefaultAdress();
                                    location.setText(locationText);
                                }
                            }
                        });
            }
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        // Remplir le recyclerView avec les Events de l'utilisateur
// Assurez-vous d'avoir récupéré les données de friendsEventsMap
        if (friendsEventsMap != null) {
            List<EventUse> filteredEventUses = new ArrayList<>();

            for (Map.Entry<String, EventUse> entry : friendsEventsMap.entrySet()) {
                EventUse eventUse = entry.getValue();
                if (eventUse.getOwnerId().equals(profileID)) {
                    filteredEventUses.add(eventUse);
                }
            }
        }


        DataManager.getInstance().fetchRelationshipInfo(new DataCallback<RelationshipInfo>() {
            @Override
            public void onDataReceived(RelationshipInfo relationshipInfo) {
                if (relationshipInfo.getFriends() != null && relationshipInfo.getFriends().contains(profileID)) {
                    AddRemove.setText("Remove");
                    friendFrame.setVisibility(View.VISIBLE);
                } else if (relationshipInfo.getPending() != null && relationshipInfo.getPending().contains(profileID)) {
                    // L'utilisateur est dans "pending", affichez les boutons "Accept" et "Refuse"
                    acceptBtn.setVisibility(View.VISIBLE);
                    refuseBtn.setVisibility(View.VISIBLE);
                    // Masquez le bouton "Add/Remove"
                    AddRemove.setVisibility(View.GONE);
                    friendFrame.setVisibility(View.GONE);
                } else if (relationshipInfo.getAsked() != null && relationshipInfo.getAsked().contains(profileID)) {
                    AddRemove.setText("Cancel request");
                    friendFrame.setVisibility(View.GONE);
                } else {
                    AddRemove.setText("Add");
                    friendFrame.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(Exception e) {
                // Gérez les erreurs ici
            }
        });

        AddRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AddRemove.getText().toString().equals("Add")) {
                    addFriend();
                    AddRemove.setText("Cancel request");
                } else if (AddRemove.getText().toString().equals("Remove")) {
                    removeFriend();
                    AddRemove.setText("Add");
                    friendFrame.setVisibility(View.GONE);
                } else if (AddRemove.getText().toString().equals("Cancel request")) {
                    cancelRequest();
                    AddRemove.setText("Add");
                    //Supprimer la notification?
                }
            }
            private void createAddFriendNotification() {
                Date currentDate1 = new Date();
                long timestampSeconds = currentDate1.getTime() / 1000;
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String message = ConnectedUser.getName() + " vous a demandé comme ami ";
                Map<String, Object> notificationData = new HashMap<>();
                notificationData.put("senderId", UserID);
                notificationData.put("receiverId", profileID);
                notificationData.put("isNew", true);
                notificationData.put("createdDate", timestampSeconds);
                notificationData.put("eventId", "");
                notificationData.put("placeId", "");
                notificationData.put("message", message);
                db.collection("notifications")
                        .add(notificationData)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                String notificationId = documentReference.getId();
                                db.collection("notifications").document(notificationId).update("id", notificationId);
                            }
                        });
            }
            private void addFriend() {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String currentUserId = UserID;

                // Créez une référence aux documents "relationships" pour les deux utilisateurs
                DocumentReference currentUserRelationshipRef = db.collection("relationships").document(currentUserId);
                DocumentReference profileUserRelationshipRef = db.collection("relationships").document(profileID);

                db.runTransaction(transaction -> {
                    // Lire le document de l'utilisateur actuel
                    DocumentSnapshot currentUserDoc = transaction.get(currentUserRelationshipRef);

                    // Lire le document du profil de l'ami
                    DocumentSnapshot profileUserDoc = transaction.get(profileUserRelationshipRef);

                    // Mettre à jour "asked" du document de l'utilisateur actuel
                    List<String> asked = currentUserDoc.contains("asked") ? (List<String>) currentUserDoc.get("asked") : new ArrayList<>();
                    asked.add(profileID);
                    transaction.update(currentUserRelationshipRef, "asked", asked);

                    // Mettre à jour "pending" du document du profil de l'ami
                    List<String> pending = profileUserDoc.contains("pending") ? (List<String>) profileUserDoc.get("pending") : new ArrayList<>();
                    pending.add(currentUserId);
                    transaction.update(profileUserRelationshipRef, "pending", pending);

                    // Réussite de la transaction
                    isFriend = true;
                    createAddFriendNotification();
                    updateDataManagerOnAddFriend(currentUserId, profileID);

                    return null;
                }).addOnSuccessListener(aVoid -> {
                }).addOnFailureListener(e -> {
                });
            }


            private void updateDataManagerOnAddFriend(String currentUserId, String profileId) {
                // Mettez à jour le DataManager pour refléter les modifications
                DataManager.getInstance().addToAsked(profileId, currentUserId);
            }


            private void removeFriend() {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String currentUserId = UserID;

                // Créez une référence aux documents "relationships" pour les deux utilisateurs
                DocumentReference currentUserRelationshipRef = db.collection("relationships").document(currentUserId);
                DocumentReference profileUserRelationshipRef = db.collection("relationships").document(profileID);

                db.runTransaction(transaction -> {
                    // Lire le document de l'utilisateur actuel
                    DocumentSnapshot currentUserDoc = transaction.get(currentUserRelationshipRef);

                    // Lire le document du profil de l'ami
                    DocumentSnapshot profileUserDoc = transaction.get(profileUserRelationshipRef);

                    // Mettre à jour "friends" du document de l'utilisateur actuel
                    List<String> friends = currentUserDoc.contains("friends") ? (List<String>) currentUserDoc.get("friends") : new ArrayList<>();
                    friends.remove(profileID);
                    transaction.update(currentUserRelationshipRef, "friends", friends);

                    // Mettre à jour "friends" du document du profil de l'ami
                    List<String> profileUserFriends = profileUserDoc.contains("friends") ? (List<String>) profileUserDoc.get("friends") : new ArrayList<>();
                    profileUserFriends.remove(currentUserId);
                    transaction.update(profileUserRelationshipRef, "friends", profileUserFriends);

                    // Réussite de la transaction
                    isFriend = false;
                    updateDataManagerOnRemoveFriend(currentUserId, profileID);

                    return null;
                }).addOnSuccessListener(aVoid -> {
                    // Succès de la transaction
                }).addOnFailureListener(e -> {
                    // Gestion de l'échec de la transaction
                });
            }

            private void updateDataManagerOnRemoveFriend(String currentUserId, String profileId) {
                // Mettez à jour le DataManager pour refléter les modifications
                DataManager.getInstance().removeFriend(currentUserId, profileId);
            }

            private void cancelRequest() {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String currentUserId = UserID;

                // Créez une référence aux documents "relationships" pour les deux utilisateurs
                DocumentReference currentUserRelationshipRef = db.collection("relationships").document(currentUserId);
                DocumentReference profileUserRelationshipRef = db.collection("relationships").document(profileID);

                // Créez un lot pour effectuer les opérations atomiques
                WriteBatch batch = db.batch();

                // Supprimez l'ID du profil du champ "friends" du document de l'utilisateur actuel
                batch.update(currentUserRelationshipRef, "asked", FieldValue.arrayRemove(profileID));
                // Supprimez l'ID de l'utilisateur actuel du champ "friends" du document du profil
                batch.update(profileUserRelationshipRef, "pending", FieldValue.arrayRemove(currentUserId));

                // Exécutez le lot
                batch.commit()
                        .addOnSuccessListener(aVoid -> {
                            isFriend = false;
                            updateDataManagerOnCancelRequest(currentUserId, profileID);
                        })
                        .addOnFailureListener(e -> {
                            // Gérez l'échec, si nécessaire
                        });
            }

            private void updateDataManagerOnCancelRequest(String currentUserId, String profileId) {
                // Mettez à jour le DataManager pour refléter les modifications
                DataManager.getInstance().cancelRequest(currentUserId, profileId);
            }
        });

        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptFriendRequest();
                acceptBtn.setVisibility(View.GONE);
                refuseBtn.setVisibility(View.GONE);
                // Masquez le bouton "Add/Remove"
                AddRemove.setVisibility(View.VISIBLE);
                AddRemove.setText("Remove");
                friendFrame.setVisibility(View.VISIBLE);
                createFriendAcceptedNotification();
            }
            private void acceptFriendRequest() {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String currentUserId = UserID;

                // Créez une référence aux documents "relationships" pour les deux utilisateurs
                DocumentReference currentUserRelationshipRef = db.collection("relationships").document(currentUserId);
                DocumentReference profileUserRelationshipRef = db.collection("relationships").document(profileID);

                db.runTransaction(transaction -> {
                    // Lire le document de l'utilisateur actuel
                    DocumentSnapshot currentUserDoc = transaction.get(currentUserRelationshipRef);

                    // Lire le document du profil de l'ami
                    DocumentSnapshot profileUserDoc = transaction.get(profileUserRelationshipRef);

                    // Mettre à jour "pending" du document de l'utilisateur actuel
                    List<String> pending = currentUserDoc.contains("pending") ? (List<String>) currentUserDoc.get("pending") : new ArrayList<>();
                    pending.remove(profileID);
                    transaction.update(currentUserRelationshipRef, "pending", pending);

                    // Mettre à jour "friends" du document de l'utilisateur actuel
                    List<String> friends = currentUserDoc.contains("friends") ? (List<String>) currentUserDoc.get("friends") : new ArrayList<>();
                    friends.add(profileID);
                    transaction.update(currentUserRelationshipRef, "friends", friends);

                    // Mettre à jour "asked" du document du profil de l'ami
                    List<String> asked = profileUserDoc.contains("asked") ? (List<String>) profileUserDoc.get("asked") : new ArrayList<>();
                    asked.remove(currentUserId);
                    transaction.update(profileUserRelationshipRef, "asked", asked);

                    // Mettre à jour "friends" du document du profil de l'ami
                    List<String> profileUserFriends = profileUserDoc.contains("friends") ? (List<String>) profileUserDoc.get("friends") : new ArrayList<>();
                    profileUserFriends.add(currentUserId);
                    transaction.update(profileUserRelationshipRef, "friends", profileUserFriends);

                    // Réussite de la transaction
                    isFriend = true;
                    updateDataManagerOnAcceptFriend(currentUserId, profileID);
                    // Vous pouvez ajouter ici des actions supplémentaires si nécessaire

                    return null;
                }).addOnSuccessListener(aVoid -> {
                    // Succès de la transaction
                    // Mettez à jour l'interface utilisateur ou effectuez d'autres actions ici
                }).addOnFailureListener(e -> {
                    // Gestion de l'échec de la transaction
                    // Vous pouvez gérer les erreurs ici
                });
            }
            private void updateDataManagerOnAcceptFriend(String currentUserId, String profileId) {
                // Mettez à jour le DataManager pour refléter les modifications
                DataManager.getInstance().addFriend(currentUserId, profileId);
            }

            private void createFriendAcceptedNotification() {
                Date currentDate1 = new Date();
                long timestampSeconds = currentDate1.getTime() / 1000;
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                        String message = ConnectedUser.getName() + " vous a accepté comme ami ";
                        Map<String, Object> notificationData = new HashMap<>();
                        notificationData.put("senderId", UserID);
                        notificationData.put("receiverId", profileID);
                        notificationData.put("isNew", true);
                        notificationData.put("createdDate", timestampSeconds);
                        notificationData.put("eventId", "");
                        notificationData.put("placeId", "");
                        notificationData.put("message", message);
                        db.collection("notifications")
                                .add(notificationData)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        String notificationId = documentReference.getId();
                                        db.collection("notifications").document(notificationId).update("id", notificationId);
                                    }
                                });
            }

        });

        refuseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refuseFriendRequest();
                acceptBtn.setVisibility(View.GONE);
                refuseBtn.setVisibility(View.GONE);
                // Masquez le bouton "Add/Remove"
                AddRemove.setVisibility(View.VISIBLE);
                AddRemove.setText("Add");
            }
            private void refuseFriendRequest() {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String currentUserId = UserID;

                // Créez une référence aux documents "relationships" pour les deux utilisateurs
                DocumentReference currentUserRelationshipRef = db.collection("relationships").document(currentUserId);
                DocumentReference profileUserRelationshipRef = db.collection("relationships").document(profileID);

                db.runTransaction(transaction -> {
                    // Lire le document de l'utilisateur actuel
                    DocumentSnapshot currentUserDoc = transaction.get(currentUserRelationshipRef);

                    // Lire le document du profil de l'ami
                    DocumentSnapshot profileUserDoc = transaction.get(profileUserRelationshipRef);

                    // Mettre à jour "pending" du document de l'utilisateur actuel
                    List<String> pending = currentUserDoc.contains("pending") ? (List<String>) currentUserDoc.get("pending") : new ArrayList<>();
                    pending.remove(profileID);
                    transaction.update(currentUserRelationshipRef, "pending", pending);

                    // Mettre à jour "asked" du document du profil de l'ami
                    List<String> asked = profileUserDoc.contains("asked") ? (List<String>) profileUserDoc.get("asked") : new ArrayList<>();
                    asked.remove(currentUserId);
                    transaction.update(profileUserRelationshipRef, "asked", asked);


                    // Réussite de la transaction
                    isFriend = true;
                    updateDataManagerOnRefuseFriend(currentUserId, profileID);
                    // Vous pouvez ajouter ici des actions supplémentaires si nécessaire

                    return null;
                }).addOnSuccessListener(aVoid -> {
                }).addOnFailureListener(e -> {
                });
            }
            private void updateDataManagerOnRefuseFriend(String currentUserId, String profileId) {
                // Mettez à jour le DataManager pour refléter les modifications
                DataManager.getInstance().refuseFriend(currentUserId, profileId);
            }
        });

        if (friendsEventsMap != null) {
            List<EventUse> filteredEventUses = new ArrayList<>();

            // Parcourez les événements dans friendsEventsMap et filtrez-les
            for (Map.Entry<String, EventUse> entry : friendsEventsMap.entrySet()) {
                EventUse eventUse = entry.getValue();
                if (eventUse.getOwnerId().equals(profileID)) {
                    filteredEventUses.add(eventUse);
                }
            }
        }

        Calendar calendar = Calendar.getInstance();
        long currentDateInMillis = calendar.getTimeInMillis();
        compactCalendar.setCurrentDate(new Date());
        compactCalendar.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                Timestamp timestampSelected = new Timestamp(dateClicked);
                fetchEventsForDate(timestampSelected);

            }
            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                month.setText(dateFormatMonth.format(firstDayOfNewMonth));
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Timestamp timestampToday = new Timestamp(Calendar.getInstance().getTime());

        fetchEventsForDate(timestampToday);

    }
    private void fetchEventsForDate(Timestamp selectedTimestamp) {
        long selectedMillis = selectedTimestamp.toDate().getTime();

        List<EventUse> filteredEvents = new ArrayList<>();

        for (EventUse eventUse : cachedProfileEvents.values()) {
            long eventStartMillis = (eventUse.getStartDate()) * 1000; // Already in milliseconds
            long eventEndMillis = (eventUse.getEndDate()) * 1000; // Already in milliseconds

            if (eventStartMillis <= selectedMillis && eventEndMillis >= selectedMillis) {
                filteredEvents.add(eventUse);
            }
        }
        Log.d("ProfileActivity", "Selected Timestamp: " + selectedTimestamp);

        Log.d("ProfileActivity", "Number of events output: " + filteredEvents.size());
        friendEventAdapter = new FriendEventAdapter(filteredEvents, profileID, UserID, selectedMillis,UserName);
        recyclerViewEvents.setAdapter(friendEventAdapter);
    }


    private void updateCompactCalendarView(Map<String, EventUse> cachedEventUses) {
        for (EventUse eventUse : cachedEventUses.values()) {
            int mainColor2 = ContextCompat.getColor(this, R.color.main_color2);
            long startMillis = (eventUse.getStartDate())*1000; // Already in milliseconds
            long endMillis = (eventUse.getEndDate())*1000; // Already in milliseconds

            long currentDateInMillis = startMillis;
            while (currentDateInMillis <= endMillis) {
                Event event = new Event(mainColor2, currentDateInMillis, eventUse.getTitle());
                compactCalendar.addEvent(event);

                currentDateInMillis += 24 * 60 * 60 * 1000; // Add one day in milliseconds
            }
        }
    }

    private Map<String, EventUse> getCachedProfileEvents(Map<String, EventUse> cachedFriendsEvents, String profileID) {
        Map<String, EventUse> cachedProfileEvents = new HashMap<>();

        if (cachedFriendsEvents != null && !cachedFriendsEvents.isEmpty()) {
            for (Map.Entry<String, EventUse> entry : cachedFriendsEvents.entrySet()) {
                String eventId = entry.getKey();
                EventUse eventUse = entry.getValue();

                if (eventUse.getOwnerId().equals(profileID)) {
                    cachedProfileEvents.put(eventId, eventUse);
                }
            }
        }

        return cachedProfileEvents;
    }

    private String formatMonth(Date date) {
        // Utilisez un format personnalisé pour obtenir le mois avec une majuscule à la première lettre
        android.icu.text.SimpleDateFormat customFormat = new android.icu.text.SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        return customFormat.format(date);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Assurez-vous de détacher le listener lors de la destruction de l'activité
        if (userListener != null) {
            userListener.remove();
        }
    }

    private void changeStatusBarColor(int color) {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(color);
    }
}