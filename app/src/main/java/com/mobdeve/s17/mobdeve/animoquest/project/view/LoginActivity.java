package com.mobdeve.s17.mobdeve.animoquest.project.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Patterns;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.internal.GoogleSignInOptionsExtensionParcelable;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mobdeve.s17.mobdeve.animoquest.project.R;
import com.mobdeve.s17.mobdeve.animoquest.project.model.NotificationHolder;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    Button loginButton;
    Button googleButton;
    TextView createAccount;
    GoogleSignInClient client;
    FirebaseAuth mAuth;
    EditText emailInput;
    EditText passwordInput;
    CheckBox keepMeSignedInCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        googleButton = findViewById(R.id.googleButton);
        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        client = GoogleSignIn.getClient(this,options);

        googleButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // Sign out the client before requesting sign-in to ensure account selection is prompted
                client.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // Now request the sign-in intent
                        Intent i = client.getSignInIntent();
                        startActivityForResult(i, 1234);
                    }
                });
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        loginButton = findViewById(R.id.loginButton);
        createAccount = findViewById(R.id.createAccount);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        keepMeSignedInCheckBox = findViewById(R.id.keepMeSignedInCheckbox);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1234) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);

                // Get profile picture URL, full name, and email
                String profilePictureUrl = account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : null;
                String fullName = account.getDisplayName();
                String userEmail = account.getEmail();

                // Split fullName into firstName and lastName
                String firstName = fullName != null && fullName.contains(" ") ? fullName.split(" ")[0] : fullName;
                String lastName = fullName != null && fullName.contains(" ") ? fullName.split(" ")[1] : "";

                // Authenticate with Firebase
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    if (user != null) {
                                        // Save user data to Firebase Realtime Database
                                        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                                                .getReference("users").child(user.getUid());

                                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    // Data exists; skip saving and proceed to the main activity
                                                    CheckBox keepMeSignedInCheckBox = findViewById(R.id.keepMeSignedInCheckbox);
                                                    if (keepMeSignedInCheckBox != null && keepMeSignedInCheckBox.isChecked()) {
                                                        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
                                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                                        editor.putBoolean("isSignedIn", true);
                                                        editor.apply();
                                                    }

                                                    syncUserNotifications(user.getUid());
                                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    // Data does not exist; save new data
                                                    Map<String, Object> userData = new HashMap<>();
                                                    userData.put("firstName", firstName);
                                                    userData.put("lastName", lastName);
                                                    userData.put("email", userEmail);
                                                    userData.put("profilePictureUrl", profilePictureUrl);

                                                    databaseReference.setValue(userData)
                                                            .addOnSuccessListener(aVoid -> {
                                                                syncUserNotifications(user.getUid());
                                                                // Data saved successfully; proceed to main activity
                                                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                                startActivity(intent);
                                                                finish();
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Toast.makeText(LoginActivity.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                                                            });
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                Toast.makeText(LoginActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                } else {
                                    Toast.makeText(LoginActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void loginFunction(View view) {


        if (!isConnectedToWifi()) {
            Snackbar.make(findViewById(android.R.id.content),
                            "Wi-Fi is required to log in. Please connect to Wi-Fi.",
                            Snackbar.LENGTH_LONG)
                    .setAction("Settings", v -> {
                        // Open Wi-Fi settings when the user taps the action
                        startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
                    })
                    .show();
            return;
        }

        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate inputs
        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Please enter a valid email");
            emailInput.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }

        // Authenticate with Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Save "Keep Me Signed In" state if checkbox is checked
                            if (keepMeSignedInCheckBox.isChecked()) {
                                SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean("isSignedIn", true);
                                editor.apply();
                            }

                            syncUserNotifications(user.getUid());


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

                            // Redirect to MainActivity
                            Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Utility method to check Wi-Fi connectivity
    private boolean isConnectedToWifi() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return networkInfo != null && networkInfo.isConnected();
    }


    public void registerFunction (View v) {
        Intent intent = new Intent(getApplicationContext(), RegistrationActivity.class);
        startActivity(intent);
    }

    public void forgotPasswordFunction (View v) {
        Intent intent = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
        startActivity(intent);
    }

    private void syncUserNotifications(String userId) {
        DatabaseReference notificationsRef = FirebaseDatabase.getInstance().getReference("notifications");
        DatabaseReference userNotificationsRef = FirebaseDatabase.getInstance().getReference("userNotifications").child(userId);

        // Fetch all notifications
        notificationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot notificationsSnapshot) {
                if (notificationsSnapshot.exists()) {
                    userNotificationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot userNotificationsSnapshot) {
                            for (DataSnapshot notification : notificationsSnapshot.getChildren()) {
                                String id = notification.getKey(); // Notification ID
                                String sender = notification.child("sender").getValue(String.class);
                                String subject = notification.child("subject").getValue(String.class);
                                String message = notification.child("message").getValue(String.class);
                                String timestamp = notification.child("timestamp").getValue(String.class);

                                // Check if the notification already exists in userNotifications
                                if (!userNotificationsSnapshot.hasChild(id)) {
                                    // Create a new notification entry for the user
                                    Map<String, Object> newNotification = new HashMap<>();
                                    newNotification.put("id", id);
                                    newNotification.put("sender", sender);
                                    newNotification.put("subject", subject);
                                    newNotification.put("message", message);
                                    newNotification.put("timestamp", timestamp);
                                    newNotification.put("isRead", false); // Default to unread

                                    // Add the notification under the userNotifications node
                                    userNotificationsRef.child(id).setValue(newNotification);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("SyncError", "Failed to fetch user notifications: " + error.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("SyncError", "Failed to fetch notifications: " + error.getMessage());
            }
        });
    }
}