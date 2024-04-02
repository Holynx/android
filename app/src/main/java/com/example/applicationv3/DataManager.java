package com.example.applicationv3;

import android.util.Log;

import com.example.applicationv3.Model.EventUse;
import com.example.applicationv3.Model.Notification;
import com.example.applicationv3.Model.PlaceUse;
import com.example.applicationv3.Model.RelationshipInfo;
import com.example.applicationv3.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataManager {

    private static DataManager instance;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private List<Notification> userNotifications = new ArrayList<>();
    private String currentUserId;
    private Map<String, PlaceUse> titleToPlaceMap = new HashMap<>();
    private List<String> userFriends = new ArrayList<>();

    private DataManager() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            fetchUserNotifications(new DataCallback<List<Notification>>() {
                @Override
                public void onDataReceived(List<Notification> notificationList) {
                    // Traitement des notifications ici
                }

                @Override
                public void onError(Exception e) {
                    // Gérer les erreurs ici
                }
            });
        }

    }

    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public void fetchUserFriends(DataCallback<List<String>> callback) {
        // Obtenez la liste des amis de l'utilisateur actuellement connecté
        db.collection("relationships")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> friendIds = (List<String>) documentSnapshot.get("friends");
                        if (friendIds != null) {
                            callback.onDataReceived(friendIds);
                        } else {
                            // L'utilisateur n'a pas d'amis
                            callback.onDataReceived(new ArrayList<>());
                        }
                    } else {
                        // Gérez le cas où le document de l'utilisateur actuellement connecté n'existe pas
                        callback.onError(new Exception("Document d'utilisateur introuvable"));
                    }
                })
                .addOnFailureListener(e -> {
                    // Gérez les erreurs ici
                    callback.onError(e);
                });
    }


    public void fetchUserProfileInfo(DataCallback<User> callback) {
        db.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        callback.onDataReceived(user);
                    } else {
                        // Gérez le cas où le document n'existe pas
                        callback.onError(new Exception("Profil non trouvé"));
                    }
                })
                .addOnFailureListener(e -> {
                    // Gérez les erreurs ici
                    callback.onError(e);
                });
    }
    public void fetchUserProfile(String userId, DataCallback<User> callback) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        callback.onDataReceived(user);
                    } else {
                        // Gérez le cas où le document n'existe pas
                        callback.onError(new Exception("Profil non trouvé"));
                    }
                })
                .addOnFailureListener(e -> {
                    // Gérez les erreurs ici
                    callback.onError(e);
                });
    }


    public void fetchUserEvents(DataCallback<List<EventUse>> callback) {
        if (currentUser != null) {
            CollectionReference eventsCollection = db.collection("events");
            Query query = eventsCollection.whereEqualTo("ownerId", currentUserId);

            query.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    List<EventUse> eventUses = new ArrayList<>();

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        EventUse eventUse = document.toObject(EventUse.class);
                        eventUses.add(eventUse);
                    }
                    Log.d("SplashScreenActivity", "Nombre d'événements récupérés : " + eventUses.size());
                    callback.onDataReceived(eventUses);
                } else {
                    callback.onError(task.getException());
                }
            });
        }
    }


    public void fetchRelationshipInfo(DataCallback<RelationshipInfo> callback) {
        if (currentUser != null) {
            DocumentReference relationshipsDocument = db.collection("relationships").document(currentUserId);

            relationshipsDocument.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    RelationshipInfo relationshipInfo = new RelationshipInfo();

                    // Récupérez les listes d'amis, de demandes en attente et de demandes envoyées
                    relationshipInfo.setFriends((List<String>) documentSnapshot.get("friends"));
                    relationshipInfo.setPending((List<String>) documentSnapshot.get("pending"));
                    relationshipInfo.setAsked((List<String>) documentSnapshot.get("asked"));

                    callback.onDataReceived(relationshipInfo);
                } else {
                    // Gérez le cas où le document de l'utilisateur actuellement connecté n'existe pas
                    callback.onError(new Exception("Document d'utilisateur introuvable"));
                }
            }).addOnFailureListener(e -> {
                // Gérez les erreurs ici
                callback.onError(e);
            });
        } else {
            // Gérez le cas où aucun utilisateur n'est connecté
            callback.onError(new Exception("Aucun utilisateur n'est connecté"));
        }
    }

    public void fetchFriendsInfo(DataCallback<Map<String, User>> callback) {
        db.collection("relationships")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> friendIds = (List<String>) documentSnapshot.get("friends");
                        if (friendIds != null && !friendIds.isEmpty()) {
                            CollectionReference usersCollection = db.collection("users");
                            Query query = usersCollection.whereIn(FieldPath.documentId(), friendIds);
                            query.get().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    QuerySnapshot querySnapshot = task.getResult();
                                    Map<String, User> friendsInfo = new HashMap<>();
                                    for (QueryDocumentSnapshot document : querySnapshot) {
                                        User friend = document.toObject(User.class);
                                        friendsInfo.put(document.getId(), friend);
                                    }
                                    callback.onDataReceived(friendsInfo);
                                } else {
                                    callback.onError(task.getException());
                                }
                            });
                        } else {
                            callback.onDataReceived(new HashMap<>());
                        }
                    } else {
                        callback.onError(new Exception("Document de relation introuvable"));
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onError(e);
                });
    }

    public void fetchUserNotifications(DataCallback<List<Notification>> callback) {
        if (currentUser != null) {
            CollectionReference notificationsCollection = db.collection("notifications");
            Query query = notificationsCollection.whereEqualTo("receiverId", currentUserId);

            query.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    List<Notification> notifications = new ArrayList<>();

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        Notification notification = document.toObject(Notification.class);
                        notifications.add(notification);
                    }

                    // Stockez les notifications localement dans userNotifications
                    userNotifications = notifications;
                    callback.onDataReceived(notifications);
                } else {
                    callback.onError(task.getException());
                }
            });
        }
    }

    public List<Notification> getUserNotifications() {
        return userNotifications;
    }

    public void markNotificationsAsRead(List<Notification> notifications, DataCallback<Void> callback) {
        if (currentUser != null && !notifications.isEmpty()) {
            CollectionReference notificationsCollection = db.collection("notifications");
            WriteBatch batch = db.batch();

            for (Notification notification : notifications) {
                if (notification.getIsNew()) {
                    DocumentReference notificationRef = notificationsCollection.document(notification.getId());
                    batch.update(notificationRef, "isNew", false);
                }
            }

            batch.commit()
                    .addOnSuccessListener(aVoid -> {
                        // Mise à jour réussie
                        // Mettez à jour localement la liste userNotifications
                        for (Notification notification : notifications) {
                            notification.setIsNew(false);
                        }
                        callback.onDataReceived(null);
                    })
                    .addOnFailureListener(e -> {
                        // Gérez les erreurs ici
                        callback.onError(e);
                    });
        }
    }

    public void getAuthorizedPlaces(DataCallback<Map<String, PlaceUse>> callback) {
        db.collection("places")
                .whereArrayContains("contactsAllowed", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, PlaceUse> authorizedPlaces = new HashMap<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String title = document.getString("title");
                        String description = document.getString("description");
                        String id = document.getString("id");
                        String titleAdress = document.getString("titleAdress");
                        double latitude = document.getDouble("latitude");
                        double longitude = document.getDouble("longitude");
                        String imageUrl = document.getString("imageUrl");
                        long endDate = document.getLong("endDate");
                        long startDate = document.getLong("startDate");
                        List<String> ownerId = (List<String>) document.get("ownersId");
                        List<String> contactsAllowed = (List<String>) document.get("contactsAllowed");



                        PlaceUse place = new PlaceUse();
                        place.setTitle(title);
                        place.setDescription(description);
                        place.setId(id);
                        place.setTitleAdress(titleAdress);
                        place.setLatitude(latitude);
                        place.setLongitude(longitude);
                        place.setImageUrl(imageUrl);
                        place.setEndDate(endDate);
                        place.setStartDate(startDate);
                        place.setOwnerId(ownerId);
                        place.setContactsAllowed(contactsAllowed);

                        authorizedPlaces.put(id, place);
                    }
                    if (authorizedPlaces.isEmpty()) {
                        // Si la liste est vide, renvoyez une liste vide
                        callback.onDataReceived(new HashMap<>());
                    } else {
                        // Sinon, stockez les places autorisées localement
                        cacheAuthorizedPlaces(authorizedPlaces);
                        // Puis renvoyez les places autorisées
                        callback.onDataReceived(authorizedPlaces);
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onError(e);
                });
    }
    // Ajoutez un attribut pour stocker les places autorisées localement
    private Map<String, PlaceUse> authorizedPlacesCache;
    // Méthode pour mettre en cache les informations des places autorisées
    public void cacheAuthorizedPlaces(Map<String, PlaceUse> authorizedPlaces) {
        authorizedPlacesCache = authorizedPlaces;
    }
    // Méthode pour récupérer les informations des places autorisées en cache
    public Map<String, PlaceUse> getCachedAuthorizedPlaces() {
        return authorizedPlacesCache;
    }

    public void fetchEventsOwnedById(String ownerId, DataCallback<List<EventUse>> callback) {
        CollectionReference eventsCollection = db.collection("events");
        Query query = eventsCollection.whereEqualTo("ownerId", ownerId);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                List<EventUse> ownerEventUses = new ArrayList<>();

                for (QueryDocumentSnapshot document : querySnapshot) {
                    EventUse eventUse = document.toObject(EventUse.class);
                    ownerEventUses.add(eventUse);
                }

                callback.onDataReceived(ownerEventUses);
            } else {
                callback.onError(task.getException());
            }
        });
    }





    public void addFriend(String currentUserId, String friendId) {

    }

    public void addToAsked(String profileId, String currentUserId) {

    }


    public void removeFriend(String currentUserId, String friendId) {

    }
    public void cancelRequest(String currentUserId, String friendId) {

    }

    public void refuseFriend(String currentUserId, String friendId) {

    }


    public void fetchEventsForPlace(String placeId, DataCallback<List<EventUse>> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<EventUse> eventsForPlace = new ArrayList<>();

        db.collection("events")
                .whereEqualTo("placeId", placeId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot eventDocument : queryDocumentSnapshots) {
                        EventUse eventUse = eventDocument.toObject(EventUse.class);
                        eventsForPlace.add(eventUse);
                    }
                    Log.d("DataManager", "Events for place: " + eventsForPlace.size());
                    callback.onDataReceived(eventsForPlace);
                })
                .addOnFailureListener(e -> {
                    Log.e("DataManager", "Failed to fetch events: " + e.getMessage());
                    callback.onError(e);
                });
    }

    public void fetchUsersForOwnerIds(List<String> ownerIds, DataCallback<List<User>> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<User> users = new ArrayList<>();

        db.collection("users")
                .whereIn("id", ownerIds)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot userDocument : queryDocumentSnapshots) {
                        User user = userDocument.toObject(User.class);
                        users.add(user);
                    }
                    callback.onDataReceived(users);
                })
                .addOnFailureListener(e -> {
                    callback.onError(e);
                });
    }
    public void fetchEventsForAuthorizedPlaces(List<String> placeIds, DataCallback<List<EventUse>> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<EventUse> authorizedEventUses = new ArrayList<>();

        if (placeIds.isEmpty()) {
            // Si la liste de placeIds est vide, renvoyez une liste vide directement
            callback.onDataReceived(authorizedEventUses);
        } else {
            CollectionReference eventsRef = db.collection("events");

            Query query = eventsRef.whereIn("placeId", placeIds);
            query.get().addOnSuccessListener(queryDocumentSnapshots -> {
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    EventUse eventUse = document.toObject(EventUse.class);
                    authorizedEventUses.add(eventUse);
                }
                callback.onDataReceived(authorizedEventUses);
            }).addOnFailureListener(callback::onError);
        }
    }

    public void refreshCachedData() {
        fetchUserNotifications(new DataCallback<List<Notification>>() {
            @Override
            public void onDataReceived(List<Notification> notificationList) {
                // Assuming MasterActivity is not null here, you might need to handle this case
                MasterActivity.setCachedNotifications(convertNotificationsToMap(notificationList));
            }

            @Override
            public void onError(Exception e) {
                // Handle errors here
            }
        });
    }
    private Map<String, Notification> convertNotificationsToMap(List<Notification> notificationList) {
        Map<String, Notification> notificationMap = new HashMap<>();
        for (Notification notification : notificationList) {
            notificationMap.put(notification.getId(), notification);
        }
        return notificationMap;
    }

}