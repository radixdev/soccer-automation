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
}
