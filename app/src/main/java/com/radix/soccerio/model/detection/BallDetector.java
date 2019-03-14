package com.radix.soccerio.model.detection;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;

import androidx.annotation.ColorInt;

import com.radix.soccerio.util.Jog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    // Bitmap copyBitmap = sourceBitmap.copy(sourceBitmap.getConfig(), true);
    List<Integer> borderPoints = getBorderPoints(sourceBitmap, sourceWidth, sourceHeight);
    List<Region> contiguousRegions = getContiguousRegions(borderPoints);
    // for (Region region : contiguousRegions) {
    //   drawRegion(copyBitmap, region);
    // }

    // Find the best region and return it
    Region bestRegion = null;
    int bestPointCount = -1;
    for (Region region : contiguousRegions) {
      if (bestPointCount < region.getContainedPoints()) {
        bestPointCount = region.getContainedPoints();
        bestRegion = region;
      }
    }

    if (bestRegion != null && bestPointCount > 15) {
      Jog.v(TAG, "Found best region with num points: " + bestPointCount);
      return bestRegion.getRegionBounds();
    } else {
      return null;
    }
  }

  /**
   * All of the {@link Rect}'s that constitute regions.
   *
   * @param borderPoints
   */
  private static List<Region> getContiguousRegions(List<Integer> borderPoints) {
    List<Region> generatedRegions = new ArrayList<>();
    // x indices that currently reside in a Region somewhere
    Set<Integer> consumedIndices = new HashSet<>();

    for (int i = 2; i < borderPoints.size(); i+=2) {
      int currX = borderPoints.get(i);
      int currY = borderPoints.get(i + 1);

      // If the current point is close to an existing region, consume it
      boolean foundRegion = false;
      for (Region region : generatedRegions) {
        if (region.shouldConsumePoint(currX, currY)) {
          // bingo
          consumedIndices.add(i);
          region.consumePoint(currX, currY);
          foundRegion = true;
          break;
        }
      }

      if (foundRegion) {
        continue;
      }

      // Get the points that came before and try to generate a region
      for (int j = 0; j < i; j+=2) {
        if (consumedIndices.contains(j)) {
          continue;
        }

        int prevX = borderPoints.get(j);
        int prevY = borderPoints.get(j + 1);

        if (DetectionUtil.getDistance(currX, currY, prevX, prevY) < Region.DISTANCE_THRESHOLD) {
          Region region = new Region(currX, currY);
          region.consumePoint(prevX, prevY);
          // This point is now done
          consumedIndices.add(j);
          generatedRegions.add(region);

          // Since all previous points are now matched to a Region, we can stop traversing them
          break;
        }
      }
    }

    return generatedRegions;
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
        }
      }
    }

    return borderPoints;
  }

  private static void drawBigBox(Bitmap bitmap, int startX, int startY, int radius, @ColorInt int color) {
    for (int x = 0; x < radius; x++) {
      for (int y = 0; y < radius; y++) {
        bitmap.setPixel(x + startX, y + startY, color);
      }
    }
  }

  private static void drawVerticalLine(Bitmap bitmap, int startX, int startY, int endY) {
    int color = Color.blue(255);
    int thickness = 10;

    int lowerY = Math.min(startY, endY);
    int higherY = Math.max(startY, endY);
    for (int y = lowerY; y < higherY; y++) {
      drawBigBox(bitmap, startX, y, thickness, color);
    }
  }

  private static void drawHorizontalLine(Bitmap bitmap, int startY, int startX, int endX) {
    int color = Color.blue(255);
    int thickness = 10;

    int lowerX = Math.min(startX, endX);
    int higherX = Math.max(startX, endX);
    for (int x = lowerX; x < higherX; x++) {
      drawBigBox(bitmap, x, startY, thickness, color);
    }
  }

  private static void drawRegion(Bitmap bitmap, Region region) {
    Rect regionBounds = region.getRegionBounds();

    // Draw left
    drawVerticalLine(bitmap, regionBounds.left, regionBounds.top, regionBounds.bottom);
    // Draw right
    drawVerticalLine(bitmap, regionBounds.right, regionBounds.top, regionBounds.bottom);

    // Draw top
    drawHorizontalLine(bitmap, regionBounds.top, regionBounds.left, regionBounds.right);
    // Draw bottom
    drawHorizontalLine(bitmap, regionBounds.bottom, regionBounds.left, regionBounds.right);
  }
}
