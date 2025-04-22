package com.example.spllabportal;

import android.app.AlertDialog;
import android.graphics.Typeface;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ProjectViewFragment extends Fragment {

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

        addHeaderTextView(headerRow, "Title");
        addHeaderTextView(headerRow, "Guide");
        addHeaderTextView(headerRow, "Department");
        addHeaderTextView(headerRow, "Roll No");
        addHeaderTextView(headerRow, "Lab");

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

        String projectTitle = getValue(projectSnapshot, "projectTitle");
        String guideName = getValue(projectSnapshot, "guideName");
        String department = getValue(projectSnapshot, "department");
        String rollNumber = getValue(projectSnapshot, "rollNumber");
        String specialLab = getValue(projectSnapshot, "specialLab");

        addTextView(row, projectTitle);
        addTextView(row, guideName);
        addTextView(row, department);
        addTextView(row, rollNumber);
        addTextView(row, specialLab);

        projectTable.addView(row);
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
