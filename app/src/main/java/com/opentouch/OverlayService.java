package com.opentouch;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class OverlayService extends Service {
    private WindowManager windowManager;
    private View overlayView;
    private View expandedView;
    private AudioManager audioManager;
    private boolean isExpanded = false;

    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        createOverlayView();
    }

    private void createOverlayView() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        overlayView = inflater.inflate(R.layout.overlay_layout, null);

        int layoutFlag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutFlag = WindowManager.LayoutParams.TYPE_PHONE;
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.END;
        params.x = 50;
        params.y = 200;

        windowManager.addView(overlayView, params);

        setupViews();
        setupDragListener(params);
    }

    private void setupViews() {
        ImageButton mainButton = overlayView.findViewById(R.id.mainButton);
        expandedView = overlayView.findViewById(R.id.expandedView);
        ImageButton volumeUpButton = overlayView.findViewById(R.id.volumeUpButton);
        ImageButton volumeDownButton = overlayView.findViewById(R.id.volumeDownButton);
        ImageButton closeButton = overlayView.findViewById(R.id.closeButton);

        // Initially hide expanded view
        expandedView.setVisibility(View.GONE);

        mainButton.setOnClickListener(v -> {
            toggleExpanded();
        });

        volumeUpButton.setOnClickListener(v -> {
            audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
        });

        volumeDownButton.setOnClickListener(v -> {
            audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
        });

        closeButton.setOnClickListener(v -> {
            stopSelf();
        });
    }

    private void toggleExpanded() {
        if (isExpanded) {
            expandedView.setVisibility(View.GONE);
            isExpanded = false;
        } else {
            expandedView.setVisibility(View.VISIBLE);
            isExpanded = true;
        }
    }

    private void setupDragListener(WindowManager.LayoutParams params) {
        overlayView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private boolean isDragging = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        isDragging = false;
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!isDragging) {
                            // If not dragging, treat as click on main button
                            if (!isExpanded) {
                                toggleExpanded();
                            }
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float deltaX = Math.abs(event.getRawX() - initialTouchX);
                        float deltaY = Math.abs(event.getRawY() - initialTouchY);

                        if (deltaX > 10 || deltaY > 10) {
                            isDragging = true;
                            params.x = initialX + (int) (event.getRawX() - initialTouchX);
                            params.y = initialY + (int) (event.getRawY() - initialTouchY);
                            windowManager.updateViewLayout(overlayView, params);
                        }
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (overlayView != null) {
            windowManager.removeView(overlayView);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}