package com.yonatan.sonora.models;

import java.util.ArrayList;
import java.util.List;

/**
 * מחלקה המייצגת אלבום מוזיקלי ב-SONORA.
 * יורשת מ-{@link MusicItem} ומכילה רשימת שירים (הכלה - Composition/Aggregation),
 * כמות רצועות, וחברת תקליטים.
 *
 * Concrete subclass representing a music album.
 */
public class Album extends MusicItem {

    private static final long serialVersionUID = 1L;

    private int trackCount;
    private String recordLabel;
    private List<Song> tracks;

    public Album() {
        super();
        this.itemType = "album";
        this.trackCount = 0;
        this.recordLabel = "";
        this.tracks = new ArrayList<>();
    }

    public Album(String id, String title, String artist, String coverUrl,
                 int releaseYear, String genre, double averageRating, int ratingsCount,
                 int trackCount, String recordLabel) {
        super(id, title, artist, coverUrl, "album", releaseYear, genre, averageRating, ratingsCount);
        this.trackCount = trackCount;
        this.recordLabel = recordLabel;
        this.tracks = new ArrayList<>();
    }

    public int getTrackCount() {
        return trackCount > 0 ? trackCount : (tracks != null ? tracks.size() : 0);
    }

    public void setTrackCount(int trackCount) {
        this.trackCount = trackCount;
    }

    public String getRecordLabel() {
        return recordLabel;
    }

    public void setRecordLabel(String recordLabel) {
        this.recordLabel = recordLabel;
    }

    public List<Song> getTracks() {
        if (tracks == null) {
            tracks = new ArrayList<>();
        }
        return tracks;
    }

    public void setTracks(List<Song> tracks) {
        this.tracks = tracks;
        if (tracks != null && this.trackCount == 0) {
            this.trackCount = tracks.size();
        }
    }

    public void addTrack(Song track) {
        if (this.tracks == null) {
            this.tracks = new ArrayList<>();
        }
        this.tracks.add(track);
        this.trackCount = this.tracks.size();
    }

    @Override
    public String getDetailedSubtitle() {
        StringBuilder sb = new StringBuilder();
        sb.append("אלבום");
        if (releaseYear > 0) {
            sb.append(" • ").append(releaseYear);
        }
        if (getTrackCount() > 0) {
            sb.append(" • ").append(getTrackCount()).append(" רצועות");
        }
        if (genre != null && !genre.isEmpty()) {
            sb.append(" • ").append(genre);
        }
        return sb.toString();
    }
}
