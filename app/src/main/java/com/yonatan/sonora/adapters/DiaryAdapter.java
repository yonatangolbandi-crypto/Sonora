package com.yonatan.sonora.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yonatan.sonora.R;
import com.yonatan.sonora.models.DiaryEntry;
import com.yonatan.sonora.models.OnItemClickListener;
import com.yonatan.sonora.utils.FormatUtils;
import com.yonatan.sonora.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * מתאם RecyclerView עבור יומן המוזיקה (Music Diary).
 * מציג היסטוריית האזנות כרונולוגית עם תאריכים, דירוג בכוכבים ותג האזנה חוזרת (Re-listen).
 *
 * Diary list adapter for listening log timeline.
 */
public class DiaryAdapter extends RecyclerView.Adapter<DiaryAdapter.DiaryViewHolder> {

    private final List<DiaryEntry> entries = new ArrayList<>();
    private OnItemClickListener<DiaryEntry> clickListener;

    public void setEntries(List<DiaryEntry> newEntries) {
        this.entries.clear();
        if (newEntries != null) {
            this.entries.addAll(newEntries);
        }
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener<DiaryEntry> listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public DiaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_diary, parent, false);
        return new DiaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiaryViewHolder holder, int position) {
        holder.bind(entries.get(position));
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    class DiaryViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgCover;
        private final TextView tvTitle;
        private final TextView tvArtist;
        private final TextView tvNotes;
        private final TextView tvDate;
        private final TextView tvRating;
        private final TextView tvRelisten;

        public DiaryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.imgDiaryCover);
            tvTitle = itemView.findViewById(R.id.tvDiaryTitle);
            tvArtist = itemView.findViewById(R.id.tvDiaryArtist);
            tvNotes = itemView.findViewById(R.id.tvDiaryNotes);
            tvDate = itemView.findViewById(R.id.tvDiaryDate);
            tvRating = itemView.findViewById(R.id.tvDiaryRating);
            tvRelisten = itemView.findViewById(R.id.tvRelistenBadge);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onItemClick(entries.get(pos), pos);
                }
            });
        }

        public void bind(DiaryEntry entry) {
            tvTitle.setText(entry.getItemTitle());
            tvArtist.setText(entry.getItemArtist());

            if (entry.getNotes() != null && !entry.getNotes().isEmpty()) {
                tvNotes.setVisibility(View.VISIBLE);
                tvNotes.setText(entry.getNotes());
            } else {
                tvNotes.setVisibility(View.GONE);
            }

            tvDate.setText(FormatUtils.getRelativeTimeSpan(entry.getListenedTimestamp()));
            tvRating.setText(FormatUtils.getStarsString(entry.getRating()));

            tvRelisten.setVisibility(entry.isReListen() ? View.VISIBLE : View.GONE);

            ImageLoader.getInstance().loadImage(entry.getItemCover(), imgCover, R.drawable.ic_album);
        }
    }
}
