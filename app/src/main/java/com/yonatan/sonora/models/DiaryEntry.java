package com.yonatan.sonora.models;

import java.io.Serializable;
import java.util.Objects;

/**
 * מחלקה המייצגת רשומת יומן מוזיקה (Music Diary / Log).
 * מתעדת כל האזנה לשיר או אלבום עם תאריך, דירוג, האם מדובר בהאזנה חוזרת והערות.
 *
 * Diary entry model representing listening history logs.
 */
public class DiaryEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String userId;
    private String musicItemId;
    private String musicItemType;
    private String itemTitle;
    private String itemArtist;
    private String itemCover;
    private double rating;
    private long listenedTimestamp;
    private String notes;
    private boolean isReListen;

    public DiaryEntry() {
        this.id = "";
        this.userId = "";
        this.musicItemId = "";
        this.musicItemType = "album";
        this.itemTitle = "";
        this.itemArtist = "";
        this.itemCover = "";
        this.rating = 5.0;
        this.listenedTimestamp = System.currentTimeMillis();
        this.notes = "";
        this.isReListen = false;
    }

    public DiaryEntry(String id, String userId, String musicItemId,
                      String musicItemType, String itemTitle, String itemArtist,
                      String itemCover, double rating, long listenedTimestamp,
                      String notes, boolean isReListen) {
        this.id = id;
        this.userId = userId;
        this.musicItemId = musicItemId;
        this.musicItemType = musicItemType;
        this.itemTitle = itemTitle;
        this.itemArtist = itemArtist;
        this.itemCover = itemCover;
        this.rating = rating;
        this.listenedTimestamp = listenedTimestamp;
        this.notes = notes;
        this.isReListen = isReListen;
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

    public long getListenedTimestamp() {
        return listenedTimestamp;
    }

    public void setListenedTimestamp(long listenedTimestamp) {
        this.listenedTimestamp = listenedTimestamp;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isReListen() {
        return isReListen;
    }

    public void setReListen(boolean reListen) {
        isReListen = reListen;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DiaryEntry)) return false;
        DiaryEntry that = (DiaryEntry) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
