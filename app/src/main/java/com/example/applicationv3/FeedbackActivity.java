package com.example.applicationv3;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class FeedbackActivity extends AppCompatActivity {

    EditText message;
    ImageView screenshot;
    Button feedbackBtn;
    private ImageView changePhoto;
    private Uri imageUri;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        message = findViewById(R.id.feedback);
        screenshot = findViewById(R.id.screenshot);
        feedbackBtn = findViewById(R.id.feedbackBtn);
        changePhoto = findViewById(R.id.change_photo);
        storageRef = FirebaseStorage.getInstance().getReference().child("uploads");



        changePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ajoutez le code pour permettre à l'utilisateur de sélectionner ou de prendre une nouvelle photo
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        feedbackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = message.getText().toString();

                // Vérifiez si une image a été sélectionnée
                if (imageUri != null) {
                    // Enregistrez l'image dans Firebase Storage
                    StorageReference imageRef = storageRef.child(imageUri.getLastPathSegment());
                    imageRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // L'image a été téléchargée avec succès
                            // Récupérez l'URL de téléchargement de l'image
                            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri downloadUri) {
                                    // L'URL de téléchargement de l'image est disponible ici
                                    String imageUrl = downloadUri.toString();

                                    // Enregistrez le message et l'URL de l'image dans Firestore
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    Map<String, Object> feedback = new HashMap<>();
                                    feedback.put("feedback", messageText);
                                    feedback.put("imageUrl", imageUrl);

                                    db.collection("feedbacks")
                                            .add(feedback)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    // Gestion du succès
                                                    finish();
                                                }
                                            });
                                }
                            });
                        }
                    });
                } else {
                    // Si aucune image n'a été sélectionnée, enregistrez seulement le message
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    Map<String, Object> feedback = new HashMap<>();
                    feedback.put("feedback", messageText);
                    feedback.put("imageUrl", "");

                    db.collection("feedbacks")
                            .add(feedback)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    // Gestion du succès
                                    finish();
                                }
                            });
                }
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            screenshot.setImageURI(imageUri);
        }
    }
}