package com.example.spllabportal;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Project_admin extends Fragment {

    private TableLayout projectTable;
    private EditText searchBar;
    private MaterialButton filterButton;
    private ImageView search;
    private DatabaseReference databaseReference;
    private List<DataSnapshot> projectList = new ArrayList<>();

    // Filter state variables
    private String selectedFilterField = "department";
    private String searchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project_view, container, false);

        projectTable = view.findViewById(R.id.projectTable);
        searchBar = view.findViewById(R.id.searchBar);
        filterButton = view.findViewById(R.id.filterButton);
        search = view.findViewById(R.id.search);
        databaseReference = FirebaseDatabase.getInstance().getReference("Projects");

        fetchProjects();

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchQuery = charSequence.toString().trim().toLowerCase();
                displayProjects();
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        search.setOnClickListener(v -> showFilterDialog());

        return view;
    }

    // Fetch project data from Firebase
    private void fetchProjects() {
        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                projectList.clear();
                for (DataSnapshot projectSnapshot : task.getResult().getChildren()) {
                    // Only add projects that have both GitHub Link and Demo Video Link
                    if (hasRequiredLinks(projectSnapshot)) {
                        projectList.add(projectSnapshot);
                    }
                }
                displayProjects();
            }
        });
    }

    // Check if a project has both GitHub Link and Demo Video Link
    private boolean hasRequiredLinks(DataSnapshot projectSnapshot) {
        String githubLink = getValue(projectSnapshot, "GitHubLink");
        String demoVideoLink = getValue(projectSnapshot, "Demo Video");

        return githubLink != null && !githubLink.isEmpty() &&
                demoVideoLink != null && !demoVideoLink.isEmpty();
    }

    // Display projects in table
    private void displayProjects() {
        projectTable.removeAllViews();
        addTableHeader();

        List<DataSnapshot> matchingProjects = new ArrayList<>();
        List<DataSnapshot> otherProjects = new ArrayList<>();

        for (DataSnapshot projectSnapshot : projectList) {
            if (matchesFilter(projectSnapshot)) {
                matchingProjects.add(projectSnapshot);
            } else {
                otherProjects.add(projectSnapshot);
            }
        }

        if (matchingProjects.isEmpty()) {
            showNoDataMessage(); // Show message if no matches are found
        } else {
            for (DataSnapshot project : matchingProjects) addProjectRow(project);
            for (DataSnapshot project : otherProjects) addProjectRow(project);
        }
    }

    // Check if project matches the search criteria
    private boolean matchesFilter(DataSnapshot projectSnapshot) {
        if (searchQuery.isEmpty()) return true;

        String fieldValue = getValue(projectSnapshot, selectedFilterField).toLowerCase();
        return fieldValue.contains(searchQuery);
    }

    // Get value from Firebase snapshot
    private String getValue(DataSnapshot snapshot, String key) {
        return snapshot.child(key).getValue(String.class) != null ?
                snapshot.child(key).getValue(String.class) : "";
    }

    // Add table header
    private void addTableHeader() {
        TableRow headerRow = new TableRow(getContext());

        addHeaderTextView(headerRow, "Team Leader Name");
        addHeaderTextView(headerRow, "Roll Number");
        addHeaderTextView(headerRow, "Special Lab");
        addHeaderTextView(headerRow, "Guide Name");
        addHeaderTextView(headerRow, "Department");
        addHeaderTextView(headerRow, "Year");
        addHeaderTextView(headerRow, "Project Title");
        addHeaderTextView(headerRow, "Demo Video Link");
        addHeaderTextView(headerRow, "GitHub Link");
        addHeaderTextView(headerRow, "Project Description");
        addHeaderTextView(headerRow, "Duration");
        addHeaderTextView(headerRow, "Start Date");

        projectTable.addView(headerRow);
    }

    // Add text to table header
    private void addHeaderTextView(TableRow row, String label) {
        TextView textView = new TextView(getContext());
        textView.setText(label);
        textView.setPadding(16, 12, 16, 12);
        textView.setGravity(Gravity.CENTER);
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        textView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white)); // White text color
        textView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.teal_700)); // Header background color

        row.addView(textView);
    }

    // Add project data to table row
    private void addProjectRow(DataSnapshot projectSnapshot) {
        TableRow row = new TableRow(getContext());

        String teamLeaderName = getValue(projectSnapshot, "teamLeaderName");
        String rollNumber = getValue(projectSnapshot, "rollNumber");
        String specialLab = getValue(projectSnapshot, "specialLab");
        String guideName = getValue(projectSnapshot, "guideName");
        String department = getValue(projectSnapshot, "department");
        String year = getValue(projectSnapshot, "year");
        String projectTitle = getValue(projectSnapshot, "projectTitle");
        String demoVideoLink = getValue(projectSnapshot, "Demo Video");
        String githubLink = getValue(projectSnapshot, "GitHubLink");
        String projectDescription = getValue(projectSnapshot, "projectDescription");
        String duration = getValue(projectSnapshot, "Duration");
        String startDate = getValue(projectSnapshot, "StartDate");

        addTextView(row, teamLeaderName);
        addTextView(row, rollNumber);
        addTextView(row, specialLab);
        addTextView(row, guideName);
        addTextView(row, department);
        addTextView(row, year);
        addTextView(row, projectTitle);
        // For Demo Video and GitHub Link, make them clickable
        addLinkTextView(row, demoVideoLink);
        addLinkTextView(row, githubLink);

        // For Project Description, if you want it clickable (maybe to show in a dialog or new activity), you can do this too
        addClickableTextView(row, projectDescription);
        addTextView(row, duration);
        addTextView(row, startDate);

        projectTable.addView(row);
    }
    private void addClickableTextView(TableRow row, String text) {
        TextView textView = new TextView(getContext());
        textView.setText("Show Context");  // Display "Show Context" as the clickable text
        textView.setPadding(12, 8, 12, 8);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));  // Blue color for the clickable text
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

        // On click, open the description in a dialog or another activity
        textView.setOnClickListener(v -> {
            // You can implement a dialog or another method to display the full description
            showDescriptionDialog(text);
        });

        row.addView(textView);
    }
    // Method to show project description in a dialog (you can replace this with another method as needed)
    private void showDescriptionDialog(String description) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Project Description");
        builder.setMessage(description);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
    private void addLinkTextView(TableRow row, String link) {
        if (link.isEmpty()) {
            addTextView(row, "N/A"); // If there's no link, display "N/A"
            return;
        }

        // Ensure the link has a valid protocol (http or https)
        if (!link.startsWith("http://") && !link.startsWith("https://")) {
            link = "https://" + link; // Default to https:// if no protocol is present
        }
        TextView textView = new TextView(getContext());
        textView.setText("Show Context");  // Display "Show Context" as the clickable text
        textView.setPadding(12, 8, 12, 8);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));  // Blue color for the clickable text
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

        // On click, open a dialog showing the link
        String finalLink = link;
        textView.setOnClickListener(v -> {
            // Show the actual link in a dialog box
            showLinkDialog(finalLink);  // Pass the actual URL to the dialog
        });

        row.addView(textView);
    }
    // Method to show the link in a dialog
    private void showLinkDialog(String link) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Project Link");  // Set the title for the dialog
        builder.setMessage(link);  // Set the content of the dialog as the link
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());  // Dismiss dialog on "OK"
        builder.show();  // Show the dialog
    }

    // Add text to table row
    private void addTextView(TableRow row, String text) {
        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setPadding(12, 8, 12, 8);
        textView.setGravity(Gravity.CENTER);
        row.addView(textView);
    }

    // Show "No data found" message
    private void showNoDataMessage() {
        TableRow row = new TableRow(getContext());
        TextView textView = new TextView(getContext());
        textView.setText("No data found");
        textView.setPadding(12, 8, 12, 8);
        textView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_dark));
        row.addView(textView);
        projectTable.addView(row);
    }

    // Show filter dialog
    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_filter, null);
        builder.setView(dialogView);

        Spinner filterSpinner = dialogView.findViewById(R.id.filterSpinner);

        List<String> filterOptions = new ArrayList<>();
        filterOptions.add("department");
        filterOptions.add("guideName");
        filterOptions.add("rollNumber");
        filterOptions.add("specialLab");
        filterOptions.add("teamLeaderName");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, filterOptions);
        filterSpinner.setAdapter(adapter);

        builder.setPositiveButton("Apply", (dialog, which) -> {
            selectedFilterField = filterSpinner.getSelectedItem().toString();
            displayProjects();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
