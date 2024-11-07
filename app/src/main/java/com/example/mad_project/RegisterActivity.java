package com.example.mad_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameField, phoneField, addressField, emailField, passwordField, rePasswordField;
    private RadioGroup genderGroup;
    private Spinner bloodTypeSpinner;
    private Button registerButton;

    private FirebaseAuth mAuth;
    private DatabaseReference dbReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        dbReference = FirebaseDatabase.getInstance().getReference("users");

        nameField = findViewById(R.id.entername_reg1);
        phoneField = findViewById(R.id.editTextPhone);
        addressField = findViewById(R.id.editTextAddress);
        emailField = findViewById(R.id.editTextEmail);
        passwordField = findViewById(R.id.editTextPassword);
        rePasswordField = findViewById(R.id.editTextRePassword);
        genderGroup = findViewById(R.id.radioGroup);
        bloodTypeSpinner = findViewById(R.id.dropdownBloodType);
        registerButton = findViewById(R.id.buttonRegister);

        String[] bloodTypes = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, bloodTypes);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        bloodTypeSpinner.setAdapter(adapter);


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String name = nameField.getText().toString().trim();
        String phone = phoneField.getText().toString().trim();
        String address = addressField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String rePassword = rePasswordField.getText().toString().trim();
        String bloodType = bloodTypeSpinner.getSelectedItem().toString();

        int selectedGenderId = genderGroup.getCheckedRadioButtonId();
        RadioButton selectedGenderButton = findViewById(selectedGenderId);
        String gender = selectedGenderButton != null ? selectedGenderButton.getText().toString() : "";

        if (TextUtils.isEmpty(name)) {
            nameField.setError("Name is required.");
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            phoneField.setError("Phone is required.");
            return;
        }
        if (TextUtils.isEmpty(address)) {
            addressField.setError("Address is required.");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            emailField.setError("Email is required.");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordField.setError("Password is required.");
            return;
        }
        if (!password.equals(rePassword)) {
            rePasswordField.setError("Passwords do not match.");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserDetails(user.getUid(), name, gender, phone, address, email, bloodType);
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserDetails(String userId, String name, String gender, String phone, String address, String email, String bloodType) {
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("gender", gender);
        userMap.put("phone", phone);
        userMap.put("address", address);
        userMap.put("email", email);
        userMap.put("bloodType", bloodType);

        dbReference.child(userId).setValue(userMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Failed to save user details: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
