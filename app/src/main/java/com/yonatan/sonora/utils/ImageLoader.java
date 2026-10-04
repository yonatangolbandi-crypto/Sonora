package com.yonatan.sonora.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;

import com.yonatan.sonora.R;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * מחלקת עזר מתקדמת לטעינה ושמירת תמונות במטמון (LRU Memory Cache & Async Image Loader).
 * מדגימה מימוש אלגוריתם מטמון LruCache, ניהול זיכרון וריבוי נימים (Threads).
 *
 * Async image loader with LRU in-memory cache and smooth transition animations.
 */
public class ImageLoader {

    private static ImageLoader instance;
    private final LruCache<String, Bitmap> memoryCache;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public static synchronized ImageLoader getInstance() {
        if (instance == null) {
            instance = new ImageLoader();
        }
        return instance;
    }

    private ImageLoader() {
        // הקצאת 1/8 מזיכרון המכשיר הזמין לטובת מטמון התמונות
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;

        this.memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };

        this.executor = Executors.newFixedThreadPool(4);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * טעינת תמונה לתוך ImageView עם שמירה במטמון ואנימציית כניסה רכה.
     *
     * @param url כתובת התמונה ברשת
     * @param imageView יעד התצוגה
     * @param placeholderRes תמונת ברירת מחדל בזמן טעינה
     */
    public void loadImage(String url, ImageView imageView, int placeholderRes) {
        if (imageView == null) return;

        if (url == null || url.trim().isEmpty()) {
            if (placeholderRes != 0) {
                imageView.setImageResource(placeholderRes);
            }
            return;
        }

        // בדיקה במטמון המהיר (RAM Cache)
        Bitmap cached = memoryCache.get(url);
        if (cached != null) {
            imageView.setImageBitmap(cached);
            return;
        }

        if (placeholderRes != 0) {
            imageView.setImageResource(placeholderRes);
        }

        imageView.setTag(url);

        executor.execute(() -> {
            Bitmap downloaded = downloadBitmap(url);
            if (downloaded != null) {
                memoryCache.put(url, downloaded);
                mainHandler.post(() -> {
                    // בדיקה שה-ImageView לא מוחדזר לפריט אחר ב-RecyclerView
                    if (url.equals(imageView.getTag())) {
                        imageView.setImageBitmap(downloaded);
                        AlphaAnimation fadeIn = new AlphaAnimation(0.4f, 1.0f);
                        fadeIn.setDuration(250);
                        imageView.startAnimation(fadeIn);
                    }
                });
            }
        });
    }

    private Bitmap downloadBitmap(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            input.close();
            connection.disconnect();
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }
}
