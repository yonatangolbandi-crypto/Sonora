package com.yonatan.sonora.activities;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yonatan.sonora.R;
import com.yonatan.sonora.adapters.NotificationAdapter;
import com.yonatan.sonora.database.SonoraRepository;
import com.yonatan.sonora.utils.SessionManager;

/**
 * מסך מרכז התראות (Notifications Activity).
 * מציג עדכונים בזמן אמת, לייקים, מעקבים והמלצות AI שנשלחו למשתמש.
 *
 * Notifications center activity.
 */
public class NotificationsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private SonoraRepository repository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        repository = SonoraRepository.getInstance(this);
        sessionManager = SessionManager.getInstance(this);

        btnBack = findViewById(R.id.btnNotifBack);
        rvNotifications = findViewById(R.id.rvNotifications);

        btnBack.setOnClickListener(v -> finish());

        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter();
        rvNotifications.setAdapter(adapter);

        loadNotifications();
    }

    private void loadNotifications() {
        String userId = sessionManager.getCurrentUserId();
        repository.getUserNotifications(userId, list -> {
            adapter.setNotifications(list);
            repository.markNotificationsAsRead(userId);
        });
    }
}
