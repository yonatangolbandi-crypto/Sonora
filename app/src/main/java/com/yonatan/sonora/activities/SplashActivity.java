package com.yonatan.sonora.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.yonatan.sonora.MainActivity;
import com.yonatan.sonora.R;
import com.yonatan.sonora.utils.SessionManager;

/**
 * מסך פתיחה מונפש (Splash Screen).
 * בודק את מצב ההתחברות של המשתמש ומנתב למסך הראשי או למסך האימות (AuthActivity).
 *
 * Splash activity checking session state and navigating accordingly.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SessionManager session = SessionManager.getInstance(this);
            Intent nextIntent;
            if (session.isLoggedIn()) {
                nextIntent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                nextIntent = new Intent(SplashActivity.this, AuthActivity.class);
            }
            startActivity(nextIntent);
            finish();
        }, 1200);
    }
}
