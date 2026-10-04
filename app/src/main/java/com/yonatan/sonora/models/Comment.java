package com.yonatan.sonora.models;

import java.io.Serializable;
import java.util.Objects;

/**
 * מחלקה המייצגת תגובה שנכתבה לביקורת.
 * Comment on a review.
 */
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String reviewId;
    private String userId;
    private String username;
    private String userAvatar;
    private String commentText;
    private long timestamp;

    public Comment() {
        this.id = "";
        this.reviewId = "";
        this.userId = "";
        this.username = "";
        this.userAvatar = "";
        this.commentText = "";
        this.timestamp = System.currentTimeMillis();
    }

    public Comment(String id, String reviewId, String userId, String username,
                   String userAvatar, String commentText, long timestamp) {
        this.id = id;
        this.reviewId = reviewId;
        this.userId = userId;
        this.username = username;
        this.userAvatar = userAvatar;
        this.commentText = commentText;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
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

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Comment)) return false;
        Comment comment = (Comment) o;
        return Objects.equals(id, comment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
