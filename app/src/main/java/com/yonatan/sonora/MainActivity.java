package com.yonatan.sonora;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.yonatan.sonora.activities.NotificationsActivity;
import com.yonatan.sonora.database.SonoraRepository;
import com.yonatan.sonora.fragments.AIRecommendationsFragment;
import com.yonatan.sonora.fragments.DiaryFragment;
import com.yonatan.sonora.fragments.DiscoverFragment;
import com.yonatan.sonora.fragments.FeedFragment;
import com.yonatan.sonora.fragments.ProfileFragment;
import com.yonatan.sonora.services.SonoraSyncService;
import com.yonatan.sonora.utils.SessionManager;

/**
 * הפעילות המרכזית (Main Activity) של אפליקציית SONORA.
 * מארחת את ה-{@link BottomNavigationView} ומנהלת את הניווט בין 5 הפרגמנטים:
 * פיד (Feed), גילוי (Discover), המלצות בינה מלאכותית (AI), יומן (Diary) ופרופיל (Profile).
 *
 * מדגימה עמידה בדרישות הבגרות: אבני יסוד Activity/Intent, שימוש ב-Fragments,
 * הפעלת שירות רקע {@link SonoraSyncService} וניהול מרכז התראות.
 *
 * Main container activity with bottom navigation bar and fragment manager.
 */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private FrameLayout btnNotifications;
    private View badgeNotificationDot;

    private final Fragment feedFragment = new FeedFragment();
    private final Fragment discoverFragment = new DiscoverFragment();
    private final Fragment aiFragment = new AIRecommendationsFragment();
    private final Fragment diaryFragment = new DiaryFragment();
    private final Fragment profileFragment = new ProfileFragment();

    private Fragment activeFragment = feedFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupNavigation();
        startBackgroundSyncService();
    }

    private void initViews() {
        bottomNav = findViewById(R.id.bottom_navigation);
        btnNotifications = findViewById(R.id.btnNotifications);
        badgeNotificationDot = findViewById(R.id.badgeNotificationDot);

        btnNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NotificationsActivity.class);
            startActivity(intent);
        });
    }

    private void setupNavigation() {
        // טעינת פרגמנט ראשוני
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, profileFragment, "profile").hide(profileFragment)
                .add(R.id.fragment_container, diaryFragment, "diary").hide(diaryFragment)
                .add(R.id.fragment_container, aiFragment, "ai").hide(aiFragment)
                .add(R.id.fragment_container, discoverFragment, "discover").hide(discoverFragment)
                .add(R.id.fragment_container, feedFragment, "feed")
                .commit();

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_feed) {
                switchFragment(feedFragment);
                return true;
            } else if (id == R.id.nav_discover) {
                switchFragment(discoverFragment);
                return true;
            } else if (id == R.id.nav_ai) {
                switchFragment(aiFragment);
                return true;
            } else if (id == R.id.nav_diary) {
                switchFragment(diaryFragment);
                return true;
            } else if (id == R.id.nav_profile) {
                switchFragment(profileFragment);
                return true;
            }
            return false;
        });
    }

    private void switchFragment(Fragment target) {
        if (activeFragment != target) {
            getSupportFragmentManager().beginTransaction()
                    .hide(activeFragment)
                    .show(target)
                    .commit();
            activeFragment = target;
        }
    }

    private void startBackgroundSyncService() {
        // הפעלת שירות הרקע SonoraSyncService (עונה על סעיף 6.3 במחוון הבגרות)
        try {
            Intent serviceIntent = new Intent(this, SonoraSyncService.class);
            serviceIntent.setAction(SonoraSyncService.ACTION_SYNC_NOW);
            startService(serviceIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUnreadNotifications();
    }

    private void checkUnreadNotifications() {
        String userId = SessionManager.getInstance(this).getCurrentUserId();
        SonoraRepository.getInstance(this).getUserNotifications(userId, list -> {
            boolean hasUnread = false;
            if (list != null) {
                for (var item : list) {
                    if (!item.isRead()) {
                        hasUnread = true;
                        break;
                    }
                }
            }
            badgeNotificationDot.setVisibility(hasUnread ? View.VISIBLE : View.GONE);
        });
    }
}