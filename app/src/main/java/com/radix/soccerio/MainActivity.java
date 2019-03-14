package com.radix.soccerio;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.radix.soccerio.controller.TapAccessibilityService;
import com.radix.soccerio.ui.projection.ProjectionManager;
import com.radix.soccerio.util.Jog;

public class MainActivity extends AppCompatActivity {
  private static final String TAG = MainActivity.class.getName();

  private TextView mTextMessage;
  private ProjectionManager mProjectionManager;

  private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
      = new BottomNavigationView.OnNavigationItemSelectedListener() {

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
      switch (item.getItemId()) {
        case R.id.navigation_home:
          mTextMessage.setText(R.string.title_home);
          return true;
        case R.id.navigation_dashboard:
          mTextMessage.setText(R.string.title_dashboard);
          return true;
        case R.id.navigation_notifications:
          mTextMessage.setText(R.string.title_notifications);
          return true;
      }
      return false;
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mTextMessage = (TextView) findViewById(R.id.message);
    BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
    navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    Activity activity = this;
    mProjectionManager = new ProjectionManager(activity);

    findViewById(R.id.bEnable).setOnClickListener(v -> {
      if (!TapAccessibilityService.isRunning()) {
        Toast.makeText(getApplicationContext(), R.string.service_is_not_running_foh, Toast.LENGTH_LONG).show();
      } else {
        mProjectionManager.startProjection(activity);
      }
    });

    findViewById(R.id.bDisable).setOnClickListener(v -> mProjectionManager.stopProjection());
    findViewById(R.id.bGotoAccSettings).setOnClickListener(v -> gotoAccessibilitySettings());
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (!TapAccessibilityService.isRunning()) {
      Jog.d(TAG, "Service is not running!");
      gotoAccessibilitySettings();
    } else {
      // Handler handler = new Handler();
      // Runnable tapLoopRunnable = () -> {
      //   // Bitmap lastCapturedBitmap = ProjectionManager.getLastCapturedBitmap();
      //   // Jog.d(TAG, "Got bitmap");
      //   // TapAccessibilityService.getInstance().sendTap(689, 2833);
      // };
      // handler.postDelayed(tapLoopRunnable, 10000L);
    }
  }

  private void gotoAccessibilitySettings() {
    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
    startActivity(intent);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    mProjectionManager.onActivityResult(this, requestCode, resultCode, data);
  }
}
