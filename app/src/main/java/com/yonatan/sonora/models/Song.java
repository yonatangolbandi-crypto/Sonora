package com.yonatan.sonora.models;

import java.util.Locale;

/**
 * מחלקה המייצגת שיר (רצועה) ב-SONORA.
 * יורשת מ-{@link MusicItem} ומוסיפה תכונות ייחודיות לשיר:
 * מספר רצועה, משך זמן בשניות, שם האלבום אליו שייך, וקישור ל-Preview שמע.
 *
 * Concrete subclass representing a song / track.
 */
public class Song extends MusicItem {

    private static final long serialVersionUID = 1L;

    private int trackNumber;
    private int durationSeconds;
    private String albumTitle;
    private String albumId;

    public Song() {
        super();
        this.itemType = "song";
        this.trackNumber = 1;
        this.durationSeconds = 180;
        this.albumTitle = "";
        this.albumId = "";
    }

    public Song(String id, String title, String artist, String coverUrl,
                int releaseYear, String genre, double averageRating, int ratingsCount,
                int trackNumber, int durationSeconds, String albumTitle, String previewUrl) {
        super(id, title, artist, coverUrl, "song", releaseYear, genre, averageRating, ratingsCount);
        this.trackNumber = trackNumber;
        this.durationSeconds = durationSeconds;
        this.albumTitle = albumTitle;
        this.previewUrl = previewUrl;
    }

    public int getTrackNumber() {
        return trackNumber;
    }

    public void setTrackNumber(int trackNumber) {
        this.trackNumber = trackNumber;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getAlbumTitle() {
        return albumTitle;
    }

    public void setAlbumTitle(String albumTitle) {
        this.albumTitle = albumTitle;
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    /**
     * פורמוט משך השיר בפורמט MM:SS
     */
    public String getFormattedDuration() {
        int minutes = durationSeconds / 60;
        int seconds = durationSeconds % 60;
        return String.format(Locale.US, "%d:%02d", minutes, seconds);
    }

    @Override
    public String getDetailedSubtitle() {
        StringBuilder sb = new StringBuilder();
        sb.append("שיר");
        if (albumTitle != null && !albumTitle.isEmpty()) {
            sb.append(" מתוך ").append(albumTitle);
        }
        if (durationSeconds > 0) {
            sb.append(" • ").append(getFormattedDuration());
        }
        if (genre != null && !genre.isEmpty()) {
            sb.append(" • ").append(genre);
        }
        return sb.toString();
    }
}
