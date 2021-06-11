package com.example.trading.Profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.trading.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

public class EditProfile extends AppCompatActivity {

    private static final String TAG = "EditProfile";
    private Context mContext = EditProfile.this;

    private EditText editProfileName, editProfileDescription;
    private Button buttonApplyChanges;
    private ImageView imageView;
    private Uri imageUri;
    StorageTask uploadTask;
    StorageReference storageReference;
    private String myUrl = "";

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        //get intent from previous activity to set text as the existing name and description
        //editProfileName.setText("");
        //editProfileDescription.setText("");
        imageView = findViewById(R.id.profile_photo);

        storageReference = FirebaseStorage.getInstance().getReference("profile_photo");

        editProfileName = (EditText) findViewById(R.id.editProfileName);
        editProfileDescription = (EditText) findViewById(R.id.editProfileDescription);
        FirebaseUser onileUser = mAuth.getCurrentUser();
        String uid = onileUser.getUid();
        reference = FirebaseDatabase.getInstance().getReference("user_profile_data").child(uid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String currentname = snapshot.child("name").getValue().toString();
                editProfileName.setText(currentname);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String currentdescription = snapshot.child("description").getValue().toString();
                editProfileDescription.setText(currentdescription);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,1);
            }
        });

        buttonApplyChanges = (Button) findViewById(R.id.buttonApplyChanges);

        buttonApplyChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //code for updating in the database and profile as well
                String changedName = editProfileName.getText().toString();
                String changedDescription = editProfileDescription.getText().toString();
                reference.child("name").setValue(changedName);
                reference.child("description").setValue(changedDescription);

                if(imageUri != null)
                {
                    final  StorageReference fileReference;
                    fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
                    uploadTask = fileReference.putFile(imageUri);
                    uploadTask.continueWithTask(new Continuation() {
                        @Override
                        public Object then(@NonNull Task task) throws Exception {
                            if(!task.isComplete()){
                                throw task.getException();
                            }
                            return fileReference.getDownloadUrl();

                        }
                    }).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful()) {
                                Uri downloadUri = (Uri) task.getResult();
                                myUrl = downloadUri.toString();
                                reference.child("profile_photo").setValue(myUrl);
                            }
                        }
                    });
                }
                startActivity(new Intent(mContext, ProfileActivity.class));
                EditProfile.this.finish();
            }
        });
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode== RESULT_OK && data!=null)
        {
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
        }
    }
}