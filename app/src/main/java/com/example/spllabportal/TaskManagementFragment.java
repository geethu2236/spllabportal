package com.example.spllabportal;
import static android.app.Activity.RESULT_OK;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskManagementFragment extends Fragment {

    private EditText etStartDate;
    private ImageView ivCalendarIcon;

    private FirebaseAuth mAuth;
    private String guideName, duration, startDate;
    private DatabaseReference mDatabase;
    private LinearLayout weekLogContainer;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()); // FIXED
    private String loggedInUserName;

    private EditText githubLink;
    private EditText demoVideoLink;
    private TextView tvSaveGitHubLink, tvSaveDemoVideoLink;

    // Holds the logged-in user's name


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_management, container, false);
        TextView tvProjectDuration = view.findViewById(R.id.tvProjectDuration);

        etStartDate = view.findViewById(R.id.etStartDate);
        ivCalendarIcon = view.findViewById(R.id.ivCalendarIcon);
        weekLogContainer = view.findViewById(R.id.weekLogContainer);
        githubLink = view.findViewById(R.id.etGitHubLink);
        demoVideoLink = view.findViewById(R.id.DemoVideoLink);
        tvSaveGitHubLink = view.findViewById(R.id.tvSaveGitHubLink);
        tvSaveDemoVideoLink = view.findViewById(R.id.tvSaveDemoVideo);

// Set click listeners
        tvSaveGitHubLink.setOnClickListener(v -> storeGitHubLink());
        tvSaveDemoVideoLink.setOnClickListener(v -> storeDemoVideoLink());

        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Projects");

        // Retrieve the logged-in user's name
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            loggedInUserName = user.getDisplayName();
            fetchProjectDetails(tvProjectDuration);// Get the user's display name
        }

        // Set up DatePickerDialog on calendar icon click
        ivCalendarIcon.setOnClickListener(v -> showDatePickerDialog());


        return view;
    }

    private void storeGitHubLink() {
        String githubUrl = githubLink.getText().toString().trim();
        if (!githubUrl.isEmpty()) {
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot projectSnapshot : snapshot.getChildren()) {
                        String teamLeader = projectSnapshot.child("teamLeaderName").getValue(String.class);
                        if (teamLeader != null && teamLeader.equals(loggedInUserName)) {
                            projectSnapshot.getRef().child("GitHubLink").setValue(githubUrl);
                            Toast.makeText(getContext(), "GitHub Link saved successfully!", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Error saving GitHub link: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "Please enter a valid GitHub link", Toast.LENGTH_SHORT).show();
        }

    }


    private void storeDemoVideoLink() {
        String demoVideoUrl = demoVideoLink.getText().toString().trim();
        if (!demoVideoUrl.isEmpty()) {
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot projectSnapshot : snapshot.getChildren()) {
                        String teamLeader = projectSnapshot.child("teamLeaderName").getValue(String.class);
                        if (teamLeader != null && teamLeader.equals(loggedInUserName)) {
                            projectSnapshot.getRef().child("Demo Video").setValue(demoVideoUrl);
                            Toast.makeText(getContext(), "Demo Video Link saved successfully!", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Error saving Demo Video link: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "Please enter a valid Demo video link", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDatePickerDialog() {
        // Get current date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Show DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year1, month1, dayOfMonth) -> {
                    // Format the selected date
                    String selectedDate = dayOfMonth + "-" + (month1 + 1) + "-" + year1;
                    etStartDate.setText(selectedDate);  // Show selected date in EditText
                    storeStartDate(selectedDate);  // Save selected date to Firebase
                }, year, month, day);
        datePickerDialog.show();
    }

    private void fetchProjectDetails(TextView tvProjectDuration) {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot projectSnapshot : snapshot.getChildren()) {
                    String teamLeader = projectSnapshot.child("teamLeaderName").getValue(String.class);
                    if (teamLeader != null && teamLeader.equals(loggedInUserName)) {

                        guideName = projectSnapshot.child("guideName").getValue(String.class);
                        duration = projectSnapshot.child("Duration").getValue(String.class);
                        startDate = projectSnapshot.child("StartDate").getValue(String.class);
                        tvProjectDuration.setText("Project Duration: " + duration + " weeks");

                        if (startDate != null && !startDate.isEmpty()) {
                            etStartDate.setText(startDate);
                            etStartDate.setEnabled(false);
                            generateWeekLogs(Integer.parseInt(duration), startDate); // ✅ Generate weeks immediately
                        }
                        if (duration != null && !duration.isEmpty()) {
                            try {
                                int totalWeeks = Integer.parseInt(duration);  // Parse the duration only if valid
                                if (totalWeeks <= 0) {
                                    Toast.makeText(getContext(), "Invalid project duration", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (startDate != null) {
                                    etStartDate.setText(startDate);
                                    generateWeekLogs(totalWeeks, startDate);

                                }
                            } catch (NumberFormatException e) {
                                Toast.makeText(getContext(), "Invalid duration format", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(getContext(), "Duration is missing or invalid", Toast.LENGTH_SHORT).show();
                        }
                        break;

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void storeStartDate(final String selectedDate) {
        if (loggedInUserName != null) {
            // Read the entire "Projects" node
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    boolean projectFound = false;

                    // Loop through all child nodes inside "Projects"
                    for (DataSnapshot projectSnapshot : dataSnapshot.getChildren()) {
                        // Get the TeamLeaderName for the current child node
                        String teamLeader = projectSnapshot.child("teamLeaderName").getValue(String.class);
                        String guideName = projectSnapshot.child("guideName").getValue(String.class); // Fetch Guide Name
                        String duration = projectSnapshot.child("Duration").getValue(String.class); // Fetch Duration

                        // If the logged-in user is the team leader, update StartDate
                        if (teamLeader != null && teamLeader.equals(loggedInUserName)) {
                            projectSnapshot.getRef().child("StartDate").setValue(selectedDate)

                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getContext(), "Start Date updated successfully", Toast.LENGTH_SHORT).show();
                                        etStartDate.setEnabled(false);
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "Failed to update Start Date", Toast.LENGTH_SHORT).show();
                                    });


                            generateWeekLogs(Integer.parseInt(duration), selectedDate);
                            projectFound = true;

                            break; // Stop after finding the matching project
                        }
                    }

                    // If no project was found for the logged-in user
                    if (!projectFound) {
                        Toast.makeText(getContext(), "No project found for the logged-in Team Leader", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle any database errors
                    Toast.makeText(getContext(), "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void generateWeekLogs(int totalWeeks, String startDate) {
        weekLogContainer.removeAllViews();

        try {
            Date start = sdf.parse(startDate);
            Calendar calendar = Calendar.getInstance();
            boolean[] completedCheckboxes = new boolean[totalWeeks]; // Track each week completion status
            boolean[] approvedWeeks = new boolean[totalWeeks]; // Track if each week is approved

            for (int i = 1; i <= totalWeeks; i++) {
                View weekLogView = getLayoutInflater().inflate(R.layout.item_week_log, null);
                TextView tvApprovalStatus = weekLogView.findViewById(R.id.tvApprovalStatus);
                TextView weekNumber = weekLogView.findViewById(R.id.tvWeekNumber);
                EditText weekDescription = weekLogView.findViewById(R.id.etWeekDescription);
                CheckBox checkBox = weekLogView.findViewById(R.id.cbWeekComplete);

                weekNumber.setText("Week " + i);
                int finalWeek = i;

                Calendar weekDeadline = Calendar.getInstance();
                weekDeadline.setTime(start);
                weekDeadline.add(Calendar.DAY_OF_MONTH, 7 * (i - 1));  // Week Start Date

                DatabaseReference currentWeekRef = mDatabase.child("WorkLog")
                        .child(guideName).child(loggedInUserName).child("Week " + finalWeek);

                DatabaseReference previousWeekRef = finalWeek > 1
                        ? mDatabase.child("WorkLog").child(guideName).child(loggedInUserName).child("Week " + (finalWeek - 1))
                        : null;

                if (previousWeekRef != null) {
                    previousWeekRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot previousSnapshot) {
                            String previousStatus = previousSnapshot.child("approvalStatus").getValue(String.class);

                            if (!"Approved".equals(previousStatus)) {
                                weekDescription.setEnabled(false);
                                checkBox.setEnabled(false);
                                tvApprovalStatus.setText("Waiting for Previous Week Approval");
                                tvApprovalStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
                            } else {
                                if (new Date().after(weekDeadline.getTime())) {
                                    loadCurrentWeekData(currentWeekRef, weekDescription, checkBox, tvApprovalStatus, completedCheckboxes, approvedWeeks, finalWeek);
                                } else {
                                    weekDescription.setEnabled(false);
                                    checkBox.setEnabled(false);
                                    tvApprovalStatus.setText("Week not reached");
                                    tvApprovalStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    if (new Date().after(weekDeadline.getTime())) {
                        loadCurrentWeekData(currentWeekRef, weekDescription, checkBox, tvApprovalStatus, completedCheckboxes, approvedWeeks, finalWeek);
                    } else {
                        weekDescription.setEnabled(false);
                        checkBox.setEnabled(false);
                        tvApprovalStatus.setText("Week not reached");
                        tvApprovalStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
                    }
                }

                weekLogContainer.addView(weekLogView);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void loadCurrentWeekData(DatabaseReference weekRef, EditText weekDescription, CheckBox checkBox, TextView tvApprovalStatus,
                                     boolean[] completedCheckboxes, boolean[] approvedWeeks, int weekNumber) {
        weekRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String savedDescription = snapshot.child("description").getValue(String.class);
                Boolean isCompleted = snapshot.child("isCompleted").getValue(Boolean.class);
                String approvalStatus = snapshot.child("approvalStatus").getValue(String.class);

                // Populate description field if exists
                if (savedDescription != null) {
                    weekDescription.setText(savedDescription);
                }

                if ("Approved".equals(approvalStatus)) {
                    tvApprovalStatus.setText("Approved");
                    tvApprovalStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    weekDescription.setEnabled(false);
                    checkBox.setEnabled(false);

                    if (Boolean.TRUE.equals(isCompleted)) {
                        checkBox.setChecked(true);
                        completedCheckboxes[weekNumber - 1] = true;
                    }

                    approvedWeeks[weekNumber - 1] = true;

                } else if ("Rejected".equals(approvalStatus)) {
                    tvApprovalStatus.setText("Rejected — Please Update");
                    tvApprovalStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    weekDescription.setEnabled(true);
                    checkBox.setEnabled(true);
                    checkBox.setChecked(false);

                    completedCheckboxes[weekNumber - 1] = false;
                    approvedWeeks[weekNumber - 1] = false;

                } else if (Boolean.TRUE.equals(isCompleted)) {
                    checkBox.setChecked(true);
                    completedCheckboxes[weekNumber - 1] = true;
                    tvApprovalStatus.setText("Pending");
                    tvApprovalStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
                    weekDescription.setEnabled(false);
                    checkBox.setEnabled(false);
                    approvedWeeks[weekNumber - 1] = false;

                } else {
                    weekDescription.setEnabled(true);
                    checkBox.setEnabled(true);
                    completedCheckboxes[weekNumber - 1] = false;
                    approvedWeeks[weekNumber - 1] = false;
                }

                // Checkbox listener
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    completedCheckboxes[weekNumber - 1] = isChecked;

                    if (isChecked) {
                        saveWorkLog(weekNumber, weekDescription.getText().toString(), true);
                        Log.d("WeekLog", "Work log saved for Week " + weekNumber + ": " + weekDescription.getText().toString());

                        checkBox.setEnabled(false);
                        weekDescription.setEnabled(false);
                    } else {
                        Log.d("WeekLog", "Checkbox unchecked for Week " + weekNumber);
                    }

                    Log.d("WeekLog", "Completed Weeks: " + Arrays.toString(completedCheckboxes));
                    Log.d("WeekLog", "Approved Weeks: " + Arrays.toString(approvedWeeks));

                    // Check and enable GitHub/Demo if all done
                    if (areAllWeeksCompleted(completedCheckboxes) && areAllWeeksApproved(approvedWeeks)) {
                        Log.d("WeekLog", "All weeks completed and approved (inside listener). Enabling GitHub and Demo fields.");
                        enableGitHubAndDemoFields(true);
                    }
                });

                // Also check outside the listener (important fix!)
                if (areAllWeeksCompleted(completedCheckboxes) && areAllWeeksApproved(approvedWeeks)) {
                    Log.d("WeekLog", "All weeks completed and approved (after load). Enabling GitHub and Demo fields.");
                    enableGitHubAndDemoFields(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private boolean areAllWeeksCompleted(boolean[] completedCheckboxes) {
        for (boolean isCompleted : completedCheckboxes) {
            if (!isCompleted) {
                return false;
            }
        }
        return true;
    }

    private boolean areAllWeeksApproved(boolean[] approvedWeeks) {
        for (boolean isApproved : approvedWeeks) {
            if (!isApproved) {
                return false;
            }
        }
        return true;
    }
    private void enableGitHubAndDemoFields(boolean enable) {
        githubLink.setEnabled(enable);
        demoVideoLink.setEnabled(enable);
        View rootView = getView();
        if (rootView != null) {
            LinearLayout completionFieldsContainer = rootView.findViewById(R.id.completionFieldsContainer);
            if (completionFieldsContainer != null) {
                completionFieldsContainer.setVisibility(enable ? View.VISIBLE : View.GONE);
            }
        }
    }

    private void saveWorkLog(int weekNumber, String description, boolean isCompleted) {
        if (guideName != null && loggedInUserName != null) {
            DatabaseReference workLogRef = mDatabase.child("WorkLog").child(guideName).child(loggedInUserName).child("Week " + weekNumber);
            String currentDate = sdf.format(new Date());

            workLogRef.child("description").setValue(description);
            workLogRef.child("isCompleted").setValue(isCompleted);
            workLogRef.child("approvalStatus").setValue("Pending"); // Mark as pending until faculty approves
        }
    }


}
