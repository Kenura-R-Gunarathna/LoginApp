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

import com.google.firebase.database.FirebaseDatabase;

import lk.krag.loginapp.databinding.ActivitySignUpBinding;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.btnSignUp.setOnClickListener(v -> signUp());
        binding.tvGoToSignIn.setOnClickListener(v -> finish());
    }

    private void signUp() {
        String email = binding.email.getText().toString().trim();
        String password = binding.password.getText().toString().trim();
        String confirmPassword = binding.confirmPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, R.string.error_passwords_dont_match, Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);

        if (prefs.getString(MainActivity.KEY_EMAIL, null) != null) {
            Toast.makeText(this, R.string.error_email_exists, Toast.LENGTH_SHORT).show();
            return;
        }

        // Save locally
        prefs.edit()
                .putString(MainActivity.KEY_EMAIL, email)
                .putString(MainActivity.KEY_PASSWORD, password)
                .apply();

        // Save to Firebase (dots not allowed in RTDB keys, replace with commas)
        String encodedEmail = email.replace(".", ",");
        FirebaseDatabase.getInstance(MainActivity.FIREBASE_DB_URL)
                .getReference("users")
                .child(encodedEmail)
                .child("password")
                .setValue(password)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Local save OK, Firebase failed: " + e.getMessage(), Toast.LENGTH_LONG).show());

        Toast.makeText(this, R.string.signup_success, Toast.LENGTH_SHORT).show();
        finish();
    }
}
