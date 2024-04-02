package com.example.applicationv3.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.applicationv3.Adapter.Attender2Adapter;
import com.example.applicationv3.DataCallback;
import com.example.applicationv3.DataManager;
import com.example.applicationv3.MasterActivity;
import com.example.applicationv3.Model.EventUse;
import com.example.applicationv3.Model.PlaceUse;
import com.example.applicationv3.Model.User;
import com.example.applicationv3.MyPlaceActivity;
import com.example.applicationv3.PlaceActivity;
import com.example.applicationv3.ProfileActivity;
import com.example.applicationv3.R;
import com.squareup.picasso.Picasso;

import org.osmdroid.config.Configuration;
import org.osmdroid.library.BuildConfig;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class MapFragment extends Fragment implements MapView.OnFirstLayoutListener {
    private MapView mapView;
    ImageView addPlace;
    ImageView center;
    ImageView sync;
    private TextView displayedDate;
    private Marker userMarker;
    private SeekBar seekBar;
    private double userLatitude;
    private double userLongitude;
    private String userImageUrl;
    private List<Marker> allMarkers = new ArrayList<>();
    private Map<String, PlaceUse> cachedAuthorizedPlaces;
    private Map<String, EventUse> cachedEvents;
    private Map<String, EventUse> cachedUserEvents;
    private Map<String, EventUse> cachedPlaceEvents;
    private Map<String,User> friendsInfoMap;
    private Map<String, Marker> displayedPlacesMarkers = new HashMap<>();
    private Map<String, Marker> displayedEventsMarkers = new HashMap<>();
    private FrameLayout drag;
    private LinearLayout goToPage;
    private String placeId;
    private String friendId;
    private User cachedUser;
    private String selectedMarkerType;
    private Attender2Adapter attender2Adapter;

    private Resources resources;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        resources = getResources();
        displayedDate = view.findViewById(R.id.displayed_date);
        mapView = view.findViewById(R.id.map_view);
        addPlace = view.findViewById(R.id.addPlace);
        center = view.findViewById(R.id.center);
        sync = view.findViewById(R.id.sync);
        seekBar = view.findViewById(R.id.seekBar);
        drag = view.findViewById(R.id.drag);
        goToPage = view.findViewById(R.id.goToPage);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        MapController mapController = (MapController) mapView.getController();
        mapController.setZoom(10); // Zoom initial

        addPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ouvrez l'EventActivity lorsque l'utilisateur clique sur addEvent
                startActivity(new Intent(getContext(), PlaceActivity.class));
            }
        });
        center.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userMarker != null) {
                    //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userMarker.getPosition(), 10), 1000, null);
                    mapView.getController().animateTo(userMarker.getPosition());
                    mapView.getController().setZoom(10);
                }
            }
        });
        drag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateInfoWindowVisibility();
            }
        });

        goToPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("Place".equals(selectedMarkerType)) {
                    Intent intent = new Intent(getContext(), MyPlaceActivity.class);
                    intent.putExtra("PLACE_ID", placeId);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getContext(), ProfileActivity.class);
                    intent.putExtra("clickedID", friendId);
                    startActivity(intent);
                }


            }
        });

        // Initialize RecyclerView and AttenderAdapter here
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_attenders);

                attender2Adapter = new Attender2Adapter(new ArrayList<>(), getActivity());
                recyclerView.setAdapter(attender2Adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView.onResume();

        LinearLayout infoWindow = view.findViewById(R.id.info_window);
        //TranslateAnimation animate;
        infoWindow.setVisibility(View.GONE);
        //animate = new TranslateAnimation(0, 0, 0, -infoWindow.getHeight());
        //animate.setDuration(300);
        //animate.setFillAfter(true);

        Calendar calendar = Calendar.getInstance();

        // Obtenez l'année, le mois et le jour actuels
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // L'index des mois commence à 0, donc ajoutez 1
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Formatez la date au format "DD/MM/YYYY"
        String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month, year);
        displayedDate.setText(formattedDate);

        MasterActivity masterActivity = (MasterActivity) getActivity();
        if (masterActivity != null) {
            User cachedUser = masterActivity.getCachedUser();
            if (cachedUser != null) {
                // Récupérer la latitude et la longitude du cachedUser
                userLatitude = cachedUser.getLatitude();
                userLongitude = cachedUser.getLongitude();
                userImageUrl = cachedUser.getImageUrl();


            }
            cachedAuthorizedPlaces = masterActivity.getCachedAuthorizedPlaces();
            cachedEvents = masterActivity.getCachedEvents();
            friendsInfoMap = masterActivity.getFriendsInfoMap();
            cachedPlaceEvents = masterActivity.getCachedPlaceEvents();
            cachedUserEvents = masterActivity.getCachedUserEvents();
            cachedUser = MasterActivity.getCachedUser();
        }

        setSeekBarInitialPosition(); // Appeler la méthode pour initialiser la SeekBar
        setupSeekBarListener();

        displayedDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMonthPicker();
            }
        });


    }


    private void addMarkersForPlaces() {
        if (cachedAuthorizedPlaces != null && mapView != null) {
            String displayedDateSec = convertStringToSec(displayedDate.getText().toString());

            for (Marker marker : displayedPlacesMarkers.values()) {
                mapView.getOverlayManager().remove(marker);
            }
            displayedPlacesMarkers.clear();

            for (Map.Entry<String, PlaceUse> entry : cachedAuthorizedPlaces.entrySet()) {
                PlaceUse place = entry.getValue();

                // Récupérer les dates de début et de fin de la place
                long placeStartDateSec = place.getStartDate();
                long placeEndDateSec = place.getEndDate();

                // Vérifier si displayedDate est comprise entre startDate et endDate
                if (displayedDateSec != null) {
                    long displayedTimestamp = Long.parseLong(displayedDateSec);
                    long placeStartTimestamp = placeStartDateSec;
                    long placeEndTimestamp = placeEndDateSec;

                    if (displayedTimestamp >= placeStartTimestamp && displayedTimestamp <= placeEndTimestamp) {
                        // Récupérer la latitude et la longitude de chaque place
                        double placeLatitude = place.getLatitude();
                        double placeLongitude = place.getLongitude();

                        GeoPoint placeLocation = new GeoPoint(placeLatitude, placeLongitude);

                        ArrayList<String> ownerIds = getOwnerIdsForPlaceEvents(place.getId(), displayedDateSec);
                        int eventCount =ownerIds.size();

                        Marker marker = createPlaceMarker(placeLocation,eventCount, place.getImageUrl());



                        // Récupérer les dates de début et de fin de la place
                        long placeStartSec = place.getStartDate();
                        long placeEndSec = place.getEndDate();

                        // Convertir les timestamps en objets Date
                        String placeStartDate = convertSecToStringDate(placeStartSec);
                        String placeEndDate = convertSecToStringDate(placeEndSec);
                        String dateRange = "du " + placeStartDate + " au " + placeEndDate;

                        // Création d'un Intent pour stocker l'ID et le titre de la place
                        Intent intent = new Intent();
                        intent.putExtra("Place_ID", place.getId());
                        intent.putExtra("Place_Title", place.getTitle());
                        intent.putExtra("Place_Date", dateRange);
                        intent.putExtra("Place_Adress", place.getTitleAdress());
                        intent.putExtra("Place_Description", place.getDescription());
                        intent.putExtra("Place_ImageUrl",place.getImageUrl());
                        intent.putStringArrayListExtra("OwnerIds", ownerIds);

                        marker.setRelatedObject(intent);

                        displayedPlacesMarkers.put(entry.getKey(), marker);

                    }
                }
            }
            //adjustAllMarkerPositions(displayedPlacesMarkers);

        } else {
            Log.d("MapFragment", "cachedAuthorizedPlaces ou googleMap non initialisé");
        }
    }
    private void addMarkersForFriends() {
        Boolean onTheMoove;

        if (friendsInfoMap != null && mapView != null && cachedEvents != null) {
            for (Marker marker : displayedEventsMarkers.values()) {
                mapView.getOverlayManager().remove(marker);
            }
            displayedEventsMarkers.clear();

            for (Map.Entry<String, User> entry : friendsInfoMap.entrySet()) {
                User friend = entry.getValue();

                    double friendLatitude = friend.getLatitude();
                    double friendLongitude = friend.getLongitude();
                    onTheMoove = false;

                    // Rechercher un événement correspondant à cet utilisateur dans cachedEvents
                    EventUse matchingEvent = findMatchingEvent(friend.getId());
                    if (matchingEvent != null) {
                        // Vérifier si l'événement est actif à la displayedDateSec
                        String displayedDateSec = convertStringToSec(displayedDate.getText().toString());
                        long displayedTimestamp = Long.parseLong(displayedDateSec);
                        long eventStartTimestamp = matchingEvent.getStartDate();
                        long eventEndTimestamp = matchingEvent.getEndDate();

                        if (displayedTimestamp >= eventStartTimestamp && displayedTimestamp <= eventEndTimestamp) {
                            // Utiliser les coordonnées de l'événement
                            friendLatitude = matchingEvent.getLatitude();
                            friendLongitude = matchingEvent.getLongitude();
                            onTheMoove = true;
                        }

                    }

                    GeoPoint friendLocation = new GeoPoint(friendLatitude, friendLongitude);

                    // Vérifier si l'utilisateur a une imageUrl définie
                    String imageUrl = friend.getImageUrl();
                    if (imageUrl == null || imageUrl.isEmpty()) {
                        // Utiliser une image par défaut si imageUrl est null ou vide
                        imageUrl = "android.resource://" + getActivity().getPackageName() + "/" + R.drawable.user_bydefault;
                    }

                    // Créer un marqueur pour l'ami
                    Marker friendMarker = createFriendMarker(friendLocation, imageUrl);
                    // Ajouter des informations à l'intent
                    Intent intent = new Intent();
                    intent.putExtra("Friend_Name", friend.getName());
                    intent.putExtra("Friend_Username", friend.getUsername());
                    intent.putExtra("Friend_ImageUrl", friend.getImageUrl());
                    intent.putExtra("Friend_ID", friend.getId());
                    intent.putExtra("Friend_onTheMoove", onTheMoove);

                    // Si l'ami est sur un événement, ajouter les informations de l'événement à l'intent
                    if (matchingEvent != null) {
                        intent.putExtra("Event_StartDate", matchingEvent.getStartDate());
                        intent.putExtra("Event_EndDate", matchingEvent.getEndDate());
                    }

                    // Ajouter le marqueur à la liste des marqueurs affichés
                    friendMarker.setRelatedObject(intent);

                    // Ajouter le marqueur à la liste des marqueurs affichés
                    displayedEventsMarkers.put(entry.getKey(), friendMarker);

            }
            //adjustAllMarkerPositions(displayedEventsMarkers);
        } else {
            Log.d("MapFragment", "friendsInfoMap, cachedEvents, ou mapView non initialisé");
        }
    }

    private EventUse findMatchingEvent(String ownerId) {
        // Recherche d'un événement correspondant à un utilisateur dans cachedEvents
        for (EventUse event : cachedEvents.values()) {
            if (event.getOwnerId().equals(ownerId) && event.getToBeShared()) {
                return event;
            }
        }
        return null;
    }

    // Définir la position initiale de la SeekBar
    private void setSeekBarInitialPosition() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        // Obtiens la date maximale et minimale pour le mois actuel
        int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int monthLength = maxDay - 1; // -1 pour tenir compte du 0-index

        // Règle la portée de la SeekBar pour correspondre au mois actuel
        seekBar.setMax(monthLength);
        seekBar.setProgress(currentDay - 1); // -1 pour tenir compte du 0-index
    }

    // Ajouter un écouteur pour la SeekBar
    private void setupSeekBarListener() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Progress est maintenant l'index du jour sélectionné (commençant à 0)
                int selectedMonth = getMonthFromDisplayedDate();
                int selectedYear = getYearFromDisplayedDate();

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.DAY_OF_MONTH, progress + 1); // Ajoute 1 pour correspondre au jour
                calendar.set(Calendar.MONTH, selectedMonth - 1); // Garde le mois actuel
                calendar.set(Calendar.YEAR, selectedYear); // Garde l'année actuelle

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                String newDate = dateFormat.format(calendar.getTime());
                displayedDate.setText(newDate);


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //FrameLayout infoWindow = getView().findViewById(R.id.info_window);
                //infoWindow.setVisibility(View.GONE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                addMarkers();
                addUserMarker();
            }
        });
    }

    private Marker createPlaceMarker(GeoPoint position,int badgeCount, String imageUrl) {
        Marker marker = new Marker(mapView); // Utilisez la classe Marker d'OSMDroid

        View markerView = LayoutInflater.from(getContext()).inflate(R.layout.marker_place_item, null);
        CircleImageView circleImageView = markerView.findViewById(R.id.imageView);
        TextView badgeTextView = markerView.findViewById(R.id.badgeTextView);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get().load(imageUrl).into(circleImageView, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {
                    // L'image a été chargée avec succès, forcez la mise à jour de la vue
                    marker.setIcon(new BitmapDrawable(getResources(), createBitmapFromView(markerView)));
                    mapView.invalidate(); // Forcez la réaffichage de la carte
                }

                @Override
                public void onError(Exception e) {
                    // Gérez les erreurs si nécessaire
                    Log.e("Picasso", "Error loading image: " + e.getMessage(), e);
                }
            });
        } else {
            circleImageView.setImageResource(R.drawable.place_bydefault);
        }

        badgeTextView.setText(String.valueOf(badgeCount));
        // Définissez la vue personnalisée comme icône du marqueur
        Drawable customMarkerDrawable = new BitmapDrawable(getResources(), createBitmapFromView(markerView));
        marker.setIcon(customMarkerDrawable);
        marker.setPosition(position);
        marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                // Récupérer l'Intent stocké dans le tag du marqueur
                Intent placeIntent = (Intent) marker.getRelatedObject();


                if (placeIntent != null) {
                    // Récupérer les valeurs associées au marqueur
                    placeId = placeIntent.getStringExtra("Place_ID");
                    String title = placeIntent.getStringExtra("Place_Title");
                    String date = placeIntent.getStringExtra("Place_Date");
                    String address = placeIntent.getStringExtra("Place_Adress");
                    String imageUrl = placeIntent.getStringExtra("Place_ImageUrl");
                    String description = placeIntent.getStringExtra("Place_Description");
                    List<String> ownerIds = placeIntent.getStringArrayListExtra("OwnerIds");

                    TextView titleTextView = getView().findViewById(R.id.title);
                    TextView dateTextView = getView().findViewById(R.id.date);
                    TextView addressTextView = getView().findViewById(R.id.adress);
                    TextView locationTextView = getView().findViewById(R.id.location);
                    CircleImageView imageProfile = getView().findViewById(R.id.image_profile);


                    titleTextView.setText(title);
                    dateTextView.setText(date);
                    addressTextView.setText(address);
                    locationTextView.setText("");
                    //numberOfAttenders.setText(description);

                    //TextView numberOfAttenders = getView().findViewById(R.id.numberOfAttenders);
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Picasso.get().load(imageUrl).into(imageProfile);
                    }else{
                        imageProfile.setImageResource(R.drawable.place_bydefault);
                    }



                    if (ownerIds != null && !ownerIds.isEmpty()) {
                        getUsersForOwnerIds(ownerIds, attender2Adapter);
                    } else {
                        ArrayList<User> emptyUserList = new ArrayList<>();
                        attender2Adapter.updateData(emptyUserList);
                        attender2Adapter.notifyDataSetChanged();
                    }
                    selectedMarkerType = "Place";
                    updateInfoWindowVisibility();

                }

                return true;
            }
        });
        mapView.getOverlays().add(marker);
        return marker;
    }

    private Marker createFriendMarker(GeoPoint position, String imageUrl) {
        Marker marker = new Marker(mapView); // Utilisez la classe Marker d'OSMDroid

        View markerView = LayoutInflater.from(getContext()).inflate(R.layout.marker_friend_item, null);
        CircleImageView circleImageView = markerView.findViewById(R.id.imageView);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get().load(imageUrl).into(circleImageView, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {
                    // L'image a été chargée avec succès, forcez la mise à jour de la vue
                    marker.setIcon(new BitmapDrawable(getResources(), createBitmapFromView(markerView)));
                    mapView.invalidate(); // Forcez la réaffichage de la carte
                }

                @Override
                public void onError(Exception e) {
                    // Gérez les erreurs si nécessaire
                    Log.e("Picasso", "Error loading image: " + e.getMessage(), e);
                }
            });
        } else {
            circleImageView.setImageResource(R.drawable.user_bydefault);
        }

        // Définissez la vue personnalisée comme icône du marqueur
        Drawable customMarkerDrawable = new BitmapDrawable(getResources(), createBitmapFromView(markerView));
        marker.setIcon(customMarkerDrawable);
        marker.setPosition(position);
        marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                // Récupérer l'Intent stocké dans le tag du marqueur
                Intent placeIntent = (Intent) marker.getRelatedObject();


                if (placeIntent != null) {
                    // Récupérer les valeurs associées au marqueur
                    friendId = placeIntent.getStringExtra("Friend_ID");
                    String friendName = placeIntent.getStringExtra("Friend_Name");
                    String friendUsername = placeIntent.getStringExtra("Friend_Username");
                    String imageUrl = placeIntent.getStringExtra("Friend_ImageUrl");
                    Boolean onTheMove = placeIntent.getBooleanExtra("Friend_onTheMoove",false);

                    TextView titleTextView = getView().findViewById(R.id.title);
                    TextView adressTextView = getView().findViewById(R.id.adress);
                    TextView usernameTextView = getView().findViewById(R.id.username);
                    TextView locationTextView = getView().findViewById(R.id.location);
                    CircleImageView imageProfile = getView().findViewById(R.id.image_profile);
                    RecyclerView recyclerView = getView().findViewById(R.id.recycler_view_attenders);
                    //TextView numberOfAttenders = getView().findViewById(R.id.numberOfAttenders);
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Picasso.get().load(imageUrl).placeholder(R.drawable.user_bydefault).into(imageProfile);
                    }

                    titleTextView.setText(friendName);
                    usernameTextView.setText(friendUsername);
                    adressTextView.setText("");
                    locationTextView.setText("");
                    //numberOfAttenders.setText(description);

                    if (onTheMove) {
                        locationTextView.setText(resources.getString(R.string.on_the_move));
                    } else {
                        locationTextView.setText(resources.getString(R.string.at_home));
                    }

                    selectedMarkerType = "Friend";
                    updateInfoWindowVisibility();

                }

                return true;
            }
        });
        mapView.getOverlays().add(marker);
        return marker;
    }

    private Marker createUserMarker(GeoPoint position, String imageUrl) {
        Marker marker = new Marker(mapView); // Utilisez la classe Marker d'OSMDroid

        View markerView = LayoutInflater.from(getContext()).inflate(R.layout.marker_user_item, null);
        // Chargez d'autres éléments de vue de votre fichier XML si nécessaire

        // Définissez la vue personnalisée comme icône du marqueur
        Drawable customMarkerDrawable = new BitmapDrawable(getResources(), createBitmapFromView(markerView));
        marker.setIcon(customMarkerDrawable);
        marker.setPosition(position);

        // Désactiver l'affichage de la bulle d'information
        marker.setInfoWindow(null);

        // Ajoutez le marqueur à la carte
        mapView.getOverlays().add(marker);
        return marker;
    }

    private Bitmap createBitmapFromView(View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    @Override
    public void onFirstLayout(View v, int left, int top, int right, int bottom)  {

        addMarkers();
        addUserMarker();

        if (cachedUserEvents != null) {
            for (Map.Entry<String, EventUse> entry : cachedUserEvents.entrySet()) {
                EventUse eventUse = entry.getValue();
                Log.d("MapFragment", "Event in cachedUserEvents - ID: " + eventUse.getId() + ", Name: " + eventUse.getTitle()+ ", Loc: " + eventUse.getTitleAdress());
                // Ajoutez d'autres champs d'événement que vous souhaitez afficher dans le log
            }
        } else {
            Log.d("MapFragment", "cachedUserEvents is null.");
        }
    }

    private void addUserMarker() {
        if (mapView != null) {
            if (userMarker != null) {
                mapView.getOverlays().remove(userMarker); // Supprime le marqueur existant s'il y en a un
            }

            // Vérifiez s'il y a un événement actif pour l'utilisateur à la date affichée
            String displayedDateSec = convertStringToSec(displayedDate.getText().toString());
            EventUse activeEventUse = null;

            if (cachedUserEvents != null) {
                for (Map.Entry<String, EventUse> entry : cachedUserEvents.entrySet()) {
                    EventUse eventUse = entry.getValue();
                    // Récupérez les dates de début et de fin de l'événement
                    long eventStartDateSec = eventUse.getStartDate();
                    long eventEndDateSec = eventUse.getEndDate();
                    if (displayedDateSec != null) {
                        long displayedTimestamp = Long.parseLong(displayedDateSec);
                        long eventStartTimestamp = eventStartDateSec;
                        long eventEndTimestamp = eventEndDateSec;

                        if (displayedTimestamp >= eventStartTimestamp && displayedTimestamp <= eventEndTimestamp) {
                            // Si l'événement est actif à la date affichée, le marquer comme actif
                            activeEventUse = eventUse;
                            break;
                        }
                    }
                }
            }
            // Obtenez les coordonnées pour le marqueur de l'utilisateur
            double markerLatitude;
            double markerLongitude;
            String imageUrl = null;

            if (activeEventUse != null) {
                markerLatitude = activeEventUse.getLatitude();
                markerLongitude = activeEventUse.getLongitude();
            } else {
                markerLatitude = userLatitude;
                markerLongitude = userLongitude;
            }
            // Placez le marqueur à la position déterminée avec une image personnalisée si elle existe
            GeoPoint userLocation = new GeoPoint(markerLatitude, markerLongitude);

            imageUrl = userImageUrl;
            userMarker = createUserMarker(userLocation, imageUrl);

            if (userMarker != null) {
                //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userMarker.getPosition(), 10), 1000, null);
                mapView.getController().animateTo(userMarker.getPosition());
                //mapView.getController().setZoom(10);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        // Vérifie si la carte est prête, puis ajoute le marqueur si nécessaire
        if (mapView != null) {
            addMarkers();
            addUserMarker();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }


    private ArrayList<String> getOwnerIdsForPlaceEvents(String placeId, String displayedDateSec) {
        ArrayList<String> ownerIds = new ArrayList<>();

        for (EventUse eventUse : cachedPlaceEvents.values()) {
            if (eventUse.getPlaceId().equals(placeId)) {
                long eventStartDateSec = eventUse.getStartDate();
                long eventEndDateSec = eventUse.getEndDate();

                if (displayedDateSec != null) {
                    long displayedTimestamp = Long.parseLong(displayedDateSec);
                    long eventStartTimestamp = eventStartDateSec;
                    long eventEndTimestamp = eventEndDateSec;

                    if (displayedTimestamp >= eventStartTimestamp && displayedTimestamp <= eventEndTimestamp) {
                        // Ajouter l'ownerId de cet événement à la liste des ownerIds
                        ownerIds.add(eventUse.getOwnerId());
                    }
                }
            }
        }

        return ownerIds;
    }
    private String convertStringToSec(String dateString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            Date parsedDate = dateFormat.parse(dateString);
            long timestampMillis = parsedDate.getTime();
            long timestampSeconds = timestampMillis / 1000;
            return String.valueOf(timestampSeconds);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
    private String convertSecToStringDate(long sec) {
        long timestampSec = sec * 1000; // Convertir en millisecondes
        Date date = new Date(timestampSec);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        return dateFormat.format(date);
    }


    private void updateInfoWindowVisibility() {
        LinearLayout infoWindow = getView().findViewById(R.id.info_window);
        TranslateAnimation animate;

        if (infoWindow.getVisibility() == View.VISIBLE) {
            // Si la vue est actuellement visible, effectuez l'animation pour la faire disparaître
            animate = new TranslateAnimation(0, 0, 0, -infoWindow.getHeight());
            animate.setDuration(200);
            animate.setFillAfter(true);

            animate.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    // Actions à effectuer au début de l'animation
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    infoWindow.setVisibility(View.GONE);
                    TextView titleTextView = getView().findViewById(R.id.title);
                    TextView dateTextView = getView().findViewById(R.id.date);
                    TextView addressTextView = getView().findViewById(R.id.adress);
                    CircleImageView imageProfile = getView().findViewById(R.id.image_profile);

                    titleTextView.setText("");
                    dateTextView.setText("");
                    addressTextView.setText("");
                    imageProfile.setImageResource(R.drawable.place_bydefault);
                    //recyclerView.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

            });
        } else {
            // Si la vue n'est pas visible, effectuez l'animation pour la faire apparaître
            RecyclerView recyclerView = getView().findViewById(R.id.recycler_view_attenders);
            if ("Place".equals(selectedMarkerType)) {
                recyclerView.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.GONE);
            }

            animate = new TranslateAnimation(0, 0, -infoWindow.getHeight(), 0);
            animate.setDuration(200);
            animate.setFillAfter(true);

            animate.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    infoWindow.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    // Actions à effectuer à la fin de l'animation
                    //infoWindow.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    // Actions à effectuer en cas de répétition de l'animation
                }
            });
        }

        // Appliquez l'animation à la vue
        infoWindow.startAnimation(animate);
    }


    private void getUsersForOwnerIds(List<String> ownerIds, Attender2Adapter attender2Adapter) {
        DataManager.getInstance().fetchUsersForOwnerIds(ownerIds, new DataCallback<List<User>>() {
            @Override
            public void onDataReceived(List<User> users) {
                // Handle the list of users received from the DataManager
                Log.d("MapFragment", "Number of users retrieved (OwnerIds output): " + users.size());

                if (users != null && !users.isEmpty()) {
                    // Mettez à jour les données dans l'adaptateur existant
                    attender2Adapter.updateData(users);
                    attender2Adapter.notifyDataSetChanged();
                } else {
                    Log.d("MapFragment", "No users found for the specified ownerIds.");
                }
            }

            @Override
            public void onError(Exception e) {
                // Handle errors if necessary
                Log.e("MapFragment", "Error fetching users for ownerIds: " + e.getMessage(), e);
            }
        });
    }


    private void showMonthPicker() {
        final NumberPicker monthPicker = new NumberPicker(getContext());
        final NumberPicker yearPicker = new NumberPicker(getContext());

        String[] monthsArray = {
                "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
        };

        // Initialisez les NumberPickers avec la date actuelle
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);

        monthPicker.setDisplayedValues(monthsArray);
        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setValue(currentMonth + 1); // NumberPicker utilise un index 1-based
        monthPicker.setTextColor(ContextCompat.getColor(requireContext(), R.color.main_color3));

        yearPicker.setMinValue(2000);
        yearPicker.setMaxValue(2100);
        yearPicker.setValue(currentYear);
        yearPicker.setTextColor(ContextCompat.getColor(requireContext(), R.color.main_color2));



        // Créez une vue personnalisée pour le dialogue
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER);
        layout.addView(monthPicker);
        layout.addView(yearPicker);


        // Ajoutez des boutons "OK" et "Annuler" et gérez leurs actions
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(layout)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int selectedMonth = monthPicker.getValue();
                        int selectedYear = yearPicker.getValue();

                        // La logique à effectuer une fois la date sélectionnée
                        updateDisplayedDate(selectedMonth, selectedYear);
                        addMarkers();
                        addUserMarker();
                    }
                })
                .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Annuler la sélection
                    }
                });
        // Créez l'AlertDialog avec le thème enveloppé
        AlertDialog alertDialog = builder.create();

        // Affichez l'AlertDialog
        alertDialog.show();

        // Personnalisez la largeur de l'AlertDialog
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(alertDialog.getWindow().getAttributes());
        int dialogWidth = (int) (getContext().getResources().getDisplayMetrics().widthPixels * 0.6); // 80% de la largeur de l'écran
        layoutParams.width = dialogWidth;
        alertDialog.getWindow().setAttributes(layoutParams);
        //builder.create().show();
    }
    private void updateDisplayedDate(int month, int year) {
        // Créez une instance de Calendar et configurez-la avec les valeurs sélectionnées
        Calendar selectedDateCalendar = Calendar.getInstance();
        selectedDateCalendar.set(Calendar.YEAR, year);
        selectedDateCalendar.set(Calendar.MONTH, month - 1); // Soustrayez 1 car Calendar utilise un index 0-based

        Calendar currentDateCalendar = Calendar.getInstance();
        if (selectedDateCalendar.after(currentDateCalendar)) {

            // Réglez la date sur le premier jour du mois sélectionné
            selectedDateCalendar.set(Calendar.DAY_OF_MONTH, 1);

            // Obtenez la date formatée
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String formattedDate = dateFormat.format(selectedDateCalendar.getTime());

            // Mettez à jour la position initiale de la SeekBar en fonction du mois sélectionné
            setSeekBarInitialPosition(selectedDateCalendar);

            // Mettez à jour displayedDate avec la nouvelle date formatée
            displayedDate.setText(formattedDate);
        } else {
            // Affichez un message ou prenez une action appropriée si la date sélectionnée n'est pas ultérieure à la date actuelle
            // Par exemple, vous pouvez afficher un Toast
            Toast.makeText(getContext(), "Regarde l'avenir et non le passé", Toast.LENGTH_SHORT).show();
        }
    }

    private void setSeekBarInitialPosition(Calendar selectedDateCalendar) {
        // Obtenir le jour maximum du mois sélectionné
        int maxDay = selectedDateCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int currentDay = selectedDateCalendar.get(Calendar.DAY_OF_MONTH);
        int monthLength = maxDay - 1; // -1 pour tenir compte du 0-index

        // Règle la portée de la SeekBar pour correspondre au mois sélectionné
        seekBar.setMax(monthLength);
        seekBar.setProgress(currentDay - 1); // -1 pour tenir compte du 0-index
    }

    private GeoPoint calculateOffset(GeoPoint position, int index) {
        double offsetLat = index * 0.01; // Ajustez la valeur selon vos besoins
        double offsetLon = index * 0.01; // Ajustez la valeur selon vos besoins

        return new GeoPoint(position.getLatitude() + offsetLat, position.getLongitude() + offsetLon);
    }

    private void addMarkers() {
        // Ajoutez tous les marqueurs, que ce soit des places, des amis ou des événements
        addMarkersForPlaces();
        addMarkersForFriends();
        // Ajoutez d'autres méthodes pour ajouter des marqueurs d'événements, si nécessaire

        // Ajoutez tous les marqueurs à la liste globale
        allMarkers.addAll(displayedPlacesMarkers.values());
        allMarkers.addAll(displayedEventsMarkers.values());
        // Ajoutez d'autres listes de marqueurs si vous avez d'autres catégories

        // Appel à la méthode pour ajuster les positions de tous les marqueurs
        adjustMarkerPositions(allMarkers);
    }

    private void adjustMarkerPositions(List<Marker> markers) {
        Map<GeoPoint, List<Marker>> groupedMarkers = new HashMap<>();

        // Regroupez les marqueurs par emplacement (GeoPoint)
        for (Marker marker : markers) {
            GeoPoint position = marker.getPosition();

            if (groupedMarkers.containsKey(position)) {
                groupedMarkers.get(position).add(marker);
            } else {
                List<Marker> markerList = new ArrayList<>();
                markerList.add(marker);
                groupedMarkers.put(position, markerList);
            }
        }

        // Ajustez les positions des marqueurs qui se chevauchent
        for (List<Marker> markersAtLocation : groupedMarkers.values()) {
            if (markersAtLocation.size() > 1) {
                for (int i = 0; i < markersAtLocation.size(); i++) {
                    Marker marker = markersAtLocation.get(i);
                    GeoPoint adjustedLocation = calculateOffset(marker.getPosition(), i);
                    marker.setPosition(adjustedLocation);
                }
            }
        }
    }

    private int getMonthFromDisplayedDate() {
        String displayedDateText = displayedDate.getText().toString();

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = dateFormat.parse(displayedDateText);

            // Utiliser un objet Calendar pour extraire le mois
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            return calendar.get(Calendar.MONTH) + 1; // Ajoutez 1 car les mois sont indexés à partir de 0
        } catch (ParseException e) {
            e.printStackTrace();
            return -1; // Gestion d'erreur, retourne -1 si la conversion échoue
        }
    }

    private int getYearFromDisplayedDate() {
        String displayedDateText = displayedDate.getText().toString();

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = dateFormat.parse(displayedDateText);

            // Utiliser un objet Calendar pour extraire l'année
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            return calendar.get(Calendar.YEAR);
        } catch (ParseException e) {
            e.printStackTrace();
            return -1; // Gestion d'erreur, retourne -1 si la conversion échoue
        }
    }

}