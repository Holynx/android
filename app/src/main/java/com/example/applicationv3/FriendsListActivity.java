package com.example.applicationv3;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.applicationv3.Adapter.UserAdapter;
import com.example.applicationv3.Model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class FriendsListActivity extends AppCompatActivity {

    private ImageView close;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);
        close = findViewById(R.id.close);

        // ...

        // Appel pour récupérer la liste des amis de l'utilisateur actuellement connecté
        DataManager.getInstance().fetchUserFriends(new DataCallback<List<String>>() {
            @Override
            public void onDataReceived(List<String> friendIds) {
                // Utilisez la liste d'IDs d'amis pour afficher les amis
                fetchAndDisplayFriends(friendIds);
            }

            @Override
            public void onError(Exception e) {
                // Gérez les erreurs ici
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void fetchAndDisplayFriends(List<String> friendIds) {
        DataManager.getInstance().fetchFriendsInfo(new DataCallback<Map<String, User>>() {
            @Override
            public void onDataReceived(Map<String, User> friendsInfo) {
                // Vous avez maintenant la liste d'amis et leurs informations
                List<User> friends = new ArrayList<>(friendsInfo.values());

                // Utilisez ces amis pour remplir votre RecyclerView
                displayFriends(friends);
            }

            @Override
            public void onError(Exception e) {
                // Gérez les erreurs ici
            }
        });
    }

    private void displayFriends(List<User> friends) {
        // Vous avez maintenant la liste d'amis, affichez-les dans votre RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recycler_view_users);
        UserAdapter adapter = new UserAdapter(this, friends, false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setVisibility(View.VISIBLE);
    }
}