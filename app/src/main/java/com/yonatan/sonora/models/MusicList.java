package com.yonatan.sonora.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * מחלקה המייצגת רשימת מוזיקה מותאמת אישית (Custom / Curated List).
 * מאפשרת למשתמשים ליצור רשימות נושאיות, פומביות או אישיות (כגון: "10 האלבומים של חיי").
 *
 * User curated list model.
 */
public class MusicList implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String userId;
    private String username;
    private String title;
    private String description;
    private boolean isPublic;
    private String coverUrl;
    private int itemCount;
    private String itemIdsString; // מזהים מופרדים בפסיקים
    private long createdAt;

    public MusicList() {
        this.id = "";
        this.userId = "";
        this.username = "";
        this.title = "";
        this.description = "";
        this.isPublic = true;
        this.coverUrl = "";
        this.itemCount = 0;
        this.itemIdsString = "";
        this.createdAt = System.currentTimeMillis();
    }

    public MusicList(String id, String userId, String username, String title,
                     String description, boolean isPublic, String coverUrl) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.title = title;
        this.description = description;
        this.isPublic = isPublic;
        this.coverUrl = coverUrl;
        this.itemCount = 0;
        this.itemIdsString = "";
        this.createdAt = System.currentTimeMillis();
    }

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public String getItemIdsString() {
        return itemIdsString;
    }

    public void setItemIdsString(String itemIdsString) {
        this.itemIdsString = itemIdsString;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getItemIdsList() {
        List<String> list = new ArrayList<>();
        if (itemIdsString != null && !itemIdsString.trim().isEmpty()) {
            String[] split = itemIdsString.split(",");
            for (String s : split) {
                if (!s.trim().isEmpty()) {
                    list.add(s.trim());
                }
            }
        }
        return list;
    }

    public void addItemId(String musicItemId) {
        List<String> current = getItemIdsList();
        if (!current.contains(musicItemId)) {
            current.add(musicItemId);
            this.itemIdsString = String.join(",", current);
            this.itemCount = current.size();
        }
    }

    public void removeItemId(String musicItemId) {
        List<String> current = getItemIdsList();
        if (current.remove(musicItemId)) {
            this.itemIdsString = String.join(",", current);
            this.itemCount = current.size();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MusicList)) return false;
        MusicList musicList = (MusicList) o;
        return Objects.equals(id, musicList.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
