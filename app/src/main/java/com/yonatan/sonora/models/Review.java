package com.yonatan.sonora.models;

import java.io.Serializable;
import java.util.Objects;

/**
 * מחלקה המייצגת ביקורת ודירוג של משתמש על שיר או אלבום.
 * מממשת את הממשק {@link SocialInteractable} ומאפשרת לייקים ותגובות.
 *
 * Review model representing user ratings, text impressions and social interactions.
 */
public class Review implements SocialInteractable, Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String userId;
    private String username;
    private String userAvatar;
    private String musicItemId;
    private String musicItemType;
    private String itemTitle;
    private String itemArtist;
    private String itemCover;
    private double rating; // 0.5 to 5.0
    private String reviewText;
    private long timestamp;
    private int likesCount;
    private int commentsCount;
    private boolean isLiked;

    public Review() {
        this.id = "";
        this.userId = "";
        this.username = "";
        this.userAvatar = "";
        this.musicItemId = "";
        this.musicItemType = "album";
        this.itemTitle = "";
        this.itemArtist = "";
        this.itemCover = "";
        this.rating = 5.0;
        this.reviewText = "";
        this.timestamp = System.currentTimeMillis();
        this.likesCount = 0;
        this.commentsCount = 0;
        this.isLiked = false;
    }

    public Review(String id, String userId, String username, String userAvatar,
                  String musicItemId, String musicItemType, String itemTitle,
                  String itemArtist, String itemCover, double rating,
                  String reviewText, long timestamp) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.userAvatar = userAvatar;
        this.musicItemId = musicItemId;
        this.musicItemType = musicItemType;
        this.itemTitle = itemTitle;
        this.itemArtist = itemArtist;
        this.itemCover = itemCover;
        this.rating = rating;
        this.reviewText = reviewText;
        this.timestamp = timestamp;
        this.likesCount = 0;
        this.commentsCount = 0;
        this.isLiked = false;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public String getMusicItemId() {
        return musicItemId;
    }

    public void setMusicItemId(String musicItemId) {
        this.musicItemId = musicItemId;
    }

    public String getMusicItemType() {
        return musicItemType;
    }

    public void setMusicItemType(String musicItemType) {
        this.musicItemType = musicItemType;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    public String getItemArtist() {
        return itemArtist;
    }

    public void setItemArtist(String itemArtist) {
        this.itemArtist = itemArtist;
    }

    public String getItemCover() {
        return itemCover;
    }

    public void setItemCover(String itemCover) {
        this.itemCover = itemCover;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int getLikesCount() {
        return likesCount;
    }

    @Override
    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    @Override
    public boolean isLiked() {
        return isLiked;
    }

    @Override
    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    @Override
    public int getCommentsCount() {
        return commentsCount;
    }

    @Override
    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Review)) return false;
        Review review = (Review) o;
        return Objects.equals(id, review.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
