package com.nightskylab.data;

import android.content.Context;
import android.graphics.Color;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nightskylab.astronomy.CelestialBody;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages star catalog data loaded from JSON resources.
 */
public class StarCatalog {
    private List<CelestialBody> stars;

    /**
     * Star data structure for JSON parsing.
     */
    private static class StarData {
        double ra; // degrees
        double dec; // degrees
        double mag;
        String name;
        String color;
    }

    public StarCatalog() {
        stars = new ArrayList<>();
    }

    /**
     * Load star catalog from raw resource.
     */
    public void loadFromResource(Context context, int resourceId) {
        try {
            InputStream is = context.getResources().openRawResource(resourceId);
            InputStreamReader reader = new InputStreamReader(is);

            Gson gson = new Gson();
            Type listType = new TypeToken<List<StarData>>() {
            }.getType();
            List<StarData> starDataList = gson.fromJson(reader, listType);

            stars.clear();
            for (StarData data : starDataList) {
                CelestialBody star = new CelestialBody(
                        data.name != null ? data.name : "",
                        CelestialBody.BodyType.STAR);

                // Convert degrees to radians
                star.setRa(Math.toRadians(data.ra));
                star.setDec(Math.toRadians(data.dec));
                star.setMagnitude(data.mag);

                // Parse color or use default
                int color = parseColor(data.color);
                star.setColor(color);

                stars.add(star);
            }

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get all stars in the catalog.
     */
    public List<CelestialBody> getStars() {
        return stars;
    }

    /**
     * Get bright stars only (magnitude < threshold).
     */
    public List<CelestialBody> getBrightStars(double magnitudeThreshold) {
        List<CelestialBody> bright = new ArrayList<>();
        for (CelestialBody star : stars) {
            if (star.getMagnitude() <= magnitudeThreshold) {
                bright.add(star);
            }
        }
        return bright;
    }

    /**
     * Parse color string to Android Color int.
     */
    private int parseColor(String colorStr) {
        if (colorStr == null || colorStr.isEmpty()) {
            return Color.WHITE;
        }

        try {
            return Color.parseColor(colorStr);
        } catch (Exception e) {
            return Color.WHITE;
        }
    }

    /**
     * Get star color based on magnitude (fallback).
     */
    public static int getStarColorByMagnitude(double mag) {
        if (mag < 0)
            return Color.rgb(200, 220, 255); // Blue-white (very bright)
        if (mag < 1)
            return Color.rgb(220, 230, 255); // Blue-white
        if (mag < 2)
            return Color.rgb(240, 245, 255); // White
        if (mag < 3)
            return Color.rgb(255, 250, 240); // Yellow-white
        if (mag < 4)
            return Color.rgb(255, 245, 230); // Yellow
        return Color.rgb(255, 240, 220); // Orange (dim)
    }
}
