package com.mobdeve.s17.mobdeve.animoquest.project.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mobdeve.s17.mobdeve.animoquest.project.R;

import java.io.File;

public class ProfileActivity extends AppCompatActivity {

    TextView fullNameText, emailText;
    ImageView profileImage;
    String profilePictureUrl;
    DatabaseReference databaseReference;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fullNameText = findViewById(R.id.fullNameText);
        emailText = findViewById(R.id.emailText);
        profileImage = findViewById(R.id.profile_image);

        profilePictureUrl = getIntent().getStringExtra("profilePictureUrl");

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        if(currentUser != null) {
            loadUserProfile();
            loadProfilePicture();
        } else {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show();
        }

        // Set the indoor_icon to green
        ImageView profileIcon = findViewById(R.id.profile_icon);
        profileIcon.setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_IN);

        // Set up click listener for indoor_icon
        ImageView indoorIcon = findViewById(R.id.indoor_icon);
        indoorIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to IndoorNavigationActivity
                Intent intent = new Intent(ProfileActivity.this, IndoorNavigationActivity.class);
                startActivity(intent);
            }
        });

        // Set up click listener for elevator_icon
        ImageView elevatorIcon = findViewById(R.id.elevator_icon);
        elevatorIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to ElevatorActivity
                Intent intent = new Intent(ProfileActivity.this, ElevatorActivity.class);
                startActivity(intent);
            }
        });

        // Set up click listener for elevator_icon
        ImageView mapIcon = findViewById(R.id.map_icon);
        mapIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to ElevatorActivity
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Set up click listener for elevator_icon
        ImageView notificationIcon = findViewById(R.id.notification_icon);
        notificationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to ElevatorActivity
                Intent intent = new Intent(ProfileActivity.this, NotificationActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Start the LoginActivity when back button is pressed
        super.onBackPressed();
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Optional: Call finish to remove this activity from the stack
    }

    public void editProfileFunction(View v) {
        Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
        startActivity(intent);
    }

    public void changePasswordFunction(View v) {
        Intent intent = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
        startActivity(intent);
    }

    public void signOutFunction(View v) {
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(ProfileActivity.this, OnboardingActivity.class);
        startActivity(intent);
        finish();
    }

    private void loadUserProfile() {
        String uid = currentUser.getUid();

        // Retrieve user data from Firebase Realtime Database
        databaseReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Retrieve user details
                    String firstName = snapshot.child("firstName").getValue(String.class);
                    String lastName = snapshot.child("lastName").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String profilePictureUrl = snapshot.child("profilePictureUrl").getValue(String.class);

                    // Update UI with the user's full name and email
                    fullNameText.setText((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : ""));
                    emailText.setText(email != null ? email : "");

                } else {
                    Log.d("ProfileActivity", "User data not found in database");
                    Toast.makeText(ProfileActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ProfileActivity", "Database error: " + error.getMessage());
                Toast.makeText(ProfileActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProfilePicture() {
        String uid = currentUser.getUid();
        File file = new File(getFilesDir(), uid + "_profile_picture.png");

        if (file.exists()) {
            // Load the cached local image
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            profileImage.setImageBitmap(bitmap);
        } else {
            // If no local image exists, fall back to the Firebase URL
            databaseReference.child(uid).child("profilePictureUrl").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String profilePictureUrl = snapshot.getValue(String.class);
                    if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                        Glide.with(ProfileActivity.this)
                                .load(profilePictureUrl)
                                .placeholder(R.drawable.profile_placeholder)
                                .error(R.drawable.profile_placeholder)
                                .into(profileImage);
                    } else {
                        // Default placeholder if no URL exists
                        profileImage.setImageResource(R.drawable.profile_placeholder);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("ProfileActivity", "Failed to fetch profile picture URL: " + error.getMessage());
                }
            });
        }
    }
}