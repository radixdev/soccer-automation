package com.radix.soccerio.util.bitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.SystemClock;

import com.radix.soccerio.util.Jog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class BitmapCache {
  private static final String TAG = BitmapCache.class.getName();

  public static void saveBitmap(Context context, Bitmap bitmap) {
    String filename = new StringBuilder()
        .append(getCurrentTimeChunkedFormat())
        .append("_")
        .append(String.valueOf(SystemClock.elapsedRealtime()))
        .append(UUID.randomUUID().toString())
        .append(".png")
        .toString();

    File bitmapOutputFile = getCacheOutputFile(context, filename);
    if (bitmapOutputFile.exists()) {
      Jog.w(TAG, "File already exists, fuck it: " + bitmapOutputFile.getAbsolutePath());
      return;
    }

    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(bitmapOutputFile);
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
      fos.flush();

      Jog.d(TAG, "Bitmap saved to external storage at: " + bitmapOutputFile.getAbsolutePath());
    } catch (Exception e) {
      Jog.e(TAG, "Bitmap write exception", e);
    } finally {
      if (fos != null) {
        try {
          fos.close();
        } catch (IOException e1) {
          e1.printStackTrace();
        }
      }
    }
  }

  public static String getCurrentTimeChunkedFormat() {
    // Get the clamped date
    long currentTimeMillis = new Date().getTime();
    long intervalMillis = TimeUnit.MINUTES.toMillis(30);
    Date currentDate = new Date(currentTimeMillis - currentTimeMillis % intervalMillis);

    // Now format it to the date we want
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM-dd-yyyy__hh-mm-aa", Locale.US);
    return simpleDateFormat.format(currentDate);
  }

  private static File getCacheOutputFile(Context context, String filename) {
    File parentCacheDir = new File(context.getFilesDir(), "screenies");
    parentCacheDir = new File(parentCacheDir, getCurrentTimeChunkedFormat());
    parentCacheDir.mkdirs();
    return new File(parentCacheDir, filename);
  }

  private static String getSanitizedFilename(String input) {
    return input.replaceAll("[^a-zA-Z0-9\\s+]", " ")
        .trim()
        .replace(" ", "_")
        .replace("__", "_");
  }
}

