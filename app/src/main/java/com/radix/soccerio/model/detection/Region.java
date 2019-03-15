package com.radix.soccerio.model.detection;

import android.graphics.Rect;

/**
 * A {@link android.graphics.Rect} and the number of points contained within
 */
public class Region {
  public static final int DISTANCE_THRESHOLD = 300;
  private Rect mRegionBounds;
  private int mPointSize;

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
    double dist = DetectionUtil.getDistance(mRegionBounds.centerX(), mRegionBounds.centerY(), x, y);
    return dist < DISTANCE_THRESHOLD;
  }

  public void consumePoint(int x, int y) {
    mPointSize++;
    mRegionBounds.union(x, y);
  }

  public Rect getRegionBounds() {
    return mRegionBounds;
  }

  public int getContainedPoints() {
    return mPointSize;
  }
}
