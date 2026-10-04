package com.yonatan.sonora.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yonatan.sonora.R;
import com.yonatan.sonora.models.NotificationItem;
import com.yonatan.sonora.models.OnItemClickListener;
import com.yonatan.sonora.utils.FormatUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * מתאם RecyclerView עבור מרכז ההתראות (Notifications Center).
 * Notification list adapter.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotifViewHolder> {

    private final List<NotificationItem> list = new ArrayList<>();
    private OnItemClickListener<NotificationItem> clickListener;

    public void setNotifications(List<NotificationItem> items) {
        this.list.clear();
        if (items != null) {
            this.list.addAll(items);
        }
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener<NotificationItem> listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public NotifViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotifViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotifViewHolder holder, int position) {
        holder.bind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class NotifViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgIcon;
        private final TextView tvTitle;
        private final TextView tvMessage;
        private final TextView tvTime;
        private final View viewDot;

        public NotifViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgNotifIcon);
            tvTitle = itemView.findViewById(R.id.tvNotifTitle);
            tvMessage = itemView.findViewById(R.id.tvNotifMessage);
            tvTime = itemView.findViewById(R.id.tvNotifTime);
            viewDot = itemView.findViewById(R.id.viewUnreadDot);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && clickListener != null) {
                    clickListener.onItemClick(list.get(pos), pos);
                }
            });
        }

        public void bind(NotificationItem item) {
            tvTitle.setText(item.getTitle());
            tvMessage.setText(item.getMessage());
            tvTime.setText(FormatUtils.getRelativeTimeSpan(item.getTimestamp()));
            viewDot.setVisibility(item.isRead() ? View.GONE : View.VISIBLE);

            if (NotificationItem.TYPE_AI_REC.equals(item.getType())) {
                imgIcon.setImageResource(R.drawable.ic_ai);
                imgIcon.setColorFilter(itemView.getContext().getColor(R.color.color_ai_glow));
            } else if (NotificationItem.TYPE_LIKE.equals(item.getType())) {
                imgIcon.setImageResource(R.drawable.ic_heart_filled);
                imgIcon.setColorFilter(itemView.getContext().getColor(R.color.color_heart));
            } else {
                imgIcon.setImageResource(R.drawable.ic_bell);
                imgIcon.setColorFilter(itemView.getContext().getColor(R.color.color_primary));
            }
        }
    }
}
