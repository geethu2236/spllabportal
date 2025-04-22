
package com.example.spllabportal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.play.integrity.internal.ac;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;


public class Achievements extends Fragment {


    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FloatingActionButton fabAddProject;
    private DatabaseReference databaseReference;


    private EditText etName, etRollNumber, etYear, etDepartment, etEventName, etProjectTitle, etProof;
    private Spinner spinnerEventType, spinnerEventMode, spinnerAchievements;
    private RecyclerView recyclerViewAchievements;


    private TextView etSpecialLab, tvSelectedDate;
    private AchievementsAdapter achievementsAdapter;
    private ArrayList<Achievement> achievementsList;


    private String specialLab, RollNumber;

    public Achievements() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_achievements, container, false);
        View rootView = inflater.inflate(R.layout.activity_achievementsadd, container, false);


        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        fabAddProject = view.findViewById(R.id.fabAddProject);

        fabAddProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create an intent to navigate to another page
                Intent intent = new Intent(getActivity(), Achievementsadd.class);  // Use getActivity() instead of Achievements.this
                startActivity(intent);
            }
        });

        // Find Views
        etName = rootView.findViewById(R.id.etName);
        etRollNumber = rootView.findViewById(R.id.etRollnumber);
        etYear = rootView.findViewById(R.id.etyear);
        etDepartment = rootView.findViewById(R.id.etdepartment);
        etSpecialLab = rootView.findViewById(R.id.etSpecialLab);
        etProjectTitle = rootView.findViewById(R.id.et_project_title);
        etProof = rootView.findViewById(R.id.et_proof);
        tvSelectedDate = rootView.findViewById(R.id.tv_selected_date);
        etEventName = rootView.findViewById(R.id.et_event_name);  // This should be an EditText
        spinnerEventType = rootView.findViewById(R.id.spinner_event_type);
        spinnerEventMode = rootView.findViewById(R.id.spinner_event_mode);
        spinnerAchievements = rootView.findViewById(R.id.spinner_achievements);


        recyclerViewAchievements = view.findViewById(R.id.recyclerView_achievements);


        // Set up RecyclerView
        recyclerViewAchievements.setLayoutManager(new LinearLayoutManager(getContext()));
        achievementsList = new ArrayList<>();
        achievementsAdapter = new AchievementsAdapter(achievementsList);
        recyclerViewAchievements.setAdapter(achievementsAdapter);


        // Set up Spinners
        ArrayAdapter<CharSequence> eventTypeAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.Event_Type, android.R.layout.simple_spinner_item);
        eventTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEventType.setAdapter(eventTypeAdapter);


        ArrayAdapter<CharSequence> eventModeAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.Event_Mode, android.R.layout.simple_spinner_item);
        eventModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEventMode.setAdapter(eventModeAdapter);


        ArrayAdapter<CharSequence> achievementsAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.Achievements, android.R.layout.simple_spinner_item);
        achievementsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAchievements.setAdapter(achievementsAdapter);


        String teamLeaderName = currentUser.getDisplayName();


        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference studentsRef = databaseReference.child("SpecialLab").child("Students");

        studentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    boolean isFound = false; // To check if the user is found
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        String name = childSnapshot.child("Name").getValue(String.class);
                        if (name != null && name.equals(teamLeaderName)) {
                            // Fetch the details for the matched user
                            specialLab = childSnapshot.child("Special Lab").getValue(String.class);
                            RollNumber = childSnapshot.child("ID").getValue(String.class);
                            String Year = childSnapshot.child("Year").getValue(String.class);
                            String Department = childSnapshot.child("Department").getValue(String.class);

                            // Set values to TextViews if needed
                            etSpecialLab.setText(specialLab);
                            etRollNumber.setText(RollNumber);
                            etYear.setText(Year);
                            etDepartment.setText(Department);

                            isFound = true; // User found

                            // Fetch achievements ONLY after retrieving user details
                            fetchSubmittedAchievements();
                            break; // Exit the loop once found
                        }
                    }
                    if (!isFound) {
                        Toast.makeText(getContext(), "No matching user found.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "No students found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Error fetching data.", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }


    private void fetchSubmittedAchievements() {
        if (specialLab == null || specialLab.isEmpty()) {
            Toast.makeText(getContext(), "Special Lab is not available.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (RollNumber == null || RollNumber.isEmpty()) {
            Toast.makeText(getContext(), "Roll Number is not available.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Debugging log
        Log.d("AchievementsDebug", "Fetching for SpecialLab: " + specialLab + ", RollNumber: " + RollNumber);

        DatabaseReference achievementsRef = databaseReference.child("Achievements").child(specialLab).child(RollNumber);

        achievementsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                achievementsList.clear();  // Clear to prevent duplicates

                // Debugging log
                Log.d("AchievementsDebug", "DataSnapshot: " + dataSnapshot.toString());

                if (!dataSnapshot.exists()) {
                    Toast.makeText(getContext(), "No achievements found!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Achievement achievement = dataSnapshot.getValue(Achievement.class);
                if (achievement != null) {
                    achievementsList.add(achievement);
                } else {
                    Log.e("AchievementDebug", "Achievement is null for snapshot: " + dataSnapshot.toString());
                }


                achievementsAdapter.notifyDataSetChanged();
                recyclerViewAchievements.scrollToPosition(achievementsList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Error fetching achievements.", Toast.LENGTH_SHORT).show();
            }
        });
    }


}
