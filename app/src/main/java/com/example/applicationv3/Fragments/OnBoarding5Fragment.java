package com.example.applicationv3.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.applicationv3.CreateAccountActivity;
import com.example.applicationv3.MainActivity;
import com.example.applicationv3.R;


public class OnBoarding5Fragment extends Fragment {

    Button connexion;
    Button creatAccount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_on_boarding5, container, false);

        connexion = view.findViewById(R.id.connexion);
        creatAccount = view.findViewById(R.id.createAccount);

        connexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        creatAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CreateAccountActivity.class);
                startActivity(intent);
            }
        });


        return view;
    }
}