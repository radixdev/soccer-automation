package com.radix.soccerio.model.detection;

import android.graphics.Rect;

/**
 * A {@link android.graphics.Rect} and the number of points contained within
 */
public class Region {
  private Rect mRegionBounds = null;
  private int mPointSize = 0;

  /**
   * Starts the region!
   * @param x
   * @param y
   */
  public Region(int x, int y) {
    mPointSize = 1;
    mRegionBounds = new Rect(x - 1, y - 1, x + 1, y + 1);
  }

  /**
   * Should this point be consumed by this {@link Region}??!!
   */
  public boolean shouldConsumePoint(int x, int y) {
    if (mRegionBounds.contains(x, y)) {
      return true;
    }

    // Treat the region like a circle lol
    int regionRadius = (mRegionBounds.width() + mRegionBounds.height()) / 2;

    double dist = DetectionUtil.getDistance(mRegionBounds.centerX(), mRegionBounds.centerY(), x, y);
    return dist < 100;
  }

  public void consumePoint(int x, int y) {
    mPointSize++;
    mRegionBounds.union(x, y);
  }
}
