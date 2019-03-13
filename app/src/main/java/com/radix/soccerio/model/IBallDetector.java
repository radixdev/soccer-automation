package com.radix.soccerio.model;

import android.graphics.Bitmap;
import android.graphics.Rect;

public interface IBallDetector {
  Rect getBallBounds(Bitmap bitmap);
}
