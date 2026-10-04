package com.yonatan.sonora.api;

import android.os.Handler;
import android.os.Looper;

import com.yonatan.sonora.models.Album;
import com.yonatan.sonora.models.Artist;
import com.yonatan.sonora.models.MusicItem;
import com.yonatan.sonora.models.Song;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * לקוח API אינטרנטי לשליפת נתוני מוזיקה בזמן אמת מ-iTunes Search API.
 * מממש את סעיף 6.2 בדרישות הבגרות ("הורדת נתוני מידע מהאינטרנט באמצעות API ושימוש בנתונים").
 * מבצע קריאות רשת א-סינכרוניות (Background Thread) ומחזיר תוצאות מנותחות לממשק המשתמש (Main Thread).
 *
 * Web API Client for querying music data from external REST service.
 */
public class ItunesApiClient {

    private static final String BASE_URL = "https://itunes.apple.com/search";
    private static ItunesApiClient instance;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    public static synchronized ItunesApiClient getInstance() {
        if (instance == null) {
            instance = new ItunesApiClient();
        }
        return instance;
    }

    private ItunesApiClient() {
        this.executor = Executors.newFixedThreadPool(3);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * חיפוש שירים, אלבומים או אמנים ברשת.
     *
     * @param query מילת החיפוש
     * @param entityType סוג: "song", "album", "musicArtist" או "all"
     * @param callback ממשק לקבלת התוצאות
     */
    public void searchMusic(String query, String entityType, ApiCallback<List<MusicItem>> callback) {
        executor.execute(() -> {
            try {
                String entityParam;
                if ("album".equalsIgnoreCase(entityType)) {
                    entityParam = "album";
                } else if ("song".equalsIgnoreCase(entityType)) {
                    entityParam = "song";
                } else if ("artist".equalsIgnoreCase(entityType)) {
                    entityParam = "musicArtist";
                } else {
                    entityParam = "musicTrack";
                }

                String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.name());
                String urlString = BASE_URL + "?term=" + encodedQuery + "&entity=" + entityParam + "&limit=30";

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestProperty("Accept", "application/json");

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();

                    List<MusicItem> parsedItems = parseJsonResponse(response.toString());
                    mainHandler.post(() -> callback.onSuccess(parsedItems));
                } else {
                    mainHandler.post(() -> callback.onError(new Exception("HTTP error code: " + responseCode)));
                }
                conn.disconnect();
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            }
        });
    }

    /**
     * ניתוח תשובת ה-JSON והמרתה לאובייקטים מונחי-עצמים מתוך היררכיית המחלקות שלנו.
     */
    private List<MusicItem> parseJsonResponse(String jsonString) {
        List<MusicItem> list = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(jsonString);
            if (!root.has("results")) return list;
            JSONArray results = root.getJSONArray("results");

            for (int i = 0; i < results.length(); i++) {
                JSONObject obj = results.getJSONObject(i);
                String wrapperType = obj.optString("wrapperType", "");
                String kind = obj.optString("kind", "");

                // שדרוג איכות תמונת העטיפה לרזולוציה גבוהה
                String artworkUrl = obj.optString("artworkUrl100", "");
                if (artworkUrl.contains("100x100bb")) {
                    artworkUrl = artworkUrl.replace("100x100bb", "600x600bb");
                }

                String genre = obj.optString("primaryGenreName", "Music");
                String releaseDate = obj.optString("releaseDate", "");
                int year = 2024;
                if (releaseDate.length() >= 4) {
                    try {
                        year = Integer.parseInt(releaseDate.substring(0, 4));
                    } catch (Exception ignored) {}
                }

                // האם מדובר באלבום
                if ("collection".equalsIgnoreCase(wrapperType)) {
                    String albumId = "itunes_alb_" + obj.optLong("collectionId");
                    String albumTitle = obj.optString("collectionName", "Unknown Album");
                    String artistName = obj.optString("artistName", "Unknown Artist");
                    int trackCount = obj.optInt("trackCount", 0);
                    String recordLabel = obj.optString("copyright", "");

                    Album album = new Album(albumId, albumTitle, artistName, artworkUrl,
                            year, genre, 4.5, 12, trackCount, recordLabel);
                    list.add(album);
                }
                // האם מדובר באמן
                else if ("artist".equalsIgnoreCase(wrapperType)) {
                    String artistId = "itunes_art_" + obj.optLong("artistId");
                    String artistName = obj.optString("artistName", "Unknown Artist");
                    Artist artist = new Artist(artistId, artistName, artworkUrl, genre,
                            "אמן ב-iTunes Music", "בינלאומי", year, 4.6, 20);
                    list.add(artist);
                }
                // ברירת מחדל: שיר (track / song)
                else {
                    String songId = "itunes_song_" + obj.optLong("trackId");
                    String songTitle = obj.optString("trackName", "Unknown Song");
                    String artistName = obj.optString("artistName", "Unknown Artist");
                    String albumTitle = obj.optString("collectionName", "");
                    String previewUrl = obj.optString("previewUrl", "");
                    int durationSeconds = obj.optInt("trackTimeMillis", 180000) / 1000;
                    int trackNumber = obj.optInt("trackNumber", 1);

                    Song song = new Song(songId, songTitle, artistName, artworkUrl,
                            year, genre, 4.7, 18, trackNumber, durationSeconds, albumTitle, previewUrl);
                    list.add(song);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
