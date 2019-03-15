package com.radix.soccerio.model.detection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.SystemClock;

import com.radix.soccerio.util.Jog;
import com.radix.soccerio.util.Stopwatch;
import com.radix.soccerio.util.save.BitmapCache;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.ColorInt;

public class BallDetector implements IBallDetector {
  private static final String TAG = BallDetector.class.getName();
  private static final int MAX_STRIDE = 50;
  private static final int Y_STRIDE = 25;
  private static final int MIN_STRIDE = 20;
  private static final float MAGNITUDE_THRESHOLD = 0.101f;
  private static List<Integer> mBorderPoints = new ArrayList<>();
  private static List<Region> mGeneratedRegions = new ArrayList<>();
  private static Set<Integer> mConsumedIndices = new HashSet<>();

  private long mLastDetectTime = 1;
  private Region mLastRegion = null;
  private Context mAppContext;

  public BallDetector(Context applicationContext) {
    mAppContext = applicationContext;
  }

  @Override
  public Rect getBallBounds(Bitmap sourceBitmap) {
    Stopwatch.Ticket ticket = Stopwatch.start("get ball bounds");
    final int sourceWidth = sourceBitmap.getWidth();
    final int sourceHeight = sourceBitmap.getHeight();

    List<Integer> borderPoints = getBorderPoints(sourceBitmap, sourceWidth, sourceHeight);
    List<Region> contiguousRegions = getContiguousRegions(borderPoints);

    // Bitmap copyBitmap = sourceBitmap.copy(sourceBitmap.getConfig(), true);
    // for (Region contiguousRegion : contiguousRegions) {
    //   drawRegion(copyBitmap, contiguousRegion);
    // }

    // Find the best region and return it
    Region bestRegion = null;
    int bestPointCount = -1;
    for (Region region : contiguousRegions) {
      Rect bounds = region.getRegionBounds();
      final int containedPoints = region.getContainedPoints();
      if (bestPointCount < containedPoints && containedPoints > 10) {
        bestPointCount = containedPoints;
        bestRegion = region;
      }
    }

    if (bestRegion != null) {
      ticket.report();
      Jog.v(TAG, "Found best region with num points: " + bestPointCount
          + " num regions total " + contiguousRegions.size()
          + " and region: "
          + bestRegion.getRegionBounds() + " with dims: " + bestRegion.getRegionBounds().width()
          + " " + bestRegion.getRegionBounds().height());

      if (contiguousRegions.size() >= 5) {
        Jog.d(TAG, "Wrote bitmap");
        BitmapCache.saveBitmap(mAppContext, sourceBitmap);
      }

      double xVel = 0;
      double yVel = 0;
      if (mLastRegion != null) {
        long delta = SystemClock.uptimeMillis() - mLastDetectTime;
        printLastRegionStats(bestRegion, delta);
        xVel = ((double) bestRegion.getRegionBounds().centerX() - (double) mLastRegion.getRegionBounds().centerX()) / delta;
        yVel = ((double) bestRegion.getRegionBounds().centerY() - (double) mLastRegion.getRegionBounds().centerY()) / delta;
      }

      mLastRegion = bestRegion;
      mLastDetectTime = SystemClock.uptimeMillis();

      // Shift by the delta
      Rect regionBounds = bestRegion.getRegionBounds();
      float scalar = 2;
      // regionBounds.offset((int) (xVel * scalar), (int) (yVel * scalar));

      return regionBounds;
    } else {
      return null;
    }
  }

  private void printLastRegionStats(Region newRegion, float delta) {
    double yVel = (newRegion.getRegionBounds().centerY() - mLastRegion.getRegionBounds().centerY()) / delta;
    Jog.d(TAG, "yvel: " + yVel);
  }

  /**
   * All of the {@link Rect}'s that constitute regions.
   *
   * @param borderPoints
   */
  private static List<Region> getContiguousRegions(List<Integer> borderPoints) {
    mGeneratedRegions.clear();
    // x indices that currently reside in a Region somewhere
    mConsumedIndices.clear();

    for (int i = 2; i < borderPoints.size(); i+=2) {
      int currX = borderPoints.get(i);
      int currY = borderPoints.get(i + 1);

      // If the current point is close to an existing region, consume it
      boolean foundRegion = false;
      for (Region region : mGeneratedRegions) {
        if (region.shouldConsumePoint(currX, currY)) {
          // bingo
          mConsumedIndices.add(i);
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
        if (mConsumedIndices.contains(j)) {
          continue;
        }

        int prevX = borderPoints.get(j);
        int prevY = borderPoints.get(j + 1);

        if (DetectionUtil.getDistance(currX, currY, prevX, prevY) < Region.DISTANCE_THRESHOLD) {
          Region region = new Region(currX, currY);
          region.consumePoint(prevX, prevY);
          // This point is now done
          mConsumedIndices.add(j);
          mGeneratedRegions.add(region);

          // Since all previous points are now matched to a Region, we can stop traversing them
          break;
        }
      }
    }

    return mGeneratedRegions;
  }

  private static List<Integer> getBorderPoints(Bitmap sourceBitmap, int sourceWidth, int sourceHeight) {
    mBorderPoints.clear();

    for (int y = MIN_STRIDE; y < sourceHeight - MIN_STRIDE; y += Y_STRIDE) {
      for (int x = MIN_STRIDE; x < sourceWidth - MIN_STRIDE; x += MAX_STRIDE) {
        float xMagnitude = DetectionUtil.getLuminance(sourceBitmap, x + MIN_STRIDE, y) - DetectionUtil.getLuminance(sourceBitmap, x - MIN_STRIDE, y);
        if (xMagnitude > MAGNITUDE_THRESHOLD) {
          mBorderPoints.add(x);
          mBorderPoints.add(y);
        }
      }
    }

    return mBorderPoints;
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
