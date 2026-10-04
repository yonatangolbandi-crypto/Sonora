package com.yonatan.sonora.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.yonatan.sonora.R;
import com.yonatan.sonora.models.OnItemClickListener;
import com.yonatan.sonora.models.Review;
import com.yonatan.sonora.utils.FormatUtils;
import com.yonatan.sonora.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * מתאם RecyclerView עבור ביקורות ופעילות חברתית (Reviews Feed).
 * כולל כפתור לייק דינמי, קישור לפרופיל המשתמש, קישור לפריט המוזיקה, וחישוב כוכבים.
 *
 * Review list adapter with interactive likes and comments.
 */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    public interface OnReviewActionListener {
        void onLikeClicked(Review review, int position);
        void onCommentClicked(Review review, int position);
        void onUserClicked(String userId);
        void onMusicItemClicked(String musicItemId);
    }

    private final List<Review> reviews = new ArrayList<>();
    private OnReviewActionListener actionListener;

    public void setReviews(List<Review> newReviews) {
        this.reviews.clear();
        if (newReviews != null) {
            this.reviews.addAll(newReviews);
        }
        notifyDataSetChanged();
    }

    public void setOnReviewActionListener(OnReviewActionListener listener) {
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        holder.bind(reviews.get(position));
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder {
        private final ShapeableImageView imgUserAvatar;
        private final TextView tvUsername;
        private final TextView tvTimestamp;
        private final TextView tvStars;
        private final View layoutTargetItem;
        private final ImageView imgTargetCover;
        private final TextView tvTargetTitle;
        private final TextView tvTargetArtist;
        private final TextView tvReviewContent;
        private final LinearLayout btnLike;
        private final ImageView imgLikeIcon;
        private final TextView tvLikesCount;
        private final LinearLayout btnComment;
        private final TextView tvCommentsCount;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            imgUserAvatar = itemView.findViewById(R.id.imgUserAvatar);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvStars = itemView.findViewById(R.id.tvStars);
            layoutTargetItem = itemView.findViewById(R.id.layoutTargetItem);
            imgTargetCover = itemView.findViewById(R.id.imgTargetCover);
            tvTargetTitle = itemView.findViewById(R.id.tvTargetTitle);
            tvTargetArtist = itemView.findViewById(R.id.tvTargetArtist);
            tvReviewContent = itemView.findViewById(R.id.tvReviewContent);
            btnLike = itemView.findViewById(R.id.btnLikeReview);
            imgLikeIcon = itemView.findViewById(R.id.imgLikeIcon);
            tvLikesCount = itemView.findViewById(R.id.tvLikesCount);
            btnComment = itemView.findViewById(R.id.btnCommentReview);
            tvCommentsCount = itemView.findViewById(R.id.tvCommentsCount);

            // לחיצה על שם/אוואטר המשתמש
            View.OnClickListener userClick = v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && actionListener != null) {
                    actionListener.onUserClicked(reviews.get(pos).getUserId());
                }
            };
            imgUserAvatar.setOnClickListener(userClick);
            tvUsername.setOnClickListener(userClick);

            // לחיצה על האלבום/שיר
            layoutTargetItem.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && actionListener != null) {
                    actionListener.onMusicItemClicked(reviews.get(pos).getMusicItemId());
                }
            });

            // לחיצה על לייק
            btnLike.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && actionListener != null) {
                    Review rev = reviews.get(pos);
                    // אנימציית קפיצה (Bounce animation) ללייק
                    ScaleAnimation scale = new ScaleAnimation(0.7f, 1.2f, 0.7f, 1.2f,
                            ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
                    scale.setDuration(150);
                    imgLikeIcon.startAnimation(scale);

                    actionListener.onLikeClicked(rev, pos);
                }
            });

            // לחיצה על תגובה
            btnComment.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && actionListener != null) {
                    actionListener.onCommentClicked(reviews.get(pos), pos);
                }
            });
        }

        public void bind(Review review) {
            tvUsername.setText(review.getUsername());
            tvTimestamp.setText(FormatUtils.getRelativeTimeSpan(review.getTimestamp()));
            tvStars.setText(FormatUtils.getStarsString(review.getRating()));

            tvTargetTitle.setText(review.getItemTitle());
            tvTargetArtist.setText(review.getItemArtist());
            tvReviewContent.setText(review.getReviewText());

            tvLikesCount.setText(String.valueOf(review.getLikesCount()));
            tvCommentsCount.setText(String.valueOf(review.getCommentsCount()));

            // מצב לייק
            if (review.isLiked()) {
                imgLikeIcon.setImageResource(R.drawable.ic_heart_filled);
                imgLikeIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.color_heart));
                tvLikesCount.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.color_heart));
            } else {
                imgLikeIcon.setImageResource(R.drawable.ic_heart);
                imgLikeIcon.setColorFilter(ContextCompat.getColor(itemView.getContext(), R.color.text_muted));
                tvLikesCount.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_muted));
            }

            ImageLoader.getInstance().loadImage(review.getUserAvatar(), imgUserAvatar, R.drawable.ic_profile);
            ImageLoader.getInstance().loadImage(review.getItemCover(), imgTargetCover, R.drawable.ic_album);
        }
    }
}
