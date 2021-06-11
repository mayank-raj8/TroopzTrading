package com.example.trading.Utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.trading.Models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.prefs.PreferenceChangeEvent;

public class FireBaseMethods {
    private static final String TAG = "FireBaseMethods";
    private FirebaseAuth mAuth;
    private Context mContext;

    DatabaseReference reference;

    private String userID;

    public FireBaseMethods(Context context) {
        mContext = context;
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            userID = mAuth.getCurrentUser().getUid();
        }
        reference= FirebaseDatabase.getInstance().getReference().child("Users");
    }

    /*
      Register a new email and password to FireBase Authentication
     */
    public void registerNewEmail(final String email, String password, String username) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");

                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            User u = new User();
                            u.setName(username);
                            u.setEmail(email);
                            reference.child(firebaseUser.getUid()).setValue(u).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        Toast.makeText(mContext,"user registered successfully",Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        Toast.makeText(mContext,"user could not be registred",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }) ;
                            userID = mAuth.getCurrentUser().getUid();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(mContext, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

}
