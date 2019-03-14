package com.radix.soccerio.util;

import android.os.SystemClock;

import androidx.annotation.NonNull;

import com.radix.soccerio.BuildConfig;

import java.util.HashMap;
import java.util.UUID;

public class Stopwatch {
  private static final String TAG = Stopwatch.class.getName();

  /**
   * A {@link Ticket} to return in production
   */
  private static final Ticket PRODUCTION_TICKET = new Ticket("no", "release mode lol");
  private static final HashMap<String, Long> TIMER_MAP = new HashMap<>();

  /**
   * Starts a new timer. Returns a {@link Ticket} identifying this timer.
   */
  public static Ticket start(String timerMessage) {
    // Why not lol
    if (!BuildConfig.DEBUG) {
      return PRODUCTION_TICKET;
    }

    // Add the message to the map
    final Ticket ticket = new Ticket(UUID.randomUUID().toString(), timerMessage);
    TIMER_MAP.put(ticket.getId(), getCurrentTime());
    return ticket;
  }

  public static void report(@NonNull Ticket ticket) {
    report(ticket, true);
  }

  public static void report(@NonNull Ticket ticket, boolean deleteAfterPosting) {
    if (!BuildConfig.DEBUG) {
      return;
    }

    String id = ticket.getId();
    if (!TIMER_MAP.containsKey(id)) {
      Jog.w(TAG, "Map doesn't contain id: " + id);
      return;
    }

    // Get the starter message
    long startTime = TIMER_MAP.get(id);
    long currentTime = getCurrentTime();

    printTime(ticket.getMessage(), currentTime - startTime);

    if (deleteAfterPosting) {
      // Remove the id from the map
      TIMER_MAP.remove(id);
    }
  }

  private static void printTime(String message, long duration) {
    Jog.d(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    Jog.d(TAG, message + " -> " + duration + " ms");
    Jog.d(TAG, "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
  }

  /**
   * Time for reference, in milliseconds
   */
  private static long getCurrentTime() {
    return SystemClock.uptimeMillis();
  }

  public static class Ticket {
    private final String mId;
    private final String mMessage;

    public Ticket(String id, String message) {
      this.mId = id;
      mMessage = message;
    }

    public String getId() {
      return mId;
    }

    public String getMessage() {
      return mMessage;
    }

    public void report() {
      Stopwatch.report(this);
    }

    public void reportAndContinue() {
      Stopwatch.report(this, false);
    }
  }
}
