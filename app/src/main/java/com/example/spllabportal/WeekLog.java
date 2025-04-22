package com.example.spllabportal;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class WeekLog extends Fragment {

    private EditText searchBar;  // Declare search bar
    private String filterText = "";  // Store current filter text


    private TableLayout projectTable;
    private DatabaseReference workLogRef;
    private int serialNo = 1;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_week_log, container, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        projectTable = view.findViewById(R.id.projectTable);


        // Replace this with the username you want to query under WorkLog
        String username = "KIRUTHIKA V R";

        workLogRef = FirebaseDatabase.getInstance().getReference()
                .child("Projects")
                .child("WorkLog")
                .child(username);

        fetchWorkLogData();
        return view;
    }

    private void fetchWorkLogData() {
        workLogRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                serialNo = 1;
                if (snapshot.exists()) {
                    for (DataSnapshot studentNode : snapshot.getChildren()) {

                        // 💡 Student Name is the node name (like "GEETHIKA S M" / "SHANMATHI L")
                        String studentName = studentNode.getKey();

                        for (DataSnapshot weekNode : studentNode.getChildren()) {

                            // Week Number is the child node name (like "Week 1", "Week 2"...)
                            String weekNo = weekNode.getKey();

                            String description = weekNode.child("description").getValue(String.class);
                            String status = weekNode.child("approvalStatus").getValue(String.class);

                            // Adding the row to the table
                            addTableRow(
                                    serialNo++,
                                    studentName,
                                    weekNo,
                                    description,
                                    status,
                                    studentName,  // Pass the node name for updates
                                    weekNo        // Pass week for updates
                            );
                        }
                    }
                } else {
                    Toast.makeText(getContext(), "No WorkLog data found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error loading data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void addTableRow(int sNo, String studentName, String weekNo, String description, String status, String studentKey, String weekKey) {
        TableRow row = new TableRow(getContext());
        row.setPadding(8, 8, 8, 8);

        // Serial Number
        TextView snoText = new TextView(getContext());
        snoText.setText(String.valueOf(sNo));
        snoText.setPadding(8, 8, 8, 8);

        // Student Name
        TextView nameText = new TextView(getContext());
        nameText.setText(studentName);
        nameText.setPadding(8, 8, 8, 8);

        // Week Number
        TextView weekText = new TextView(getContext());
        weekText.setText(weekNo);
        weekText.setPadding(8, 8, 8, 8);

        // Description
        TextView descriptionText = new TextView(getContext());
        descriptionText.setText(description != null ? description : "No Description");
        descriptionText.setPadding(8, 8, 8, 8);
        descriptionText.setMaxLines(1);
        descriptionText.setEllipsize(TextUtils.TruncateAt.END); // shows "..." if it's too long

        descriptionText.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Description");
            builder.setMessage(description);
            builder.setPositiveButton("Close", null);
            builder.show();
        });


        // Status Spinner
        Spinner statusSpinner = new Spinner(getContext());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Pending", "Approved", "Rejected"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(adapter);

        // Set current status to spinner
        if (status != null) {
            int pos = adapter.getPosition(status);
            if (pos >= 0) statusSpinner.setSelection(pos);
        }

        // Spinner listener to update status in Firebase
        statusSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            boolean firstSelect = true;

            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (firstSelect) {
                    firstSelect = false;
                    return;  // skip the initial trigger
                }

                String newStatus = parent.getItemAtPosition(position).toString();
                workLogRef.child(studentKey).child(weekKey).child("approvalStatus")
                        .setValue(newStatus)
                        .addOnSuccessListener(unused -> Toast.makeText(getContext(), "Status updated to " + newStatus, Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        // Add Views to TableRow
        row.addView(snoText);
        row.addView(nameText);
        row.addView(weekText);
        row.addView(descriptionText);
        row.addView(statusSpinner);

        projectTable.addView(row);
    }
}
