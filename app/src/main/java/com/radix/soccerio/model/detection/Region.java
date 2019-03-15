package com.radix.soccerio.model.detection;

import android.graphics.Rect;

/**
 * A {@link android.graphics.Rect} and the number of points contained within
 */
public class Region {
  static final float DISTANCE_THRESHOLD = 150;
  private Rect mRegionBounds;
  private int mPointSize;

  private boolean mIsConsumed = false;

  /**
   * Starts the region!
   *
   * @param x
   * @param y
   */
  Region(int x, int y) {
    mPointSize = 1;
    mRegionBounds = new Rect(x - 1, y - 1, x + 1, y + 1);
  }

  public boolean isConsumed() {
    return mIsConsumed;
  }

  public void setConsumed(boolean consumed) {
    mIsConsumed = consumed;
  }

  /**
   * Should this point be consumed by this {@link Region}??!!
   */
  boolean shouldConsumePoint(int x, int y) {
    if (mRegionBounds.contains(x, y)) {
      return true;
    }

    // Treat the region like a circle lol
    double dist = DetectionUtil.getDistance(mRegionBounds.centerX(), mRegionBounds.centerY(), x, y);
    float threshold = DISTANCE_THRESHOLD / (mPointSize);
    return dist < threshold;
  }

  void consumePoint(int x, int y) {
    mPointSize++;
    mRegionBounds.union(x, y);
  }

  boolean shouldConsumeRegion(Region otherRegion) {
    return Rect.intersects(mRegionBounds, otherRegion.mRegionBounds);
  }

  void consumeRegion(Region otherRegion) {
    mPointSize += otherRegion.mPointSize;
    mRegionBounds.union(otherRegion.getRegionBounds());
  }

  public Rect getRegionBounds() {
    return mRegionBounds;
  }

  public int getContainedPoints() {
    return mPointSize;
  }
}
