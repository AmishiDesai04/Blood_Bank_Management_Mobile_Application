package com.example.mad_project;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegisterDonerActivity extends AppCompatActivity {

    private TextView nameTextView, genderTextView, bloodTypeTextView, phoneTextView, emailTextView, userNameTextView;
    private CheckBox checkAlcohol, checkTattoo, checkSmoke, checkMedication;
    private Button registerButton;

    private FirebaseAuth mAuth;
    private DatabaseReference dbReference;
    private FirebaseUser currentUser;

    private static final String CHANNEL_ID = "donation_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_doner);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        userNameTextView = findViewById(R.id.profileName);

        if (currentUser != null) {
            String userId = currentUser.getUid();
            dbReference = FirebaseDatabase.getInstance().getReference("users").child(userId);

            dbReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue(String.class);
                        userNameTextView.setText(name);
                    } else {
                        Toast.makeText(RegisterDonerActivity.this, "User data not found!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(RegisterDonerActivity.this, "Failed to fetch user data!", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Error Encountered!", Toast.LENGTH_SHORT).show();
        }

        nameTextView = findViewById(R.id.displayName);
        genderTextView = findViewById(R.id.displayGender);
        bloodTypeTextView = findViewById(R.id.displayBloodType);
        phoneTextView = findViewById(R.id.displayPhoneNumber);
        emailTextView = findViewById(R.id.displayEmail);
        checkAlcohol = findViewById(R.id.checkAlcohol);
        checkTattoo = findViewById(R.id.checkTattoo);
        checkSmoke = findViewById(R.id.checkSmoke);
        checkMedication = findViewById(R.id.checkMedication);
        registerButton = findViewById(R.id.registerButton);

        dbReference = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());

        fetchUserDetails();

        registerButton.setOnClickListener(v -> handleRegistration());

        createNotificationChannel();

    }

    private void fetchUserDetails() {
        dbReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String gender = snapshot.child("gender").getValue(String.class);
                    String bloodType = snapshot.child("bloodType").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);

                    nameTextView.setText(name);
                    genderTextView.setText(gender);
                    bloodTypeTextView.setText(bloodType);
                    phoneTextView.setText(phone);
                    emailTextView.setText(email);
                } else {
                    Toast.makeText(RegisterDonerActivity.this, "User data not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RegisterDonerActivity.this, "Failed to fetch user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleRegistration() {
        boolean isEligible = checkAlcohol.isChecked() && checkTattoo.isChecked() && checkSmoke.isChecked() && checkMedication.isChecked();

        if (isEligible) {
            incrementDonatedBloodCount();
        } else {
            Toast.makeText(this, "You are not eligible to donate blood.", Toast.LENGTH_SHORT).show();
        }
    }

    private void incrementDonatedBloodCount() {
        dbReference.child("donatedBloodCount").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int donatedBloodCount = 0;
                if (snapshot.exists()) {
                    donatedBloodCount = snapshot.getValue(Integer.class);
                }
                dbReference.child("donatedBloodCount").setValue(donatedBloodCount + 1).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        sendSuccessNotification();
                    } else {
                        Toast.makeText(RegisterDonerActivity.this, "Failed to update donation count.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RegisterDonerActivity.this, "Failed to fetch donation count.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendSuccessNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("DONORLINK Blood Donation Registration")
                .setContentText("You have successfully registered for blood donation.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            return;
        }
        notificationManager.notify(1, builder.build());

        Toast.makeText(this, "Registration successful! Notification sent.", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(RegisterDonerActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendSuccessNotification();
            } else {
                Toast.makeText(this, "Notification permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Donation Channel";
            String description = "Channel for donation registration notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
