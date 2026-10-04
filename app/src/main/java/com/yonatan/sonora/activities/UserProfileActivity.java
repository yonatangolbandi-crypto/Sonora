package com.yonatan.sonora.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.yonatan.sonora.R;
import com.yonatan.sonora.adapters.ReviewAdapter;
import com.yonatan.sonora.database.SonoraRepository;
import com.yonatan.sonora.models.User;
import com.yonatan.sonora.utils.ImageLoader;
import com.yonatan.sonora.utils.SessionManager;

/**
 * מסך פרופיל של משתמש אחר בקהילה (Community User Profile).
 * מאפשר מעקב (Follow / Unfollow) וצפייה בביקורות שהמשתמש כתב.
 *
 * User profile view with follow interaction.
 */
public class UserProfileActivity extends AppCompatActivity {

    private String targetUserId;
    private User targetUser;

    private ImageButton btnBack;
    private TextView tvHeaderTitle, tvDisplayName, tvUsername, tvBio;
    private ShapeableImageView imgAvatar;
    private Button btnFollow;
    private RecyclerView rvReviews;

    private ReviewAdapter reviewAdapter;
    private SonoraRepository repository;
    private SessionManager sessionManager;
    private boolean isFollowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        repository = SonoraRepository.getInstance(this);
        sessionManager = SessionManager.getInstance(this);

        targetUserId = getIntent().getStringExtra("user_id");
        if (targetUserId == null) {
            finish();
            return;
        }

        initViews();
        setupListeners();
        loadUserData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnUserBack);
        tvHeaderTitle = findViewById(R.id.tvUserHeaderTitle);
        tvDisplayName = findViewById(R.id.tvOtherDisplayName);
        tvUsername = findViewById(R.id.tvOtherUsername);
        tvBio = findViewById(R.id.tvOtherBio);
        imgAvatar = findViewById(R.id.imgOtherAvatar);
        btnFollow = findViewById(R.id.btnFollowToggle);

        rvReviews = findViewById(R.id.rvOtherUserReviews);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewAdapter();
        rvReviews.setAdapter(reviewAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnFollow.setOnClickListener(v -> {
            String currentUserId = sessionManager.getCurrentUserId();
            repository.toggleFollow(currentUserId, targetUserId, nowFollowing -> {
                isFollowing = nowFollowing;
                updateFollowButton();
                Toast.makeText(this, nowFollowing ? "אתה עוקב כעת אחרי " + targetUser.getDisplayName() : "הוסר המעקב", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void loadUserData() {
        repository.getUserById(targetUserId, user -> {
            if (user != null) {
                targetUser = user;
                tvHeaderTitle.setText(user.getDisplayName());
                tvDisplayName.setText(user.getDisplayName());
                tvUsername.setText("@" + user.getUsername());
                tvBio.setText(user.getBio());

                ImageLoader.getInstance().loadImage(user.getAvatarUrl(), imgAvatar, R.drawable.ic_profile);

                checkFollowStatus();
                loadUserReviews();
            }
        });
    }

    private void checkFollowStatus() {
        String currentUserId = sessionManager.getCurrentUserId();
        repository.isFollowing(currentUserId, targetUserId, following -> {
            isFollowing = following;
            updateFollowButton();
        });
    }

    private void updateFollowButton() {
        if (isFollowing) {
            btnFollow.setText(R.string.following);
            btnFollow.setBackgroundTintList(getColorStateList(R.color.bg_card_elevated));
            btnFollow.setTextColor(getColor(R.color.text_primary));
        } else {
            btnFollow.setText(R.string.follow);
            btnFollow.setBackgroundTintList(getColorStateList(R.color.color_primary));
            btnFollow.setTextColor(getColor(R.color.text_dark));
        }
    }

    private void loadUserReviews() {
        String currentUserId = sessionManager.getCurrentUserId();
        repository.getReviewsByUser(targetUserId, currentUserId, reviews -> {
            reviewAdapter.setReviews(reviews);
        });
    }
}
