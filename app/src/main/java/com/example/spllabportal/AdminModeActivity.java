package com.example.spllabportal;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminModeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_mode);

        BottomNavigationView bottomNavigationView = findViewById(R.id.admin_bottom_navigation);
        loadFragment(new ProjectEntryFragment());
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_achievements) {
                selectedFragment = new Achievements_admin();
            } else if (item.getItemId() == R.id.nav_review_projects) {
                selectedFragment = new ReviewProjectsFragment();
            } else if (item.getItemId() == R.id.nav_projects) {
                selectedFragment = new Project_admin();
            } else if (item.getItemId() == R.id.nav_weeklog) {
                selectedFragment = new WeekLog();
            } else if (item.getItemId() == R.id.nav_admin_profile) {
                selectedFragment = new AdminProfileFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;
        });

    }
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.admin_fragment_container, fragment)
                .commit();
    }
}
