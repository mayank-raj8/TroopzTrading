package com.example.trading.Authentication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.example.trading.Models.User;
import com.example.trading.Models.UserProfileData;
import com.example.trading.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;


public class SignUpScreen extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SignUpScreen";
    private Context mContext = SignUpScreen.this;

    private EditText editTextNewEmail, editTextNewUsername, editTextNewPassword;
    private Button buttonSignUpUser;
    private String email, username, password;
    private TextView textViewAccountExists;
    private String append = "";

    private FirebaseAuth mAuth;
    private FirebaseDatabase mfirebaseDatabase;
    private DatabaseReference myRef;

    ProgressDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_screen);
        Log.d(TAG, "onCreate: started.");

        mAuth = FirebaseAuth.getInstance();
        initWidgets();
        dialog = new ProgressDialog(this);
        dialog.setTitle("Creating Account");
        dialog.setMessage("");
        buttonSignUpUser.setOnClickListener(this);
        textViewAccountExists.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonSignUpUser:
                Log.d(TAG, "onClick: attempting to register the user");
                //dialog.show();
                dialog.show();
                checkCredentials();
                break;

            case R.id.tvAlreadyHaveAccount:
                Log.d(TAG, "onClick: Moving to Sign In Screen");
                /*Intent intent = new Intent(mContext, LogInScreen.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                SignUpScreen.this.finish();*/
                Intent intent = new Intent(mContext, LogInScreen.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP );
                startActivity(intent);
                break;
        }
    }


    /**
     * ---------------------------- Initializing the activity widgets-------------------------------
     */
    private void initWidgets() {
        Log.d(TAG, "initWidgets: Initializing Widgets.");
        editTextNewEmail = (EditText) findViewById(R.id.editTextNewEmail);
        editTextNewUsername = (EditText) findViewById(R.id.editTextNewUserName);
        editTextNewPassword = (EditText) findViewById(R.id.editTextNewPassword);
        buttonSignUpUser = (Button) findViewById(R.id.buttonSignUpUser);
        textViewAccountExists = (TextView) findViewById(R.id.tvAlreadyHaveAccount);
    }
    /*
    ------------------------Checking the credentials for sign up process----------------------------
     */

    private void checkCredentials() {
        Log.d(TAG, "checkCredentials: checking if the input fields are correct or not.");
        email = editTextNewEmail.getText().toString();
        username = editTextNewUsername.getText().toString();
        password = editTextNewPassword.getText().toString();
        if (isEmailValid(email) && isNotNull(username) && isPasswordValid(password)) {
            registerNewEmail(email, password, username);
        } else
            dialog.dismiss();
    }

    public boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        if (!matcher.matches())
            Toast.makeText(SignUpScreen.this, "Invalid Email Address", Toast.LENGTH_SHORT).show();
        return matcher.matches();
    }

    public boolean isPasswordValid(String password) {
        if (password.length() < 6) {
            Toast.makeText(mContext, "Password: min 8 characters", Toast.LENGTH_SHORT).show();
            return false;
        }
        /*final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";
        Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
        Matcher matcher = pattern.matcher(password);
        if (!matcher.matches())
            Toast.makeText(mContext, "Invalid Password", Toast.LENGTH_SHORT).show();
        return matcher.matches();*/
        return true;
    }

    private boolean isNotNull(String string) {
        Log.d(TAG, "isNotNull: checking string if not null.");
        if (string.equals("")) {
            Toast.makeText(mContext, "Invalid Credentials", Toast.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }

    /*
    ---------------------------Registering the user in the authentication firebase----------------------
     */
    private void registerNewEmail(String email, String password, String username) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //send verification email
                            sendVerificationEmail();
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success ");
                            setUpFireBaseAuthDatabase();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(mContext, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }

                    }
                });

    }

    public void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "onComplete: verification email sent.");
                            } else {
                                Toast.makeText(mContext, "Couldn't send Verification Email", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    /*
    -----------------Putting the details in the realtime database firebase--------------------------
    */

    private void setUpFireBaseAuthDatabase() {
        Log.d(TAG, "setUpFireBaseAuth: setting up firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mfirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mfirebaseDatabase.getReference();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            //user is signed in
            Log.d(TAG, "setUpFireBaseAuth: signed_in" + currentUser.getUid());
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //add new user to the database
                    addNewUser(email, username, "", "");
                    Toast.makeText(mContext, "SignUp Successful. Sending verification email.", Toast.LENGTH_SHORT).show();
                    mAuth.signOut();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w(TAG, "onCancelled: ", error.toException());
                }
            });
            Intent intent = new Intent(mContext, LogInScreen.class);
            intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            SignUpScreen.this.finish();
            dialog.dismiss();
        } else {
            //user is signed out
            Log.d(TAG, "setUpFireBaseAuth: signed_out");
        }
    }

    public void addNewUser(String email, String username, String description, String profile_photo) {
        String userID = mAuth.getCurrentUser().getUid();
        Log.d(TAG, "addNewUser: checking the current userID" + userID);
        User user = new User(userID, username, email);
        myRef.child(mContext.getString(R.string.dbName_Users))
                .child(userID)
                .setValue(user);

        UserProfileData settings = new UserProfileData(
                profile_photo,
                username,
                description,
                email
        );
        myRef.child(mContext.getString(R.string.dbName_user_profile_data))
                .child(userID)
                .setValue(settings);
        Log.d(TAG, "addNewUser: added");
    }


}