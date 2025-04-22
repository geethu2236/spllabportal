package com.example.spllabportal;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "GoogleSignIn";
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private Button signInButton;
    private TextView welcomeTextView;
    private TextView nameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signInButton = findViewById(R.id.signinbutton);
        welcomeTextView = findViewById(R.id.welcome);
        nameTextView = findViewById(R.id.name);
        mAuth = FirebaseAuth.getInstance();
        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("1006634308462-ecn0hegen139lpm0vfdlmhett0cced0m.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOutAndSignIn(); // Sign out and then sign in again to show account chooser
            }
        });
    }

    private void signOutAndSignIn() {
        // Sign out before showing the account chooser
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, task -> {
                    // After signing out, trigger the sign-in flow again
                    signIn();
                });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                String email = account.getEmail();
                if (email != null && email.endsWith("@bitsathy.ac.in") || email.equalsIgnoreCase("geethuiit9@gmail.com")) {
                    AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

                    mAuth.signInWithCredential(credential)
                            .addOnCompleteListener(this, task -> {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null) {
                                        String userEmail = user.getEmail();
                                        String userName = user.getDisplayName();

                                        Log.d(TAG, "Logged in as: " + userEmail);

                                        if (userEmail != null && userEmail.equalsIgnoreCase("geethuiit9@gmail.com")) {
                                            // Direct Admin Mode for this specific email
                                            Log.d(TAG, "Admin recognized: Redirecting to Admin Mode!");
                                            startActivity(new Intent(MainActivity.this, AdminModeActivity.class));
                                            finish();
                                        } else {
                                            // Validate normal @bitsathy.ac.in users
                                            DatabaseReference labRef = FirebaseDatabase.getInstance().getReference("SpecialLab");
                                            labRef.get().addOnCompleteListener(labTask -> {
                                                if (labTask.isSuccessful() && labTask.getResult().exists()) {
                                                    boolean userExists = false;

                                                    for (DataSnapshot lab : labTask.getResult().getChildren()) {
                                                        for (DataSnapshot userEntry : lab.getChildren()) {
                                                            String storedName = userEntry.child("Name").getValue(String.class);
                                                            Log.d(TAG, "Checking: " + storedName);

                                                            if (storedName != null && storedName.equalsIgnoreCase(userName)) {
                                                                Log.d(TAG, "User Found in: " + lab.getKey());
                                                                userExists = true;
                                                                break;
                                                            }
                                                        }
                                                        if (userExists) break;
                                                    }

                                                    if (userExists) {
                                                        Log.d(TAG, "Student verified: Redirecting to User Mode.");
                                                        startActivity(new Intent(MainActivity.this, UserModeActivity.class));
                                                    } else {
                                                        Log.d(TAG, "User not found in database. Redirecting to Registration.");
                                                        startActivity(new Intent(MainActivity.this, RegistrationActivity.class));
                                                    }
                                                } else {
                                                    Log.d(TAG, "No SpecialLab records found. Redirecting to Registration.");
                                                    startActivity(new Intent(MainActivity.this, RegistrationActivity.class));
                                                }
                                                finish();
                                            });
                                        }
                                    }
                                } else {
                                    Toast.makeText(this, "Firebase Authentication failed.", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(this, "Invalid email domain.", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, "Sign-in failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
//    package com.example.spllabportal;
//import android.content.Intent;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//
//
//import androidx.appcompat.app.AppCompatActivity;
//
//
//import com.google.android.gms.auth.api.signin.GoogleSignIn;
//import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
//import com.google.android.gms.auth.api.signin.GoogleSignInClient;
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
//import com.google.android.gms.common.api.ApiException;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.auth.AuthCredential;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.auth.GoogleAuthProvider;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//
//
//import java.util.HashMap;
//
//
//    public class MainActivity extends AppCompatActivity {
//
//
//        private static final String TAG = "GoogleSignIn";
//        private static final int RC_SIGN_IN = 9001;
//        private GoogleSignInClient mGoogleSignInClient;
//        private FirebaseAuth mAuth;
//        private Button signInButton;
//        private TextView welcomeTextView;
//        private TextView nameTextView;
//
//
//        @Override
//        protected void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//            setContentView(R.layout.activity_main);
//
//
//            signInButton = findViewById(R.id.signinbutton);
//            welcomeTextView = findViewById(R.id.welcome);
//            nameTextView = findViewById(R.id.name);
//            mAuth = FirebaseAuth.getInstance();
//            // Configure Google Sign-In
//            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                    .requestIdToken("1006634308462-ecn0hegen139lpm0vfdlmhett0cced0m.apps.googleusercontent.com")
//                    .requestEmail()
//                    .build();
//
//
//            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
//
//
//            signInButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    signOutAndSignIn(); // Sign out and then sign in again to show account chooser
//                }
//            });
//        }
//
//
//        private void signOutAndSignIn() {
//            // Sign out before showing the account chooser
//            mGoogleSignInClient.signOut()
//                    .addOnCompleteListener(this, task -> {
//                        // After signing out, trigger the sign-in flow again
//                        signIn();
//                    });
//        }
//
//
//        private void signIn() {
//            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//            startActivityForResult(signInIntent, RC_SIGN_IN);
//        }
//
//
//        @Override
//        public void onActivityResult(int requestCode, int resultCode, Intent data) {
//            super.onActivityResult(requestCode, resultCode, data);
//
//
//            if (requestCode == RC_SIGN_IN) {
//                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//                handleSignInResult(task);
//            }
//        }
//
//
//        private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
//            try {
//                GoogleSignInAccount account = completedTask.getResult(ApiException.class);
//                if (account != null) {
//                    String email = account.getEmail();
//                    if (email != null && email.endsWith("@bitsathy.ac.in")) {
//                        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
//
//
//                        mAuth.signInWithCredential(credential)
//                                .addOnCompleteListener(this, task -> {
//                                    if (task.isSuccessful()) {
//                                        FirebaseUser user = mAuth.getCurrentUser();
//                                        if (user != null) {
//                                            String userEmail = user.getEmail();
//                                            boolean isStudent = isStudentEmail(userEmail);
//                                            String userName = user.getDisplayName(); // ✅ Get Name instead of extracting ID
//
//
//                                            Log.d(TAG, "Retrieved Name: " + userName);
//
//
//                                            DatabaseReference labRef = FirebaseDatabase.getInstance().getReference("SpecialLab");
//                                            labRef.get().addOnCompleteListener(labTask -> {
//                                                if (labTask.isSuccessful() && labTask.getResult().exists()) {
//                                                    boolean userExists = false;
//
//
//                                                    for (DataSnapshot lab : labTask.getResult().getChildren()) {
//                                                        for (DataSnapshot userEntry : lab.getChildren()) {
//                                                            String storedName = userEntry.child("Name").getValue(String.class);
//                                                            Log.d(TAG, "Checking: " + storedName);
//
//
//                                                            if (storedName != null && storedName.equalsIgnoreCase(userName)) {
//                                                                Log.d(TAG, "User Found in: " + lab.getKey());
//                                                                userExists = true;
//                                                                break;
//                                                            }
//                                                        }
//                                                        if (userExists) break; // Exit loop early if user is found
//                                                    }
//
//
//                                                    if (userExists) {
//                                                        Log.d(TAG, "User exists. Redirecting...");
//                                                        if (isStudent) {
//                                                            startActivity(new Intent(MainActivity.this, UserModeActivity.class));
//                                                        } else {
//                                                            startActivity(new Intent(MainActivity.this, AdminModeActivity.class));
//                                                        }
//                                                    } else {
//                                                        Log.d(TAG, "User not found. Redirecting to RegistrationActivity.");
//                                                        startActivity(new Intent(MainActivity.this, RegistrationActivity.class));
//                                                    }
//                                                } else {
//                                                    Log.d(TAG, "No SpecialLab found. Redirecting to Registration.");
//                                                    startActivity(new Intent(MainActivity.this, RegistrationActivity.class));
//                                                }
//                                                finish();
//                                            });
//                                        }
//                                    } else {
//                                        Toast.makeText(this, "Firebase Authentication failed.", Toast.LENGTH_SHORT).show();
//                                    }
//                                });
//                    } else {
//                        Toast.makeText(this, "Invalid email domain.", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            } catch (ApiException e) {
//                Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
//                Toast.makeText(this, "Sign-in failed. Please try again.", Toast.LENGTH_SHORT).show();
//            }
//        }
//
//
//        private boolean isStudentEmail(String email) {
//            return email.matches("^[a-zA-Z]+\\.[a-zA-Z]{2}\\d{2}@bitsathy\\.ac\\.in$");
//        }
//
//
//        private boolean isFacultyEmail(String email) {
//            return email.matches("^[a-zA-Z]+@bitsathy\\.ac\\.in$");
//        }
//    }



}