package com.yonatan.sonora.models;

/**
 * מנשק בסיסי עבור כל פריט מוזיקלי באפליקציית SONORA.
 * מגדיר את החוזה עבור שירים, אלבומים ואמנים.
 *
 * Base interface representing any music entity in the SONORA platform.
 */
public interface MusicItemInterface {

    /**
     * @return מזהה ייחודי של הפריט (Unique item ID)
     */
    String getId();

    /**
     * @return כותרת הפריט או שם השיר/אלבום/אמן (Title or name)
     */
    String getTitle();

    /**
     * @return שם האמן או הלהקה (Artist or band name)
     */
    String getArtist();

    /**
     * @return כתובת URL לתמונת עטיפה (Artwork URL)
     */
    String getCoverUrl();

    /**
     * @return סוג הפריט: "song", "album", "artist" (Item category)
     */
    String getItemType();

    /**
     * @return שנת הוצאה לאור (Release year)
     */
    int getReleaseYear();

    /**
     * @return סגנון מוזיקלי (Genre)
     */
    String getGenre();

    /**
     * @return ממוצע דירוג קהילתי בין 0.0 ל-5.0 (Average community rating)
     */
    double getAverageRating();

    /**
     * @return כמות הדירוגים שניתנו לפריט (Total rating count)
     */
    int getRatingsCount();
}
