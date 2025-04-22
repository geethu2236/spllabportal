package com.example.spllabportal;


import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.database.DatabaseReference;


public class RegistrationActivity extends AppCompatActivity {


    private EditText editTextName, editTextID;
    private Spinner spinnerDepartment, spinnerSpecialLab, spinnerYear;
    private TextView textViewYear;
    private Button buttonRegister;
    private DatabaseReference databaseRef;
    private FirebaseAuth mAuth;
    private String userEmail, userID;
    private boolean isStudent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();


        if (user == null) {
            // If no authenticated user, go back to login
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }


        userEmail = user.getEmail();
        userID = user.getUid();


        // Check if user is already registered
        checkUserRegistration();
    }


    private void checkUserRegistration() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }


        String userName = user.getDisplayName(); // Get user's name from Firebase Auth


        if (userName == null || userName.trim().isEmpty()) {
            Toast.makeText(this, "Unable to retrieve user name", Toast.LENGTH_SHORT).show();
            return;
        }
        DatabaseReference specialLabRef = FirebaseDatabase.getInstance().getReference("SpecialLab");


        specialLabRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot labSnapshot : snapshot.getChildren()) {
                    // Loop through each Special Lab
                    DataSnapshot studentsRef = labSnapshot.child("Student");
                    DataSnapshot facultyRef = labSnapshot.child("Faculty");


                    // Check in Students
                    for (DataSnapshot student : studentsRef.getChildren()) {
                        String storedName = student.child("Name").getValue(String.class);


                        if (storedName != null && storedName.equalsIgnoreCase(userName)) {
                            // User is a Student -> Redirect
                            startActivity(new Intent(RegistrationActivity.this, UserModeActivity.class));
                            finish();
                            return;
                        }
                    }


                    // Check in Faculty
                    for (DataSnapshot faculty : facultyRef.getChildren()) {
                        String storedName = faculty.child("Name").getValue(String.class);


                        if (storedName != null && storedName.equalsIgnoreCase(userName)) {
                            // User is a Faculty -> Redirect
                            startActivity(new Intent(RegistrationActivity.this, AdminModeActivity.class));
                            finish();
                            return;
                        }
                    }
                }


                // If user is not found, show Registration Form
                showRegistrationForm();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RegistrationActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }






    private void showRegistrationForm() {
        setContentView(R.layout.activity_registration);


        // Initialize UI components
        editTextName = findViewById(R.id.editTextName);
        editTextID = findViewById(R.id.editTextID);
        spinnerDepartment = findViewById(R.id.spinnerDepartment);
        spinnerSpecialLab = findViewById(R.id.spinnerSpecialLab);
        spinnerYear = findViewById(R.id.spinnerYear);
        textViewYear = findViewById(R.id.textViewYear);
        buttonRegister = findViewById(R.id.buttonRegister);


        databaseRef = FirebaseDatabase.getInstance().getReference("SpecialLab");


        // Auto-fill Name from Firebase Authentication
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            editTextName.setText(user.getDisplayName());
            editTextName.setInputType(InputType.TYPE_NULL);
            editTextName.setFocusable(false);


            if (userEmail != null) {
                validateUserRole(userEmail);
            }
        }


        // Populate Spinners
        setupSpinners();


        buttonRegister.setOnClickListener(v -> registerUser());
    }


    private void setupSpinners() {
        ArrayAdapter<CharSequence> departmentAdapter = ArrayAdapter.createFromResource(
                this, R.array.departments, android.R.layout.simple_spinner_item);
        departmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDepartment.setAdapter(departmentAdapter);


        ArrayAdapter<CharSequence> labAdapter = ArrayAdapter.createFromResource(
                this, R.array.special_labs, android.R.layout.simple_spinner_item);
        labAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpecialLab.setAdapter(labAdapter);


        ArrayAdapter<CharSequence> yearAdapter = ArrayAdapter.createFromResource(
                this, R.array.years, android.R.layout.simple_spinner_item);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);
    }


    private void validateUserRole(String email) {
        if (isStudentEmail(email)) {
            spinnerYear.setVisibility(View.VISIBLE);
            textViewYear.setVisibility(View.VISIBLE);
            isStudent = true;
        } else if (isFacultyEmail(email)) {
            spinnerYear.setVisibility(View.GONE);
            textViewYear.setVisibility(View.GONE);
            isStudent = false;
        } else {
            Toast.makeText(this, "Invalid BITSathy Email!", Toast.LENGTH_LONG).show();
            buttonRegister.setEnabled(false);
        }
    }


    private boolean isStudentEmail(String email) {
        return email.matches("^[a-zA-Z]+\\.[a-zA-Z]{2}\\d{2}@bitsathy\\.ac\\.in$");
    }


    private boolean isFacultyEmail(String email) {
        return email.matches("^[a-zA-Z]+@bitsathy\\.ac\\.in$");
    }


    private void registerUser() {
        String name = editTextName.getText().toString().trim();
        String id = editTextID.getText().toString().trim();
        String department = spinnerDepartment.getSelectedItem().toString();
        String specialLab = spinnerSpecialLab.getSelectedItem().toString();
        String year = isStudent ? spinnerYear.getSelectedItem().toString() : "N/A";
        String role = isStudent ? "Student" : "Faculty";


        if (name.isEmpty() || id.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }


        DatabaseReference userRef = databaseRef.child(isStudent ? "Students" : "Faculty").child(id);
        userRef.child("Name").setValue(name);
        userRef.child("ID").setValue(id);
        userRef.child("Department").setValue(department);
        userRef.child("Special Lab").setValue(specialLab);
        userRef.child("Email").setValue(userEmail);
        userRef.child("Role").setValue(role);


        if (isStudent) {
            userRef.child("Year").setValue(year);
        }


        Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show();


        // Redirect to appropriate mode
        Intent intent = isStudent ? new Intent(this, UserModeActivity.class) :
                new Intent(this, AdminModeActivity.class);
        startActivity(intent);
        finish();
    }
}

