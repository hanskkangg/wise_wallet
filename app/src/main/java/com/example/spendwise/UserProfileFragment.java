package com.example.spendwise;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class UserProfileFragment extends Fragment {

    private TextView nameTextView, emailTextView;
    private ImageView profileImageView;
    private Button editProfileButton;
    private DatabaseReference userRef;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        // Initialize Firebase references
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());

        // Initialize UI components
        nameTextView = view.findViewById(R.id.nameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        profileImageView = view.findViewById(R.id.profileImageView);
        editProfileButton = view.findViewById(R.id.editProfileButton);

        // Load user data
        loadUserProfile();

        // Navigate to Edit Profile Page
        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void loadUserProfile() {
        if (currentUser != null) {
            emailTextView.setText(currentUser.getEmail());

            userRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue(String.class);
                        String profilePictureUrl = snapshot.child("profilePictureUrl").getValue(String.class);

                        nameTextView.setText(name);
                        if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                            Glide.with(requireContext()).load(profilePictureUrl).into(profileImageView);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Failed to load profile data.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
