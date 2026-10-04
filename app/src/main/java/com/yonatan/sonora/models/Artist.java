package com.yonatan.sonora.models;

import java.util.ArrayList;
import java.util.List;

/**
 * מחלקה המייצגת אמן או להקה ב-SONORA.
 * יורשת מ-{@link MusicItem} ומכילה ביוגרפיה, מדינת מוצא, שנת הקמה/פעילות,
 * ודיסקוגרפיה מקושרת.
 *
 * Concrete subclass representing a musical artist or band.
 */
public class Artist extends MusicItem {

    private static final long serialVersionUID = 1L;

    private String bio;
    private String country;
    private int activeSinceYear;
    private List<Album> discography;

    public Artist() {
        super();
        this.itemType = "artist";
        this.bio = "";
        this.country = "";
        this.activeSinceYear = 2000;
        this.discography = new ArrayList<>();
    }

    public Artist(String id, String name, String coverUrl, String genre,
                  String bio, String country, int activeSinceYear,
                  double averageRating, int ratingsCount) {
        super(id, name, name, coverUrl, "artist", activeSinceYear, genre, averageRating, ratingsCount);
        this.bio = bio;
        this.country = country;
        this.activeSinceYear = activeSinceYear;
        this.discography = new ArrayList<>();
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getActiveSinceYear() {
        return activeSinceYear;
    }

    public void setActiveSinceYear(int activeSinceYear) {
        this.activeSinceYear = activeSinceYear;
    }

    public List<Album> getDiscography() {
        if (discography == null) {
            discography = new ArrayList<>();
        }
        return discography;
    }

    public void setDiscography(List<Album> discography) {
        this.discography = discography;
    }

    @Override
    public String getDetailedSubtitle() {
        StringBuilder sb = new StringBuilder();
        sb.append("אמן / להקה");
        if (country != null && !country.isEmpty()) {
            sb.append(" • ").append(country);
        }
        if (genre != null && !genre.isEmpty()) {
            sb.append(" • ").append(genre);
        }
        return sb.toString();
    }
}
