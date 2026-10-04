package com.yonatan.sonora.models;

/**
 * ממשק עבור ישויות בעלות אינטראקציה חברתית (לייקים ותגובות).
 * Interface representing social content that can be liked and commented on.
 */
public interface SocialInteractable {

    /**
     * @return מזהה הישות
     */
    String getId();

    /**
     * @return מספר הלייקים שהפריט קיבל
     */
    int getLikesCount();

    /**
     * עדכון מספר הלייקים
     * @param count כמות חדשה
     */
    void setLikesCount(int count);

    /**
     * האם המשתמש הנוכחי סימן לייק לפריט זה
     */
    boolean isLiked();

    /**
     * שינוי מצב הלייק של המשתמש הנוכחי
     */
    void setLiked(boolean liked);

    /**
     * @return מספר התגובות שנכתבו לפריט
     */
    int getCommentsCount();

    /**
     * עדכון מספר התגובות
     */
    void setCommentsCount(int count);
}
