package com.nightskylab.utils;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/**
 * Handles touch gestures for sky navigation.
 */
public class GestureHandler {

    public interface GestureListener {
        void onPan(float deltaX, float deltaY);

        void onZoom(float scaleFactor);

        void onDoubleTap();

        void onLongPress();

        void onTwoFingerTap();

        void onThreeFingerSwipeUp();

        void onThreeFingerSwipeDown();

        void onTwoFingerDoubleTap();
    }

    private GestureListener listener;
    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;
    private int pointerCount = 0;
    private float lastTouchX = 0;
    private float lastTouchY = 0;

    public GestureHandler(android.content.Context context, GestureListener listener) {
        this.listener = listener;

        // Scale gesture detector for pinch zoom
        scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                if (GestureHandler.this.listener != null) {
                    GestureHandler.this.listener.onZoom(detector.getScaleFactor());
                }
                return true;
            }
        });

        // Regular gesture detector
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                int pointers = e.getPointerCount();
                if (pointers == 1 && GestureHandler.this.listener != null) {
                    GestureHandler.this.listener.onDoubleTap();
                } else if (pointers == 2 && GestureHandler.this.listener != null) {
                    GestureHandler.this.listener.onTwoFingerDoubleTap();
                }
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if (GestureHandler.this.listener != null) {
                    GestureHandler.this.listener.onLongPress();
                }
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }
        });
    }

    public boolean onTouchEvent(MotionEvent event) {
        // Track pointer count
        pointerCount = event.getPointerCount();

        // Handle scale gestures
        scaleDetector.onTouchEvent(event);

        // Handle standard gestures
        gestureDetector.onTouchEvent(event);

        // Handle custom multi-finger gestures
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                lastTouchX = event.getX(0);
                lastTouchY = event.getY(0);

                // Two-finger tap detection
                if (pointerCount == 2 && event.getAction() == MotionEvent.ACTION_POINTER_DOWN) {
                    if (listener != null) {
                        listener.onTwoFingerTap();
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (pointerCount == 1 && !scaleDetector.isInProgress()) {
                    // Single finger drag for panning
                    float x = event.getX();
                    float y = event.getY();
                    float dx = x - lastTouchX;
                    float dy = y - lastTouchY;

                    if (listener != null && (Math.abs(dx) > 1 || Math.abs(dy) > 1)) {
                        listener.onPan(dx, dy);
                    }

                    lastTouchX = x;
                    lastTouchY = y;
                } else if (pointerCount == 3) {
                    // Three-finger swipe detection
                    float y = event.getY(0);
                    float dy = y - lastTouchY;

                    if (Math.abs(dy) > 50) { // Threshold for swipe
                        if (dy > 0 && listener != null) {
                            listener.onThreeFingerSwipeDown();
                        } else if (dy < 0 && listener != null) {
                            listener.onThreeFingerSwipeUp();
                        }
                        lastTouchY = y;
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                break;
        }

        return true;
    }
}
