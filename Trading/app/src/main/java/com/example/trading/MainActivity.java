package com.example.trading;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.trading.Authentication.LogInScreen;
import com.example.trading.Profile.ProfileActivity;
import com.example.trading.Utils.BottomNavigationViewHelper;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    /*
    -------------------------------------Variables---------------------------------------------------
     */
    private FirebaseAuth mAuth;
    NavigationView nav;
    ActionBarDrawerToggle toggle;
    DrawerLayout drawerLayout;
    private Context mContext;
    private static final int ACTIVITY_NUM = 0;

    /*----------------------------------ON CREATE-----------------------------------------------------

     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: started");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = MainActivity.this;
        mAuth = FirebaseAuth.getInstance();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        nav = findViewById(R.id.navmenu);
        drawerLayout = findViewById(R.id.drawer);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        setupBottomNavigationView();
/*
 -------------------------------Navigation Drawer----------------------------------------------
 */
        nav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_feed:
                        startActivity(new Intent(mContext, CreatePostActivity.class));
                        break;
                    case R.id.menu_profile:
                        startActivity(new Intent(mContext, ProfileActivity.class));
                        break;
                    case R.id.menu_chatroom:
                        startActivity(new Intent(mContext, ChatActivity.class));
                        break;
                    case R.id.menu_course:
                        startActivity(new Intent(mContext, RetreivePostActivity.class));
                        break;
                }
                return true;
            }
        });

        checkCurrentUser(); //checks if the user is logged in or not

    }
/*
-----------------------------------------ON STOP----------------------------------------------------
 */

    /*@Override
    protected void onStop() {
        super.onStop();
        mAuth.signOut();
    }*/

    /*
        -----------------------------------------FireBase-------------------------------------------
         */
    private void checkCurrentUser() {
        // Check if user is signed in (non-null) , if not it goes to the login screen
        FirebaseUser currentUser = mAuth.getCurrentUser();
        Log.d(TAG, "checkCurrentUser: " + currentUser);
        if (currentUser == null) {
            startActivity(new Intent(this, LogInScreen.class));
        }
    }

    /**
     * BottomNavigationView Setup
     */
    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}