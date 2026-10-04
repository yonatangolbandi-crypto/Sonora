package com.yonatan.sonora.models;

/**
 * ממשק כללי (Generic) להאזנה ללחיצה על פריטים ב-RecyclerView.
 * Generic item click listener callback.
 *
 * @param <T> סוג הפריט שנלחץ
 */
public interface OnItemClickListener<T> {
    void onItemClick(T item, int position);
}
