package com.example.spllabportal;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ReviewProjectsFragment extends Fragment {

    private TableLayout projectTable;
    private TextView noDataMessage;
    private FirebaseDatabase database;
    private DatabaseReference achievementsRef;
    private final String adminLab = "IOT Lab";  // Replace this with your logged-in faculty's assigned lab.

    public ReviewProjectsFragment() {
        // Required empty public constructor
    }
    private String userSpecialLab; // You should fetch this from your login session or Firebase before calling loadAchievements()

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review_projects, container, false);



        projectTable = view.findViewById(R.id.projectTable);
        noDataMessage = view.findViewById(R.id.noDataMessage);

        // Initialize Firebase
        database = FirebaseDatabase.getInstance();

        // Example: assume you already know user's special lab
      //  userSpecialLab ="IOT Lab";

       // Log.d("ReviewProjects", "Special Lab: " + userSpecialLab);


//        if (userSpecialLab != null) {
            //achievementsRef = database.getReference("Achievements").child(userSpecialLab);
            achievementsRef = database.getReference("Achievements");
            loadAchievements();
//        } else {
//            Toast.makeText(getContext(), "Special Lab not found for user!", Toast.LENGTH_SHORT).show();
//        }

        return view;
    }

//    private String getUserSpecialLab() {
//        // Fetch your special lab from logged-in user's data (e.g., FirebaseAuth.getCurrentUser().getUid())
//        // Here it's hardcoded for the example.
//        return "IOT Lab";
//    }

    private void loadAchievements() {
        achievementsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean hasData = false;

                for (DataSnapshot studentSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot achievementSnapshot : studentSnapshot.getChildren()) {

                        String status = achievementSnapshot.child("status").getValue(String.class);

                        if ("Pending".equalsIgnoreCase(status)) {
                            hasData = true;

                            String achievementType = getValue(achievementSnapshot, "achievementType");
                            String department = getValue(achievementSnapshot, "department");
                            String eventDate = getValue(achievementSnapshot, "eventDate");
                            String eventMode = getValue(achievementSnapshot, "eventMode");
                            String eventName = getValue(achievementSnapshot, "eventName");
                            String eventType = getValue(achievementSnapshot, "eventType");
                            String name = getValue(achievementSnapshot, "name");
                            String projectTitle = getValue(achievementSnapshot, "projectTitle");
                            String proof = getValue(achievementSnapshot, "prooflink");
                            String rollNumber = getValue(achievementSnapshot, "rollNumber");
                            String specialLab = getValue(achievementSnapshot, "specialLab");
                            String year = getValue(achievementSnapshot, "year");


                            addTableRow(
                                    achievementSnapshot,achievementType, department, eventDate, eventMode, eventName,
                                    eventType, name, projectTitle, proof, rollNumber, specialLab, status, year);
                        }
                    }
                }

                if (!hasData) {
                    noDataMessage.setVisibility(View.VISIBLE);
                } else {
                    noDataMessage.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addTableRow(DataSnapshot achievementSnapshot,String achievementType, String department, String eventDate, String eventMode, String eventName,
                             String eventType, String name, String projectTitle, String proof, String rollNumber,
                             String specialLab, String status, String year) {

        TableRow row = new TableRow(getContext());
        row.setPadding(8, 8, 8, 8);

        row.addView(createCell(achievementType));
        row.addView(createCell(department));
        row.addView(createCell(eventDate));
        row.addView(createCell(eventMode));
        row.addView(createCell(eventName));
        row.addView(createCell(eventType));
        row.addView(createCell(name));
        row.addView(createCell(projectTitle));
        row.addView(createProofCell(proof)); // Make proof link clickable
        row.addView(createCell(rollNumber));
        row.addView(createCell(specialLab));

        if ("Pending".equalsIgnoreCase(status)) {
            Spinner statusSpinner = new Spinner(getContext());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_spinner_item,
                    new String[]{"Pending", "Approved", "Rejected"});
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            statusSpinner.setAdapter(adapter);
            statusSpinner.setSelection(0);

            statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                boolean isFirstTime = true;

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (isFirstTime) {
                        isFirstTime = false;
                        return;
                    }

                    String selectedStatus = parent.getItemAtPosition(position).toString();

                    // Update status in Firebase
                    achievementSnapshot.getRef().child("status").setValue(selectedStatus)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Status updated to " + selectedStatus, Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to update status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });

            row.addView(statusSpinner);

        } else {
            row.addView(createCell(status));  // If already approved/rejected, just show plain text.
        }
        row.addView(createCell(year));


        projectTable.addView(row);
    }


    private TextView createCell(String text) {
        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setPadding(8, 8, 8, 8);
        textView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
        textView.setGravity(Gravity.CENTER);
        return textView;
    }

    private TextView createProofCell(String url) {
        TextView link = new TextView(getContext());
        link.setText("Open Link");
        link.setPadding(8, 8, 8, 8);
        link.setTextColor(Color.BLUE);
        link.setPaintFlags(link.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        link.setGravity(Gravity.CENTER);
        link.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        });
        return link;
    }

    private String getValue(DataSnapshot snapshot, String key) {
        return snapshot.child(key).getValue() != null ? snapshot.child(key).getValue().toString() : "N/A";
    }
}
