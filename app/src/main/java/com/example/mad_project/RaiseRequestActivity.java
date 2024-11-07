package com.example.mad_project;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RaiseRequestActivity extends AppCompatActivity {

    private Spinner bloodTypeSpinner;
    private NumberPicker bloodAmountPicker;
    private Button submitRequestButton;
    private TextView userNameTextView;
    private FirebaseAuth mAuth;
    private DatabaseReference dbReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raise_request);

        bloodAmountPicker = findViewById(R.id.bloodAmountPicker);
        bloodAmountPicker.setMinValue(1);
        bloodAmountPicker.setMaxValue(10);
        bloodAmountPicker.setWrapSelectorWheel(true);
        changeNumberPickerTextColor(bloodAmountPicker, Color.RED);

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
                        Toast.makeText(RaiseRequestActivity.this, "User data not found!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(RaiseRequestActivity.this, "Failed to fetch user data!", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Error Encountered!", Toast.LENGTH_SHORT).show();
        }

        bloodTypeSpinner = findViewById(R.id.bloodTypeSpinner);
        bloodAmountPicker = findViewById(R.id.bloodAmountPicker);
        submitRequestButton = findViewById(R.id.submitRequestButton);
        userNameTextView = findViewById(R.id.profileName);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.blood_types, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        bloodTypeSpinner.setAdapter(adapter);

        submitRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSubmitRequest();
            }
        });
    }

    private void changeNumberPickerTextColor(NumberPicker numberPicker, int color) {
        try {
            Field pickerField = NumberPicker.class.getDeclaredField("mInputText");
            pickerField.setAccessible(true);
            EditText inputText = (EditText) pickerField.get(numberPicker);
            inputText.setTextColor(Color.RED);

            for (int i = 0; i < numberPicker.getChildCount(); i++) {
                View child = numberPicker.getChildAt(i);
                if (child instanceof EditText) {
                    ((EditText) child).setTextColor(Color.RED);
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    private void handleSubmitRequest() {
        String requestedBloodType = bloodTypeSpinner.getSelectedItem().toString();
        int requestedBloodCount = bloodAmountPicker.getValue();

        if (requestedBloodType.isEmpty()) {
            Toast.makeText(this, "Please select a blood type.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();

            HashMap<String, Object> requestData = new HashMap<>();
            requestData.put("requestedBloodType", requestedBloodType);
            requestData.put("requestedbloodCount", requestedBloodCount);

            dbReference.child("requests").push().setValue(requestData)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(RaiseRequestActivity.this, "Request submitted successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RaiseRequestActivity.this, "Failed to submit request.", Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }
}
