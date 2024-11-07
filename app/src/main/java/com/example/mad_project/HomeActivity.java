package com.example.mad_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity {

    private ImageButton registerButton, raiseRequestButton, findHospitalButton, activityReportButton;
    private TextView userNameTextView;
    private FirebaseAuth mAuth;
    private DatabaseReference dbReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        registerButton = findViewById(R.id.registerButton);
        raiseRequestButton = findViewById(R.id.raiseRequestButton);
        findHospitalButton = findViewById(R.id.findHospitalButton);
        activityReportButton = findViewById(R.id.activityReportButton);
        userNameTextView = findViewById(R.id.profileName);

        if (currentUser != null) {
            String userId = currentUser.getUid();
            dbReference = FirebaseDatabase.getInstance().getReference("users").child(userId);

            dbReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue(String.class);
                        userNameTextView.setText("Welcome, " + name);
                    } else {
                        Toast.makeText(HomeActivity.this, "User data not found!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(HomeActivity.this, "Failed to fetch user data!", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Error Encountered!", Toast.LENGTH_SHORT).show();
        }

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, RegisterDonerActivity.class);
                startActivity(intent);
            }
        });

        raiseRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, RaiseRequestActivity.class);
                startActivity(intent);
            }
        });

        findHospitalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, FindHospitalActivity.class);
                startActivity(intent);
            }
        });

        activityReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, ActivityReportActivity.class);
                startActivity(intent);
            }
        });
    }
}
