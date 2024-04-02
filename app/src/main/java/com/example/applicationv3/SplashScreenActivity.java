package com.example.applicationv3;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.applicationv3.Model.EventUse;
import com.example.applicationv3.Model.Notification;
import com.example.applicationv3.Model.PlaceUse;
import com.example.applicationv3.Model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SplashScreenActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        changeStatusBarColor(getResources().getColor(R.color.main_color3));

        // Utilisez DataManager pour récupérer les informations de l'utilisateur
        DataManager.getInstance().fetchUserProfileInfo(new DataCallback<User>() {
            @Override
            public void onDataReceived(User user) {
                Log.d("SplashScreenActivity", "fetchUserProfileInfo success: " + (user != null));
                if (user != null) {
                    MasterActivity.setCachedUser(user);
                    fetchFriendsAndEvents();


                }else {
                    navigateToMasterActivity();

                }
                DataManager.getInstance().getAuthorizedPlaces(new DataCallback<Map<String, PlaceUse>>() {
                    @Override
                    public void onDataReceived(Map<String, PlaceUse> authorizedPlaces) {
                        MasterActivity.setCachedAuthorizedPlaces(authorizedPlaces);
                        fetchEventsForAuthorizedPlaces(authorizedPlaces);
                    }

                    @Override
                    public void onError(Exception e) {
                        // Gérez les erreurs
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(SplashScreenActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }
        private void fetchFriendsAndEvents() {
            // Dans SplashScreenActivity, après la récupération des autres données
            DataManager.getInstance().fetchUserEvents(new DataCallback<List<EventUse>>() {
                @Override
                public void onDataReceived(List<EventUse> userEventUses) {

                    if (userEventUses != null && !userEventUses.isEmpty()) {
                        for (EventUse eventUse : userEventUses) {
                            MasterActivity.getCachedUserEvents().put(eventUse.getId(), eventUse);
                        }

                    } else {
                        // Gérez le cas où l'utilisateur n'a pas d'événements
                    }

                }

                @Override
                public void onError(Exception e) {
                    // Gérez les erreurs lors du chargement des événements de l'utilisateur
                }
            });

            DataManager.getInstance().fetchUserFriends(new DataCallback<List<String>>() {
                @Override
                public void onDataReceived(List<String> friendIds) {
                    if (friendIds != null && !friendIds.isEmpty()) {
                        final int[] remainingFriends = {friendIds.size()};
                    for (String friendId : friendIds) {
                        DataManager.getInstance().fetchUserProfile(friendId, new DataCallback<User>() {
                            @Override
                            public void onDataReceived(User friendUser) {
                                MasterActivity.getFriendsInfoMap().put(friendId, friendUser);

                            }

                            @Override
                            public void onError(Exception e) {
                                // Gestion des erreurs
                            }
                        });

                        // Récupération des événements des amis
                        DataManager.getInstance().fetchEventsOwnedById(friendId, new DataCallback<List<EventUse>>() {
                            @Override
                            public void onDataReceived(List<EventUse> friendEventUses) {
                                for (EventUse eventUse : friendEventUses) {
                                    MasterActivity.getCachedEvents().put(eventUse.getId(), eventUse);
                                }

                                remainingFriends[0]--; // Décrémentation du nombre d'amis restants
                                if (remainingFriends[0] == 0) {
                                    // Appeler navigateToMasterActivity() uniquement lorsque tous les amis ont été traités
                                    navigateToMasterActivity();
                                }
                            }

                            @Override
                            public void onError(Exception e) {
                                // Gestion des erreurs
                            }
                        });
                    }
                    } else {

                        navigateToMasterActivity();
                    }
                }

                @Override
                public void onError(Exception e) {
                    // Gestion des erreurs
                }
            });
            DataManager.getInstance().fetchUserNotifications(new DataCallback<List<Notification>>() {
                @Override
                public void onDataReceived(List<Notification> notificationList) {
                    for (Notification notification : notificationList) {
                        MasterActivity.getCachedNotification().put(notification.getId(), notification);

                    }
                    Log.d("SplashScreenActivity", "Nombre de notifications: " + MasterActivity.getCachedNotification().size());
                }

                @Override
                public void onError(Exception e) {

                }
            });

        }

    private void fetchEventsForAuthorizedPlaces(Map<String, PlaceUse> authorizedPlaces) {
        // Récupérez les IDs des places autorisées
        List<String> placeIds = new ArrayList<>();
        for (PlaceUse place : authorizedPlaces.values()) {
            placeIds.add(place.getId());
        }
        DataManager.getInstance().fetchEventsForAuthorizedPlaces(placeIds, new DataCallback<List<EventUse>>() {
            @Override
            public void onDataReceived(List<EventUse> eventUses) {
                for (EventUse eventUse : eventUses) {
                    MasterActivity.getCachedPlaceEvents().put(eventUse.getId(), eventUse);
                }
            }

            @Override
            public void onError(Exception e) {
                // Gérez les erreurs ici
            }
        });
    }

    private void navigateToMasterActivity() {
            Intent intent = new Intent(SplashScreenActivity.this, MasterActivity.class);
            startActivity(intent);
            finish();
    }
    private void changeStatusBarColor(int color) {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(color);
    }
}