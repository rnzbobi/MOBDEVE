package com.mobdeve.s17.mobdeve.animoquest.project.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import androidx.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private EditText firstNameInput, lastNameInput, idNumberInput, emailAddressInput, passwordInput;
    private CircleImageView profileImage;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private Button confirmEditButton;
    private static final int PICK_IMAGE_REQUEST = 1;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        idNumberInput = findViewById(R.id.idNumberInput);
        emailAddressInput = findViewById(R.id.emailAddressInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmEditButton = findViewById(R.id.confirmEditButton);
        profileImage = findViewById(R.id.profile_picture);

        mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(uid);

        // Pre-fill fields with existing user data
        loadUserData();
        loadProfilePicture();
    }

    public void onProfilePictureClick(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void loadUserData() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String firstName = snapshot.child("firstName").getValue(String.class);
                    String lastName = snapshot.child("lastName").getValue(String.class);
                    String idNumber = snapshot.child("idNumber").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);

                    firstNameInput.setText(firstName);
                    lastNameInput.setText(lastName);
                    idNumberInput.setText(idNumber);
                    emailAddressInput.setText(email);

                } else {
                    Toast.makeText(EditProfileActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProfileActivity.this, "Failed to load user data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try (InputStream inputStream = getContentResolver().openInputStream(imageUri)) {
                Bitmap selectedImage = BitmapFactory.decodeStream(inputStream);
                profileImage.setImageBitmap(selectedImage);

                // Save the image locally
                saveProfilePictureLocally(selectedImage);

                // Update the profilePictureUrl in Firebase
                String imageUriString = imageUri.toString();
                updateProfilePictureInFirebase(imageUriString);

            } catch (IOException e) {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateProfilePictureInFirebase(String imageUriString) {
        String uid = mAuth.getCurrentUser().getUid();
        if (uid != null) {
            databaseReference.child("profilePictureUrl").setValue(imageUriString)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(EditProfileActivity.this, "Profile picture URL updated in Firebase!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(EditProfileActivity.this, "Failed to update profile picture in Firebase.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveProfilePictureLocally(Bitmap bitmap) {
        String uid = mAuth.getCurrentUser().getUid();
        if (uid != null) {
            File file = new File(getFilesDir(), uid + "_profile_picture.png");
            try (FileOutputStream out = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(this, "Failed to save profile picture locally", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
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
                        Glide.with(EditProfileActivity.this)
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

    public void confirmEditFunction(View view) {
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String idNumber = idNumberInput.getText().toString().trim();
        String email = emailAddressInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(email)) {
            Toast.makeText(this, "All fields except password must be filled.", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.child("firstName").setValue(firstName);
        databaseReference.child("lastName").setValue(lastName);
        databaseReference.child("idNumber").setValue(idNumber);
        databaseReference.child("email").setValue(email);

        if (!TextUtils.isEmpty(password)) {
            // Hash the password before saving
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            databaseReference.child("password").setValue(hashedPassword);
        }

        Toast.makeText(this, "Profile updated successfully.", Toast.LENGTH_SHORT).show();
        finish();
    }
}