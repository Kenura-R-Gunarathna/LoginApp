package lk.krag.loginapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;

import com.google.firebase.auth.FirebaseAuth;

import lk.krag.loginapp.databinding.ActivitySignInBinding;

public class SignInActivity extends AppCompatActivity {

    static final String PREFS_NAME = "login_prefs";
    static final String KEY_EMAIL = "email";
    static final String KEY_PASSWORD = "password";
    static final String FIREBASE_DB_URL = "https://krag-login-app-default-rtdb.asia-southeast1.firebasedatabase.app/";

    private ActivitySignInBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_in);
        mAuth = FirebaseAuth.getInstance();
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.toggleLoginMethod.setText(R.string.switch_local);
        binding.toggleLoginMethod.setOnCheckedChangeListener((btn, isChecked) ->
                btn.setText(isChecked ? R.string.switch_firebase : R.string.switch_local));

        binding.btnSignIn.setOnClickListener(v -> {
            String email = binding.email.getText().toString().trim();
            String password = binding.password.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            signIn(email, password, binding.toggleLoginMethod.isChecked());
        });

        binding.tvGoToSignUp.setOnClickListener(v ->
                startActivity(new Intent(this, SignUpActivity.class)));
    }

    private void signIn(String email, String password, boolean useFirebase) {
        if (useFirebase) {
            signInWithFirebase(email, password);
        } else {
            signInWithSharedPreferences(email, password);
        }
    }

    private void signInWithFirebase(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, ProfileActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void signInWithSharedPreferences(String email, String password) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedEmail = prefs.getString(KEY_EMAIL, null);
        String savedPassword = prefs.getString(KEY_PASSWORD, null);

        if (savedEmail == null) {
            Toast.makeText(this, R.string.error_no_local_credentials, Toast.LENGTH_LONG).show();
            return;
        }

        if (email.equals(savedEmail) && password.equals(savedPassword)) {
            Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("email", email);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, R.string.error_invalid_credentials, Toast.LENGTH_SHORT).show();
        }
    }
}
