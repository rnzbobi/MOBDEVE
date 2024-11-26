package com.mobdeve.s17.mobdeve.animoquest.project.view;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.mobdeve.s17.mobdeve.animoquest.project.R;
import com.mobdeve.s17.mobdeve.animoquest.project.model.User;

import org.mindrot.jbcrypt.BCrypt;

public class RegistrationActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference databaseUsers;
    TextView loginRedirect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loginRedirect = findViewById(R.id.loginRedirect);
        mAuth = FirebaseAuth.getInstance();
        databaseUsers = FirebaseDatabase.getInstance().getReference("users");
    }

    public void loginFunction(View v) {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void registerFunction(View view) {

        // Check Wi-Fi connectivity
        if (!isConnectedToWifi()) {
            Snackbar.make(findViewById(android.R.id.content),
                            "Wi-Fi is required to register. Please connect to Wi-Fi.",
                            Snackbar.LENGTH_LONG)
                    .setAction("Settings", v -> {
                        // Open Wi-Fi settings when the user taps the action
                        startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
                    })
                    .show();
            return;
        }

        String firstName = ((EditText) findViewById(R.id.firstNameInput)).getText().toString();
        String lastName = ((EditText) findViewById(R.id.lastNameInput)).getText().toString();
        String idNumber = ((EditText) findViewById(R.id.idNumberInput)).getText().toString();
        String email = ((EditText) findViewById(R.id.emailAddressInput)).getText().toString();
        String password = ((EditText) findViewById(R.id.passwordInput)).getText().toString();

        // Regex for ID number validation
        String idNumberPattern = "^(\\d{3})\\d{5}$";

        // Regex for DLSU email validation
        String dlsuEmailPattern = "^[a-zA-Z0-9._%+-]+@dlsu\\.edu\\.ph$";

        // Validate ID number
        if (!idNumber.matches(idNumberPattern)) {
            Toast.makeText(this, "Invalid ID number.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate DLSU email
        if (!email.matches(dlsuEmailPattern)) {
            Toast.makeText(this, "Please use a valid DLSU email (e.g., firstname_lastname@dlsu.edu.ph).", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hash the password using BCrypt
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // Create Firebase Auth account with email and password
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (firebaseUser != null) {
                            // Create User object with hashed password
                            User user = new User(firstName, lastName, idNumber, email, hashedPassword);

                            // Save User object to Firebase Realtime Database
                            databaseUsers.child(firebaseUser.getUid()).setValue(user)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(RegistrationActivity.this, "Registration successful. Please log in.", Toast.LENGTH_SHORT).show();
                                        // Redirect to LoginActivity
                                        Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(RegistrationActivity.this, "Failed to save user data", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        Toast.makeText(RegistrationActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Utility method to check Wi-Fi connectivity
    private boolean isConnectedToWifi() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return networkInfo != null && networkInfo.isConnected();
    }

}