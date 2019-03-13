package com.radix.soccerio;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.test.InstrumentationRegistry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AssetsReader {
  private static Context mContext = InstrumentationRegistry.getTargetContext();
  private static AssetManager mAssets = mContext.getAssets();

  public static String readStringFromAssets(String filename) throws IOException {
    StringBuilder stringBuilder = new StringBuilder();
    InputStream json = mAssets.open(filename);
    BufferedReader in = new BufferedReader(new InputStreamReader(json, "UTF-8"));

    String str;
    while ((str = in.readLine()) != null) {
      stringBuilder.append(str);
    }

    in.close();
    return stringBuilder.toString();
  }

  public static Bitmap readBitmapFromAssets(String filename) throws IOException {
    InputStream inputStream = mAssets.open(filename);
    return BitmapFactory.decodeStream(inputStream);
  }
}
