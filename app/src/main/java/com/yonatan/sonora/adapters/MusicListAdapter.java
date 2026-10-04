package com.yonatan.sonora.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yonatan.sonora.R;
import com.yonatan.sonora.models.MusicList;
import com.yonatan.sonora.models.OnItemClickListener;
import com.yonatan.sonora.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * מתאם RecyclerView עבור רשימות מוזיקה (Curated Music Lists).
 * מציג כותרת רשימה, יוצר, כמות פריטים ותיאור.
 *
 * Adapter for user created music lists.
 */
public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.ListViewHolder> {

    private final List<MusicList> lists = new ArrayList<>();
    private OnItemClickListener<MusicList> clickListener;

    public void setLists(List<MusicList> newLists) {
        this.lists.clear();
        if (newLists != null) {
            this.lists.addAll(newLists);
        }
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener<MusicList> listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_music_list, parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        holder.bind(lists.get(position));
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    class ListViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgCover;
        private final TextView tvTitle;
        private final TextView tvAuthor;
        private final TextView tvDescription;
        private final TextView tvCount;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.imgListCover);
            tvTitle = itemView.findViewById(R.id.tvListTitle);
            tvAuthor = itemView.findViewById(R.id.tvListAuthor);
            tvDescription = itemView.findViewById(R.id.tvListDescription);
            tvCount = itemView.findViewById(R.id.tvListItemCount);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onItemClick(lists.get(pos), pos);
                }
            });
        }

        public void bind(MusicList item) {
            tvTitle.setText(item.getTitle());
            tvAuthor.setText("נוצר על ידי " + item.getUsername());

            if (item.getDescription() != null && !item.getDescription().isEmpty()) {
                tvDescription.setVisibility(View.VISIBLE);
                tvDescription.setText(item.getDescription());
            } else {
                tvDescription.setVisibility(View.GONE);
            }

            tvCount.setText(item.getItemCount() + " פריטים");
            ImageLoader.getInstance().loadImage(item.getCoverUrl(), imgCover, R.drawable.ic_list);
        }
    }
}
