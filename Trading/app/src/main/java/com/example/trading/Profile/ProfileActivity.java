package com.example.trading.Profile;

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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.trading.Adapter.PostAdapter;
import com.example.trading.Authentication.LogInScreen;
import com.example.trading.ChatActivity;
import com.example.trading.CourseActivity;
import com.example.trading.MainActivity;
import com.example.trading.Models.Post;
import com.example.trading.Models.UserProfileData;
import com.example.trading.R;
import com.example.trading.RetreivePostActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.List;


public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private Context mContext = ProfileActivity.this;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mRef;
    private TextView mDisplayName, mUserName, mDescription, mEmail;
    private ImageView imageView;

    Button buttonSignOut, buttonEditProfile;
    GoogleSignInClient mGoogleSignInClient;

    NavigationView nav;
    ActionBarDrawerToggle toggle;
    DrawerLayout drawerLayout;

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;


    private FirebaseUser mUser;
    private String onlineUserId = "";
    DatabaseReference referenceim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        nav = findViewById(R.id.navmenu);
        drawerLayout = findViewById(R.id.drawer);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        Log.d(TAG, "onCreate: started.");
        mAuth = FirebaseAuth.getInstance();
        initWidgets();


        setUpFirebaseAuth();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mUser = mAuth.getCurrentUser();
        onlineUserId = mUser.getUid();
        imageView = findViewById(R.id.profile_image);

        referenceim = FirebaseDatabase.getInstance().getReference("user_profile_data").child(onlineUserId);
        referenceim.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String profileImage = snapshot.child("profile_photo").getValue().toString();
                Glide.with(mContext).load(profileImage).into(imageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        recyclerView = findViewById(R.id.recyclerView1);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(ProfileActivity.this, postList);
        recyclerView.setAdapter(postAdapter);

        readPosts();


        nav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_feed:
                        startActivity(new Intent(getApplicationContext(), RetreivePostActivity.class));
                        break;
                    case R.id.menu_profile:
                        break;
                    case R.id.menu_chatroom:
                        startActivity(new Intent(getApplicationContext(), ChatActivity.class));
                        break;
                    case R.id.menu_course:
                        startActivity(new Intent(getApplicationContext(), CourseActivity.class));
                        break;
                }
                return true;
            }
        });

        buttonSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGoogleSignInClient.signOut()
                        .addOnCompleteListener(ProfileActivity.this, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Intent intent = new Intent(ProfileActivity.this, LogInScreen.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                ProfileActivity.this.finish();
                                mAuth.signOut();
                            }
                        });
            }
        });

        buttonEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, EditProfile.class));
            }
        });
    }

    private void setUpFirebaseAuth() {
        Log.d(TAG, "setUpFirebaseAuth: setting up firebase auth.");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mRef = mFirebaseDatabase.getReference();
        FirebaseUser user = mAuth.getCurrentUser();

        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //retrieve user data information from the database
                UserProfileData userProfileData = getUserProfileData(snapshot);
                setProfileFragments(userProfileData);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private UserProfileData getUserProfileData(DataSnapshot dataSnapshot) {
        Log.d(TAG, "userAccountSettings: retrieving user account profile data from firebase.");

        UserProfileData userProfileData = new UserProfileData();
        if (mAuth.getCurrentUser() == null)
            Log.d(TAG, "getUserSettings: current user is null");
        String userID = mAuth.getCurrentUser().getUid();
        Log.d(TAG, "getUserSettings: current user ID" + userID);

        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            //user_profile_data node
            if (ds.getKey().equals(mContext.getString(R.string.dbName_user_profile_data))) {
                Log.d(TAG, "userAccountSettings: datsnapshot " + ds);

                try {
                    userProfileData.setName(
                            ds.child(userID)
                                    .getValue(UserProfileData.class)
                                    .getName()
                    );
                    userProfileData.setDescription(
                            ds.child(userID)
                                    .getValue(UserProfileData.class)
                                    .getDescription()
                    );
                    userProfileData.setEmail(
                            ds.child(userID)
                                    .getValue(UserProfileData.class)
                                    .getEmail()
                    );

                } catch (NullPointerException e) {
                    Log.d(TAG, "userAccountSettings: NULL point exception " + e.getMessage());
                }

            }


        }
        return userProfileData;

    }

    private void setProfileFragments(UserProfileData userProfileData) {
        Log.d(TAG, "setProfileFragments: setting widgets with data retrieving from firebase database " + userProfileData.toString());
        mDisplayName.setText(userProfileData.getName());
        mDescription.setText(userProfileData.getDescription());

    }

    public void initWidgets() {
        mDisplayName = (TextView) findViewById(R.id.profileUserName);
        mDescription = (TextView) findViewById(R.id.profileDescription);
        buttonSignOut = (Button) findViewById(R.id.buttonSignOut);
        buttonEditProfile = (Button) findViewById(R.id.buttonEditProfile);
    }

    private void readPosts() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(onlineUserId).child("post");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    postList.add(post);
                }

                postAdapter.notifyDataSetChanged();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


}