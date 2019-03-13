package com.radix.soccerio.controller;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;

import com.radix.soccerio.util.Jog;

public class TapAccessibilityService extends AccessibilityService {
  private static final String TAG = TapAccessibilityService.class.getName();
  private static boolean sIsRunning = false;
  private static TapAccessibilityService sInstance = null;
  private static final int TAP_DURATION_MILLIS = 10;

  @Override
  public void onAccessibilityEvent(AccessibilityEvent event) {}

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
    Jog.d(TAG, "Gesture dispatched? " + result);
  }

  public static boolean isRunning() {
    return sIsRunning;
  }

  public static TapAccessibilityService getInstance() {
    if (sInstance == null) {
      Jog.w(TAG, "TapAccessibilityService is not connected yet.");
      throw new RuntimeException("TapAccessibilityService is not connected yet.");
    }
    return sInstance;
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
