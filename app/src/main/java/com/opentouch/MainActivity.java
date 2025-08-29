package com.opentouch;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int OVERLAY_PERMISSION_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startButton = findViewById(R.id.startButton);
        Button stopButton = findViewById(R.id.stopButton);

        startButton.setOnClickListener(v -> {
            if (checkOverlayPermission()) {
                startOverlayService();
            } else {
                requestOverlayPermission();
            }
        });

        stopButton.setOnClickListener(v -> stopOverlayService());
    }

    private boolean checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        }
        return true;
    }

    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQUEST) {
            if (checkOverlayPermission()) {
                startOverlayService();
            } else {
                Toast.makeText(this, "Overlay permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startOverlayService() {
        Intent intent = new Intent(this, OverlayService.class);
        startService(intent);
        Toast.makeText(this, "OpenTouch started", Toast.LENGTH_SHORT).show();
    }

    private void stopOverlayService() {
        Intent intent = new Intent(this, OverlayService.class);
        stopService(intent);
        Toast.makeText(this, "OpenTouch stopped", Toast.LENGTH_SHORT).show();
    }
}