package com.radix.soccerio.model.detection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;

import com.radix.soccerio.util.Jog;
import com.radix.soccerio.util.Stopwatch;
import com.radix.soccerio.util.bitmap.AssetsReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import androidx.annotation.ColorInt;

public class BallDetector implements IBallDetector {
  private static final String TAG = BallDetector.class.getName();
  private static final int MAX_STRIDE = 30;
  private static final int Y_STRIDE = 20;
  private static final int MIN_STRIDE = 20;
  private static final float MAGNITUDE_THRESHOLD = 0.801f;
  private static final boolean DRAW_DEBUG = true;
  private static List<Integer> mBorderPoints = new ArrayList<>();
  private static List<Region> mGeneratedRegions = new ArrayList<>();
  private static Set<Integer> mConsumedIndices = new HashSet<>();

  private Context mAppContext;
  private final List<Integer> mAnchorColors;

  public BallDetector(Context applicationContext) {
    mAppContext = applicationContext;
    try {
      Bitmap anchor = AssetsReader.readBitmapFromAssets(mAppContext, "ball_color.png");
      mAnchorColors = getAnchorColors(anchor);
      anchor.recycle();
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
    Bitmap copy;
    if (DRAW_DEBUG) {
      copy = sourceBitmap.copy(sourceBitmap.getConfig(), true);
    }

    List<Integer> borderPoints = getBorderPoints(copy, sourceBitmap, sourceWidth, sourceHeight);
    List<Region> contiguousRegions = mergeRegions(mergeRegions(getContiguousRegions(borderPoints)));

    if (DRAW_DEBUG) {
      for (Region contiguousRegion : contiguousRegions) {
        drawRegion(copy, contiguousRegion);
      }
    }

    // Find the best region and return it
    Region bestRegion = null;
    int bestPointCount = -1;
    for (Region region : contiguousRegions) {
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

      // if (contiguousRegions.size() >= 5) {
      //   Jog.d(TAG, "Wrote bitmap");
      //   BitmapCache.saveBitmap(mAppContext, sourceBitmap);
      // }

      return bestRegion.getRegionBounds();
    } else {
      return null;
    }
  }

  private List<Integer> getBorderPoints(Bitmap copyBitmap, Bitmap sourceBitmap, int sourceWidth, int sourceHeight) {
    mBorderPoints.clear();

    for (int y = MIN_STRIDE; y < sourceHeight - MIN_STRIDE; y += Y_STRIDE) {
      for (int x = MIN_STRIDE; x < sourceWidth - MIN_STRIDE; x += MAX_STRIDE) {
        final int bitmapPixel = sourceBitmap.getPixel(x, y);
        if (Color.luminance(bitmapPixel) > 0.8f || Color.alpha(bitmapPixel) < 0.1f) {
          continue;
        }

        float xMagnitude = DetectionUtil.getLuminance(sourceBitmap, x + MIN_STRIDE, y) - DetectionUtil.getLuminance(sourceBitmap, x - MIN_STRIDE, y);
        float yMagnitude = DetectionUtil.getLuminance(sourceBitmap, x, y + MIN_STRIDE) - DetectionUtil.getLuminance(sourceBitmap, x, y - MIN_STRIDE);
        double normalMagnitude = Math.sqrt(xMagnitude * xMagnitude + yMagnitude * yMagnitude);

        if (normalMagnitude > MAGNITUDE_THRESHOLD) {
          int closeColorsFound = 0;
          for (int anchorColor : mAnchorColors) {
            if (getColorDistance(bitmapPixel, anchorColor) < 50) {
              closeColorsFound++;
            }
          }

          if (closeColorsFound < 3) {
            continue;
          }

          mBorderPoints.add(x);
          mBorderPoints.add(y);

          if (DRAW_DEBUG) {
            drawBigBox(copyBitmap, x, y, 25, Color.MAGENTA);
          }
        }
      }
    }

    return mBorderPoints;
  }

  private static List<Region> mergeRegions(List<Region> regions) {
    for (Region next : regions) {
      if (next.isConsumed()) {
        continue;
      }

      for (Region region : regions) {
        if (next.shouldConsumeRegion(region) && region != next && !region.isConsumed()) {
          next.consumeRegion(region);
          region.setConsumed(true);
        }
      }
    }

    for (Iterator<Region> iterator = regions.iterator(); iterator.hasNext(); ) {
      Region next = iterator.next();
      if (next.isConsumed()) {
        iterator.remove();
      }
    }

    return regions;
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

  private static List<Integer> getAnchorColors(Bitmap bitmap) {
    List<Integer> colors = new ArrayList<>();
    final int sourceWidth = bitmap.getWidth();
    final int sourceHeight = bitmap.getHeight();

    final int stride = (int) (Math.sqrt(sourceHeight * sourceWidth / 32f));
    for (int x = 0; x < sourceWidth; x += stride) {
      for (int y = 0; y < sourceHeight; y += stride) {
        colors.add(bitmap.getPixel(x, y));
      }
    }

    return colors;
  }

  private static double getColorDistance(int c1, int c2) {
    int r = Color.red(c1) - Color.red(c2);
    int g = Color.green(c1) - Color.green(c2);
    int b = Color.blue(c1) - Color.blue(c2);
    return Math.sqrt(r * r + g * g + b * b);
  }

  private static void drawBigBox(Bitmap bitmap, int startX, int startY, int radius, @ColorInt int color) {
    for (int x = 0; x < radius; x++) {
      for (int y = 0; y < radius; y++) {
        bitmap.setPixel(x + startX, y + startY, color);
      }
    }
  }

  private static void drawVerticalLine(Bitmap bitmap, int startX, int startY, int endY) {
    int color = Color.BLUE;
    int thickness = 10;

    int lowerY = Math.min(startY, endY);
    int higherY = Math.max(startY, endY);
    for (int y = lowerY; y < higherY; y++) {
      drawBigBox(bitmap, startX, y, thickness, color);
    }
  }

  private static void drawHorizontalLine(Bitmap bitmap, int startY, int startX, int endX) {
    int color = Color.BLUE;
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
