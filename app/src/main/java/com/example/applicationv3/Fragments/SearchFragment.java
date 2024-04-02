package com.example.applicationv3.Fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.applicationv3.Adapter.UserAdapter;
import com.example.applicationv3.Model.User;
import com.example.applicationv3.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hendraanggrian.appcompat.socialview.widget.SocialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<User> mUsers;
    private List<User> allUsers; // Nouvelle liste pour stocker tous les utilisateurs
    private UserAdapter userAdapter;
    private SocialAutoCompleteTextView search_bar;
    private boolean isFirstLoad = true;
    private CollectionReference usersCollection;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_users);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mUsers = new ArrayList<>();
        allUsers = new ArrayList<>(); // Initialisation de la nouvelle liste
        userAdapter = new UserAdapter(getContext(), mUsers, true);
        recyclerView.setAdapter(userAdapter);

        search_bar = view.findViewById(R.id.search_bar);

        // Rendez le RecyclerView visible dès la création de la vue
        recyclerView.setVisibility(View.VISIBLE);
// Trouvez votre RecyclerView
        RecyclerView recyclerViewUsers = view.findViewById(R.id.recycler_view_users);



        usersCollection = FirebaseFirestore.getInstance().collection("users");

        // Ajoutez un TextWatcher à la barre de recherche
        search_bar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (isFirstLoad && charSequence.length() >= 1) {
                    isFirstLoad = false;
                    readUsers();
                } else if (charSequence.length() < 1) {
                    mUsers.clear();
                    userAdapter.notifyDataSetChanged();
                } else {
                    searchUser(charSequence.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


        return view;
    }


    private void readUsers() {
        usersCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                allUsers = task.getResult().toObjects(User.class);
                if (TextUtils.isEmpty(search_bar.getText().toString())) {
                    mUsers.clear();
                    mUsers.addAll(allUsers);
                    userAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void searchUser(String s) {
        mUsers.clear();
        if (s.length() >= 1) {
            for (User user : allUsers) {
                if (user.getUsername().toLowerCase().startsWith(s.toLowerCase())) {
                    mUsers.add(user);
                }
            }
            userAdapter.notifyDataSetChanged();
        }
    }
}
