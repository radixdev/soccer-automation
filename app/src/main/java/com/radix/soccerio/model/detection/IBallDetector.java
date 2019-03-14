package com.radix.soccerio.model.detection;

import android.graphics.Bitmap;
import android.graphics.Rect;

public interface IBallDetector {
  Rect getBallBounds(Bitmap bitmap);
}
