package com.yonatan.sonora.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.yonatan.sonora.MainActivity;
import com.yonatan.sonora.R;
import com.yonatan.sonora.database.SonoraRepository;
import com.yonatan.sonora.models.User;
import com.yonatan.sonora.utils.SessionManager;

import java.util.UUID;

/**
 * מסך הרשמה והתחברות (Authentication Activity).
 * מאפשר מעבר דינמי בין טופס התחברות לטופס הרשמה, אימות שדות,
 * וכניסה מהירה עם משתמש מוגדר מראש עבור נוחות בדיקת הבוחן.
 *
 * Authentication activity for login and registration.
 */
public class AuthActivity extends AppCompatActivity {

    private boolean isLoginMode = true;

    private Button btnTabLogin;
    private Button btnTabRegister;
    private EditText etDisplayName;
    private EditText etEmail;
    private EditText etUsername;
    private EditText etPassword;
    private TextView tvAuthError;
    private Button btnSubmitAuth;
    private Button btnDemoLogin;

    private SonoraRepository repository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        repository = SonoraRepository.getInstance(this);
        sessionManager = SessionManager.getInstance(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnTabLogin = findViewById(R.id.btnTabLogin);
        btnTabRegister = findViewById(R.id.btnTabRegister);
        etDisplayName = findViewById(R.id.etDisplayName);
        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        tvAuthError = findViewById(R.id.tvAuthError);
        btnSubmitAuth = findViewById(R.id.btnSubmitAuth);
        btnDemoLogin = findViewById(R.id.btnDemoLogin);
    }

    private void setupListeners() {
        btnTabLogin.setOnClickListener(v -> setMode(true));
        btnTabRegister.setOnClickListener(v -> setMode(false));

        btnSubmitAuth.setOnClickListener(v -> {
            if (isLoginMode) {
                performLogin();
            } else {
                performRegister();
            }
        });

        // כפתור התחברות מהירה עבור בדיקת הפרויקט
        btnDemoLogin.setOnClickListener(v -> {
            etUsername.setText("yonatan");
            etPassword.setText("123456");
            performLogin();
        });
    }

    private void setMode(boolean login) {
        isLoginMode = login;
        tvAuthError.setVisibility(View.GONE);

        if (login) {
            btnTabLogin.setBackgroundTintList(getColorStateList(R.color.color_primary));
            btnTabLogin.setTextColor(getColor(R.color.text_dark));
            btnTabRegister.setBackgroundTintList(getColorStateList(R.color.transparent));
            btnTabRegister.setTextColor(getColor(R.color.text_secondary));

            etDisplayName.setVisibility(View.GONE);
            etEmail.setVisibility(View.GONE);
            btnSubmitAuth.setText(R.string.login);
        } else {
            btnTabRegister.setBackgroundTintList(getColorStateList(R.color.color_primary));
            btnTabRegister.setTextColor(getColor(R.color.text_dark));
            btnTabLogin.setBackgroundTintList(getColorStateList(R.color.transparent));
            btnTabLogin.setTextColor(getColor(R.color.text_secondary));

            etDisplayName.setVisibility(View.VISIBLE);
            etEmail.setVisibility(View.VISIBLE);
            btnSubmitAuth.setText(R.string.register);
        }
    }

    private void performLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("נא למלא את כל השדות");
            return;
        }

        repository.authenticateUser(username, password, user -> {
            if (user != null) {
                sessionManager.saveUserLogin(user);
                Toast.makeText(this, "ברוך הבא ל-SONORA, " + user.getDisplayName() + "!", Toast.LENGTH_SHORT).show();
                goToMain();
            } else {
                showError("שם משתמש או סיסמה שגויים");
            }
        });
    }

    private void performRegister() {
        String name = etDisplayName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showError("נא למלא את כל שדות ההרשמה");
            return;
        }

        if (password.length() < 4) {
            showError("אורך הסיסמה חייב להיות לפחות 4 תווים");
            return;
        }

        User newUser = new User(
                UUID.randomUUID().toString(),
                username,
                email,
                password,
                name,
                "משתמש חדש בקהילת המוזיקה SONORA",
                ""
        );

        repository.registerUser(newUser, success -> {
            if (success) {
                sessionManager.saveUserLogin(newUser);
                Toast.makeText(this, R.string.register_success, Toast.LENGTH_SHORT).show();
                goToMain();
            } else {
                showError("שם משתמש או אימייל כבר קיימים במערכת");
            }
        });
    }

    private void showError(String msg) {
        tvAuthError.setText(msg);
        tvAuthError.setVisibility(View.VISIBLE);
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
