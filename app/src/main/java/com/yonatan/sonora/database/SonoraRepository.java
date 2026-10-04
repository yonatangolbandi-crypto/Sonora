package com.yonatan.sonora.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * שכבת ה-Repository המרכזית באפליקציית SONORA.
 * מנהלת את הגישה לנתונים, ביצוע שאילתות מורכבות במסד הנתונים (JOIN, GROUP BY),
 * והפעלת פעולות ברקע תוך שימוש ב-{@link ExecutorService} ו-{@link Handler} (סעיף 6.6 בדרישות).
 *
 * Central data repository managing database CRUD, relational joins and async execution.
 */
public class SonoraRepository {

    private static SonoraRepository instance;
    private final SonoraDatabaseHelper dbHelper;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public interface RepositoryCallback<T> {
        void onComplete(T result);
    }

    public static synchronized SonoraRepository getInstance(Context context) {
        if (instance == null) {
            instance = new SonoraRepository(context.getApplicationContext());
        }
        return instance;
    }

    private SonoraRepository(Context context) {
        this.dbHelper = SonoraDatabaseHelper.getInstance(context);
        this.executor = Executors.newFixedThreadPool(4);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    // ==========================================
    // משתמשים ואימות (User Authentication)
    // ==========================================

    public void authenticateUser(String username, String password, RepositoryCallback<User> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            User user = null;
            Cursor cursor = db.rawQuery(
                    "SELECT * FROM " + SonoraDatabaseHelper.TABLE_USERS +
                            " WHERE (username = ? OR email = ?) AND password_hash = ?",
                    new String[]{username.trim(), username.trim(), password}
            );
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    user = cursorToUser(cursor);
                }
                cursor.close();
            }
            User finalUser = user;
            mainHandler.post(() -> callback.onComplete(finalUser));
        });
    }

    public void registerUser(User user, RepositoryCallback<Boolean> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            boolean success = false;
            try {
                ContentValues cv = new ContentValues();
                if (user.getId() == null || user.getId().isEmpty()) {
                    user.setId(UUID.randomUUID().toString());
                }
                cv.put("id", user.getId());
                cv.put("username", user.getUsername());
                cv.put("email", user.getEmail());
                cv.put("password_hash", user.getPasswordHash());
                cv.put("display_name", user.getDisplayName());
                cv.put("bio", user.getBio());
                cv.put("avatar_url", user.getAvatarUrl());
                cv.put("followers_count", 0);
                cv.put("following_count", 0);
                cv.put("logged_count", 0);
                cv.put("reviews_count", 0);
                cv.put("join_date", System.currentTimeMillis());
                cv.put("favorite_ids", "");

                long row = db.insert(SonoraDatabaseHelper.TABLE_USERS, null, cv);
                success = (row != -1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            boolean finalSuccess = success;
            mainHandler.post(() -> callback.onComplete(finalSuccess));
        });
    }

    public void getUserById(String userId, RepositoryCallback<User> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            User user = null;
            Cursor cursor = db.rawQuery(
                    "SELECT * FROM " + SonoraDatabaseHelper.TABLE_USERS + " WHERE id = ?",
                    new String[]{userId}
            );
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    user = cursorToUser(cursor);
                }
                cursor.close();
            }
            User finalUser = user;
            mainHandler.post(() -> callback.onComplete(finalUser));
        });
    }

    public void updateUserProfile(User user, RepositoryCallback<Boolean> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("display_name", user.getDisplayName());
            cv.put("bio", user.getBio());
            cv.put("avatar_url", user.getAvatarUrl());
            cv.put("favorite_ids", user.getFavoriteIdsJson());
            int rows = db.update(SonoraDatabaseHelper.TABLE_USERS, cv, "id = ?", new String[]{user.getId()});
            mainHandler.post(() -> callback.onComplete(rows > 0));
        });
    }

    public void isFollowing(String currentUserId, String targetUserId, RepositoryCallback<Boolean> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery(
                    "SELECT 1 FROM " + SonoraDatabaseHelper.TABLE_FOLLOWS +
                            " WHERE follower_user_id = ? AND followed_user_id = ?",
                    new String[]{currentUserId, targetUserId}
            );
            boolean isFollow = (cursor != null && cursor.getCount() > 0);
            if (cursor != null) cursor.close();
            mainHandler.post(() -> callback.onComplete(isFollow));
        });
    }

    public void toggleFollow(String currentUserId, String targetUserId, RepositoryCallback<Boolean> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            boolean nowFollowing = false;
            db.beginTransaction();
            try {
                Cursor cursor = db.rawQuery(
                        "SELECT 1 FROM " + SonoraDatabaseHelper.TABLE_FOLLOWS +
                                " WHERE follower_user_id = ? AND followed_user_id = ?",
                        new String[]{currentUserId, targetUserId}
                );
                boolean exists = (cursor != null && cursor.getCount() > 0);
                if (cursor != null) cursor.close();

                if (exists) {
                    db.delete(SonoraDatabaseHelper.TABLE_FOLLOWS,
                            "follower_user_id = ? AND followed_user_id = ?",
                            new String[]{currentUserId, targetUserId});
                    db.execSQL("UPDATE " + SonoraDatabaseHelper.TABLE_USERS + " SET following_count = MAX(0, following_count - 1) WHERE id = ?", new Object[]{currentUserId});
                    db.execSQL("UPDATE " + SonoraDatabaseHelper.TABLE_USERS + " SET followers_count = MAX(0, followers_count - 1) WHERE id = ?", new Object[]{targetUserId});
                    nowFollowing = false;
                } else {
                    ContentValues cv = new ContentValues();
                    cv.put("follower_user_id", currentUserId);
                    cv.put("followed_user_id", targetUserId);
                    db.insert(SonoraDatabaseHelper.TABLE_FOLLOWS, null, cv);
                    db.execSQL("UPDATE " + SonoraDatabaseHelper.TABLE_USERS + " SET following_count = following_count + 1 WHERE id = ?", new Object[]{currentUserId});
                    db.execSQL("UPDATE " + SonoraDatabaseHelper.TABLE_USERS + " SET followers_count = followers_count + 1 WHERE id = ?", new Object[]{targetUserId});
                    nowFollowing = true;
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            boolean finalRes = nowFollowing;
            mainHandler.post(() -> callback.onComplete(finalRes));
        });
    }

    // ==========================================
    // פריטי מוזיקה (Music Items - Songs, Albums, Artists)
    // ==========================================

    public void saveMusicItem(MusicItem item) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("id", item.getId());
            cv.put("title", item.getTitle());
            cv.put("artist", item.getArtist());
            cv.put("cover_url", item.getCoverUrl());
            cv.put("item_type", item.getItemType());
            cv.put("release_year", item.getReleaseYear());
            cv.put("genre", item.getGenre());
            cv.put("average_rating", item.getAverageRating());
            cv.put("ratings_count", item.getRatingsCount());
            cv.put("preview_url", item.getPreviewUrl());

            if (item instanceof Song) {
                Song song = (Song) item;
                cv.put("extra_info_1", song.getAlbumTitle());
                cv.put("extra_info_2", String.valueOf(song.getDurationSeconds()));
            } else if (item instanceof Album) {
                Album album = (Album) item;
                cv.put("extra_info_1", album.getRecordLabel());
                cv.put("extra_info_2", String.valueOf(album.getTrackCount()));
            } else if (item instanceof Artist) {
                Artist artist = (Artist) item;
                cv.put("extra_info_1", artist.getBio());
                cv.put("extra_info_2", artist.getCountry());
            }

            db.insertWithOnConflict(SonoraDatabaseHelper.TABLE_MUSIC_ITEMS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        });
    }

    public void getMusicItemById(String id, RepositoryCallback<MusicItem> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            MusicItem item = null;
            Cursor cursor = db.rawQuery(
                    "SELECT * FROM " + SonoraDatabaseHelper.TABLE_MUSIC_ITEMS + " WHERE id = ?",
                    new String[]{id}
            );
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    item = cursorToMusicItem(cursor);
                }
                cursor.close();
            }
            MusicItem finalItem = item;
            mainHandler.post(() -> callback.onComplete(finalItem));
        });
    }

    public void getTrendingAlbums(RepositoryCallback<List<MusicItem>> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            List<MusicItem> list = new ArrayList<>();
            Cursor cursor = db.rawQuery(
                    "SELECT * FROM " + SonoraDatabaseHelper.TABLE_MUSIC_ITEMS +
                            " WHERE item_type = 'album' ORDER BY average_rating DESC, ratings_count DESC LIMIT 15",
                    null
            );
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    list.add(cursorToMusicItem(cursor));
                }
                cursor.close();
            }
            mainHandler.post(() -> callback.onComplete(list));
        });
    }

    public void getPopularSongs(RepositoryCallback<List<MusicItem>> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            List<MusicItem> list = new ArrayList<>();
            Cursor cursor = db.rawQuery(
                    "SELECT * FROM " + SonoraDatabaseHelper.TABLE_MUSIC_ITEMS +
                            " WHERE item_type = 'song' ORDER BY average_rating DESC, ratings_count DESC LIMIT 15",
                    null
            );
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    list.add(cursorToMusicItem(cursor));
                }
                cursor.close();
            }
            mainHandler.post(() -> callback.onComplete(list));
        });
    }

    public void searchLocalMusic(String query, String typeFilter, RepositoryCallback<List<MusicItem>> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            List<MusicItem> list = new ArrayList<>();
            String sql;
            String[] args;

            if (typeFilter == null || typeFilter.equalsIgnoreCase("all")) {
                sql = "SELECT * FROM " + SonoraDatabaseHelper.TABLE_MUSIC_ITEMS +
                        " WHERE title LIKE ? OR artist LIKE ? OR genre LIKE ? ORDER BY average_rating DESC LIMIT 30";
                args = new String[]{"%" + query + "%", "%" + query + "%", "%" + query + "%"};
            } else {
                sql = "SELECT * FROM " + SonoraDatabaseHelper.TABLE_MUSIC_ITEMS +
                        " WHERE (title LIKE ? OR artist LIKE ?) AND item_type = ? ORDER BY average_rating DESC LIMIT 30";
                args = new String[]{"%" + query + "%", "%" + query + "%", typeFilter};
            }

            Cursor cursor = db.rawQuery(sql, args);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    list.add(cursorToMusicItem(cursor));
                }
                cursor.close();
            }
            mainHandler.post(() -> callback.onComplete(list));
        });
    }

    /**
     * שאילתת התפלגות דירוגים עם GROUP BY (לסעיף 6.4 בדרישות)
     */
    public void getRatingDistribution(String musicItemId, RepositoryCallback<Map<Integer, Integer>> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Map<Integer, Integer> distribution = new HashMap<>();
            for (int i = 1; i <= 5; i++) {
                distribution.put(i, 0);
            }

            Cursor cursor = db.rawQuery(
                    "SELECT CAST(ROUND(rating) AS INTEGER) AS star_level, COUNT(*) AS count " +
                            "FROM " + SonoraDatabaseHelper.TABLE_REVIEWS + " " +
                            "WHERE music_item_id = ? " +
                            "GROUP BY star_level",
                    new String[]{musicItemId}
            );

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int star = cursor.getInt(cursor.getColumnIndexOrThrow("star_level"));
                    int count = cursor.getInt(cursor.getColumnIndexOrThrow("count"));
                    if (star >= 1 && star <= 5) {
                        distribution.put(star, count);
                    }
                }
                cursor.close();
            }
            mainHandler.post(() -> callback.onComplete(distribution));
        });
    }

    // ==========================================
    // ביקורות ורשת חברתית (Reviews & Social Feed)
    // ==========================================

    public void addReview(Review review, RepositoryCallback<Boolean> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            boolean success = false;
            db.beginTransaction();
            try {
                if (review.getId() == null || review.getId().isEmpty()) {
                    review.setId(UUID.randomUUID().toString());
                }
                ContentValues cv = new ContentValues();
                cv.put("id", review.getId());
                cv.put("user_id", review.getUserId());
                cv.put("username", review.getUsername());
                cv.put("user_avatar", review.getUserAvatar());
                cv.put("music_item_id", review.getMusicItemId());
                cv.put("music_item_type", review.getMusicItemType());
                cv.put("item_title", review.getItemTitle());
                cv.put("item_artist", review.getItemArtist());
                cv.put("item_cover", review.getItemCover());
                cv.put("rating", review.getRating());
                cv.put("review_text", review.getReviewText());
                cv.put("timestamp", review.getTimestamp());
                cv.put("likes_count", 0);
                cv.put("comments_count", 0);

                long row = db.insertWithOnConflict(SonoraDatabaseHelper.TABLE_REVIEWS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);

                // עדכון מונה ביקורות למשתמש
                db.execSQL("UPDATE " + SonoraDatabaseHelper.TABLE_USERS + " SET reviews_count = reviews_count + 1 WHERE id = ?",
                        new Object[]{review.getUserId()});

                // הוספה אוטומטית ליומן ההאזנות (Music Diary) של המשתמש
                ContentValues diaryCv = new ContentValues();
                diaryCv.put("id", UUID.randomUUID().toString());
                diaryCv.put("user_id", review.getUserId());
                diaryCv.put("music_item_id", review.getMusicItemId());
                diaryCv.put("music_item_type", review.getMusicItemType());
                diaryCv.put("item_title", review.getItemTitle());
                diaryCv.put("item_artist", review.getItemArtist());
                diaryCv.put("item_cover", review.getItemCover());
                diaryCv.put("rating", review.getRating());
                diaryCv.put("listened_timestamp", review.getTimestamp());
                diaryCv.put("notes", review.getReviewText());
                diaryCv.put("is_relisten", 0);
                db.insert(SonoraDatabaseHelper.TABLE_DIARY, null, diaryCv);

                db.execSQL("UPDATE " + SonoraDatabaseHelper.TABLE_USERS + " SET logged_count = logged_count + 1 WHERE id = ?",
                        new Object[]{review.getUserId()});

                // עדכון ממוצע דירוג לפריט המוזיקה
                updateItemAverageRating(db, review.getMusicItemId());

                db.setTransactionSuccessful();
                success = (row != -1);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();
            }
            boolean finalSuccess = success;
            mainHandler.post(() -> callback.onComplete(finalSuccess));
        });
    }

    private void updateItemAverageRating(SQLiteDatabase db, String itemId) {
        Cursor c = db.rawQuery(
                "SELECT AVG(rating) as avg_rating, COUNT(*) as count FROM " +
                        SonoraDatabaseHelper.TABLE_REVIEWS + " WHERE music_item_id = ?",
                new String[]{itemId}
        );
        if (c != null) {
            if (c.moveToFirst()) {
                double avg = c.getDouble(c.getColumnIndexOrThrow("avg_rating"));
                int count = c.getInt(c.getColumnIndexOrThrow("count"));
                ContentValues cv = new ContentValues();
                cv.put("average_rating", avg);
                cv.put("ratings_count", count);
                db.update(SonoraDatabaseHelper.TABLE_MUSIC_ITEMS, cv, "id = ?", new String[]{itemId});
            }
            c.close();
        }
    }

    public void getFeedReviews(String currentUserId, RepositoryCallback<List<Review>> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            List<Review> list = new ArrayList<>();
            // שאילתת JOIN עם טבלת מעקבים - מציגה ביקורות של מי שעוקבים אחריו ובנוסף ביקורות אחרונות בקהילה
            String sql = "SELECT DISTINCT r.*, " +
                    "(CASE WHEN l.user_id IS NOT NULL THEN 1 ELSE 0 END) AS is_liked " +
                    "FROM " + SonoraDatabaseHelper.TABLE_REVIEWS + " r " +
                    "LEFT JOIN " + SonoraDatabaseHelper.TABLE_FOLLOWS + " f " +
                    "ON r.user_id = f.followed_user_id AND f.follower_user_id = ? " +
                    "LEFT JOIN " + SonoraDatabaseHelper.TABLE_LIKES + " l " +
                    "ON r.id = l.review_id AND l.user_id = ? " +
                    "ORDER BY (f.followed_user_id IS NOT NULL) DESC, r.timestamp DESC LIMIT 40";

            Cursor cursor = db.rawQuery(sql, new String[]{currentUserId, currentUserId});
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Review rev = cursorToReview(cursor);
                    int likedCol = cursor.getColumnIndex("is_liked");
                    if (likedCol != -1) {
                        rev.setLiked(cursor.getInt(likedCol) == 1);
                    }
                    list.add(rev);
                }
                cursor.close();
            }
            mainHandler.post(() -> callback.onComplete(list));
        });
    }

    public void getReviewsForItem(String musicItemId, String currentUserId, RepositoryCallback<List<Review>> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            List<Review> list = new ArrayList<>();
            String sql = "SELECT r.*, " +
                    "(CASE WHEN l.user_id IS NOT NULL THEN 1 ELSE 0 END) AS is_liked " +
                    "FROM " + SonoraDatabaseHelper.TABLE_REVIEWS + " r " +
                    "LEFT JOIN " + SonoraDatabaseHelper.TABLE_LIKES + " l " +
                    "ON r.id = l.review_id AND l.user_id = ? " +
                    "WHERE r.music_item_id = ? " +
                    "ORDER BY r.likes_count DESC, r.timestamp DESC";

            Cursor cursor = db.rawQuery(sql, new String[]{currentUserId, musicItemId});
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Review rev = cursorToReview(cursor);
                    int likedCol = cursor.getColumnIndex("is_liked");
                    if (likedCol != -1) {
                        rev.setLiked(cursor.getInt(likedCol) == 1);
                    }
                    list.add(rev);
                }
                cursor.close();
            }
            mainHandler.post(() -> callback.onComplete(list));
        });
    }

    public void getReviewsByUser(String userId, String currentUserId, RepositoryCallback<List<Review>> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            List<Review> list = new ArrayList<>();
            String sql = "SELECT r.*, " +
                    "(CASE WHEN l.user_id IS NOT NULL THEN 1 ELSE 0 END) AS is_liked " +
                    "FROM " + SonoraDatabaseHelper.TABLE_REVIEWS + " r " +
                    "LEFT JOIN " + SonoraDatabaseHelper.TABLE_LIKES + " l " +
                    "ON r.id = l.review_id AND l.user_id = ? " +
                    "WHERE r.user_id = ? " +
                    "ORDER BY r.timestamp DESC";

            Cursor cursor = db.rawQuery(sql, new String[]{currentUserId, userId});
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Review rev = cursorToReview(cursor);
                    int likedCol = cursor.getColumnIndex("is_liked");
                    if (likedCol != -1) {
                        rev.setLiked(cursor.getInt(likedCol) == 1);
                    }
                    list.add(rev);
                }
                cursor.close();
            }
            mainHandler.post(() -> callback.onComplete(list));
        });
    }

    public void toggleReviewLike(String currentUserId, String reviewId, RepositoryCallback<Boolean> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            boolean nowLiked = false;
            db.beginTransaction();
            try {
                Cursor cursor = db.rawQuery(
                        "SELECT 1 FROM " + SonoraDatabaseHelper.TABLE_LIKES +
                                " WHERE user_id = ? AND review_id = ?",
                        new String[]{currentUserId, reviewId}
                );
                boolean exists = (cursor != null && cursor.getCount() > 0);
                if (cursor != null) cursor.close();

                if (exists) {
                    db.delete(SonoraDatabaseHelper.TABLE_LIKES, "user_id = ? AND review_id = ?", new String[]{currentUserId, reviewId});
                    db.execSQL("UPDATE " + SonoraDatabaseHelper.TABLE_REVIEWS + " SET likes_count = MAX(0, likes_count - 1) WHERE id = ?", new Object[]{reviewId});
                    nowLiked = false;
                } else {
                    ContentValues cv = new ContentValues();
                    cv.put("user_id", currentUserId);
                    cv.put("review_id", reviewId);
                    db.insert(SonoraDatabaseHelper.TABLE_LIKES, null, cv);
                    db.execSQL("UPDATE " + SonoraDatabaseHelper.TABLE_REVIEWS + " SET likes_count = likes_count + 1 WHERE id = ?", new Object[]{reviewId});
                    nowLiked = true;
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            boolean finalRes = nowLiked;
            mainHandler.post(() -> callback.onComplete(finalRes));
        });
    }

    // ==========================================
    // יומן מוזיקה (Music Diary & Log)
    // ==========================================

    public void addDiaryEntry(DiaryEntry entry, RepositoryCallback<Boolean> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            if (entry.getId() == null || entry.getId().isEmpty()) {
                entry.setId(UUID.randomUUID().toString());
            }
            ContentValues cv = new ContentValues();
            cv.put("id", entry.getId());
            cv.put("user_id", entry.getUserId());
            cv.put("music_item_id", entry.getMusicItemId());
            cv.put("music_item_type", entry.getMusicItemType());
            cv.put("item_title", entry.getItemTitle());
            cv.put("item_artist", entry.getItemArtist());
            cv.put("item_cover", entry.getItemCover());
            cv.put("rating", entry.getRating());
            cv.put("listened_timestamp", entry.getListenedTimestamp());
            cv.put("notes", entry.getNotes());
            cv.put("is_relisten", entry.isReListen() ? 1 : 0);

            long res = db.insertWithOnConflict(SonoraDatabaseHelper.TABLE_DIARY, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
            if (res != -1) {
                db.execSQL("UPDATE " + SonoraDatabaseHelper.TABLE_USERS + " SET logged_count = logged_count + 1 WHERE id = ?",
                        new Object[]{entry.getUserId()});
            }
            boolean success = (res != -1);
            mainHandler.post(() -> callback.onComplete(success));
        });
    }

    public void getUserDiary(String userId, RepositoryCallback<List<DiaryEntry>> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            List<DiaryEntry> list = new ArrayList<>();
            Cursor cursor = db.rawQuery(
                    "SELECT * FROM " + SonoraDatabaseHelper.TABLE_DIARY +
                            " WHERE user_id = ? ORDER BY listened_timestamp DESC",
                    new String[]{userId}
            );
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    list.add(cursorToDiaryEntry(cursor));
                }
                cursor.close();
            }
            mainHandler.post(() -> callback.onComplete(list));
        });
    }

    // ==========================================
    // רשימות מוזיקה (Music Lists)
    // ==========================================

    public void createMusicList(MusicList list, RepositoryCallback<Boolean> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            if (list.getId() == null || list.getId().isEmpty()) {
                list.setId(UUID.randomUUID().toString());
            }
            ContentValues cv = new ContentValues();
            cv.put("id", list.getId());
            cv.put("user_id", list.getUserId());
            cv.put("username", list.getUsername());
            cv.put("title", list.getTitle());
            cv.put("description", list.getDescription());
            cv.put("is_public", list.isPublic() ? 1 : 0);
            cv.put("cover_url", list.getCoverUrl());
            cv.put("item_count", list.getItemCount());
            cv.put("item_ids", list.getItemIdsString());
            cv.put("created_at", list.getCreatedAt());

            long row = db.insert(SonoraDatabaseHelper.TABLE_LISTS, null, cv);
            mainHandler.post(() -> callback.onComplete(row != -1));
        });
    }

    public void getUserLists(String userId, RepositoryCallback<List<MusicList>> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            List<MusicList> list = new ArrayList<>();
            Cursor cursor = db.rawQuery(
                    "SELECT * FROM " + SonoraDatabaseHelper.TABLE_LISTS +
                            " WHERE user_id = ? ORDER BY created_at DESC",
                    new String[]{userId}
            );
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    list.add(cursorToMusicList(cursor));
                }
                cursor.close();
            }
            mainHandler.post(() -> callback.onComplete(list));
        });
    }

    public void getPublicLists(RepositoryCallback<List<MusicList>> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            List<MusicList> list = new ArrayList<>();
            Cursor cursor = db.rawQuery(
                    "SELECT * FROM " + SonoraDatabaseHelper.TABLE_LISTS +
                            " WHERE is_public = 1 ORDER BY created_at DESC LIMIT 20",
                    null
            );
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    list.add(cursorToMusicList(cursor));
                }
                cursor.close();
            }
            mainHandler.post(() -> callback.onComplete(list));
        });
    }

    public void addItemToList(String listId, String musicItemId, RepositoryCallback<Boolean> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor cursor = db.rawQuery(
                    "SELECT * FROM " + SonoraDatabaseHelper.TABLE_LISTS + " WHERE id = ?",
                    new String[]{listId}
            );
            boolean updated = false;
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    MusicList mList = cursorToMusicList(cursor);
                    mList.addItemId(musicItemId);

                    ContentValues cv = new ContentValues();
                    cv.put("item_ids", mList.getItemIdsString());
                    cv.put("item_count", mList.getItemCount());
                    int r = db.update(SonoraDatabaseHelper.TABLE_LISTS, cv, "id = ?", new String[]{listId});
                    updated = (r > 0);
                }
                cursor.close();
            }
            boolean finalUpdated = updated;
            mainHandler.post(() -> callback.onComplete(finalUpdated));
        });
    }

    public void getMusicItemsForList(MusicList list, RepositoryCallback<List<MusicItem>> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            List<MusicItem> results = new ArrayList<>();
            List<String> ids = list.getItemIdsList();
            for (String id : ids) {
                Cursor c = db.rawQuery(
                        "SELECT * FROM " + SonoraDatabaseHelper.TABLE_MUSIC_ITEMS + " WHERE id = ?",
                        new String[]{id}
                );
                if (c != null) {
                    if (c.moveToFirst()) {
                        results.add(cursorToMusicItem(c));
                    }
                    c.close();
                }
            }
            mainHandler.post(() -> callback.onComplete(results));
        });
    }

    // ==========================================
    // תגובות (Comments)
    // ==========================================

    public void addComment(Comment comment, RepositoryCallback<Boolean> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            if (comment.getId() == null || comment.getId().isEmpty()) {
                comment.setId(UUID.randomUUID().toString());
            }
            ContentValues cv = new ContentValues();
            cv.put("id", comment.getId());
            cv.put("review_id", comment.getReviewId());
            cv.put("user_id", comment.getUserId());
            cv.put("username", comment.getUsername());
            cv.put("user_avatar", comment.getUserAvatar());
            cv.put("comment_text", comment.getCommentText());
            cv.put("timestamp", comment.getTimestamp());

            long row = db.insert(SonoraDatabaseHelper.TABLE_COMMENTS, null, cv);
            if (row != -1) {
                db.execSQL("UPDATE " + SonoraDatabaseHelper.TABLE_REVIEWS + " SET comments_count = comments_count + 1 WHERE id = ?",
                        new Object[]{comment.getReviewId()});
            }
            mainHandler.post(() -> callback.onComplete(row != -1));
        });
    }

    public void getCommentsForReview(String reviewId, RepositoryCallback<List<Comment>> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            List<Comment> list = new ArrayList<>();
            Cursor cursor = db.rawQuery(
                    "SELECT * FROM " + SonoraDatabaseHelper.TABLE_COMMENTS +
                            " WHERE review_id = ? ORDER BY timestamp ASC",
                    new String[]{reviewId}
            );
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    list.add(cursorToComment(cursor));
                }
                cursor.close();
            }
            mainHandler.post(() -> callback.onComplete(list));
        });
    }

    // ==========================================
    // התראות (Notifications)
    // ==========================================

    public void getUserNotifications(String userId, RepositoryCallback<List<NotificationItem>> callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            List<NotificationItem> list = new ArrayList<>();
            Cursor cursor = db.rawQuery(
                    "SELECT * FROM " + SonoraDatabaseHelper.TABLE_NOTIFICATIONS +
                            " WHERE recipient_user_id = ? ORDER BY timestamp DESC",
                    new String[]{userId}
            );
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    list.add(cursorToNotificationItem(cursor));
                }
                cursor.close();
            }
            mainHandler.post(() -> callback.onComplete(list));
        });
    }

    public void markNotificationsAsRead(String userId) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("is_read", 1);
            db.update(SonoraDatabaseHelper.TABLE_NOTIFICATIONS, cv, "recipient_user_id = ?", new String[]{userId});
        });
    }

    // ==========================================
    // Helper Cursor Parsers
    // ==========================================

    private User cursorToUser(Cursor c) {
        User u = new User();
        u.setId(c.getString(c.getColumnIndexOrThrow("id")));
        u.setUsername(c.getString(c.getColumnIndexOrThrow("username")));
        u.setEmail(c.getString(c.getColumnIndexOrThrow("email")));
        u.setPasswordHash(c.getString(c.getColumnIndexOrThrow("password_hash")));
        u.setDisplayName(c.getString(c.getColumnIndexOrThrow("display_name")));
        u.setBio(c.getString(c.getColumnIndexOrThrow("bio")));
        u.setAvatarUrl(c.getString(c.getColumnIndexOrThrow("avatar_url")));
        u.setFollowersCount(c.getInt(c.getColumnIndexOrThrow("followers_count")));
        u.setFollowingCount(c.getInt(c.getColumnIndexOrThrow("following_count")));
        u.setLoggedCount(c.getInt(c.getColumnIndexOrThrow("logged_count")));
        u.setReviewsCount(c.getInt(c.getColumnIndexOrThrow("reviews_count")));
        u.setJoinDateTimestamp(c.getLong(c.getColumnIndexOrThrow("join_date")));
        u.setFavoriteIdsJson(c.getString(c.getColumnIndexOrThrow("favorite_ids")));
        return u;
    }

    private MusicItem cursorToMusicItem(Cursor c) {
        String type = c.getString(c.getColumnIndexOrThrow("item_type"));
        String id = c.getString(c.getColumnIndexOrThrow("id"));
        String title = c.getString(c.getColumnIndexOrThrow("title"));
        String artist = c.getString(c.getColumnIndexOrThrow("artist"));
        String cover = c.getString(c.getColumnIndexOrThrow("cover_url"));
        int year = c.getInt(c.getColumnIndexOrThrow("release_year"));
        String genre = c.getString(c.getColumnIndexOrThrow("genre"));
        double avg = c.getDouble(c.getColumnIndexOrThrow("average_rating"));
        int count = c.getInt(c.getColumnIndexOrThrow("ratings_count"));
        String preview = c.getString(c.getColumnIndexOrThrow("preview_url"));
        String extra1 = c.getString(c.getColumnIndexOrThrow("extra_info_1"));
        String extra2 = c.getString(c.getColumnIndexOrThrow("extra_info_2"));

        if ("album".equalsIgnoreCase(type)) {
            int trackCount = 0;
            try {
                if (extra2 != null) trackCount = Integer.parseInt(extra2);
            } catch (Exception ignored) {}
            Album album = new Album(id, title, artist, cover, year, genre, avg, count, trackCount, extra1);
            album.setPreviewUrl(preview);
            return album;
        } else if ("artist".equalsIgnoreCase(type)) {
            Artist a = new Artist(id, title, cover, genre, extra1, extra2, year, avg, count);
            a.setPreviewUrl(preview);
            return a;
        } else {
            int duration = 180;
            try {
                if (extra2 != null) duration = Integer.parseInt(extra2);
            } catch (Exception ignored) {}
            Song song = new Song(id, title, artist, cover, year, genre, avg, count, 1, duration, extra1, preview);
            return song;
        }
    }

    private Review cursorToReview(Cursor c) {
        Review r = new Review();
        r.setId(c.getString(c.getColumnIndexOrThrow("id")));
        r.setUserId(c.getString(c.getColumnIndexOrThrow("user_id")));
        r.setUsername(c.getString(c.getColumnIndexOrThrow("username")));
        r.setUserAvatar(c.getString(c.getColumnIndexOrThrow("user_avatar")));
        r.setMusicItemId(c.getString(c.getColumnIndexOrThrow("music_item_id")));
        r.setMusicItemType(c.getString(c.getColumnIndexOrThrow("music_item_type")));
        r.setItemTitle(c.getString(c.getColumnIndexOrThrow("item_title")));
        r.setItemArtist(c.getString(c.getColumnIndexOrThrow("item_artist")));
        r.setItemCover(c.getString(c.getColumnIndexOrThrow("item_cover")));
        r.setRating(c.getDouble(c.getColumnIndexOrThrow("rating")));
        r.setReviewText(c.getString(c.getColumnIndexOrThrow("review_text")));
        r.setTimestamp(c.getLong(c.getColumnIndexOrThrow("timestamp")));
        r.setLikesCount(c.getInt(c.getColumnIndexOrThrow("likes_count")));
        r.setCommentsCount(c.getInt(c.getColumnIndexOrThrow("comments_count")));
        return r;
    }

    private DiaryEntry cursorToDiaryEntry(Cursor c) {
        DiaryEntry d = new DiaryEntry();
        d.setId(c.getString(c.getColumnIndexOrThrow("id")));
        d.setUserId(c.getString(c.getColumnIndexOrThrow("user_id")));
        d.setMusicItemId(c.getString(c.getColumnIndexOrThrow("music_item_id")));
        d.setMusicItemType(c.getString(c.getColumnIndexOrThrow("music_item_type")));
        d.setItemTitle(c.getString(c.getColumnIndexOrThrow("item_title")));
        d.setItemArtist(c.getString(c.getColumnIndexOrThrow("item_artist")));
        d.setItemCover(c.getString(c.getColumnIndexOrThrow("item_cover")));
        d.setRating(c.getDouble(c.getColumnIndexOrThrow("rating")));
        d.setListenedTimestamp(c.getLong(c.getColumnIndexOrThrow("listened_timestamp")));
        d.setNotes(c.getString(c.getColumnIndexOrThrow("notes")));
        d.setReListen(c.getInt(c.getColumnIndexOrThrow("is_relisten")) == 1);
        return d;
    }

    private MusicList cursorToMusicList(Cursor c) {
        MusicList l = new MusicList();
        l.setId(c.getString(c.getColumnIndexOrThrow("id")));
        l.setUserId(c.getString(c.getColumnIndexOrThrow("user_id")));
        l.setUsername(c.getString(c.getColumnIndexOrThrow("username")));
        l.setTitle(c.getString(c.getColumnIndexOrThrow("title")));
        l.setDescription(c.getString(c.getColumnIndexOrThrow("description")));
        l.setPublic(c.getInt(c.getColumnIndexOrThrow("is_public")) == 1);
        l.setCoverUrl(c.getString(c.getColumnIndexOrThrow("cover_url")));
        l.setItemCount(c.getInt(c.getColumnIndexOrThrow("item_count")));
        l.setItemIdsString(c.getString(c.getColumnIndexOrThrow("item_ids")));
        l.setCreatedAt(c.getLong(c.getColumnIndexOrThrow("created_at")));
        return l;
    }

    private Comment cursorToComment(Cursor c) {
        Comment comment = new Comment();
        comment.setId(c.getString(c.getColumnIndexOrThrow("id")));
        comment.setReviewId(c.getString(c.getColumnIndexOrThrow("review_id")));
        comment.setUserId(c.getString(c.getColumnIndexOrThrow("user_id")));
        comment.setUsername(c.getString(c.getColumnIndexOrThrow("username")));
        comment.setUserAvatar(c.getString(c.getColumnIndexOrThrow("user_avatar")));
        comment.setCommentText(c.getString(c.getColumnIndexOrThrow("comment_text")));
        comment.setTimestamp(c.getLong(c.getColumnIndexOrThrow("timestamp")));
        return comment;
    }

    private NotificationItem cursorToNotificationItem(Cursor c) {
        NotificationItem n = new NotificationItem();
        n.setId(c.getString(c.getColumnIndexOrThrow("id")));
        n.setRecipientUserId(c.getString(c.getColumnIndexOrThrow("recipient_user_id")));
        n.setActorUsername(c.getString(c.getColumnIndexOrThrow("actor_username")));
        n.setActorAvatar(c.getString(c.getColumnIndexOrThrow("actor_avatar")));
        n.setType(c.getString(c.getColumnIndexOrThrow("type")));
        n.setTitle(c.getString(c.getColumnIndexOrThrow("title")));
        n.setMessage(c.getString(c.getColumnIndexOrThrow("message")));
        n.setTargetId(c.getString(c.getColumnIndexOrThrow("target_id")));
        n.setTimestamp(c.getLong(c.getColumnIndexOrThrow("timestamp")));
        n.setRead(c.getInt(c.getColumnIndexOrThrow("is_read")) == 1);
        return n;
    }
}
