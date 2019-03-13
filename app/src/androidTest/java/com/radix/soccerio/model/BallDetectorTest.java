package com.radix.soccerio.model;

import android.graphics.Bitmap;
import android.graphics.Rect;

import androidx.test.runner.AndroidJUnit4;

import com.radix.soccerio.AssetsReader;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class BallDetectorTest {
  @Test
  public void testGeneralDetection() throws IOException {
    Bitmap ball = AssetsReader.readBitmapFromAssets("gameshots/ball5.png");
    BallDetector ballDetector = new BallDetector();

    Rect bounds = ballDetector.getBallBounds(ball);
    Rect expected = new Rect(200, 1606, 626, 2046);
    assertRectBoundsMatch(expected, bounds, 100);
  }

  private static void assertRectBoundsMatch(Rect expected, Rect actual, int threshold) {
    assertTrue("Bottom does not match", Math.abs(expected.bottom - actual.bottom) < threshold);
    assertTrue("Left does not match", Math.abs(expected.left - actual.left) < threshold);
    assertTrue("Top does not match", Math.abs(expected.top - actual.top) < threshold);
    assertTrue("Right does not match", Math.abs(expected.right - actual.right) < threshold);
  }
}