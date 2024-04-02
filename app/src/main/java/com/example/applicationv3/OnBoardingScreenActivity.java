package com.example.applicationv3;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.applicationv3.Adapter.IntroductionPagerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

public class OnBoardingScreenActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    ViewPager viewPager;


    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(),SplashScreenActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_bording_screen);
        mAuth= FirebaseAuth.getInstance();
        ViewPager viewPager = findViewById(R.id.viewPager);

        changeStatusBarColor(getResources().getColor(R.color.main_color3));

        // Initialisez votre adaptateur ViewPager ici
        IntroductionPagerAdapter pagerAdapter = new IntroductionPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        // Associez le DotsIndicator à votre ViewPager
        DotsIndicator dotsIndicator = findViewById(R.id.dotsIndicator);
        dotsIndicator.setViewPager(viewPager);

        // Ajoutez un écouteur pour détecter le changement de page
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Ne rien faire ici
            }

            @Override
            public void onPageSelected(int position) {
                // Si vous atteignez le dernier fragment
                if (position == pagerAdapter.getCount() - 1) {
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // Ne rien faire ici
            }
        });
    }



    private void changeStatusBarColor(int color) {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(color);
    }

}