package com.radix.soccerio.controller;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Path;
import android.graphics.Rect;
import android.view.accessibility.AccessibilityEvent;

import com.radix.soccerio.model.detection.BallDetector;
import com.radix.soccerio.model.detection.IBallDetector;
import com.radix.soccerio.util.Jog;

public class TapAccessibilityService3 extends AccessibilityService {
  private static final String TAG = TapAccessibilityService3.class.getName();
  private static boolean sIsRunning = false;
  private static TapAccessibilityService3 sInstance = null;
  private static final int TAP_DURATION_MILLIS = 10;

  private IBallDetector mBallDetector = new BallDetector();

  @Override
  public void onAccessibilityEvent(AccessibilityEvent event) {
    if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
      if (event.getPackageName() != null && event.getClassName() != null) {
        ComponentName componentName = new ComponentName(
            event.getPackageName().toString(),
            event.getClassName().toString()
        );

        ActivityInfo activityInfo = tryGetActivity(componentName);
        boolean isActivity = activityInfo != null;
        if (isActivity)
          Jog.i("CurrentActivity", componentName.flattenToShortString());
      }
    }
  }

  @Override
  public void onInterrupt() {}

  @Override
  protected void onServiceConnected() {
    super.onServiceConnected();
    sInstance = this;
    sIsRunning = true;
    Jog.i(TAG, "Accessibility service connected");
  }

  @Override
  public boolean onUnbind(Intent intent) {
    sIsRunning = false;
    sInstance = null;
    Jog.i(TAG, "Accessibility service unbound");
    return super.onUnbind(intent);
  }

  public void sendTap(float x, float y) {
    boolean result = dispatchGesture(createClick(x, y), null, null);
    Jog.d(TAG, "Tap gesture to " + x + " " + y + " dispatched? " + result);
  }

  public static boolean isRunning() {
    return sIsRunning;
  }

  public static TapAccessibilityService3 getInstance() {
    if (sInstance == null) {
      Jog.w(TAG, "AccessibilityService is not connected yet.");
      throw new RuntimeException("AccessibilityService is not connected yet.");
    }
    return sInstance;
  }

  public void onScreenBitmapAvailable(Bitmap bitmap) {
    Rect ballBounds = mBallDetector.getBallBounds(bitmap);

    if (ballBounds == null || ballBounds.bottom < bitmap.getHeight() * 0.2) {
      Jog.v(TAG, "Skipping bounds at: " + ballBounds);
      return;
    }
    // Choose a point in the middle, about 75% down
    float tapX = ballBounds.exactCenterX();
    float tapY = ballBounds.top + (ballBounds.height()) * 0.15f;
    sendTap(tapX, tapY);
  }

  private ActivityInfo tryGetActivity(ComponentName componentName) {
    try {
      return getPackageManager().getActivityInfo(componentName, 0);
    } catch (PackageManager.NameNotFoundException e) {
      return null;
    }
  }

  /**
   * @param x (x, y) in screen coordinates
   * @param y (x, y) in screen coordinates
   */
  private static GestureDescription createClick(float x, float y) {
    Path clickPath = new Path();
    clickPath.moveTo(x, y);
    GestureDescription.StrokeDescription clickStroke =
        new GestureDescription.StrokeDescription(clickPath, 0, TAP_DURATION_MILLIS);
    GestureDescription.Builder clickBuilder = new GestureDescription.Builder();
    clickBuilder.addStroke(clickStroke);
    return clickBuilder.build();
  }
}
