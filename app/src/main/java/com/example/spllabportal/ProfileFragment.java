package com.example.spllabportal;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    private TextView studentName, studentDepartment, studentYear, studentSpecialLab;
    private TextView completedProjectsCount, ongoingProjectsCount;
    private TextView paperPresentationCount, hackathonCount, startupCount, projectPresentationCount, patentPresentationCount, ConferencePresentationCount;
    private FirebaseAuth mAuth;
    private DatabaseReference studentsRef, projectsRef, achievementsRef;

    private Button logoutButton;
    private String loggedInUserName, specialLabName = ""; // Store Special Lab globally

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Firebase Initialization
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        loggedInUserName = (user != null) ? user.getDisplayName() : "";

        // Initialize UI Elements
        studentName = view.findViewById(R.id.studentName);
        studentDepartment = view.findViewById(R.id.studentDepartment);
        studentYear = view.findViewById(R.id.studentYear);
        studentSpecialLab = view.findViewById(R.id.studentSpecialLab);
        completedProjectsCount = view.findViewById(R.id.completedProjectsCount);
        ongoingProjectsCount = view.findViewById(R.id.ongoingProjectsCount);
        paperPresentationCount = view.findViewById(R.id.paperPresentationCount);
        projectPresentationCount = view.findViewById(R.id.projectPresentationCount);
        patentPresentationCount = view.findViewById(R.id.patent);
        ConferencePresentationCount = view.findViewById(R.id.Conference);
        hackathonCount = view.findViewById(R.id.hackathon);
        startupCount = view.findViewById(R.id.startupevent);
        logoutButton = view.findViewById(R.id.logout);

        // Logout Button Logic
        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(getContext(), "Logged out successfully!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish(); // Close the current activity
        });

        // Fetch Student Details from "SpecialLab/Students"
        studentsRef = FirebaseDatabase.getInstance().getReference("SpecialLab").child("Students");
        studentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot student : snapshot.getChildren()) {
                    String name = student.child("Name").getValue(String.class);
                    if (name != null && name.equalsIgnoreCase(loggedInUserName)) {
                        studentName.setText(name);
                        studentDepartment.setText(student.child("Department").getValue(String.class));
                        studentYear.setText(student.child("Year").getValue(String.class));
                        specialLabName = student.child("Special Lab").getValue(String.class); // Store Special Lab
                        studentSpecialLab.setText(specialLabName);

                        // Once Special Lab is retrieved, fetch achievements
                        fetchAchievements();
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error fetching student details", Toast.LENGTH_SHORT).show();
            }
        });

        // Fetch Projects Data from "Projects"
        projectsRef = FirebaseDatabase.getInstance().getReference("Projects");
        projectsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int completedCount = 0, ongoingCount = 0;

                for (DataSnapshot project : snapshot.getChildren()) {
                    String teamLeader = project.child("teamLeaderName").getValue(String.class);
                    String githubLink = project.child("GitHubLink").getValue(String.class);
                    String demoVideo = project.child("Demo Video").getValue(String.class);

                    if (teamLeader != null && teamLeader.equalsIgnoreCase(loggedInUserName)) {
                        if (githubLink != null && !githubLink.isEmpty() && demoVideo != null && !demoVideo.isEmpty()) {
                            completedCount++;
                        } else {
                            ongoingCount++;
                        }
                    }
                }
                completedProjectsCount.setText(String.valueOf(completedCount));
                ongoingProjectsCount.setText(String.valueOf(ongoingCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error fetching projects", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void fetchAchievements() {
        if (specialLabName.isEmpty()) {
            return; // Prevent unnecessary database calls
        }

        achievementsRef = FirebaseDatabase.getInstance().getReference("Achievements").child(specialLabName);
        achievementsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int paperCount = 0, hackathonCountVal = 0, startupCountVal = 0, projectCountVal = 0, ConferenceCountVal = 0, PatentCountVal = 0;

                for (DataSnapshot achievement : snapshot.getChildren()) {
                    String name = achievement.child("name").getValue(String.class);
                    String eventType = achievement.child("eventType").getValue(String.class);

                    if (name != null && name.equalsIgnoreCase(loggedInUserName)) {
                        switch (eventType) {
                            case "Paper Presentation":
                                paperCount++;
                                break;
                            case "Hackathon":
                                hackathonCountVal++;
                                break;
                            case "Startup_events":
                                startupCountVal++;
                                break;
                            case "Project Presentation":
                                projectCountVal++;
                                break;
                            case "Conference":
                                ConferenceCountVal++;
                                break;
                            case "Patent-Published":
                            case "Patent-Grant":
                                PatentCountVal++;
                                break;
                        }
                    }
                }

                paperPresentationCount.setText(String.valueOf(paperCount));
                hackathonCount.setText(String.valueOf(hackathonCountVal));
                startupCount.setText(String.valueOf(startupCountVal));
                projectPresentationCount.setText(String.valueOf(projectCountVal));
                ConferencePresentationCount.setText(String.valueOf(ConferenceCountVal));
                patentPresentationCount.setText(String.valueOf(PatentCountVal));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error fetching achievements", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
