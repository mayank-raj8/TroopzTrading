package com.example.trading.Utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.trading.CreatePostActivity;
import com.example.trading.Profile.ProfileActivity;
import com.example.trading.R;
import com.example.trading.RetreivePostActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

public class BottomNavigationViewHelper {

    public static final String TAG = "BottomNavigationViewHel";

    public static void setupBottomNavigationView(BottomNavigationViewEx bottomNavigationViewEx) {
        Log.d(TAG, "setUpBottomNavigationView : setting up");
        bottomNavigationViewEx.enableAnimation(false);
        bottomNavigationViewEx.enableItemShiftingMode(false);
        bottomNavigationViewEx.enableShiftingMode(false);
        bottomNavigationViewEx.setTextVisibility(false);
    }

    public static void enableNavigation(final Context context, BottomNavigationViewEx view) {
        view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.ic_feed_screen:
                        context.startActivity(new Intent(context, RetreivePostActivity.class));
                        break;
                    case R.id.ic_create_post:
                        context.startActivity(new Intent(context, CreatePostActivity.class));
                        break;
                    case R.id.ic_saved:
                        Toast.makeText(context, "Nothing Yet !", Toast.LENGTH_SHORT).show();
                        break;
                }
                return false;
            }
        });
    }
}
