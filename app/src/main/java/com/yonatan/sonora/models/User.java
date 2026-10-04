package com.yonatan.sonora.models;

import java.io.Serializable;
import java.util.Objects;

/**
 * מחלקה המייצגת משתמש במערכת SONORA.
 * כוללת פרטים אישיים, נתוני פרופיל וסטטיסטיקות פעילות (עוקבים, נעקבים, כמות האזנות וביקורות).
 *
 * User entity model.
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String username;
    private String email;
    private String passwordHash;
    private String displayName;
    private String bio;
    private String avatarUrl;
    private int followersCount;
    private int followingCount;
    private int loggedCount;
    private int reviewsCount;
    private long joinDateTimestamp;
    private String favoriteIdsJson; // רשימת מזהים של 4 הפריטים האהובים

    public User() {
        this.id = "";
        this.username = "";
        this.email = "";
        this.passwordHash = "";
        this.displayName = "";
        this.bio = "";
        this.avatarUrl = "";
        this.followersCount = 0;
        this.followingCount = 0;
        this.loggedCount = 0;
        this.reviewsCount = 0;
        this.joinDateTimestamp = System.currentTimeMillis();
        this.favoriteIdsJson = "";
    }

    public User(String id, String username, String email, String passwordHash,
                String displayName, String bio, String avatarUrl) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
        this.followersCount = 0;
        this.followingCount = 0;
        this.loggedCount = 0;
        this.reviewsCount = 0;
        this.joinDateTimestamp = System.currentTimeMillis();
        this.favoriteIdsJson = "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getDisplayName() {
        return (displayName == null || displayName.trim().isEmpty()) ? username : displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }

    public int getLoggedCount() {
        return loggedCount;
    }

    public void setLoggedCount(int loggedCount) {
        this.loggedCount = loggedCount;
    }

    public int getReviewsCount() {
        return reviewsCount;
    }

    public void setReviewsCount(int reviewsCount) {
        this.reviewsCount = reviewsCount;
    }

    public long getJoinDateTimestamp() {
        return joinDateTimestamp;
    }

    public void setJoinDateTimestamp(long joinDateTimestamp) {
        this.joinDateTimestamp = joinDateTimestamp;
    }

    public String getFavoriteIdsJson() {
        return favoriteIdsJson;
    }

    public void setFavoriteIdsJson(String favoriteIdsJson) {
        this.favoriteIdsJson = favoriteIdsJson;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
