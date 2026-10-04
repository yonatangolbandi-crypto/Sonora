package com.yonatan.sonora.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yonatan.sonora.R;
import com.yonatan.sonora.activities.MusicDetailActivity;
import com.yonatan.sonora.activities.UserProfileActivity;
import com.yonatan.sonora.adapters.MusicItemAdapter;
import com.yonatan.sonora.adapters.ReviewAdapter;
import com.yonatan.sonora.database.SonoraRepository;
import com.yonatan.sonora.models.Comment;
import com.yonatan.sonora.models.MusicItem;
import com.yonatan.sonora.models.Review;
import com.yonatan.sonora.utils.SessionManager;

import java.util.List;

/**
 * פרגמנט הפיד החברתי (Social Feed Fragment).
 * מציג אלבומים פופולריים אופקית (Horizontal Carousel) וביקורות אחרונות של משתמשי הקהילה אנכית.
 * מאפשר לייקים אינטראקטיביים, תגובות, ומעבר לפרופילים ולעמודי מוזיקה.
 *
 * Community feed fragment.
 */
public class FeedFragment extends Fragment implements ReviewAdapter.OnReviewActionListener {

    private RecyclerView rvTrending;
    private RecyclerView rvFeed;
    private TextView tvEmptyFeed;
    private TextView btnRefresh;

    private MusicItemAdapter trendingAdapter;
    private ReviewAdapter reviewAdapter;
    private SonoraRepository repository;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        repository = SonoraRepository.getInstance(requireContext());
        sessionManager = SessionManager.getInstance(requireContext());

        rvTrending = view.findViewById(R.id.rvTrendingHorizontal);
        rvFeed = view.findViewById(R.id.rvFeed);
        tvEmptyFeed = view.findViewById(R.id.tvEmptyFeed);
        btnRefresh = view.findViewById(R.id.btnRefreshFeed);

        setupTrendingRecycler();
        setupFeedRecycler();

        btnRefresh.setOnClickListener(v -> loadData());

        loadData();
        return view;
    }

    private void setupTrendingRecycler() {
        rvTrending.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        trendingAdapter = new MusicItemAdapter(MusicItemAdapter.VIEW_TYPE_GRID);
        trendingAdapter.setOnItemClickListener((item, position) -> openMusicDetail(item));
        rvTrending.setAdapter(trendingAdapter);
    }

    private void setupFeedRecycler() {
        rvFeed.setLayoutManager(new LinearLayoutManager(getContext()));
        reviewAdapter = new ReviewAdapter();
        reviewAdapter.setOnReviewActionListener(this);
        rvFeed.setAdapter(reviewAdapter);
    }

    public void loadData() {
        // טעינת אלבומים טרנדיים
        repository.getTrendingAlbums(albums -> {
            if (isAdded()) {
                trendingAdapter.setItems(albums);
            }
        });

        // טעינת ביקורות הפיד
        String currentUserId = sessionManager.getCurrentUserId();
        repository.getFeedReviews(currentUserId, reviews -> {
            if (isAdded()) {
                reviewAdapter.setReviews(reviews);
                tvEmptyFeed.setVisibility(reviews.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void openMusicDetail(MusicItem item) {
        Intent intent = new Intent(getActivity(), MusicDetailActivity.class);
        intent.putExtra("music_item", item);
        startActivity(intent);
    }

    @Override
    public void onLikeClicked(Review review, int position) {
        String userId = sessionManager.getCurrentUserId();
        repository.toggleReviewLike(userId, review.getId(), nowLiked -> {
            review.setLiked(nowLiked);
            review.setLikesCount(nowLiked ? review.getLikesCount() + 1 : Math.max(0, review.getLikesCount() - 1));
            reviewAdapter.notifyItemChanged(position);
        });
    }

    @Override
    public void onCommentClicked(Review review, int position) {
        showCommentDialog(review, position);
    }

    @Override
    public void onUserClicked(String userId) {
        Intent intent = new Intent(getActivity(), UserProfileActivity.class);
        intent.putExtra("user_id", userId);
        startActivity(intent);
    }

    @Override
    public void onMusicItemClicked(String musicItemId) {
        repository.getMusicItemById(musicItemId, item -> {
            if (item != null) {
                openMusicDetail(item);
            }
        });
    }

    private void showCommentDialog(Review review, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.Base_Theme_Sonora);
        builder.setTitle("תגובה לביקורת של " + review.getUsername());

        final EditText input = new EditText(requireContext());
        input.setHint("כתוב את תגובתך כאן...");
        input.setTextColor(requireContext().getColor(R.color.text_primary));
        input.setHintTextColor(requireContext().getColor(R.color.text_muted));
        builder.setView(input);

        builder.setPositiveButton("שלח", (dialog, which) -> {
            String text = input.getText().toString().trim();
            if (!text.isEmpty()) {
                Comment comment = new Comment(
                        null,
                        review.getId(),
                        sessionManager.getCurrentUserId(),
                        sessionManager.getCurrentUsername(),
                        sessionManager.getCurrentAvatarUrl(),
                        text,
                        System.currentTimeMillis()
                );
                repository.addComment(comment, success -> {
                    if (success) {
                        review.setCommentsCount(review.getCommentsCount() + 1);
                        reviewAdapter.notifyItemChanged(position);
                        Toast.makeText(getContext(), "התגובה נוספה בהצלחה!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }
}
