package lk.krag.loginapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;

import lk.krag.loginapp.databinding.ActivityProfileBinding;

public class ProfileActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "profile_prefs";
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_FACULTY = "faculty";

    private ActivityProfileBinding binding;

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

        loadProfile();

        binding.btnUpdate.setOnClickListener(v -> saveProfile());
    }

    private void loadProfile() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
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

        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                .putString(KEY_FIRST_NAME, firstName)
                .putString(KEY_LAST_NAME, lastName)
                .putString(KEY_PHONE, phone)
                .putString(KEY_FACULTY, faculty)
                .apply();

        Toast.makeText(this, R.string.profile_updated, Toast.LENGTH_SHORT).show();
    }
}
