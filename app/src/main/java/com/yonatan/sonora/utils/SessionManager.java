package com.yonatan.sonora.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.yonatan.sonora.models.User;

/**
 * מנהל הפעלת המשתמש (Session Manager) באמצעות {@link SharedPreferences}.
 * עונה על סעיף 10.2 בדרישות הבגרות (SharedPreferences).
 * שומר את פרטי המשתמש המחובר, הגדרות מערכת ומצב אימות.
 *
 * Session manager storing user preferences and active authentication state.
 */
public class SessionManager {

    private static final String PREF_NAME = "sonora_session_pref";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_DISPLAY_NAME = "display_name";
    private static final String KEY_AVATAR_URL = "avatar_url";
    private static final String KEY_BIO = "bio";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";

    private static SessionManager instance;
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
        return instance;
    }

    private SessionManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = prefs.edit();
    }

    public void saveUserLogin(User user) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, user.getId());
        editor.putString(KEY_USERNAME, user.getUsername());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putString(KEY_DISPLAY_NAME, user.getDisplayName());
        editor.putString(KEY_AVATAR_URL, user.getAvatarUrl());
        editor.putString(KEY_BIO, user.getBio());
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getCurrentUserId() {
        return prefs.getString(KEY_USER_ID, "user_demo");
    }

    public String getCurrentUsername() {
        return prefs.getString(KEY_USERNAME, "yonatan");
    }

    public String getCurrentDisplayName() {
        return prefs.getString(KEY_DISPLAY_NAME, "יונתן כהן");
    }

    public String getCurrentAvatarUrl() {
        return prefs.getString(KEY_AVATAR_URL, "");
    }

    public void setAvatarUrl(String url) {
        editor.putString(KEY_AVATAR_URL, url).apply();
    }

    public String getCurrentBio() {
        return prefs.getString(KEY_BIO, "");
    }

    public void setBio(String bio) {
        editor.putString(KEY_BIO, bio).apply();
    }

    public boolean isNotificationsEnabled() {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
    }

    public void setNotificationsEnabled(boolean enabled) {
        editor.putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply();
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
