package com.nightskylab.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.nightskylab.astronomy.AstronomyEngine;
import com.nightskylab.astronomy.CelestialBody;
import com.nightskylab.astronomy.Coordinates;
import com.nightskylab.data.ConstellationData;
import com.nightskylab.data.StarCatalog;
import com.nightskylab.utils.GestureHandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Custom view that renders the night sky with stars, planets, and
 * constellations.
 * Uses a fish-eye/globe projection similar to the web version.
 */
public class SkyView extends View implements GestureHandler.GestureListener {

    // Display state
    private float centerX, centerY;
    private float globeRadius = 400f;

    // Field of view scale for zoom (affects projection, not globe size)
    private double fovScale = 1.0;
    private double minFovScale = 0.3; // Zoomed in (narrow FOV)
    private double maxFovScale = 2.5; // Zoomed out (wide FOV)

    // Observer location
    private double latitude = 38.7; // Lisbon default
    private double longitude = -9.1;
    private String locationName = "Lisbon";

    // View direction
    private double viewAzimuth = Math.PI; // South
    private double viewAltitude = Math.PI / 6; // 30 degrees up

    // Time
    private Date currentDate = new Date();
    private double timeSpeed = 1.0; // Real-time
    private boolean paused = false;
    private long lastFrameTime = System.currentTimeMillis();

    // Display modes
    public enum ConstellationMode {
        OFF, LINES, LINES_NAMES
    }

    public enum StarNameMode {
        OFF, BRIGHT, ALL
    }

    private ConstellationMode constellationMode = ConstellationMode.LINES;
    private StarNameMode starNameMode = StarNameMode.OFF;
    private boolean gridVisible = false;

    // Data
    private StarCatalog starCatalog;
    private ConstellationData constellationData;
    private List<CelestialBody> planets;
    private CelestialBody sun, moon;

    // Rendering
    private Paint skyPaint, starPaint, planetPaint, linePaint, textPaint, gridPaint;
    private GestureHandler gestureHandler;
    private boolean dataLoaded = false;

    // Callback for gestures that need MainActivity
    public interface GestureCallbacks {
        void onLongPress();

        void onDoubleTap();

        void onTwoFingerTap();
    }

    private GestureCallbacks gestureCallbacks;

    public SkyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        // Initialize paints
        skyPaint = new Paint();
        skyPaint.setColor(Color.parseColor("#000000"));
        skyPaint.setStyle(Paint.Style.FILL);
        skyPaint.setAntiAlias(true);

        starPaint = new Paint();
        starPaint.setStyle(Paint.Style.FILL);
        starPaint.setAntiAlias(true);

        planetPaint = new Paint();
        planetPaint.setStyle(Paint.Style.FILL);
        planetPaint.setAntiAlias(true);

        linePaint = new Paint();
        linePaint.setColor(Color.parseColor("#004488"));
        linePaint.setAlpha(180);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(2f);
        linePaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setColor(Color.parseColor("#00AAFF"));
        textPaint.setTextSize(28f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);

        gridPaint = new Paint();
        gridPaint.setColor(Color.parseColor("#004400")); // Slightly brighter green
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1.5f); // Slightly thicker
        gridPaint.setAntiAlias(true);

        // Initialize gesture handler
        gestureHandler = new GestureHandler(context, this);

        // Initialize data structures
        starCatalog = new StarCatalog();
        constellationData = new ConstellationData();
        planets = new ArrayList<>();

        // Start animation loop
        post(animationRunnable);
    }

    /**
     * Load sky data from resources.
     */
    public void loadData(int starsResourceId, int constellationsResourceId) {
        starCatalog.loadFromResource(getContext(), starsResourceId);
        constellationData.loadFromResource(getContext(), constellationsResourceId);
        dataLoaded = true;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2f;
        centerY = h / 2f;

        // Adjust globe radius to use full screen width (but stay circular)
        // Use the minimum dimension to ensure it fits, but maximize size
        float minDim = Math.min(w, h);
        globeRadius = minDim * 0.48f; // Increased from 0.45 to 0.48 for larger globe
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!dataLoaded) {
            return;
        }

        // Draw void background
        canvas.drawColor(Color.parseColor("#06060C"));

        // Draw sky globe
        canvas.drawCircle(centerX, centerY, globeRadius, skyPaint);

        // Save canvas state and clip to globe
        canvas.save();
        Path clipPath = new Path();
        clipPath.addCircle(centerX, centerY, globeRadius, Path.Direction.CW);
        canvas.clipPath(clipPath);

        // Calculate celestial positions
        updateCelestialBodies();

        // Draw horizon grid
        drawHorizonGrid(canvas);

        // Draw constellations
        if (constellationMode != ConstellationMode.OFF) {
            drawConstellations(canvas);
        }

        // Draw stars
        drawStars(canvas);

        // Draw planets
        drawPlanets(canvas);

        // Draw Sun and Moon
        if (sun != null)
            drawCelestialBody(canvas, sun);
        if (moon != null)
            drawCelestialBody(canvas, moon);

        // Restore canvas
        canvas.restore();
    }

    /**
     * Update positions of celestial bodies.
     */
    private void updateCelestialBodies() {
        sun = AstronomyEngine.calculateSun(currentDate);
        moon = AstronomyEngine.calculateMoon(currentDate);

        planets.clear();
        planets.add(AstronomyEngine.calculatePlanet("Mercury", currentDate));
        planets.add(AstronomyEngine.calculatePlanet("Venus", currentDate));
        planets.add(AstronomyEngine.calculatePlanet("Mars", currentDate));
        planets.add(AstronomyEngine.calculatePlanet("Jupiter", currentDate));
        planets.add(AstronomyEngine.calculatePlanet("Saturn", currentDate));
    }

    /**
     * Draw horizon grid.
     */
    private void drawHorizonGrid(Canvas canvas) {
        double lst = Coordinates.calculateLST(currentDate.getTime(), longitude);
        double latRad = Math.toRadians(latitude);

        // Always draw prominent horizon line at 0° altitude
        Paint horizonPaint = new Paint();
        horizonPaint.setColor(Color.parseColor("#00AAAA")); // Cyan
        horizonPaint.setStyle(Paint.Style.STROKE);
        horizonPaint.setStrokeWidth(3f); // Thick line
        horizonPaint.setAntiAlias(true);
        horizonPaint.setAlpha(180);
        drawAltitudeCircleWithPaint(canvas, 0, lst, latRad, horizonPaint);

        // Always draw cardinal labels (N, E, S, W)
        String[] cardinalNames = { "N", "E", "S", "W" };
        int[] cardinalAzimuths = { 0, 90, 180, 270 };
        for (int i = 0; i < 4; i++) {
            drawCardinalLabel(canvas, cardinalAzimuths[i], cardinalNames[i]);
        }

        // Only draw additional grid if enabled
        if (gridVisible) {
            // Draw altitude circles at 30° and 60°
            for (int alt = 30; alt <= 60; alt += 30) {
                drawAltitudeCircle(canvas, alt, lst, latRad);
            }

            // Draw azimuth lines (N, E, S, W)
            for (int i = 0; i < 4; i++) {
                drawAzimuthLine(canvas, cardinalAzimuths[i], lst, latRad);
            }
        }
    }

    /**
     * Draw cardinal direction label below horizon.
     */
    private void drawCardinalLabel(Canvas canvas, int azDeg, String label) {
        // Project at horizon (0°) but draw text BELOW the point
        PointF pt = project3D(Math.toRadians(azDeg), Math.toRadians(2));
        if (pt != null) {
            Paint cardinalPaint = new Paint();
            cardinalPaint.setColor(Color.parseColor("#FFCC00"));
            cardinalPaint.setTextSize(32f);
            cardinalPaint.setTextAlign(Paint.Align.CENTER);
            cardinalPaint.setAntiAlias(true);
            cardinalPaint.setFakeBoldText(true);
            cardinalPaint.setShadowLayer(3f, 1f, 1f, Color.BLACK);

            // Offset text downward (below the horizon line)
            canvas.drawText(label, pt.x, pt.y + 40, cardinalPaint);
        }
    }

    private void drawAltitudeCircle(Canvas canvas, int altDeg, double lst, double latRad) {
        drawAltitudeCircleWithPaint(canvas, altDeg, lst, latRad, gridPaint);
    }

    private void drawAltitudeCircleWithPaint(Canvas canvas, int altDeg, double lst, double latRad, Paint paint) {
        Path path = new Path();
        PointF lastPt = null;
        boolean pathStarted = false;

        for (int az = 0; az <= 360; az += 3) { // Finer steps, include 360 for closure
            PointF pt = project3D(Math.toRadians(az), Math.toRadians(altDeg));
            if (pt != null) {
                // Check for large jumps (wrap-around artifact)
                if (lastPt != null && pathStarted) {
                    float jumpDist = (float) Math.sqrt(
                            Math.pow(pt.x - lastPt.x, 2) + Math.pow(pt.y - lastPt.y, 2));
                    if (jumpDist > globeRadius * 0.4f) {
                        // Large jump - break the path and start new segment
                        pathStarted = false;
                    }
                }

                if (!pathStarted) {
                    path.moveTo(pt.x, pt.y);
                    pathStarted = true;
                } else {
                    path.lineTo(pt.x, pt.y);
                }
                lastPt = pt;
            } else {
                // Point not visible - break path
                pathStarted = false;
            }
        }
        canvas.drawPath(path, paint);
    }

    private void drawAzimuthLine(Canvas canvas, int azDeg, double lst, double latRad) {
        Path path = new Path();
        PointF lastPt = null;
        boolean pathStarted = false;

        for (int alt = 0; alt <= 90; alt += 3) {
            PointF pt = project3D(Math.toRadians(azDeg), Math.toRadians(alt));
            if (pt != null) {
                if (lastPt != null && pathStarted) {
                    float jumpDist = (float) Math.sqrt(
                            Math.pow(pt.x - lastPt.x, 2) + Math.pow(pt.y - lastPt.y, 2));
                    if (jumpDist > globeRadius * 0.3f) {
                        pathStarted = false;
                    }
                }

                if (!pathStarted) {
                    path.moveTo(pt.x, pt.y);
                    pathStarted = true;
                } else {
                    path.lineTo(pt.x, pt.y);
                }
                lastPt = pt;
            } else {
                pathStarted = false;
            }
        }
        canvas.drawPath(path, gridPaint);
    }

    /**
     * Draw constellations.
     */
    private void drawConstellations(Canvas canvas) {
        double lst = Coordinates.calculateLST(currentDate.getTime(), longitude);
        double latRad = Math.toRadians(latitude);

        for (ConstellationData.Constellation constellation : constellationData.getConstellations()) {
            // Draw lines
            for (List<ConstellationData.Point> line : constellation.lines) {
                Path path = new Path();
                boolean first = true;

                for (ConstellationData.Point point : line) {
                    double ra = Math.toRadians(point.ra);
                    double dec = Math.toRadians(point.dec);
                    Coordinates.HorizontalCoords hor = Coordinates.equatorialToHorizontal(ra, dec, lst, latRad);

                    PointF pt = project3D(hor.azimuth, hor.altitude);
                    if (pt != null) {
                        if (first) {
                            path.moveTo(pt.x, pt.y);
                            first = false;
                        } else {
                            path.lineTo(pt.x, pt.y);
                        }
                    } else {
                        first = true;
                    }
                }
                canvas.drawPath(path, linePaint);
            }

            // Draw names if LINES_NAMES mode is enabled
            if (constellationMode == ConstellationMode.LINES_NAMES && constellation.centroid != null) {
                double ra = Math.toRadians(constellation.centroid.ra);
                double dec = Math.toRadians(constellation.centroid.dec);
                Coordinates.HorizontalCoords hor = Coordinates.equatorialToHorizontal(ra, dec, lst, latRad);

                PointF pt = project3D(hor.azimuth, hor.altitude);
                if (pt != null) {
                    canvas.drawText(constellation.name, pt.x, pt.y, textPaint);
                }
            }
        }
    }

    /**
     * Draw stars.
     */
    private void drawStars(Canvas canvas) {
        double lst = Coordinates.calculateLST(currentDate.getTime(), longitude);
        double latRad = Math.toRadians(latitude);

        for (CelestialBody star : starCatalog.getStars()) {
            Coordinates.HorizontalCoords hor = Coordinates.equatorialToHorizontal(
                    star.getRa(), star.getDec(), lst, latRad);

            if (hor.altitude > 0) { // Only visible stars
                PointF pt = project3D(hor.azimuth, hor.altitude);
                if (pt != null) {
                    starPaint.setColor(star.getColor());
                    float radius = star.getRenderRadius();
                    canvas.drawCircle(pt.x, pt.y, radius, starPaint);

                    // Draw star names if enabled
                    if (starNameMode != StarNameMode.OFF && star.getName() != null && !star.getName().isEmpty()) {
                        boolean shouldDrawName = false;
                        if (starNameMode == StarNameMode.BRIGHT && star.getMagnitude() < 1.5) {
                            shouldDrawName = true;
                        } else if (starNameMode == StarNameMode.ALL && star.getMagnitude() < 3.0) {
                            shouldDrawName = true;
                        }

                        if (shouldDrawName) {
                            Paint namePaint = new Paint(textPaint);
                            namePaint.setTextSize(20f);
                            namePaint.setColor(Color.parseColor("#8899AA"));
                            canvas.drawText(star.getName(), pt.x, pt.y - radius - 8, namePaint);
                        }
                    }
                }
            }
        }
    }

    /**
     * Draw planets.
     */
    private void drawPlanets(Canvas canvas) {
        for (CelestialBody planet : planets) {
            drawCelestialBody(canvas, planet);
        }
    }

    /**
     * Draw a celestial body (planet, sun, moon).
     */
    private void drawCelestialBody(Canvas canvas, CelestialBody body) {
        double lst = Coordinates.calculateLST(currentDate.getTime(), longitude);
        double latRad = Math.toRadians(latitude);

        Coordinates.HorizontalCoords hor = Coordinates.equatorialToHorizontal(
                body.getRa(), body.getDec(), lst, latRad);

        if (hor.altitude > 0) {
            PointF pt = project3D(hor.azimuth, hor.altitude);
            if (pt != null) {
                planetPaint.setColor(body.getColor());
                float radius = body.getRenderRadius();
                canvas.drawCircle(pt.x, pt.y, radius, planetPaint);

                // Draw name
                Paint namePaint = new Paint(textPaint);
                namePaint.setTextSize(24f);
                canvas.drawText(body.getName(), pt.x, pt.y - radius - 10, namePaint);
            }
        }
    }

    /**
     * Project 3D celestial sphere to 2D globe view.
     * Stereographic projection centered on view direction.
     */
    private PointF project3D(double az, double alt) {
        if (alt < -0.01) // Small tolerance for horizon
            return null;

        // Normalize azimuth difference to [-PI, PI] to handle wrap-around
        double dAz = az - viewAzimuth;
        while (dAz > Math.PI)
            dAz -= 2 * Math.PI;
        while (dAz < -Math.PI)
            dAz += 2 * Math.PI;

        // Clamp view altitude to avoid singularity at poles
        double clampedViewAlt = Math.max(-Math.PI / 2 + 0.02, Math.min(Math.PI / 2 - 0.02, viewAltitude));

        // Convert to 3D rotated coordinates (point relative to view center)
        double cosAlt = Math.cos(alt);
        double sinAlt = Math.sin(alt);
        double cosViewAlt = Math.cos(clampedViewAlt);
        double sinViewAlt = Math.sin(clampedViewAlt);
        double cosDaz = Math.cos(dAz);
        double sinDaz = Math.sin(dAz);

        double x1 = cosAlt * sinDaz;
        double y1 = sinAlt * cosViewAlt - cosAlt * sinViewAlt * cosDaz;
        double z1 = sinAlt * sinViewAlt + cosAlt * cosViewAlt * cosDaz;

        // Check if behind viewing hemisphere (with tolerance)
        if (z1 < 0.02)
            return null;

        // Stereographic projection with FOV scaling
        double denom = 1.0 + z1;
        if (denom < 0.1)
            denom = 0.1; // Prevent division issues

        double projX = x1 / (denom * fovScale);
        double projY = y1 / (denom * fovScale);

        // Check if within globe bounds
        double r = Math.sqrt(projX * projX + projY * projY);
        if (r > 0.95) // Clip at 95% of globe
            return null;

        // Convert to screen coordinates
        float screenX = centerX + (float) (projX * globeRadius);
        float screenY = centerY - (float) (projY * globeRadius);

        return new PointF(screenX, screenY);
    }

    // Animation loop
    private final Runnable animationRunnable = new Runnable() {
        @Override
        public void run() {
            updateTime();
            invalidate();
            postDelayed(this, 16); // ~60
                                   // FPS
        }
    };

    private void updateTime() {
        long now = System.currentTimeMillis();
        long dt = now - lastFrameTime;
        lastFrameTime = now;

        if (!paused) {
            long timeIncrement = (long) (dt * timeSpeed);
            currentDate = new Date(currentDate.getTime() + timeIncrement);
        }
    }

    // Gesture handlers
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Only handle touches within the globe circle
        // Allow touches outside globe to pass through for system gestures (screenshots,
        // etc.)
        float touchX = event.getX();
        float touchY = event.getY();
        float dx = touchX - centerX;
        float dy = touchY - centerY;
        float distanceFromCenter = (float) Math.sqrt(dx * dx + dy * dy);

        if (distanceFromCenter <= globeRadius) {
            return gestureHandler.onTouchEvent(event);
        }

        // Pass through touches outside the globe
        return false;
    }

    @Override
    public void onPan(float deltaX, float deltaY) {
        viewAzimuth -= deltaX * 0.005;
        viewAltitude += deltaY * 0.005;
        viewAltitude = Math.max(-Math.PI / 2, Math.min(Math.PI / 2, viewAltitude));
        invalidate();
    }

    @Override
    public void onZoom(float scaleFactor) {
        // Zoom affects FOV, not globe size
        // scaleFactor > 1 means zooming in (reduce FOV)
        // scaleFactor < 1 means zooming out (increase FOV)
        fovScale /= scaleFactor;
        fovScale = Math.max(minFovScale, Math.min(maxFovScale, fovScale));
        invalidate();
    }

    @Override
    public void onDoubleTap() {
        if (gestureCallbacks != null) {
            gestureCallbacks.onDoubleTap();
        }
    }

    @Override
    public void onLongPress() {
        // Long press disabled - options accessible via settings button
    }

    @Override
    public void onTwoFingerTap() {
        if (gestureCallbacks != null) {
            gestureCallbacks.onTwoFingerTap();
        }
    }

    @Override
    public void onThreeFingerSwipeUp() {
        timeSpeed = Math.min(timeSpeed * 10, 10000);
        invalidate();
    }

    @Override
    public void onThreeFingerSwipeDown() {
        timeSpeed = Math.max(timeSpeed / 10, -10000);
        if (timeSpeed > -1 && timeSpeed < 1)
            timeSpeed = timeSpeed > 0 ? 1 : -1;
        invalidate();
    }

    @Override
    public void onTwoFingerDoubleTap() {
        paused = !paused;
        invalidate();
    }

    // Public API
    public void setGestureCallbacks(GestureCallbacks callbacks) {
        this.gestureCallbacks = callbacks;
    }

    public void setLocation(double latitude, double longitude, String name) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationName = name;
        invalidate();
    }

    public void setViewDirection(double azimuth, double altitude) {
        this.viewAzimuth = azimuth;
        this.viewAltitude = altitude;
        invalidate();
    }

    public void setTimeSpeed(double speed) {
        this.timeSpeed = speed;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public void resetTime() {
        currentDate = new Date();
        invalidate();
    }

    public void cycleConstellationMode() {
        switch (constellationMode) {
            case OFF:
                constellationMode = ConstellationMode.LINES;
                break;
            case LINES:
                constellationMode = ConstellationMode.LINES_NAMES;
                break;
            case LINES_NAMES:
                constellationMode = ConstellationMode.OFF;
                break;
        }
        invalidate();
    }

    public ConstellationMode getConstellationMode() {
        return constellationMode;
    }

    public void cycleStarNameMode() {
        switch (starNameMode) {
            case OFF:
                starNameMode = StarNameMode.BRIGHT;
                break;
            case BRIGHT:
                starNameMode = StarNameMode.ALL;
                break;
            case ALL:
                starNameMode = StarNameMode.OFF;
                break;
        }
        invalidate();
    }

    public StarNameMode getStarNameMode() {
        return starNameMode;
    }

    public void toggleGrid() {
        gridVisible = !gridVisible;
        invalidate();
    }

    public boolean isGridVisible() {
        return gridVisible;
    }

    public Date getCurrentDate() {
        return currentDate;
    }

    public String getLocationName() {
        return locationName;
    }

    public double getViewAzimuth() {
        return viewAzimuth;
    }

    public double getViewAltitude() {
        return viewAltitude;
    }

    public double getTimeSpeed() {
        return timeSpeed;
    }

    public boolean isPaused() {
        return paused;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }
}
