package com.example.spllabportal;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AdminProfileFragment extends Fragment {

    private FirebaseAuth mAuth;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_admin_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        TextView adminName = root.findViewById(R.id.admin_name);
        TextView adminEmail = root.findViewById(R.id.admin_email);
        ImageView profileImage = root.findViewById(R.id.profile_image);
        Button logoutButton = root.findViewById(R.id.logout_button);

        if (user != null) {
            adminName.setText(user.getDisplayName());
            adminEmail.setText(user.getEmail());

            // Load profile image using Glide
            Glide.with(this).load(user.getPhotoUrl()).into(profileImage);
        }

        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        });

        return root;
    }
}
