package com.radix.soccerio.model.detection;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 *
 */
public class DetectionUtil {
  /**
   * Gets the Manhattan Distance between the points
   *
   * @param x1
   * @param y1
   * @param x2
   * @param y2
   * @return
   */
  public static float getManhattanDistance(int x1, int y1, int x2, int y2) {
    return Math.abs(x2 - x1) + Math.abs(y2 - y1);
  }

  public static double getDistance(int x1, int y1, int x2, int y2) {
    int xDiff = x2 - x1;
    int yDiff = y2 - y1;
    return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
  }

  static float getLuminance(Bitmap bitmap, int x, int y) {
    if (x < 0 || y < 0 || x > bitmap.getWidth() || y > bitmap.getHeight()) {
      return 0;
    }
    int pixelColor = bitmap.getPixel(x, y);
    return Color.luminance(pixelColor);
  }
}
