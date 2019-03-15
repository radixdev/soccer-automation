package com.radix.soccerio.model;

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.radix.soccerio.AssetsReader;
import com.radix.soccerio.model.detection.BallDetector;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class BallDetectorTest {
  private static final int REGION_RECT_BOUND_THRESHOLD = 100;

  private BallDetector mBallDetector;

  @Before
  public void setUp() throws Exception {
    mBallDetector = new BallDetector(InstrumentationRegistry.getTargetContext());
  }

  @Test
  public void testGeneralDetection_Ball1() throws IOException {
    Bitmap ball = AssetsReader.readBitmapFromAssets("gameshots/ball1.png");

    Rect bounds = mBallDetector.getBallBounds(ball);
    Rect expected = new Rect(503, 2449, 944, 2878);
    assertRectBoundsMatch(expected, bounds, REGION_RECT_BOUND_THRESHOLD);
  }

  @Test
  public void testGeneralDetection_Ball2() throws IOException {
    Bitmap ball = AssetsReader.readBitmapFromAssets("gameshots/ball2.png");

    Rect bounds = mBallDetector.getBallBounds(ball);
    Rect expected = new Rect(642, 1632, 1096, 2070);
    assertRectBoundsMatch(expected, bounds, REGION_RECT_BOUND_THRESHOLD);
  }

  @Test
  public void testGeneralDetection_Ball3() throws IOException {
    Bitmap ball = AssetsReader.readBitmapFromAssets("gameshots/ball3.png");

    Rect bounds = mBallDetector.getBallBounds(ball);
    Rect expected = new Rect(200, 1606, 626, 2046);
    assertRectBoundsMatch(expected, bounds, REGION_RECT_BOUND_THRESHOLD);
  }

  @Test
  public void testGeneralDetection_Ball4() throws IOException {
    Bitmap ball = AssetsReader.readBitmapFromAssets("gameshots/ball4.png");

    Rect bounds = mBallDetector.getBallBounds(ball);
    Rect expected = new Rect(510, 2088, 960, 2528);
    assertRectBoundsMatch(expected, bounds, REGION_RECT_BOUND_THRESHOLD);
  }

  @Test
  public void testGeneralDetection_Ball6() throws IOException {
    Bitmap ball = AssetsReader.readBitmapFromAssets("gameshots/ball6.png");

    Rect bounds = mBallDetector.getBallBounds(ball);
    Rect expected = new Rect(510, 2088, 960, 2528);
    assertRectBoundsMatch(expected, bounds, REGION_RECT_BOUND_THRESHOLD);
  }

  private static void assertRectBoundsMatch(Rect expected, Rect actual, int threshold) {
    assertTrue("Bottom does not match. Expected:  " + expected + " actual: " + actual,
        Math.abs(expected.bottom - actual.bottom) < threshold);
    assertTrue("Left does not match. Expected:  " + expected + " actual: " + actual,
        Math.abs(expected.left - actual.left) < threshold);
    assertTrue("Top does not match. Expected:  " + expected + " actual: " + actual,
        Math.abs(expected.top - actual.top) < threshold);
    assertTrue("Right does not match. Expected:  " + expected + " actual: " + actual,
        Math.abs(expected.right - actual.right) < threshold);
  }
}
