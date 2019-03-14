package com.radix.soccerio.ui.projection;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.OrientationEventListener;

import com.radix.soccerio.controller.TapAccessibilityService3;
import com.radix.soccerio.util.Constants;
import com.radix.soccerio.util.Jog;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

/**
 * Delegation class for all the bitmap stuffs
 *
 * https://github.com/mtsahakis/MediaProjectionDemo/blob/master/src/com/mtsahakis/mediaprojectiondemo/ScreenCaptureImageActivity.java
 */
public class ProjectionManager {
  private static final String TAG = ProjectionManager.class.getName();
  private static final int REQUEST_CODE = 100;
  private static final int VIRTUAL_DISPLAY_FLAGS = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
  private static final int IMAGE_READER_MAX_IMAGES = 1;
  private final MediaProjectionManager mProjectionManager;
  private static MediaProjection sMediaProjection;
  /**
   * The last captured bitmap, or null.
   */
  private static Bitmap mLastCapturedBitmap = null;
  private ImageReader mImageReader;
  private Handler mHandler;
  private Display mDisplay;
  private VirtualDisplay mVirtualDisplay;
  private int mDensity;
  private int mWidth;
  private int mHeight;
  private int mRotation;
  private OrientationChangeCallback mOrientationChangeCallback;
  private long mLastScreenshotTimeMillis = SystemClock.uptimeMillis() + TimeUnit.SECONDS.toMillis(10);

  public ProjectionManager(Context context) {
    // call for the projection manager
    mProjectionManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);

    // start capture handling thread
    new Thread() {
      @Override
      public void run() {
        Looper.prepare();
        mHandler = new Handler();
        Looper.loop();
      }
    }.start();
  }

  public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CODE) {
      sMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);

      if (sMediaProjection != null) {
        // Display metrics
        DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
        mDensity = metrics.densityDpi;
        mDisplay = activity.getWindowManager().getDefaultDisplay();

        // Create virtual display based on device width / height
        createVirtualDisplay();

        // Register orientation change callback
        mOrientationChangeCallback = new OrientationChangeCallback(activity);
        if (mOrientationChangeCallback.canDetectOrientation()) {
          mOrientationChangeCallback.enable();
        }

        // Register media projection stop callback
        sMediaProjection.registerCallback(new MediaProjectionStopCallback(), mHandler);
      }
    }
  }

  public void startProjection(Activity activity) {
    activity.startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
    mLastScreenshotTimeMillis = SystemClock.uptimeMillis() + TimeUnit.SECONDS.toMillis(5);
  }

  public void stopProjection() {
    Jog.i(TAG, "Stop projection called");
    mHandler.post(() -> {
      if (sMediaProjection != null) {
        sMediaProjection.stop();
      }
    });
  }

  private void createVirtualDisplay() {
    // get width and height
    Point size = new Point();
    mDisplay.getSize(size);
    mWidth = size.x;
    mHeight = size.y;

    Jog.d(TAG, "Virtual display width: " + mWidth + " and height: " + mHeight);

    // Start capture reader
    mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, IMAGE_READER_MAX_IMAGES);
    mVirtualDisplay = sMediaProjection.createVirtualDisplay("screen_capture", mWidth, mHeight, mDensity,
        VIRTUAL_DISPLAY_FLAGS, mImageReader.getSurface(), null, mHandler);
    mImageReader.setOnImageAvailableListener(new ImageAvailableListener(), mHandler);
  }

  private class ImageAvailableListener implements ImageReader.OnImageAvailableListener {
    @Override
    public void onImageAvailable(ImageReader reader) {
      try (Image image = reader.acquireNextImage()) {
        if (image != null) {
          if (SystemClock.uptimeMillis() - mLastScreenshotTimeMillis < Constants.MIN_BITMAP_RETRIEVAL_INTERVAL_MILLIS) {
            // Skip until the next image
            image.close();
          } else {
            Image.Plane[] planes = image.getPlanes();
            ByteBuffer buffer = planes[0].getBuffer();
            int pixelStride = planes[0].getPixelStride();
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * mWidth;

            // create bitmap
            mLastCapturedBitmap = Bitmap.createBitmap(mWidth + rowPadding / pixelStride, mHeight, Bitmap.Config.ARGB_8888);
            mLastCapturedBitmap.copyPixelsFromBuffer(buffer);
            image.close();

            mLastScreenshotTimeMillis = SystemClock.uptimeMillis();
            TapAccessibilityService3.getInstance().onScreenBitmapAvailable(mLastCapturedBitmap);
          }
        }
      } catch (Exception e) {
        Jog.e(TAG, "Failed to capture new image bitmap", e);
      }
    }
  }

  private class OrientationChangeCallback extends OrientationEventListener {
    OrientationChangeCallback(Context context) {
      super(context);
    }

    @Override
    public void onOrientationChanged(int orientation) {
      final int rotation = mDisplay.getRotation();
      if (rotation != mRotation) {
        mRotation = rotation;
        try {
          // clean up
          if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
          }
          if (mImageReader != null) {
            mImageReader.setOnImageAvailableListener(null, null);
          }

          // re-create virtual display depending on device width / height
          createVirtualDisplay();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  private class MediaProjectionStopCallback extends MediaProjection.Callback {
    @Override
    public void onStop() {
      Jog.w(TAG, "stopping projection.");
      mHandler.post(() -> {
        if (mVirtualDisplay != null) {
          mVirtualDisplay.release();
        }
        if (mImageReader != null) {
          mImageReader.setOnImageAvailableListener(null, null);
        }
        if (mOrientationChangeCallback != null) {
          mOrientationChangeCallback.disable();
        }
        sMediaProjection.unregisterCallback(this);
      });
    }
  }
}
