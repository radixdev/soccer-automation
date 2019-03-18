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
import com.radix.soccerio.util.Constants;
import com.radix.soccerio.util.Jog;

public class TapAccessibilityService5 extends AccessibilityService {
  private static final String TAG = TapAccessibilityService5.class.getName();
  private static boolean sIsRunning = false;
  private static TapAccessibilityService5 sInstance = null;

  private IBallDetector mBallDetector;

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
    mBallDetector = new BallDetector(this.getApplicationContext());
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
    // Jog.d(TAG, "Tap gesture to " + x + " " + y + " dispatched? " + result);
  }

  public static boolean isRunning() {
    return sIsRunning;
  }

  public static TapAccessibilityService5 getInstance() {
    if (sInstance == null) {
      Jog.w(TAG, "AccessibilityService is not connected yet.");
      throw new RuntimeException("AccessibilityService is not connected yet.");
    }
    return sInstance;
  }

  public void onScreenBitmapAvailable(Bitmap bitmap) {
    Rect ballBounds = mBallDetector.getBallBounds(bitmap);

    if (ballBounds == null) {
      return;
    }

    // if (ballBounds.bottom < 100) {
    //   return;
    // }
    // Choose a point in the middle, near the bottom
    float tapX = ballBounds.exactCenterX();

    int halfWidth = bitmap.getWidth() / 2;
    int overageX = (int) (tapX - halfWidth) / 7;
    tapX += overageX;
    float tapY = ballBounds.top + (ballBounds.height()) * Constants.REGION_TAP_SCALAR;
    sendTap(tapX, tapY);

    // Try to click below the ball for safety on drops
    sendTap(tapX, tapY + 20);
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
    if (x < 0 || y < 0) {
      return null;
    }
    Path clickPath = new Path();
    clickPath.moveTo(x, y);
    GestureDescription.StrokeDescription clickStroke =
        new GestureDescription.StrokeDescription(clickPath, 0, Constants.TAP_DURATION_MILLIS);
    GestureDescription.Builder clickBuilder = new GestureDescription.Builder();
    clickBuilder.addStroke(clickStroke);
    return clickBuilder.build();
  }
}
