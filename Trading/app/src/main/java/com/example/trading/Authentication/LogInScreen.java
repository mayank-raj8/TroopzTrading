package com.example.trading.Authentication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.example.trading.MainActivity;
import com.example.trading.Models.User;
import com.example.trading.Models.UserProfileData;
import com.example.trading.R;
import com.example.trading.RetreivePostActivity;
import com.firebase.ui.auth.ui.email.SignInActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

public class LogInScreen extends AppCompatActivity implements View.OnClickListener {

    /*
    --------------------------------------Variables-----------------------------------------------
     */
    private static final String TAG = "LogInScreen";
    private final Context mContext = LogInScreen.this;
    private EditText editTextUsername, editTextPassword;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private Button buttonLogin, buttonSignUpScreen;
    private String username, password;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 65;
    private Button buttonSignUpWithGoogle;

    /*
    --------------------------------Activity LifeCycles-------------------------------------------
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in_screen);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        editTextUsername = (EditText) findViewById(R.id.editTextUserName);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        buttonLogin = (Button) findViewById(R.id.buttonLogin);
        buttonSignUpScreen = (Button) findViewById(R.id.buttonSignUpScreen);
        buttonSignUpWithGoogle = (Button) findViewById(R.id.buttonSignUpWithGoogle);

        buttonLogin.setOnClickListener(this);
        buttonSignUpScreen.setOnClickListener(this);
        buttonSignUpWithGoogle.setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(mContext, gso);

        if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().isEmailVerified()) {
            Intent intent = new Intent(mContext, RetreivePostActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            LogInScreen.this.finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /*
        --------------------------Button OnClick-----------------------------------------
         */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonSignUpScreen:              //Will Shift to SignUp Screen
                startActivity(new Intent(mContext, SignUpScreen.class));
                break;
            case R.id.buttonLogin:                      //Will SignIn User
                Log.d(TAG, "onClick: Log In Button Pressed");
                signInUser();
                break;
            case R.id.buttonSignUpWithGoogle:
                Log.d(TAG, "onClick: Sign Up with Google Button Pressed");
                signUpWithGoogle();
                break;
        }
    }

    /*
    -----------------------sign in user through email and password-----------------------------------
     */
    public void signInUser() {
        // Check if the credentials are valid or not and call the LoginUser function
        username = editTextUsername.getText().toString();
        password = editTextPassword.getText().toString();
        if (isNotEmpty(username) && isNotEmpty(password)) {
            LoginUser();
        } else {
            Toast.makeText(mContext, "Enter the fields correctly", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isNotEmpty(String string) {
        return !string.equals("");
    }

    public void LoginUser() {
        //FireBase SignIn user with email and password
        mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(LogInScreen.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        Log.d(TAG, "onComplete: signIn with Email Complete: " + task.isSuccessful());
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            try {
                                if (user.isEmailVerified()) {
                                    Log.d(TAG, "onComplete: success. Email is verified.");
                                    startActivity(new Intent(mContext, RetreivePostActivity.class));
                                    LogInScreen.this.finish();
                                } else {
                                    Toast.makeText(mContext, "Email is not verified \n Check your email inbox.", Toast.LENGTH_SHORT).show();
                                    mAuth.signOut();
                                }
                            } catch (NullPointerException e) {
                                Log.e(TAG, "onComplete: NullPointException" + e.getMessage());
                            }


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(mContext, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });

    }

    /*
    -------------------------sign in through google-----------------------------------
     */
    public void signUpWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (task.isSuccessful()) {
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                    firebaseAuthWithGoogle(account.getIdToken());
                } catch (ApiException e) {
                    // Google Sign In failed, update UI appropriately
                    Log.w(TAG, "Google sign in failed", e);
                    Toast.makeText(mContext, "Google Sign In failed", Toast.LENGTH_SHORT).show();

                }
            } else {
                Toast.makeText(mContext, "Task Not Successful", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            Log.d("TAG", "signInWithCredential:success");
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            UserProfileData users = new UserProfileData();
                            String userID = mAuth.getCurrentUser().getUid();

                            users.setName(firebaseUser.getDisplayName());
                            users.setProfile_photo(firebaseUser.getPhotoUrl().toString());
                            users.setEmail(firebaseUser.getEmail());
                            users.setDescription("");
                            mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbName_user_profile_data))
                                    .child(userID).setValue(users);


                            User user = new User();
                            user.setName(firebaseUser.getDisplayName());
                            user.setUid(userID);
                            user.setEmail(firebaseUser.getEmail());
                            mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbName_Users))
                                    .child(userID)
                                    .setValue(user);
                            Intent intent = new Intent(mContext, RetreivePostActivity.class);
                            intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            LogInScreen.this.finish();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(mContext, "Not able to SignUp User", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}