package com.example.spendwise;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Patterns;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuthException;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailEditText, passwordEditText;
    private Button signupButton;
    private EditText nameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.signupEmailEditText);
        passwordEditText = findViewById(R.id.signupPasswordEditText);
        signupButton = findViewById(R.id.signupButton);
        TextView loginButton = findViewById(R.id.loginLink);
        nameEditText = findViewById(R.id.nameEditText);
        signupButton.setOnClickListener(view -> registerUser());

        loginButton.setOnClickListener(view -> startActivity(new Intent(SignupActivity.this, LoginActivity.class)));
    }

    private void registerUser() {
        String name = nameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        if (name.isEmpty()) {
            nameEditText.setError("Name is required");
            nameEditText.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            emailEditText.setError("Email is required.");
            emailEditText.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Please provide a valid email.");
            emailEditText.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Password is required.");
            passwordEditText.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordEditText.setError("Password should be at least 6 characters.");
            passwordEditText.requestFocus();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        String userId = user.getUid();
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference usersRef = database.getReference("Users").child(userId);
                        usersRef.child("name").setValue(name).addOnCompleteListener(nameTask -> {
                            if (nameTask.isSuccessful()) {
                                updateUI(user);
                            } else {
                                Toast.makeText(SignupActivity.this, "Failed to save name", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        if (task.getException() != null) {
                            String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                            showErrorMessage(errorCode);
                        }
                    }
                });
    }
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Toast.makeText(SignupActivity.this, "USER REGISTERED", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
        }
    }
    private void showErrorMessage(String errorCode) {
        switch (errorCode) {
            case "ERROR_INVALID_EMAIL":
                Toast.makeText(SignupActivity.this, "The email address is badly formatted.", Toast.LENGTH_LONG).show();
                emailEditText.setError("Invalid email format.");
                emailEditText.requestFocus();
                break;

            case "ERROR_EMAIL_ALREADY_IN_USE":
                Toast.makeText(SignupActivity.this, "The email is already in use by another account.", Toast.LENGTH_LONG).show();
                emailEditText.setError("Email is already in use.");
                emailEditText.requestFocus();
                break;

            case "ERROR_WEAK_PASSWORD":
                Toast.makeText(SignupActivity.this, "The password is too weak.", Toast.LENGTH_LONG).show();
                passwordEditText.setError("Weak password.");
                passwordEditText.requestFocus();
                break;

            case "ERROR_INVALID_PASSWORD":
                Toast.makeText(SignupActivity.this, "The password is invalid. It must be at least 6 characters.", Toast.LENGTH_LONG).show();
                passwordEditText.setError("Password too short.");
                passwordEditText.requestFocus();
                break;

            default:
                Toast.makeText(SignupActivity.this, "Registration failed: " + errorCode, Toast.LENGTH_LONG).show();
                break;
        }
    }
}
