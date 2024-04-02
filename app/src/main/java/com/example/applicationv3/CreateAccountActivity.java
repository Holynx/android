package com.example.applicationv3;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateAccountActivity extends AppCompatActivity {

    TextView alreadyHasAccountBtn;
    TextView errorCreateAccountTextView;
    Button createAccountBtn;
    EditText editTextEmail, editTextPassword, editTextName, editTextUsername, editTextlocation;
    private String email, password,name, username, uid, location;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    double latitudeExt;
    double longitudeExt;


    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(),MasterActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        Places.initialize(getApplicationContext(), getString(R.string.GOOGLE_MAPS_API_KEY));
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        changeStatusBarColor(getResources().getColor(R.color.main_color3));

        mAuth= FirebaseAuth.getInstance();
        alreadyHasAccountBtn = findViewById(R.id.alreadyHasAccountBtn);
        createAccountBtn = findViewById(R.id.createAccountBtn);
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextName = findViewById(R.id.name);
        editTextUsername = findViewById(R.id.username);
        editTextlocation = findViewById(R.id.location);

        errorCreateAccountTextView = findViewById(R.id.errorCreateAccountTextView);
        progressBar = findViewById(R.id.progressBar);
        // Initialize the Places API
        Places.initialize(getApplicationContext(), getString(R.string.GOOGLE_MAPS_API_KEY));



        // Make the location EditText non-focusable
        editTextlocation.setFocusable(false);

        editTextlocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the Places Autocomplete activity
                List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(CreateAccountActivity.this);
                startActivityForResult(intent, 101);
            }
        });

        alreadyHasAccountBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        createAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                progressBar.setVisibility(View.VISIBLE);
                email = String.valueOf(editTextEmail.getText());
                password = String.valueOf(editTextPassword.getText());
                name= String.valueOf(editTextName.getText());
                username = String.valueOf(editTextUsername.getText());
                location = String.valueOf(editTextlocation.getText());

                if(TextUtils.isEmpty(email)){
                    Toast.makeText(CreateAccountActivity.this,"Enter email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    Toast.makeText(CreateAccountActivity.this,"Enter password", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    Toast.makeText(CreateAccountActivity.this, "Account created.",
                                            Toast.LENGTH_SHORT).show();

                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user!=null){
                                        uid = user.getUid();

                                        Map <String, Object> userData = new HashMap<>();
                                        userData.put("name",name);
                                        userData.put("email",email);
                                        userData.put("username",username);
                                        userData.put("id",uid);
                                        userData.put("num","");
                                        userData.put("defaultAdress",location);
                                        userData.put("latitude",latitudeExt);
                                        userData.put("longitude",longitudeExt);
                                        userData.put("imageUrl","default");
                                        userData.put("bio","");


                                        db.collection("users")
                                                .document(uid)
                                                .set(userData);
                                    }

                                    // Maintenant, nous allons créer un document dans la collection "relationships"
                                    String relationshipDocumentId = uid; // Utilisez l'ID de l'utilisateur comme nom de document

                                    Map<String, Object> initialData = new HashMap<>();
                                    List<String> emptyList = new ArrayList<>(); // Créez une liste vide

                                    initialData.put("friends", emptyList); // Champ "friends" initialisé avec une liste vide
                                    initialData.put("pending", emptyList); // Champ "pending" initialisé avec une liste vide
                                    initialData.put("asked", emptyList); // Champ "asked" initialisé avec une liste vide

                                            db.collection("relationships")
                                            .document(relationshipDocumentId)
                                            .set(initialData);


                                    // Redirigez l'utilisateur vers MainActivity
                                    Intent intent = new Intent(getApplicationContext(), SplashScreenActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(CreateAccountActivity.this, task.getException().getLocalizedMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            editTextlocation.setText(place.getAddress());
            latitudeExt = place.getLatLng().latitude;
            longitudeExt = place.getLatLng().longitude;
            editTextlocation.setEnabled(true);
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(this, status.getStatusMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void changeStatusBarColor(int color) {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(color);
    }
}