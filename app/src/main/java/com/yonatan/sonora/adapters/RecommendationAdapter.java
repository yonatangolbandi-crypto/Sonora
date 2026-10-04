package com.yonatan.sonora.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yonatan.sonora.R;
import com.yonatan.sonora.models.MusicItem;
import com.yonatan.sonora.models.OnItemClickListener;
import com.yonatan.sonora.models.Recommendation;
import com.yonatan.sonora.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * מתאם RecyclerView עבור תוצאות מנוע ההמלצות Sonora AI.
 * מציג תג התאמה באחוזים, נימוק אלגוריתמי ותמונת עטיפה.
 *
 * Recommendation adapter with match scores and AI reasoning explanations.
 */
public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.RecommendationViewHolder> {

    private final List<Recommendation> recommendations = new ArrayList<>();
    private OnItemClickListener<Recommendation> clickListener;

    public void setRecommendations(List<Recommendation> newRecommendations) {
        this.recommendations.clear();
        if (newRecommendations != null) {
            this.recommendations.addAll(newRecommendations);
        }
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener<Recommendation> listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public RecommendationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommendation, parent, false);
        return new RecommendationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecommendationViewHolder holder, int position) {
        holder.bind(recommendations.get(position));
    }

    @Override
    public int getItemCount() {
        return recommendations.size();
    }

    class RecommendationViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgCover;
        private final TextView tvTitle;
        private final TextView tvArtist;
        private final TextView tvGenre;
        private final TextView tvMatchBadge;
        private final TextView tvReasoning;

        public RecommendationViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.imgRecCover);
            tvTitle = itemView.findViewById(R.id.tvRecTitle);
            tvArtist = itemView.findViewById(R.id.tvRecArtist);
            tvGenre = itemView.findViewById(R.id.tvRecGenre);
            tvMatchBadge = itemView.findViewById(R.id.tvMatchBadge);
            tvReasoning = itemView.findViewById(R.id.tvAiReasoning);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onItemClick(recommendations.get(pos), pos);
                }
            });
        }

        public void bind(Recommendation rec) {
            MusicItem item = rec.getMusicItem();
            if (item != null) {
                tvTitle.setText(item.getTitle());
                tvArtist.setText(item.getArtist());
                tvGenre.setText(item.getDetailedSubtitle());
                ImageLoader.getInstance().loadImage(item.getCoverUrl(), imgCover, R.drawable.ic_album);
            }

            tvMatchBadge.setText(rec.getMatchPercentage() + "% התאמה");
            tvReasoning.setText(rec.getAiReasoning());
        }
    }
}
