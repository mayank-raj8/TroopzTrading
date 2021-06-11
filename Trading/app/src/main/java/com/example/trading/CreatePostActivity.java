package com.example.trading;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.trading.Profile.ProfileActivity;
import com.example.trading.Utils.BottomNavigationViewHelper;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class CreatePostActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private static final String TAG = "CreatePostActivity";
    private EditText postBox;
    private ImageView imageView;
    private Button cancelBtn, postBtn;
    private Context mContext = CreatePostActivity.this;
    private static final int ACTIVITY_NUM = 1;


    private String askedByname = "";
    private DatabaseReference askedByRef;
    private ProgressDialog loader;
    private String myUrl = "";
    StorageTask uploadTask;
    StorageReference storageReference;
    private Uri imageUri;
    NavigationView nav;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;

    private FirebaseAuth mauth;
    private FirebaseUser mUser;
    private String onlineUserId = "";

    DatabaseReference ref2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        mauth = FirebaseAuth.getInstance();
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

        postBox = findViewById(R.id.post_text);
        imageView = findViewById(R.id.post_image);
        cancelBtn = findViewById(R.id.cancel);
        postBtn = findViewById(R.id.post);

        loader = new ProgressDialog(this);
        mauth = FirebaseAuth.getInstance();
        mUser = mauth.getCurrentUser();
        onlineUserId = mUser.getUid();

        ref2 = FirebaseDatabase.getInstance().getReference("Users").child(onlineUserId).child("post");

        askedByRef = FirebaseDatabase.getInstance().getReference("users").child(onlineUserId);
        askedByRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                askedByname = snapshot.child("name").getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        storageReference = FirebaseStorage.getInstance().getReference("posts");

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performValidations();
            }
        });

    }

    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    String getPostText() {
        return postBox.getText().toString().trim();
    }

    String mdate = DateFormat.getDateInstance().format(new Date());
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("posts");

    private void performValidations() {
        if (getPostText().isEmpty()) {
            postBox.setError("post required!");
        } else if (imageUri == null) {
            uploadPostWithnoImage();
        } else {
            uploadPostWithImage();
        }
    }

    private void startLoader() {
        loader.setMessage("posting");
        loader.setCanceledOnTouchOutside(false);
        loader.show();
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadPostWithnoImage() {
        startLoader();
        String postid = ref.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("postid", postid);
        hashMap.put("posttext", getPostText());
        hashMap.put("publisher", onlineUserId);
        hashMap.put("postby", askedByname);
        hashMap.put("date", mdate);

        ref2.child(postid).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(CreatePostActivity.this, "posted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CreatePostActivity.this, "could not upload image" + task.getException().toString(), Toast.LENGTH_SHORT).show();
                }

            }
        });

        ref.child(postid).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(CreatePostActivity.this, "posted successfully", Toast.LENGTH_SHORT).show();
                    loader.dismiss();
                } else {
                    Toast.makeText(CreatePostActivity.this, "could not upload image" + task.getException().toString(), Toast.LENGTH_SHORT).show();
                    loader.dismiss();
                }

            }
        });
    }

    private void uploadPostWithImage() {
        startLoader();
        final StorageReference fileReference;
        fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
        uploadTask = fileReference.putFile(imageUri);
        uploadTask.continueWithTask(new Continuation() {
            @Override
            public Object then(@NonNull Task task) throws Exception {
                if (!task.isComplete()) {
                    throw task.getException();
                }
                return fileReference.getDownloadUrl();

            }
        }).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = (Uri) task.getResult();
                    myUrl = downloadUri.toString();
                    String postid = ref.push().getKey();

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("postid", postid);
                    hashMap.put("posttext", getPostText());
                    hashMap.put("publisher", onlineUserId);
                    hashMap.put("postby", askedByname);
                    hashMap.put("postimage", myUrl);
                    hashMap.put("date", mdate);

                    ref2.child(postid).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(CreatePostActivity.this, "posted successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(CreatePostActivity.this, "could not upload image" + task.getException().toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    ref.child(postid).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(CreatePostActivity.this, "posted successfully", Toast.LENGTH_SHORT).show();
                                loader.dismiss();
                            } else {
                                Toast.makeText(CreatePostActivity.this, "could not upload image" + task.getException().toString(), Toast.LENGTH_SHORT).show();
                                loader.dismiss();
                            }
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreatePostActivity.this, "failed to upload", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
        }
    }

}