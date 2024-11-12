package com.mobdeve.s17.mobdeve.animoquest.project.view;

import android.content.Intent;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.mobdeve.s17.mobdeve.animoquest.project.R;
import com.mobdeve.s17.mobdeve.animoquest.project.model.User;

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

    public void registerFunction (View v) {
        String email = ((EditText) findViewById(R.id.emailAddressInput)).getText().toString();
        String password = ((EditText) findViewById(R.id.passwordInput)).getText().toString();
        String firstName = ((EditText) findViewById(R.id.firstNameInput)).getText().toString();
        String lastName = ((EditText) findViewById(R.id.lastNameInput)).getText().toString();
        String idNumber = ((EditText) findViewById(R.id.idNumberInput)).getText().toString();

        // Register user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Get the registered user
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();

                            // Create a new User object
                            User user = new User(firstName, lastName, idNumber, email);

                            // Save the User object in Realtime Database under the user's UID
                            databaseUsers.child("users").child(firebaseUser.getUid()).setValue(user)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Successfully saved user info
                                            Log.d("RealtimeDB", "User profile created for " + firebaseUser.getUid());
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w("RealtimeDB", "Error adding user info", e);
                                            Toast.makeText(RegistrationActivity.this, "Failed to save user info.",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            // If registration fails, display a message to the user.
                            Log.w("Registration", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegistrationActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}