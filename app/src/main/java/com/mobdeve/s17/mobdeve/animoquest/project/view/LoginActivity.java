package com.mobdeve.s17.mobdeve.animoquest.project.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mobdeve.s17.mobdeve.animoquest.project.R;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    Button loginButton;
    Button googleButton;
    TextView createAccount;
    GoogleSignInClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        googleButton = findViewById(R.id.googleButton);

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

                                        // Create a map to store user data
                                        Map<String, Object> userData = new HashMap<>();
                                        userData.put("firstName", firstName);
                                        userData.put("lastName", lastName);
                                        userData.put("email", userEmail);
                                        userData.put("profilePictureUrl", profilePictureUrl);

                                        databaseReference.setValue(userData)
                                                .addOnSuccessListener(aVoid -> {
                                                    // Data saved successfully; proceed to main activity
                                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(LoginActivity.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
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


    public void loginFunction (View v) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void registerFunction (View v) {
        Intent intent = new Intent(getApplicationContext(), RegistrationActivity.class);
        startActivity(intent);
    }

    public void forgotPasswordFunction (View v) {
        Intent intent = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
        startActivity(intent);
    }
}