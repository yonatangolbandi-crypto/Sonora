package com.yonatan.sonora.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yonatan.sonora.R;
import com.yonatan.sonora.adapters.ReviewAdapter;
import com.yonatan.sonora.database.SonoraRepository;
import com.yonatan.sonora.models.MusicItem;
import com.yonatan.sonora.models.MusicList;
import com.yonatan.sonora.models.Review;
import com.yonatan.sonora.utils.AudioPreviewPlayer;
import com.yonatan.sonora.utils.FormatUtils;
import com.yonatan.sonora.utils.ImageLoader;
import com.yonatan.sonora.utils.SessionManager;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * מסך פרטי פריט מוזיקלי (Music Item Detail Activity).
 * מציג תמונת עטיפה ברזולוציה גבוהה, מטא-דאטה, תרשים התפלגות דירוגים קהילתי (Histogram),
 * נגן תצוגה מקדימה (30s Audio Preview), אפשרות הוספה לרשימה, כתיבת ביקורת ודירוג כוכבים.
 *
 * Detailed view for Song, Album, or Artist.
 */
public class MusicDetailActivity extends AppCompatActivity {

    private MusicItem musicItem;

    private ImageButton btnBack, btnShare;
    private TextView tvHeaderTitle, tvTitle, tvArtist, tvMeta;
    private ImageView imgCover;
    private Button btnRateReview, btnPlayPreview, btnAddToList;
    private TextView tvAvgRatingBig, tvStars, tvRatingsCount;
    private ProgressBar bar5Star, bar4Star, bar3Star;
    private RecyclerView rvItemReviews;

    private ReviewAdapter reviewAdapter;
    private SonoraRepository repository;
    private SessionManager sessionManager;
    private AudioPreviewPlayer audioPlayer;

    private double selectedRating = 5.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_detail);

        repository = SonoraRepository.getInstance(this);
        sessionManager = SessionManager.getInstance(this);
        audioPlayer = AudioPreviewPlayer.getInstance();

        musicItem = (MusicItem) getIntent().getSerializableExtra("music_item");
        if (musicItem == null) {
            Toast.makeText(this, "שגיאה בטעינת פרטי הפריט", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        populateData();
        setupListeners();
        loadRatingsAndReviews();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnDetailBack);
        btnShare = findViewById(R.id.btnDetailShare);
        tvHeaderTitle = findViewById(R.id.tvDetailHeaderTitle);
        tvTitle = findViewById(R.id.tvDetailTitle);
        tvArtist = findViewById(R.id.tvDetailArtist);
        tvMeta = findViewById(R.id.tvDetailMeta);
        imgCover = findViewById(R.id.imgDetailCover);

        btnRateReview = findViewById(R.id.btnRateAndReview);
        btnPlayPreview = findViewById(R.id.btnPlayTrackPreview);
        btnAddToList = findViewById(R.id.btnAddToList);

        tvAvgRatingBig = findViewById(R.id.tvAverageRatingBig);
        tvStars = findViewById(R.id.tvRatingStars);
        tvRatingsCount = findViewById(R.id.tvTotalRatingsCount);

        bar5Star = findViewById(R.id.bar5Star);
        bar4Star = findViewById(R.id.bar4Star);
        bar3Star = findViewById(R.id.bar3Star);

        rvItemReviews = findViewById(R.id.rvItemReviews);
        rvItemReviews.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewAdapter();
        rvItemReviews.setAdapter(reviewAdapter);
    }

    private void populateData() {
        tvHeaderTitle.setText(musicItem.getTitle());
        tvTitle.setText(musicItem.getTitle());
        tvArtist.setText(musicItem.getArtist());
        tvMeta.setText(musicItem.getDetailedSubtitle());

        ImageLoader.getInstance().loadImage(musicItem.getCoverUrl(), imgCover, R.drawable.ic_album);

        updateRatingDisplay(musicItem.getAverageRating(), musicItem.getRatingsCount());

        if (musicItem.getPreviewUrl() != null && !musicItem.getPreviewUrl().isEmpty()) {
            btnPlayPreview.setVisibility(View.VISIBLE);
        } else {
            btnPlayPreview.setVisibility(View.GONE);
        }
    }

    private void updateRatingDisplay(double avg, int count) {
        if (count > 0) {
            tvAvgRatingBig.setText(String.format(Locale.US, "%.1f", avg));
            tvStars.setText(FormatUtils.getStarsString(avg));
            tvRatingsCount.setText(count + " דירוגים בקהילה");
        } else {
            tvAvgRatingBig.setText("—");
            tvStars.setText("☆☆☆☆☆");
            tvRatingsCount.setText("טרם דורג");
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnShare.setOnClickListener(v -> {
            Toast.makeText(this, "הקישור לפריט הועתק לשיתוף!", Toast.LENGTH_SHORT).show();
        });

        btnPlayPreview.setOnClickListener(v -> {
            if (audioPlayer.isPlaying() && audioPlayer.getCurrentPlayingUrl().equals(musicItem.getPreviewUrl())) {
                audioPlayer.stop();
                btnPlayPreview.setText("השמעה מוקדמת ▶");
            } else {
                audioPlayer.playOrPause(musicItem.getPreviewUrl(), musicItem.getTitle());
                btnPlayPreview.setText("עצור השמעה ⏹");
            }
        });

        btnRateReview.setOnClickListener(v -> showWriteReviewDialog());
        btnAddToList.setOnClickListener(v -> showAddToListDialog());
    }

    private void loadRatingsAndReviews() {
        String currentUserId = sessionManager.getCurrentUserId();

        // טעינת ביקורות
        repository.getReviewsForItem(musicItem.getId(), currentUserId, reviews -> {
            reviewAdapter.setReviews(reviews);
        });

        // טעינת התפלגות דירוגים (שאילתת GROUP BY)
        repository.getRatingDistribution(musicItem.getId(), dist -> {
            int c5 = dist.getOrDefault(5, 0);
            int c4 = dist.getOrDefault(4, 0);
            int c3 = dist.getOrDefault(3, 0);
            int total = c5 + c4 + c3 + dist.getOrDefault(2, 0) + dist.getOrDefault(1, 0);

            if (total > 0) {
                bar5Star.setProgress((c5 * 100) / total);
                bar4Star.setProgress((c4 * 100) / total);
                bar3Star.setProgress((c3 * 100) / total);
            }
        });
    }

    private void showWriteReviewDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_write_review, null);
        TextView title = dialogView.findViewById(R.id.dialogItemTitle);
        TextView artist = dialogView.findViewById(R.id.dialogItemArtist);
        ImageView cover = dialogView.findViewById(R.id.dialogCover);
        TextView tvNumeric = dialogView.findViewById(R.id.tvRatingNumeric);
        EditText etReview = dialogView.findViewById(R.id.etReviewText);
        CheckBox cbRelisten = dialogView.findViewById(R.id.cbRelisten);

        ImageView s1 = dialogView.findViewById(R.id.star1);
        ImageView s2 = dialogView.findViewById(R.id.star2);
        ImageView s3 = dialogView.findViewById(R.id.star3);
        ImageView s4 = dialogView.findViewById(R.id.star4);
        ImageView s5 = dialogView.findViewById(R.id.star5);

        title.setText(musicItem.getTitle());
        artist.setText(musicItem.getArtist());
        ImageLoader.getInstance().loadImage(musicItem.getCoverUrl(), cover, R.drawable.ic_album);

        selectedRating = 5.0;
        updateStarIcons(new ImageView[]{s1, s2, s3, s4, s5}, tvNumeric, 5.0);

        s1.setOnClickListener(v -> updateStarIcons(new ImageView[]{s1, s2, s3, s4, s5}, tvNumeric, 1.0));
        s2.setOnClickListener(v -> updateStarIcons(new ImageView[]{s1, s2, s3, s4, s5}, tvNumeric, 2.0));
        s3.setOnClickListener(v -> updateStarIcons(new ImageView[]{s1, s2, s3, s4, s5}, tvNumeric, 3.0));
        s4.setOnClickListener(v -> updateStarIcons(new ImageView[]{s1, s2, s3, s4, s5}, tvNumeric, 4.0));
        s5.setOnClickListener(v -> updateStarIcons(new ImageView[]{s1, s2, s3, s4, s5}, tvNumeric, 5.0));

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.Base_Theme_Sonora)
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btnCancelReview).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnSubmitReview).setOnClickListener(v -> {
            String reviewText = etReview.getText().toString().trim();
            if (reviewText.isEmpty()) {
                reviewText = "דירוג בלבד ללא פירוט טקסטואלי.";
            }

            Review review = new Review(
                    UUID.randomUUID().toString(),
                    sessionManager.getCurrentUserId(),
                    sessionManager.getCurrentUsername(),
                    sessionManager.getCurrentAvatarUrl(),
                    musicItem.getId(),
                    musicItem.getItemType(),
                    musicItem.getTitle(),
                    musicItem.getArtist(),
                    musicItem.getCoverUrl(),
                    selectedRating,
                    reviewText,
                    System.currentTimeMillis()
            );

            repository.addReview(review, success -> {
                if (success) {
                    Toast.makeText(this, "הביקורת פורסמה ונוספה ליומן ההאזנות שלך!", Toast.LENGTH_SHORT).show();
                    loadRatingsAndReviews();
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "שגיאה בשמירת הביקורת", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void updateStarIcons(ImageView[] stars, TextView tvNumeric, double rating) {
        selectedRating = rating;
        tvNumeric.setText(String.format(Locale.US, "%.1f", rating));
        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                stars[i].setImageResource(R.drawable.ic_star_full);
            } else {
                stars[i].setImageResource(R.drawable.ic_star_empty);
            }
        }
    }

    private void showAddToListDialog() {
        String userId = sessionManager.getCurrentUserId();
        repository.getUserLists(userId, lists -> {
            if (lists.isEmpty()) {
                Toast.makeText(this, "אין לך עדיין רשימות. צור רשימה בעמוד הפרופיל!", Toast.LENGTH_LONG).show();
                return;
            }

            String[] titles = new String[lists.size()];
            for (int i = 0; i < lists.size(); i++) {
                titles[i] = lists.get(i).getTitle();
            }

            new AlertDialog.Builder(this, R.style.Base_Theme_Sonora)
                    .setTitle("בחר רשימה להוספה:")
                    .setItems(titles, (dialog, which) -> {
                        MusicList chosen = lists.get(which);
                        repository.addItemToList(chosen.getId(), musicItem.getId(), success -> {
                            if (success) {
                                Toast.makeText(this, "הפריט נוסף בהצלחה לרשימה " + chosen.getTitle(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioPlayer != null && audioPlayer.isPlaying()) {
            audioPlayer.stop();
        }
    }
}
