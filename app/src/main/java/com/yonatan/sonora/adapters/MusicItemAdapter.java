package com.yonatan.sonora.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yonatan.sonora.R;
import com.yonatan.sonora.models.Album;
import com.yonatan.sonora.models.Artist;
import com.yonatan.sonora.models.MusicItem;
import com.yonatan.sonora.models.OnItemClickListener;
import com.yonatan.sonora.models.Song;
import com.yonatan.sonora.utils.AudioPreviewPlayer;
import com.yonatan.sonora.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * מתאם RecyclerView עבור פריטי מוזיקה (שירים, אלבומים, אמנים).
 * תומך בשני מצבי תצוגה: רשת (Grid) ושורות (Linear).
 * מדגים שימוש ב-RecyclerView, ViewHolders, טעינת תמונות א-סינכרונית ואירועי לחיצה.
 *
 * RecyclerView adapter supporting Grid and Linear presentation of MusicItem subclasses.
 */
public class MusicItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_GRID = 1;
    public static final int VIEW_TYPE_LINEAR = 2;

    private final List<MusicItem> items = new ArrayList<>();
    private final int viewType;
    private OnItemClickListener<MusicItem> clickListener;

    public MusicItemAdapter(int viewType) {
        this.viewType = viewType;
    }

    public void setItems(List<MusicItem> newItems) {
        this.items.clear();
        if (newItems != null) {
            this.items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener<MusicItem> listener) {
        this.clickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return viewType;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_GRID) {
            View view = inflater.inflate(R.layout.item_music_grid, parent, false);
            return new GridViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_music_linear, parent, false);
            return new LinearViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MusicItem item = items.get(position);
        if (holder instanceof GridViewHolder) {
            ((GridViewHolder) holder).bind(item);
        } else if (holder instanceof LinearViewHolder) {
            ((LinearViewHolder) holder).bind(item);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // --- Grid ViewHolder ---
    class GridViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgCover;
        private final TextView tvRatingBadge;
        private final TextView tvTitle;
        private final TextView tvArtist;
        private final TextView tvTypeYear;

        public GridViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.imgCover);
            tvRatingBadge = itemView.findViewById(R.id.tvRatingBadge);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvArtist = itemView.findViewById(R.id.tvArtist);
            tvTypeYear = itemView.findViewById(R.id.tvTypeYear);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onItemClick(items.get(pos), pos);
                }
            });
        }

        public void bind(MusicItem item) {
            tvTitle.setText(item.getTitle());
            tvArtist.setText(item.getArtist());
            tvTypeYear.setText(item.getReleaseYear() + " • " + item.getItemType());

            if (item.getAverageRating() > 0) {
                tvRatingBadge.setVisibility(View.VISIBLE);
                tvRatingBadge.setText("★ " + item.getFormattedRating());
            } else {
                tvRatingBadge.setVisibility(View.GONE);
            }

            ImageLoader.getInstance().loadImage(item.getCoverUrl(), imgCover, R.drawable.ic_album);
        }
    }

    // --- Linear ViewHolder ---
    class LinearViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgArtwork;
        private final TextView tvTitle;
        private final TextView tvArtist;
        private final TextView tvSubtitle;
        private final TextView tvRating;
        private final ImageButton btnPlay;

        public LinearViewHolder(@NonNull View itemView) {
            super(itemView);
            imgArtwork = itemView.findViewById(R.id.imgItemArtwork);
            tvTitle = itemView.findViewById(R.id.tvItemTitle);
            tvArtist = itemView.findViewById(R.id.tvItemArtist);
            tvSubtitle = itemView.findViewById(R.id.tvItemSubtitle);
            tvRating = itemView.findViewById(R.id.tvItemRating);
            btnPlay = itemView.findViewById(R.id.btnPlayPreview);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onItemClick(items.get(pos), pos);
                }
            });
        }

        public void bind(MusicItem item) {
            tvTitle.setText(item.getTitle());
            tvArtist.setText(item.getArtist());
            tvSubtitle.setText(item.getDetailedSubtitle());

            if (item.getAverageRating() > 0) {
                tvRating.setVisibility(View.VISIBLE);
                tvRating.setText("★ " + item.getFormattedRating());
            } else {
                tvRating.setVisibility(View.GONE);
            }

            ImageLoader.getInstance().loadImage(item.getCoverUrl(), imgArtwork, R.drawable.ic_album);

            if (item.getPreviewUrl() != null && !item.getPreviewUrl().isEmpty()) {
                btnPlay.setVisibility(View.VISIBLE);
                btnPlay.setOnClickListener(v -> {
                    AudioPreviewPlayer player = AudioPreviewPlayer.getInstance();
                    player.playOrPause(item.getPreviewUrl(), item.getTitle());
                    btnPlay.setImageResource(player.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
                });
            } else {
                btnPlay.setVisibility(View.GONE);
            }
        }
    }
}
