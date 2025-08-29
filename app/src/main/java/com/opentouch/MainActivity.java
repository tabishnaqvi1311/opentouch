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
    private Button toggleButton;
    private boolean isServiceRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toggleButton = findViewById(R.id.toggleButton);
        updateToggleButton();

        toggleButton.setOnClickListener(v -> {
            if (isServiceRunning) {
                stopOverlayService();
            } else {
                if (checkOverlayPermission()) {
                    startOverlayService();
                } else {
                    requestOverlayPermission();
                }
            }
        });
    }

    private void updateToggleButton() {
        if (isServiceRunning) {
            toggleButton.setText("OpenTouch ON\n\nTap to Stop");
            toggleButton.setBackgroundColor(0xFF4CAF50); // Green
        } else {
            toggleButton.setText("OpenTouch OFF\n\nTap to Start");
            toggleButton.setBackgroundColor(0xFF757575); // Gray
        }
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
        isServiceRunning = true;
        updateToggleButton();
        Toast.makeText(this, "OpenTouch started", Toast.LENGTH_SHORT).show();
    }

    private void stopOverlayService() {
        Intent intent = new Intent(this, OverlayService.class);
        stopService(intent);
        isServiceRunning = false;
        updateToggleButton();
        Toast.makeText(this, "OpenTouch stopped", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // You could add service state checking here if needed
    }
}