package lk.krag.loginapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import android.widget.TextView;

import lk.krag.loginapp.databinding.ActivityProfileBinding;

public class ProfileActivity extends AppCompatActivity {

    private static final String PROFILE_PREFS_PREFIX = "profile_";
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_FACULTY = "faculty";

    private ActivityProfileBinding binding;
    private String userEmail;
    private String userPrefsName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userEmail = getIntent().getStringExtra("email");
        if (userEmail == null) {
            Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.tvUserEmail.setText(userEmail);
        userPrefsName = PROFILE_PREFS_PREFIX + userEmail.replace(".", "_");

        loadProfile();
        loadTasks();

        binding.btnUpdate.setOnClickListener(v -> saveProfile());
        binding.btnLogout.setOnClickListener(v -> logout());
        binding.btnDeleteProfile.setOnClickListener(v -> showDeleteConfirmation());
        binding.btnAddTask.setOnClickListener(v -> addTask());
    }

    private void loadTasks() {
        String encodedEmail = userEmail.replace(".", ",");
        FirebaseDatabase.getInstance(SignInActivity.FIREBASE_DB_URL)
                .getReference("users")
                .child(encodedEmail)
                .child("tasks")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        binding.tasksContainer.removeAllViews();
                        for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                            Task task = taskSnapshot.getValue(Task.class);
                            if (task != null) {
                                addTaskToView(task);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void addTask() {
        String title = binding.inputTask.getText().toString().trim();
        if (title.isEmpty()) return;

        String encodedEmail = userEmail.replace(".", ",");
        DatabaseReference ref = FirebaseDatabase.getInstance(SignInActivity.FIREBASE_DB_URL)
                .getReference("users")
                .child(encodedEmail)
                .child("tasks")
                .push();

        Task task = new Task(ref.getKey(), title, false);
        ref.setValue(task).addOnSuccessListener(aVoid -> binding.inputTask.setText(""));
    }

    private void addTaskToView(Task task) {
        TextView textView = new TextView(this);
        textView.setText(task.title + (task.completed ? " ✓" : ""));
        textView.setTextSize(16);
        textView.setPadding(0, 20, 0, 20);
        textView.setOnClickListener(v -> toggleTask(task));
        textView.setOnLongClickListener(v -> {
            deleteTask(task);
            return true;
        });
        binding.tasksContainer.addView(textView);
    }

    private void toggleTask(Task task) {
        String encodedEmail = userEmail.replace(".", ",");
        FirebaseDatabase.getInstance(SignInActivity.FIREBASE_DB_URL)
                .getReference("users")
                .child(encodedEmail)
                .child("tasks")
                .child(task.id)
                .child("completed")
                .setValue(!task.completed);
    }

    private void deleteTask(Task task) {
        String encodedEmail = userEmail.replace(".", ",");
        FirebaseDatabase.getInstance(SignInActivity.FIREBASE_DB_URL)
                .getReference("users")
                .child(encodedEmail)
                .child("tasks")
                .child(task.id)
                .removeValue();
    }

    private void loadProfile() {
        SharedPreferences prefs = getSharedPreferences(userPrefsName, MODE_PRIVATE);
        binding.inputFirstName.setText(prefs.getString(KEY_FIRST_NAME, ""));
        binding.inputLastName.setText(prefs.getString(KEY_LAST_NAME, ""));
        binding.inputPhone.setText(prefs.getString(KEY_PHONE, ""));
        binding.inputFaculty.setText(prefs.getString(KEY_FACULTY, ""));
    }

    private void saveProfile() {
        String firstName = binding.inputFirstName.getText().toString().trim();
        String lastName = binding.inputLastName.getText().toString().trim();
        String phone = binding.inputPhone.getText().toString().trim();
        String faculty = binding.inputFaculty.getText().toString().trim();

        getSharedPreferences(userPrefsName, MODE_PRIVATE).edit()
                .putString(KEY_FIRST_NAME, firstName)
                .putString(KEY_LAST_NAME, lastName)
                .putString(KEY_PHONE, phone)
                .putString(KEY_FACULTY, faculty)
                .apply();

        Toast.makeText(this, R.string.profile_updated, Toast.LENGTH_SHORT).show();
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> deleteAccountAndProfile())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void deleteAccountAndProfile() {
        // 1. Delete Profile SharedPreferences
        getSharedPreferences(userPrefsName, MODE_PRIVATE).edit().clear().apply();

        // 2. Delete from Local Login if it matches
        SharedPreferences loginPrefs = getSharedPreferences(SignInActivity.PREFS_NAME, MODE_PRIVATE);
        String savedEmail = loginPrefs.getString(SignInActivity.KEY_EMAIL, "");
        if (userEmail.equals(savedEmail)) {
            loginPrefs.edit().clear().apply();
        }

        // 3. Delete from Firebase Auth
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, R.string.account_deleted, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to delete account", Toast.LENGTH_SHORT).show();
                }
                logout();
            });
        } else {
            logout();
        }
    }
}
