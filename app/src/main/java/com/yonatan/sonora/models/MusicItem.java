package com.yonatan.sonora.models;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

/**
 * מחלקת בסיס מופשטת עבור כל פריט מוזיקה באפליקציית SONORA.
 * מממשת את הממשק {@link MusicItemInterface} ומדגימה עקרונות תכנות מונחה עצמים:
 * כימוס (Encapsulation), הפשטה (Abstraction) וירושה (Inheritance).
 *
 * Abstract base class for all music catalog entities.
 */
public abstract class MusicItem implements MusicItemInterface, Serializable, Comparable<MusicItem> {

    private static final long serialVersionUID = 1L;

    protected String id;
    protected String title;
    protected String artist;
    protected String coverUrl;
    protected String itemType; // "song", "album", "artist"
    protected int releaseYear;
    protected String genre;
    protected double averageRating;
    protected int ratingsCount;
    protected String previewUrl;

    /**
     * בנאי ברירת מחדל
     */
    public MusicItem() {
        this.id = "";
        this.title = "";
        this.artist = "";
        this.coverUrl = "";
        this.itemType = "song";
        this.releaseYear = 2024;
        this.genre = "Music";
        this.averageRating = 0.0;
        this.ratingsCount = 0;
        this.previewUrl = "";
    }

    /**
     * בנאי מלא
     *
     * @param id מזהה ייחודי
     * @param title כותרת
     * @param artist שם אמן
     * @param coverUrl קישור לעטיפה
     * @param itemType סוג הפריט
     * @param releaseYear שנת שחרור
     * @param genre ז'אנר
     * @param averageRating דירוג ממוצע
     * @param ratingsCount כמות מדרגים
     */
    public MusicItem(String id, String title, String artist, String coverUrl,
                     String itemType, int releaseYear, String genre,
                     double averageRating, int ratingsCount) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.coverUrl = coverUrl;
        this.itemType = itemType;
        this.releaseYear = releaseYear;
        this.genre = genre;
        this.averageRating = averageRating;
        this.ratingsCount = ratingsCount;
        this.previewUrl = "";
    }

    // --- יישום ממשק MusicItemInterface ---

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    @Override
    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    @Override
    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    @Override
    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    @Override
    public String getGenre() {
        return (genre == null || genre.isEmpty()) ? "Music" : genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    @Override
    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = Math.max(0.0, Math.min(5.0, averageRating));
    }

    @Override
    public int getRatingsCount() {
        return ratingsCount;
    }

    public void setRatingsCount(int ratingsCount) {
        this.ratingsCount = ratingsCount;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    /**
     * החזרת הדירוג הממוצע כמחרוזת מעוצבת עם ספרה אחת אחרי הנקודה (לדוגמה: "4.5")
     */
    public String getFormattedRating() {
        if (ratingsCount == 0 || averageRating == 0.0) {
            return "—";
        }
        return String.format(Locale.US, "%.1f", averageRating);
    }

    /**
     * החזרת תיאור קצר של הפריט (לדוגמה: "Album • 1973 • Rock")
     */
    public abstract String getDetailedSubtitle();

    @Override
    public int compareTo(MusicItem other) {
        if (other == null) return 1;
        // מיון ברירת מחדל לפי דירוג ממוצע יורד
        return Double.compare(other.averageRating, this.averageRating);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MusicItem)) return false;
        MusicItem musicItem = (MusicItem) o;
        return Objects.equals(id, musicItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
