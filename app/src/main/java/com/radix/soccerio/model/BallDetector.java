package com.radix.soccerio.model;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;

public class BallDetector implements IBallDetector {
  private static final String TAG = BallDetector.class.getName();
  private static final int mMaxStride = 15;

  @Override
  public Rect getBallBounds(Bitmap sourceBitmap) {
    Bitmap copyBitmap = sourceBitmap.copy(sourceBitmap.getConfig(), true);

    final int sourceWidth = sourceBitmap.getWidth();
    final int sourceHeight = sourceBitmap.getHeight();

    int stride = mMaxStride;

    for (int x = mMaxStride; x < sourceWidth - mMaxStride; x += stride) {
      for (int y = mMaxStride; y < sourceHeight - mMaxStride; y += stride) {
        float xMagnitude = getLuminance(sourceBitmap, x + stride, y) - getLuminance(sourceBitmap, x - stride, y);
        float yMagnitude = getLuminance(sourceBitmap, x, y + stride) - getLuminance(sourceBitmap, x, y - stride);
        double normalMagnitude = Math.sqrt(xMagnitude * xMagnitude + yMagnitude * yMagnitude);

        // large swaths of the image are just white, so stride on them
        if (normalMagnitude < 0.0001f) {
          stride = Math.min(stride + 1, mMaxStride);
        } else {
          stride = 2;
        }

        int c = (int) normalMagnitude * 255;
        if (normalMagnitude > 0.001) {
          copyBitmap.setPixel(x, y, Color.rgb(c, c, c));
        }
      }
    }
    return null;
  }

  private static float getLuminance(Bitmap bitmap, int x, int y) {
    int pixelColor = bitmap.getPixel(x, y);
    return Color.luminance(pixelColor);
  }
}
