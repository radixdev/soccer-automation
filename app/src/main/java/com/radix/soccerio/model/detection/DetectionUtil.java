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
  public static float getDistance(int x1, int y1, int x2, int y2) {
    return Math.abs(x2 - x1) + Math.abs(y2 - y1);
  }

  static float getLuminance(Bitmap bitmap, int x, int y) {
    int pixelColor = bitmap.getPixel(x, y);
    return Color.luminance(pixelColor);
  }
}
