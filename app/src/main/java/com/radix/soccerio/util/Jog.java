package com.radix.soccerio.util;

import android.util.Log;

import com.radix.soccerio.BuildConfig;

/**
 * A logger that does nothing out of DEBUG mode!
 */
public class Jog {
  private static boolean sLoggingEnabled = true;

  public static void setLoggingEnabled(boolean enabled) {
    sLoggingEnabled = enabled;
  }

  public static void v(String tag, String msg) {
    if (!sLoggingEnabled) return;

    if (BuildConfig.DEBUG) {
      Log.v(tag, msg);
    }
  }

  public static void d(String tag, String msg) {
    if (!sLoggingEnabled) return;

    if (BuildConfig.DEBUG) {
      Log.d(tag, msg);
    }
  }

  public static void i(String tag, String msg) {
    if (!sLoggingEnabled) return;

    if (BuildConfig.DEBUG) {
      Log.i(tag, msg);
    }
  }

  public static void w(String tag, String msg) {
    if (!sLoggingEnabled) return;

    if (BuildConfig.DEBUG) {
      Log.w(tag, msg);
    }
  }

  public static void w(String tag, String msg, Throwable tr) {
    if (!sLoggingEnabled) return;

    if (BuildConfig.DEBUG) {
      Log.w(tag, msg, tr);
    }
  }

  public static void e(String tag, String msg, Throwable tr) {
    if (!sLoggingEnabled) return;

    if (BuildConfig.DEBUG) {
      Log.e(tag, msg, tr);
    }
  }
}
