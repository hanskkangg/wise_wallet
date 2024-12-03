package com.example.spendwise;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class EditProfileActivity extends AppCompatActivity {

    private EditText nameEditText, passwordEditText;
    private ImageView profileImageView;
    private Button changePictureButton, saveButton, changePasswordButton;
    private DatabaseReference userRef;
    private FirebaseUser currentUser;
    private StorageReference storageRef;
    private Uri profilePictureUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize Firebase references
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
        storageRef = FirebaseStorage.getInstance().getReference("profile_pictures").child(currentUser.getUid());

        // Initialize UI components
        nameEditText = findViewById(R.id.nameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        profileImageView = findViewById(R.id.profileImageView);
        changePictureButton = findViewById(R.id.changePictureButton);
        saveButton = findViewById(R.id.saveButton);
        changePasswordButton = findViewById(R.id.changePasswordButton);

        // Load existing data
        loadUserProfile();

        // Change picture button
        changePictureButton.setOnClickListener(v -> selectProfilePicture());

        // Save profile changes
        saveButton.setOnClickListener(v -> saveUserProfile());

        // Change password
        changePasswordButton.setOnClickListener(v -> changeUserPassword());
    }

    private void loadUserProfile() {
        userRef.child("name").get().addOnSuccessListener(snapshot -> {
            String name = snapshot.getValue(String.class);
            nameEditText.setText(name);
        });

        storageRef.getDownloadUrl().addOnSuccessListener(uri -> Glide.with(this).load(uri).into(profileImageView));
    }

    private void selectProfilePicture() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            profilePictureUri = data.getData();
            profileImageView.setImageURI(profilePictureUri); // Preview the selected image
        }
    }

    private void saveUserProfile() {
        String name = nameEditText.getText().toString().trim();
        if (name.isEmpty()) {
            nameEditText.setError("Name is required");
            return;
        }
        userRef.child("name").setValue(name).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Profile updated successfully.", Toast.LENGTH_SHORT).show();
            }
        });

        if (profilePictureUri != null) {
            storageRef.putFile(profilePictureUri).addOnSuccessListener(taskSnapshot ->
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> userRef.child("profilePictureUrl").setValue(uri.toString())));
        }
    }

    private void changeUserPassword() {
        String newPassword = passwordEditText.getText().toString().trim();
        if (newPassword.isEmpty() || newPassword.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            return;
        }

        currentUser.updatePassword(newPassword).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Password updated successfully.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to update password.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
