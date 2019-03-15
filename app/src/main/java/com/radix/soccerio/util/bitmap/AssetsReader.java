package com.radix.soccerio.util.bitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;

public class AssetsReader {
  public static Bitmap readBitmapFromAssets(Context context, String filename) throws IOException {
    InputStream inputStream = context.getAssets().open(filename);
    return BitmapFactory.decodeStream(inputStream);
  }
}
