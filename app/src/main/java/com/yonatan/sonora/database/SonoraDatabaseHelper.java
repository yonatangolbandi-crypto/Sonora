package com.yonatan.sonora.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.yonatan.sonora.models.Album;
import com.yonatan.sonora.models.Artist;
import com.yonatan.sonora.models.Comment;
import com.yonatan.sonora.models.DiaryEntry;
import com.yonatan.sonora.models.MusicItem;
import com.yonatan.sonora.models.MusicList;
import com.yonatan.sonora.models.NotificationItem;
import com.yonatan.sonora.models.Review;
import com.yonatan.sonora.models.Song;
import com.yonatan.sonora.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * מחלקת עזר לניהול מסד הנתונים SQLite של אפליקציית SONORA.
 * כוללת הגדרת טבלאות יחסיות (Relational Tables), מפתחות זרים, שאילתות מורכבות (JOIN, GROUP BY),
 * וטעינת נתוני בסיס (Seed Data) של אלבומים, שירים, ביקורות ומשתמשים.
 *
 * Database open helper managing schema, migrations, queries and preloaded music catalog.
 */
public class SonoraDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "sonora_app.db";
    private static final int DATABASE_VERSION = 1;

    private static SonoraDatabaseHelper instance;

    // --- שמות טבלאות ---
    public static final String TABLE_USERS = "users";
    public static final String TABLE_MUSIC_ITEMS = "music_items";
    public static final String TABLE_REVIEWS = "reviews";
    public static final String TABLE_DIARY = "diary_entries";
    public static final String TABLE_LISTS = "music_lists";
    public static final String TABLE_COMMENTS = "comments";
    public static final String TABLE_FOLLOWS = "social_follows";
    public static final String TABLE_LIKES = "review_likes";
    public static final String TABLE_NOTIFICATIONS = "notifications";

    public static synchronized SonoraDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SonoraDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private SonoraDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. טבלת משתמשים
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                "id TEXT PRIMARY KEY, " +
                "username TEXT UNIQUE NOT NULL, " +
                "email TEXT NOT NULL, " +
                "password_hash TEXT NOT NULL, " +
                "display_name TEXT, " +
                "bio TEXT, " +
                "avatar_url TEXT, " +
                "followers_count INTEGER DEFAULT 0, " +
                "following_count INTEGER DEFAULT 0, " +
                "logged_count INTEGER DEFAULT 0, " +
                "reviews_count INTEGER DEFAULT 0, " +
                "join_date INTEGER, " +
                "favorite_ids TEXT" +
                ");");

        // 2. טבלת פריטי מוזיקה (שירים, אלבומים, אמנים)
        db.execSQL("CREATE TABLE " + TABLE_MUSIC_ITEMS + " (" +
                "id TEXT PRIMARY KEY, " +
                "title TEXT NOT NULL, " +
                "artist TEXT NOT NULL, " +
                "cover_url TEXT, " +
                "item_type TEXT NOT NULL, " +
                "release_year INTEGER, " +
                "genre TEXT, " +
                "average_rating REAL DEFAULT 0.0, " +
                "ratings_count INTEGER DEFAULT 0, " +
                "preview_url TEXT, " +
                "extra_info_1 TEXT, " + // albumTitle עבור שיר / recordLabel עבור אלבום / bio עבור אמן
                "extra_info_2 TEXT" +   // duration עבור שיר / trackCount עבור אלבום
                ");");

        // 3. טבלת ביקורות (Reviews)
        db.execSQL("CREATE TABLE " + TABLE_REVIEWS + " (" +
                "id TEXT PRIMARY KEY, " +
                "user_id TEXT NOT NULL, " +
                "username TEXT NOT NULL, " +
                "user_avatar TEXT, " +
                "music_item_id TEXT NOT NULL, " +
                "music_item_type TEXT NOT NULL, " +
                "item_title TEXT NOT NULL, " +
                "item_artist TEXT NOT NULL, " +
                "item_cover TEXT, " +
                "rating REAL NOT NULL, " +
                "review_text TEXT, " +
                "timestamp INTEGER NOT NULL, " +
                "likes_count INTEGER DEFAULT 0, " +
                "comments_count INTEGER DEFAULT 0, " +
                "FOREIGN KEY (user_id) REFERENCES " + TABLE_USERS + "(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (music_item_id) REFERENCES " + TABLE_MUSIC_ITEMS + "(id) ON DELETE CASCADE" +
                ");");

        // 4. טבלת יומן מוזיקה (Diary Log)
        db.execSQL("CREATE TABLE " + TABLE_DIARY + " (" +
                "id TEXT PRIMARY KEY, " +
                "user_id TEXT NOT NULL, " +
                "music_item_id TEXT NOT NULL, " +
                "music_item_type TEXT NOT NULL, " +
                "item_title TEXT NOT NULL, " +
                "item_artist TEXT NOT NULL, " +
                "item_cover TEXT, " +
                "rating REAL, " +
                "listened_timestamp INTEGER NOT NULL, " +
                "notes TEXT, " +
                "is_relisten INTEGER DEFAULT 0, " +
                "FOREIGN KEY (user_id) REFERENCES " + TABLE_USERS + "(id) ON DELETE CASCADE" +
                ");");

        // 5. טבלת רשימות מוזיקה (Music Lists)
        db.execSQL("CREATE TABLE " + TABLE_LISTS + " (" +
                "id TEXT PRIMARY KEY, " +
                "user_id TEXT NOT NULL, " +
                "username TEXT NOT NULL, " +
                "title TEXT NOT NULL, " +
                "description TEXT, " +
                "is_public INTEGER DEFAULT 1, " +
                "cover_url TEXT, " +
                "item_count INTEGER DEFAULT 0, " +
                "item_ids TEXT, " +
                "created_at INTEGER NOT NULL, " +
                "FOREIGN KEY (user_id) REFERENCES " + TABLE_USERS + "(id) ON DELETE CASCADE" +
                ");");

        // 6. טבלת תגובות (Comments)
        db.execSQL("CREATE TABLE " + TABLE_COMMENTS + " (" +
                "id TEXT PRIMARY KEY, " +
                "review_id TEXT NOT NULL, " +
                "user_id TEXT NOT NULL, " +
                "username TEXT NOT NULL, " +
                "user_avatar TEXT, " +
                "comment_text TEXT NOT NULL, " +
                "timestamp INTEGER NOT NULL, " +
                "FOREIGN KEY (review_id) REFERENCES " + TABLE_REVIEWS + "(id) ON DELETE CASCADE" +
                ");");

        // 7. טבלת עוקבים (Social Follows)
        db.execSQL("CREATE TABLE " + TABLE_FOLLOWS + " (" +
                "follower_user_id TEXT NOT NULL, " +
                "followed_user_id TEXT NOT NULL, " +
                "PRIMARY KEY (follower_user_id, followed_user_id)" +
                ");");

        // 8. טבלת לייקים לביקורות (Review Likes)
        db.execSQL("CREATE TABLE " + TABLE_LIKES + " (" +
                "user_id TEXT NOT NULL, " +
                "review_id TEXT NOT NULL, " +
                "PRIMARY KEY (user_id, review_id)" +
                ");");

        // 9. טבלת התראות (Notifications)
        db.execSQL("CREATE TABLE " + TABLE_NOTIFICATIONS + " (" +
                "id TEXT PRIMARY KEY, " +
                "recipient_user_id TEXT NOT NULL, " +
                "actor_username TEXT NOT NULL, " +
                "actor_avatar TEXT, " +
                "type TEXT NOT NULL, " +
                "title TEXT NOT NULL, " +
                "message TEXT NOT NULL, " +
                "target_id TEXT, " +
                "timestamp INTEGER NOT NULL, " +
                "is_read INTEGER DEFAULT 0" +
                ");");

        // הוספת נתוני בסיס עשירים
        seedInitialData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LIKES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOLLOWS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LISTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DIARY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REVIEWS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MUSIC_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    /**
     * הזנת נתונים ראשוניים עשירים למסד הנתונים:
     * משתמשי קהילה, אלבומים אייקוניים, שירים מובילים וביקורות.
     */
    private void seedInitialData(SQLiteDatabase db) {
        long now = System.currentTimeMillis();

        // --- 1. משתמשי דמה קהילתיים ---
        insertUserRaw(db, "user_demo", "yonatan", "yonatan@sonora.app", "123456",
                "יונתן כהן", "אוהב פרוגרסיב רוק, ג'אז ואינדי פופ. מקשיב למוזיקה מסביב לשעון.",
                "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200", 42, 18, 56, 12, now - 86400000L * 60);

        insertUserRaw(db, "user_maya", "Maya_Music", "maya@music.com", "123456",
                "מיה לוי", "מבקרת מוזיקה, שומעת הכל מפסיכדליה ועד היפ הופ אלטרנטיבי.",
                "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200", 128, 95, 140, 48, now - 86400000L * 120);

        insertUserRaw(db, "user_dan", "Dan_Groove", "dan@groove.com", "123456",
                "דן גרוב", "אספן וינילים. לטרבוקסד של שירים זה החלום שלי.",
                "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=200", 87, 44, 92, 26, now - 86400000L * 90);

        // --- 2. אלבומים קלאסיים ופופולריים ---
        insertMusicItemRaw(db, "alb_dark_side", "The Dark Side of the Moon", "Pink Floyd",
                "https://upload.wikimedia.org/wikipedia/en/3/3b/Dark_Side_of_the_Moon.png",
                "album", 1973, "Progressive Rock", 4.9, 1420, "", "Harvest Records", "10");

        insertMusicItemRaw(db, "alb_abbey_road", "Abbey Road", "The Beatles",
                "https://upload.wikimedia.org/wikipedia/en/4/42/Beatles_-_Abbey_Road.jpg",
                "album", 1969, "Classic Rock", 4.8, 1850, "", "Apple Records", "17");

        insertMusicItemRaw(db, "alb_ok_computer", "OK Computer", "Radiohead",
                "https://upload.wikimedia.org/wikipedia/en/b/ba/Radioheadokcomputer.png",
                "album", 1997, "Alternative Rock", 4.9, 1310, "", "Parlophone", "12");

        insertMusicItemRaw(db, "alb_tpab", "To Pimp a Butterfly", "Kendrick Lamar",
                "https://upload.wikimedia.org/wikipedia/en/f/f6/Kendrick_Lamar_-_To_Pimp_a_Butterfly.png",
                "album", 2015, "Hip Hop", 4.9, 2100, "", "Top Dawg / Aftermath", "16");

        insertMusicItemRaw(db, "alb_rumours", "Rumours", "Fleetwood Mac",
                "https://upload.wikimedia.org/wikipedia/en/f/fb/FMacRumours.PNG",
                "album", 1977, "Pop Rock", 4.7, 980, "", "Warner Bros.", "11");

        insertMusicItemRaw(db, "alb_ram", "Random Access Memories", "Daft Punk",
                "https://upload.wikimedia.org/wikipedia/en/a/a7/Random_Access_Memories.jpg",
                "album", 2013, "Electronic", 4.8, 1600, "", "Columbia", "13");

        insertMusicItemRaw(db, "alb_folklore", "Folklore", "Taylor Swift",
                "https://upload.wikimedia.org/wikipedia/en/f/f8/Taylor_Swift_-_Folklore.png",
                "album", 2020, "Indie Folk", 4.6, 1250, "", "Republic", "16");

        insertMusicItemRaw(db, "alb_kind_of_blue", "Kind of Blue", "Miles Davis",
                "https://upload.wikimedia.org/wikipedia/en/9/9c/MilesDavisKindofBlue.jpg",
                "album", 1959, "Jazz", 4.9, 870, "", "Columbia", "5");

        // --- 3. שירים בולטים ---
        insertMusicItemRaw(db, "song_time", "Time", "Pink Floyd",
                "https://upload.wikimedia.org/wikipedia/en/3/3b/Dark_Side_of_the_Moon.png",
                "song", 1973, "Progressive Rock", 4.9, 950,
                "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview125/v4/4a/12/57/4a12574e-503c-83b4-fbb4-098522e83ee9/mzaf_10915655474326588265.plus.aac.p.m4a",
                "The Dark Side of the Moon", "425");

        insertMusicItemRaw(db, "song_paranoid_android", "Paranoid Android", "Radiohead",
                "https://upload.wikimedia.org/wikipedia/en/b/ba/Radioheadokcomputer.png",
                "song", 1997, "Alternative Rock", 4.9, 890,
                "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview115/v4/bf/25/7a/bf257a07-8898-32ad-48b8-ee63e77f062a/mzaf_8494951475752391090.plus.aac.p.m4a",
                "OK Computer", "387");

        insertMusicItemRaw(db, "song_get_lucky", "Get Lucky", "Daft Punk ft. Pharrell Williams",
                "https://upload.wikimedia.org/wikipedia/en/a/a7/Random_Access_Memories.jpg",
                "song", 2013, "Electronic / Disco", 4.8, 1400,
                "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview115/v4/80/7e/16/807e1637-2cf3-69be-b8e7-fa293cfd95c4/mzaf_3114004928131379796.plus.aac.p.m4a",
                "Random Access Memories", "248");

        insertMusicItemRaw(db, "song_king_kunta", "King Kunta", "Kendrick Lamar",
                "https://upload.wikimedia.org/wikipedia/en/f/f6/Kendrick_Lamar_-_To_Pimp_a_Butterfly.png",
                "song", 2015, "Hip Hop", 4.8, 1200,
                "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview115/v4/37/10/72/37107297-c7ba-4aee-b223-9c8cb0a8b9f4/mzaf_13506163777553259837.plus.aac.p.m4a",
                "To Pimp a Butterfly", "234");

        // --- 4. אמנים ---
        insertMusicItemRaw(db, "art_pink_floyd", "Pink Floyd", "Pink Floyd",
                "https://upload.wikimedia.org/wikipedia/en/3/3b/Dark_Side_of_the_Moon.png",
                "artist", 1965, "Progressive Rock", 4.9, 3200, "",
                "להקת רוק בריטית מהמשפיעות והמצליחות בכל הזמנים.", "בריטניה");

        insertMusicItemRaw(db, "art_radiohead", "Radiohead", "Radiohead",
                "https://upload.wikimedia.org/wikipedia/en/b/ba/Radioheadokcomputer.png",
                "artist", 1985, "Alternative Rock", 4.9, 2900, "",
                "להקת רוק אלטרנטיבי מאוקספורדשייר ששינתה את פני המוזיקה המודרנית.", "בריטניה");

        // --- 5. ביקורות חברתיות ראשוניות לפיד ---
        insertReviewRaw(db, "rev_1", "user_maya", "Maya_Music",
                "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200",
                "alb_dark_side", "album", "The Dark Side of the Moon", "Pink Floyd",
                "https://upload.wikimedia.org/wikipedia/en/3/3b/Dark_Side_of_the_Moon.png",
                5.0, "יצירת מופת שאינה מתיישנת לעולם. סולו הגיטרה ב-Time ומעברי הסינתיסייזרים ב-On The Run פשוט מהפנטים. חובה בכל בית!",
                now - 3600000L * 4, 18, 3);

        insertReviewRaw(db, "rev_2", "user_dan", "Dan_Groove",
                "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=200",
                "alb_ok_computer", "album", "OK Computer", "Radiohead",
                "https://upload.wikimedia.org/wikipedia/en/b/ba/Radioheadokcomputer.png",
                5.0, "אלבום שלקח את החרדות הטכנולוגיות של סוף המאה ה-20 והפך אותן לשירה צרופה. Paranoid Android היא הבואמיאן רפסודי של הניינטיז.",
                now - 3600000L * 8, 12, 1);

        insertReviewRaw(db, "rev_3", "user_maya", "Maya_Music",
                "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200",
                "alb_tpab", "album", "To Pimp a Butterfly", "Kendrick Lamar",
                "https://upload.wikimedia.org/wikipedia/en/f/f6/Kendrick_Lamar_-_To_Pimp_a_Butterfly.png",
                5.0, "מורכבות מוזיקלית ברמה של מיילס דייוויס משולבת עם טקסטים חברתיים חדים ונוקבים. אחד מאלבומי העשור.",
                now - 86400000L * 1, 24, 5);

        // --- 6. יומן מוזיקה למשתמש הדגמה (Diary) ---
        insertDiaryRaw(db, "diary_1", "user_demo", "alb_dark_side", "album",
                "The Dark Side of the Moon", "Pink Floyd",
                "https://upload.wikimedia.org/wikipedia/en/3/3b/Dark_Side_of_the_Moon.png",
                5.0, now - 86400000L * 2, "האזנה עם אוזניות בלילה בחושך מוחלט. חוויה רוחנית.", 1);

        insertDiaryRaw(db, "diary_2", "user_demo", "alb_abbey_road", "album",
                "Abbey Road", "The Beatles",
                "https://upload.wikimedia.org/wikipedia/en/4/42/Beatles_-_Abbey_Road.jpg",
                4.5, now - 86400000L * 4, "הצד השני (Medley) הוא מהרגעים הגדולים בפופ.", 0);

        insertDiaryRaw(db, "diary_3", "user_demo", "alb_ram", "album",
                "Random Access Memories", "Daft Punk",
                "https://upload.wikimedia.org/wikipedia/en/a/a7/Random_Access_Memories.jpg",
                5.0, now - 86400000L * 6, "ההפקה הכי נקייה ומבריקה שנשמעה בעשור האחרון.", 1);

        // --- 7. רשימות מוזיקה (Lists) ---
        insertListRaw(db, "list_1", "user_maya", "Maya_Music",
                "אלבומי מופת של שנות ה-70",
                "בחירה אישית של האלבומים שהגדירו את עשור הזהב של הרוק והפופ.",
                1, "https://upload.wikimedia.org/wikipedia/en/3/3b/Dark_Side_of_the_Moon.png",
                3, "alb_dark_side,alb_rumours,alb_abbey_road", now - 86400000L * 10);

        insertListRaw(db, "list_2", "user_dan", "Dan_Groove",
                "חובה באוזניות: הפקות סאונד מושלמות",
                "אלבומים ושירים שחובה לשמוע באוזניות איכותיות כדי להבחין בכל שכבה.",
                1, "https://upload.wikimedia.org/wikipedia/en/a/a7/Random_Access_Memories.jpg",
                2, "alb_ram,alb_ok_computer", now - 86400000L * 5);

        // --- 8. קשרי מעקב חברתי (Social follows) ---
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_FOLLOWS + " VALUES ('user_demo', 'user_maya');");
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_FOLLOWS + " VALUES ('user_demo', 'user_dan');");
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_FOLLOWS + " VALUES ('user_dan', 'user_demo');");

        // --- 9. התראות ראשוניות למשתמש ---
        insertNotificationRaw(db, "notif_1", "user_demo", "Maya_Music",
                "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200",
                NotificationItem.TYPE_LIKE, "לייק חדש לביקורת שלך",
                "מיה אהבה את הביקורת שלך על The Dark Side of the Moon", "rev_1", now - 3600000L * 2);

        insertNotificationRaw(db, "notif_2", "user_demo", "Sonora AI",
                "", NotificationItem.TYPE_AI_REC, "המלצה שבועית חדשה עבורך",
                "בהתבסס על אהבתך לרוק פסיכדלי, מצאנו עבורך 5 אלבומים חדשים!", "ai_weekly", now - 86400000L);
    }

    private void insertUserRaw(SQLiteDatabase db, String id, String username, String email,
                               String pass, String name, String bio, String avatar,
                               int followers, int following, int logged, int reviews, long joinDate) {
        ContentValues cv = new ContentValues();
        cv.put("id", id);
        cv.put("username", username);
        cv.put("email", email);
        cv.put("password_hash", pass);
        cv.put("display_name", name);
        cv.put("bio", bio);
        cv.put("avatar_url", avatar);
        cv.put("followers_count", followers);
        cv.put("following_count", following);
        cv.put("logged_count", logged);
        cv.put("reviews_count", reviews);
        cv.put("join_date", joinDate);
        cv.put("favorite_ids", "alb_dark_side,alb_ok_computer,alb_ram,alb_tpab");
        db.insertWithOnConflict(TABLE_USERS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    private void insertMusicItemRaw(SQLiteDatabase db, String id, String title, String artist,
                                    String cover, String type, int year, String genre,
                                    double avgRating, int ratingsCount, String preview,
                                    String extra1, String extra2) {
        ContentValues cv = new ContentValues();
        cv.put("id", id);
        cv.put("title", title);
        cv.put("artist", artist);
        cv.put("cover_url", cover);
        cv.put("item_type", type);
        cv.put("release_year", year);
        cv.put("genre", genre);
        cv.put("average_rating", avgRating);
        cv.put("ratings_count", ratingsCount);
        cv.put("preview_url", preview);
        cv.put("extra_info_1", extra1);
        cv.put("extra_info_2", extra2);
        db.insertWithOnConflict(TABLE_MUSIC_ITEMS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    private void insertReviewRaw(SQLiteDatabase db, String id, String userId, String username,
                                 String avatar, String itemId, String itemType, String itemTitle,
                                 String itemArtist, String itemCover, double rating,
                                 String text, long timestamp, int likes, int comments) {
        ContentValues cv = new ContentValues();
        cv.put("id", id);
        cv.put("user_id", userId);
        cv.put("username", username);
        cv.put("user_avatar", avatar);
        cv.put("music_item_id", itemId);
        cv.put("music_item_type", itemType);
        cv.put("item_title", itemTitle);
        cv.put("item_artist", itemArtist);
        cv.put("item_cover", itemCover);
        cv.put("rating", rating);
        cv.put("review_text", text);
        cv.put("timestamp", timestamp);
        cv.put("likes_count", likes);
        cv.put("comments_count", comments);
        db.insertWithOnConflict(TABLE_REVIEWS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    private void insertDiaryRaw(SQLiteDatabase db, String id, String userId, String itemId,
                                String itemType, String title, String artist, String cover,
                                double rating, long timestamp, String notes, int relisten) {
        ContentValues cv = new ContentValues();
        cv.put("id", id);
        cv.put("user_id", userId);
        cv.put("music_item_id", itemId);
        cv.put("music_item_type", itemType);
        cv.put("item_title", title);
        cv.put("item_artist", artist);
        cv.put("item_cover", cover);
        cv.put("rating", rating);
        cv.put("listened_timestamp", timestamp);
        cv.put("notes", notes);
        cv.put("is_relisten", relisten);
        db.insertWithOnConflict(TABLE_DIARY, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    private void insertListRaw(SQLiteDatabase db, String id, String userId, String username,
                               String title, String desc, int isPublic, String cover,
                               int count, String itemIds, long createdAt) {
        ContentValues cv = new ContentValues();
        cv.put("id", id);
        cv.put("user_id", userId);
        cv.put("username", username);
        cv.put("title", title);
        cv.put("description", desc);
        cv.put("is_public", isPublic);
        cv.put("cover_url", cover);
        cv.put("item_count", count);
        cv.put("item_ids", itemIds);
        cv.put("created_at", createdAt);
        db.insertWithOnConflict(TABLE_LISTS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    private void insertNotificationRaw(SQLiteDatabase db, String id, String recipientId,
                                       String actor, String avatar, String type,
                                       String title, String msg, String targetId, long timestamp) {
        ContentValues cv = new ContentValues();
        cv.put("id", id);
        cv.put("recipient_user_id", recipientId);
        cv.put("actor_username", actor);
        cv.put("actor_avatar", avatar);
        cv.put("type", type);
        cv.put("title", title);
        cv.put("message", msg);
        cv.put("target_id", targetId);
        cv.put("timestamp", timestamp);
        cv.put("is_read", 0);
        db.insertWithOnConflict(TABLE_NOTIFICATIONS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }
}
