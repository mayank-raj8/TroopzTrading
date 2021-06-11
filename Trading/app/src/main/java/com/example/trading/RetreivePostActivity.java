package com.example.trading;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.trading.Adapter.PostAdapter;
import com.example.trading.Authentication.LogInScreen;
import com.example.trading.Models.Post;
import com.example.trading.Profile.ProfileActivity;
import com.example.trading.Utils.BottomNavigationViewHelper;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.List;

public class RetreivePostActivity extends AppCompatActivity {

    private static final String TAG = "RetreivePostActivity";
    private Context mContext = RetreivePostActivity.this;
    private RecyclerView recyclerView;
    private ProgressBar progress_circular;

    private PostAdapter postAdapter;
    private List<Post> postList;

    private static final int ACTIVITY_NUM = 0;

    private FirebaseAuth mAuth;
    NavigationView nav;
    ActionBarDrawerToggle toggle;
    DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retreive_post);

        progress_circular = findViewById(R.id.progress_circular);
        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(RetreivePostActivity.this, postList);
        recyclerView.setAdapter(postAdapter);

        readPosts();

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
                        break;
                    case R.id.menu_profile:
                        startActivity(new Intent(mContext, ProfileActivity.class));
                        break;
                    case R.id.menu_chatroom:
                        startActivity(new Intent(mContext, ChatActivity.class));
                        break;
                    case R.id.menu_course:
                        startActivity(new Intent(mContext, CourseActivity.class));
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

    private void readPosts() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    postList.add(post);
                }

                postAdapter.notifyDataSetChanged();
                progress_circular.setVisibility(View.GONE);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RetreivePostActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}