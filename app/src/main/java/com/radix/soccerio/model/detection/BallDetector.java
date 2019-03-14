package com.radix.soccerio.model.detection;

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
  private static final float MAGNITUDE_THRESHOLD = 0.0001f;

  @Override
  public Rect getBallBounds(Bitmap sourceBitmap) {

    final int sourceWidth = sourceBitmap.getWidth();
    final int sourceHeight = sourceBitmap.getHeight();

    Bitmap copyBitmap = sourceBitmap.copy(sourceBitmap.getConfig(), true);
    List<Integer> borderPoints = getBorderPoints(copyBitmap, sourceWidth, sourceHeight);
    getContiguousRects(copyBitmap, borderPoints);
    return null;
  }

  /**
   * All of the {@link Rect}'s that constitute regions.
   *
   * @param borderPoints
   */
  private static void getContiguousRects(Bitmap sourceBitmap, List<Integer> borderPoints) {
    for (int i = 2; i < borderPoints.size(); i+=2) {
      int x = borderPoints.get(i);
      int y = borderPoints.get(i + 1);

      // Get the points that came before
      for (int j = 0; j < i; j+=2) {
        int prevX = borderPoints.get(j);
        int prevY = borderPoints.get(j + 1);
      }
    }
  }

  private static List<Integer> getBorderPoints(Bitmap sourceBitmap, int sourceWidth, int sourceHeight) {
    List<Integer> borderPoints = new ArrayList<>();

    int stride = MAX_STRIDE;
    for (int x = MIN_STRIDE; x < sourceWidth - MIN_STRIDE; x += stride) {
      for (int y = MIN_STRIDE; y < sourceHeight - MIN_STRIDE; y += Y_STRIDE) {
        float xMagnitude = DetectionUtil.getLuminance(sourceBitmap, x + MIN_STRIDE, y) - DetectionUtil.getLuminance(sourceBitmap, x - MIN_STRIDE, y);
        float yMagnitude = 0;//getLuminance(sourceBitmap, x, y + MIN_STRIDE) - getLuminance(sourceBitmap, x, y - MIN_STRIDE);
        double normalMagnitude = Math.sqrt(xMagnitude * xMagnitude + yMagnitude * yMagnitude);

        if (normalMagnitude > MAGNITUDE_THRESHOLD) {
          borderPoints.add(x);
          borderPoints.add(y);
          drawBigBox(sourceBitmap, x, y);
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

}
