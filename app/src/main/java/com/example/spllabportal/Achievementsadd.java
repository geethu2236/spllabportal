package com.example.spllabportal;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class Achievementsadd extends AppCompatActivity {


    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()); // FIXED
    private DatabaseReference databaseReference;
    private ImageView ivCalendarIcon;

    private EditText etName, etRollNumber, etYear, etDepartment, etEventName, etProjectTitle, etProof;
    private Spinner spinnerEventType, spinnerEventMode, spinnerAchievements;

    private TextView etSpecialLab,tvSelectedDate;
    private Button btnSubmit;

    private String specialLab, name, rollNumber, year, department;

    public Achievementsadd() {
        // Required empty public constructor
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievementsadd);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        ivCalendarIcon = findViewById(R.id.ivCalendarIcon);
        // Find Views
        etName = findViewById(R.id.etName);
        etRollNumber =findViewById(R.id.etRollnumber);
        etYear = findViewById(R.id.etyear);
        etDepartment =findViewById(R.id.etdepartment);
        etSpecialLab = findViewById(R.id.etSpecialLab);
        etProjectTitle = findViewById(R.id.et_project_title);
        etProof =findViewById(R.id.et_proof);
        tvSelectedDate = findViewById(R.id.tv_selected_date);
        etEventName = findViewById(R.id.et_event_name);  // This should be an EditText
        spinnerEventType = findViewById(R.id.spinner_event_type);
        spinnerEventMode = findViewById(R.id.spinner_event_mode);
        spinnerAchievements =findViewById(R.id.spinner_achievements);
        btnSubmit = findViewById(R.id.btn_submit);
        // Set up Spinners
        ArrayAdapter<CharSequence> eventTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.Event_Type, android.R.layout.simple_spinner_item);
        spinnerEventType.setAdapter(eventTypeAdapter);

        ArrayAdapter<CharSequence> eventModeAdapter = ArrayAdapter.createFromResource(this,
                R.array.Event_Mode, android.R.layout.simple_spinner_item);
        spinnerEventMode.setAdapter(eventModeAdapter);

        ArrayAdapter<CharSequence> achievementsAdapter = ArrayAdapter.createFromResource(this,
                R.array.Achievements, android.R.layout.simple_spinner_item);
        spinnerAchievements.setAdapter(achievementsAdapter);

        ivCalendarIcon.setOnClickListener(v -> showDatePicker());

        btnSubmit.setOnClickListener(v -> submitAchievement());

        fetchmethods();


    }

    private void fetchmethods() {

        if(currentUser==null) return;

        String teamLeaderName = currentUser.getDisplayName();


        // Query Projects to find the Special Lab where teamLeaderName == loggedInUserName
        Query query = databaseReference.child("SpecialLab").child("Students").orderByChild("Name").equalTo(teamLeaderName);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        // Fetch the Special Lab for the logged-in user
                        specialLab = snapshot.child("Special Lab").getValue(String.class);
                        String  Name = snapshot.child("Name").getValue(String.class);
                        String RollNumber = snapshot.child("ID").getValue(String.class);
                        String Year =  snapshot.child("Year").getValue(String.class);
                        String Department =  snapshot.child("Department").getValue(String.class);
                        etSpecialLab.setText(specialLab);
                        etName.setText(Name);
                        etRollNumber.setText(RollNumber);
                        etYear.setText(Year);
                        etDepartment.setText(Department);
                        etDepartment.setEnabled(false);
                        etSpecialLab.setEnabled(false);
                        etName.setEnabled(false);
                        etRollNumber.setEnabled(false);
                        etYear.setEnabled(false);
                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Achievementsadd.this, "Error fetching data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePicker() {
        // Get current date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Show DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(Achievementsadd.this,
                (view, year1, month1, dayOfMonth) -> {
                    // Format the selected date
                    String selectedDate = dayOfMonth+ "-"+ (month1 + 1)+"-"+year1;
                    tvSelectedDate.setText(selectedDate);
                    tvSelectedDate.setEnabled(false);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void submitAchievement() {
         name = etName.getText().toString();
         rollNumber = etRollNumber.getText().toString();
         year = etYear.getText().toString();
         department = etDepartment.getText().toString();
        String eventName = etEventName.getText().toString();
        String projectTitle = etProjectTitle.getText().toString();
        String proofLink = etProof.getText().toString();
        String eventDate = tvSelectedDate.getText().toString();
        String eventType = spinnerEventType.getSelectedItem().toString();
        String eventMode = spinnerEventMode.getSelectedItem().toString();
        String achievementType = spinnerAchievements.getSelectedItem().toString();


        if (name.isEmpty() || rollNumber.isEmpty() || eventName.isEmpty() || projectTitle.isEmpty() || proofLink.isEmpty()) {
            Toast.makeText(Achievementsadd.this, "Please fill all the required fields.", Toast.LENGTH_SHORT).show();
            return;
        }


        // Create Achievement object
        Achievement achievement = new Achievement(name, rollNumber, year, department, specialLab, eventName, projectTitle,
                eventDate, eventType, eventMode, achievementType, "Pending", proofLink);


        // Store in Firebase under Achievements/SpecialLab/RollNumber
        databaseReference.child("Achievements").child(specialLab).child(rollNumber).setValue(achievement)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(Achievementsadd.this, "Achievement submitted successfully.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Achievementsadd.this, "Error submitting achievement.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
