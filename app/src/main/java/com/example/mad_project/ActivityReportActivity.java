package com.example.mad_project;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ActivityReportActivity extends AppCompatActivity {
    private TableLayout donatedBloodTable;
    private TableLayout requestedBloodTable;
    private DatabaseReference databaseReference;
    private TextView userNameTextView;
    private FirebaseAuth mAuth;
    private DatabaseReference dbReference;
    private static final String TAG = "ActivityReportActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        userNameTextView = findViewById(R.id.profileName);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        dbReference = FirebaseDatabase.getInstance().getReference("bloodRequests");

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
                        Toast.makeText(ActivityReportActivity.this, "User data not found!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ActivityReportActivity.this, "Failed to fetch user data!", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "User not signed in!", Toast.LENGTH_SHORT).show();
        }

        if (currentUser != null) {
            String userId = currentUser.getUid();

            databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);

            donatedBloodTable = findViewById(R.id.donatedBloodTable);
            requestedBloodTable = findViewById(R.id.requestsTable);

            fetchAndDisplayDonatedBloodCount(userId);
            fetchAndDisplayRequestedBlood(userId);
        } else {
            Log.e(TAG, "Error Encountrered.");
        }
    }

    private void fetchAndDisplayDonatedBloodCount(String userId) {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                donatedBloodTable.removeViews(1, Math.max(0, donatedBloodTable.getChildCount() - 1));

                String donatedBloodCount = String.valueOf(dataSnapshot.child("donatedBloodCount").getValue());

                addDonatedBloodRow(donatedBloodCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to read data from Firebase", databaseError.toException());
            }
        });
    }

    private void addDonatedBloodRow(String donatedBloodCount) {
        TableRow tableRow = new TableRow(this);

        TextView donatedBloodCountView = new TextView(this);
        donatedBloodCountView.setText(donatedBloodCount != null && !donatedBloodCount.equals("null") ? donatedBloodCount : "N/A");
        donatedBloodCountView.setTextColor(Color.BLACK);
        donatedBloodCountView.setPadding(10, 10, 10, 10);

        tableRow.addView(donatedBloodCountView);

        donatedBloodTable.addView(tableRow);
    }

    private void fetchAndDisplayRequestedBlood(String userId) {
        databaseReference.child("requests").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                requestedBloodTable.removeViews(1, Math.max(0, requestedBloodTable.getChildCount() - 1));

                for (DataSnapshot requestSnapshot : dataSnapshot.getChildren()) {
                    String requestedBloodType = requestSnapshot.child("requestedBloodType").getValue(String.class);
                    String requestedBloodCount = String.valueOf(requestSnapshot.child("requestedbloodCount").getValue());

                    addRequestedBloodRow(requestedBloodType, requestedBloodCount);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to read data from Firebase", databaseError.toException());
            }
        });
    }

    private void addRequestedBloodRow(String requestedBloodType, String requestedBloodCount) {
        TableRow tableRow = new TableRow(this);

        TextView requestedBloodTypeView = new TextView(this);
        requestedBloodTypeView.setText(requestedBloodType != null && !requestedBloodType.equals("null") ? requestedBloodType : "N/A");
        requestedBloodTypeView.setTextColor(Color.BLACK);
        requestedBloodTypeView.setPadding(10, 10, 10, 10);

        TextView requestedBloodCountView = new TextView(this);
        requestedBloodCountView.setText(requestedBloodCount != null && !requestedBloodCount.equals("null") ? requestedBloodCount : "N/A");
        requestedBloodCountView.setTextColor(Color.BLACK);
        requestedBloodCountView.setPadding(10, 10, 10, 10);

        tableRow.addView(requestedBloodTypeView);
        tableRow.addView(requestedBloodCountView);

        requestedBloodTable.addView(tableRow);
    }
}