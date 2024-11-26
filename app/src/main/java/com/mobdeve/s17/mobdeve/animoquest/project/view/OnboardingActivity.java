package com.mobdeve.s17.mobdeve.animoquest.project.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mobdeve.s17.mobdeve.animoquest.project.R;

public class OnboardingActivity extends AppCompatActivity {

    Button loginButton;
    TextView createAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check "Keep Me Signed In" state
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        boolean isSignedIn = sharedPreferences.getBoolean("isSignedIn", false);

        if (isSignedIn) {

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {

                if (!isConnectedToWifi()) {
                    Snackbar.make(findViewById(android.R.id.content),
                                    "Wi-Fi is required to proceed. Please connect to Wi-Fi.",
                                    Snackbar.LENGTH_LONG)
                            .setAction("Settings", v -> {
                                // Open Wi-Fi settings when the user taps the action
                                startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
                            })
                            .show();
                    return; // Prevent navigation to MainActivity
                }
                // Redirect to MainActivity if the user is still authenticated
                Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Close OnboardingActivity
                return;
            } else {
                // Clear the "isSignedIn" flag if the Firebase session expired
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isSignedIn", false);
                editor.apply();
            }
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboarding);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loginButton = findViewById(R.id.loginButton);
        createAccount = findViewById(R.id.createAccount);
    }

    public void loginFunction(View v) {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }

    public void registerFunction (View v) {
        Intent intent = new Intent(getApplicationContext(), RegistrationActivity.class);
        startActivity(intent);
    }

    // Utility method to check Wi-Fi connectivity
    private boolean isConnectedToWifi() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return networkInfo != null && networkInfo.isConnected();
    }


}