package com.radix.soccerio.model;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

public class BallDetector implements IBallDetector {
  private static final String TAG = BallDetector.class.getName();
  private static final int MAX_STRIDE = 100;
  private static final int Y_STRIDE = 50;
  private static final int MIN_STRIDE = 30;
  public static final float MAGNITUDE_THRESHOLD = 0.0001f;

  @Override
  public Rect getBallBounds(Bitmap sourceBitmap) {

    final int sourceWidth = sourceBitmap.getWidth();
    final int sourceHeight = sourceBitmap.getHeight();

    List<Integer> borderPoints = getBorderPoints(sourceBitmap, sourceWidth, sourceHeight);
    return null;
  }

  private static List<Integer> getBorderLines(Bitmap sourceBitmap, int sourceWidth, int sourceHeight) {
    List<Integer> borderPoints = new ArrayList<>();

    Bitmap copyBitmap = sourceBitmap.copy(sourceBitmap.getConfig(), true);
    int stride = MAX_STRIDE;
    for (int x = MAX_STRIDE; x < sourceWidth - MAX_STRIDE; x += stride) {
      for (int y = MAX_STRIDE; y < sourceHeight - MAX_STRIDE; y += stride) {
        float xMagnitude = getLuminance(sourceBitmap, x + MIN_STRIDE, y) - getLuminance(sourceBitmap, x - MIN_STRIDE, y);
        float yMagnitude = getLuminance(sourceBitmap, x, y + MIN_STRIDE) - getLuminance(sourceBitmap, x, y - MIN_STRIDE);
        double normalMagnitude = Math.sqrt(xMagnitude * xMagnitude + yMagnitude * yMagnitude);

        // large swaths of the image are just white, so stride on them
        if (normalMagnitude < MAGNITUDE_THRESHOLD) {
          stride = Math.min(stride + 1, MAX_STRIDE);
        } else {
          stride = MIN_STRIDE;
          borderPoints.add(x);
          borderPoints.add(y);

          int c = (int) normalMagnitude * 255;
          copyBitmap.setPixel(x, y, Color.rgb(c, c, c));
        }
      }
    }

    return borderPoints;
  }

  private static List<Integer> getBorderPoints(Bitmap sourceBitmap, int sourceWidth, int sourceHeight) {
    List<Integer> borderPoints = new ArrayList<>();

    Bitmap copyBitmap = sourceBitmap.copy(sourceBitmap.getConfig(), true);
    int stride = MAX_STRIDE;
    for (int x = MIN_STRIDE; x < sourceWidth - MIN_STRIDE; x += stride) {
      for (int y = MIN_STRIDE; y < sourceHeight - MIN_STRIDE; y += Y_STRIDE) {
        float xMagnitude = getLuminance(sourceBitmap, x + MIN_STRIDE, y) - getLuminance(sourceBitmap, x - MIN_STRIDE, y);
        float yMagnitude = getLuminance(sourceBitmap, x, y + MIN_STRIDE) - getLuminance(sourceBitmap, x, y - MIN_STRIDE);
        double normalMagnitude = Math.sqrt(xMagnitude * xMagnitude + yMagnitude * yMagnitude);

        // large swaths of the image are just white, so stride on them
        // if (normalMagnitude < MAGNITUDE_THRESHOLD) {
        //   stride = Math.min(stride + 1, MAX_STRIDE);
        // } else {
        //   stride = MAX_STRIDE;
        // }

        int c = (int) normalMagnitude * 255;
        if (normalMagnitude > 0.001) {
          borderPoints.add(x);
          borderPoints.add(y);
          // copyBitmap.setPixel(x, y, Color.rgb(c, c, c));
          // copyBitmap.setPixel(x, y, Color.rgb(255, 0, 0));
          drawBigBox(copyBitmap, x, y);
        }
      }
    }

    return borderPoints;
  }

  private static void drawBigBox(Bitmap bitmap, int startX, int startY) {
    final int radius = 5;
    for (int x = 0; x < radius; x++) {
      for (int y = 0; y < radius; y++) {
        bitmap.setPixel(x + startX, y + startY, Color.rgb(255, 0, 0));
      }
    }
  }

  private static float getLuminance(Bitmap bitmap, int x, int y) {
    int pixelColor = bitmap.getPixel(x, y);
    return Color.luminance(pixelColor);
  }
}
