package com.radix.soccerio.model.detection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;

import com.radix.soccerio.util.Jog;
import com.radix.soccerio.util.Stopwatch;
import com.radix.soccerio.util.bitmap.AssetsReader;
import com.radix.soccerio.util.bitmap.BitmapCache;

import java.io.IOException;
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
  private static final boolean DRAW_DEBUG = true;
  private static List<Integer> mBorderPoints = new ArrayList<>();
  private static List<Region> mGeneratedRegions = new ArrayList<>();
  private static Set<Integer> mConsumedIndices = new HashSet<>();

  private Context mAppContext;
  private final Bitmap mBallAnchor;

  public BallDetector(Context applicationContext) {
    mAppContext = applicationContext;
    try {
      mBallAnchor = AssetsReader.readBitmapFromAssets(mAppContext, "ball_color.png");
    } catch (IOException e) {
      throw new RuntimeException("Couldn't read anchor bitmap", e);
    }
  }

  @Override
  public Rect getBallBounds(Bitmap sourceBitmap) {
    Stopwatch.Ticket ticket = Stopwatch.start("get ball bounds");
    final int sourceWidth = sourceBitmap.getWidth();
    final int sourceHeight = sourceBitmap.getHeight();

    // Make a copy
    if (DRAW_DEBUG) {
      sourceBitmap = sourceBitmap.copy(sourceBitmap.getConfig(), true);
    }

    List<Integer> borderPoints = getBorderPoints(sourceBitmap, sourceWidth, sourceHeight);
    List<Region> contiguousRegions = getContiguousRegions(borderPoints);

    if (DRAW_DEBUG) {
      for (Region contiguousRegion : contiguousRegions) {
        drawRegion(sourceBitmap, contiguousRegion);
      }
    }

    // Find the best region and return it
    Region bestRegion = null;
    int bestPointCount = -1;
    for (Region region : contiguousRegions) {
      // Rect bounds = region.getRegionBounds();
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

          if (DRAW_DEBUG) {
            drawBigBox(sourceBitmap, x, y, 25, Color.MAGENTA);
          }
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
