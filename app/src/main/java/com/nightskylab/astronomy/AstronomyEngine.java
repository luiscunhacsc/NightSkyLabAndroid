package com.nightskylab.astronomy;

import android.graphics.Color;
import java.util.Date;

/**
 * Simplified astronomy engine for calculating celestial body positions.
 * Based on low-precision formulas suitable for planetarium visualization.
 */
public class AstronomyEngine {
    private static final double RAD = Math.PI / 180.0;
    private static final double DEG = 180.0 / Math.PI;

    /**
     * Calculate Sun position.
     * Uses simplified formulas accurate to ~0.01 degrees.
     */
    public static CelestialBody calculateSun(Date date) {
        CelestialBody sun = new CelestialBody("Sun", CelestialBody.BodyType.SUN);

        // Julian date
        double jd = (date.getTime() / 86400000.0) + 2440587.5;
        double d = jd - 2451545.0; // Days since J2000.0

        // Mean anomaly
        double M = 357.529 + 0.98560028 * d;
        M = normalizeAngle(M) * RAD;

        // Ecliptic longitude
        double L = 280.459 + 0.98564736 * d;
        double lambda = L + 1.915 * Math.sin(M) + 0.020 * Math.sin(2 * M);
        lambda = normalizeAngle(lambda) * RAD;

        // Obliquity of ecliptic
        double epsilon = 23.439 * RAD;

        // Convert to equatorial coordinates
        double ra = Math.atan2(Math.cos(epsilon) * Math.sin(lambda), Math.cos(lambda));
        double dec = Math.asin(Math.sin(epsilon) * Math.sin(lambda));

        sun.setRa(Coordinates.normalizeAngle(ra));
        sun.setDec(dec);
        sun.setMagnitude(-26.7);
        sun.setColor(Color.rgb(255, 255, 0));

        return sun;
    }

    /**
     * Calculate Moon position and phase.
     * Simplified formula accurate to ~0.5 degrees.
     */
    public static CelestialBody calculateMoon(Date date) {
        CelestialBody moon = new CelestialBody("Moon", CelestialBody.BodyType.MOON);

        double jd = (date.getTime() / 86400000.0) + 2440587.5;
        double d = jd - 2451545.0;

        // Moon's mean longitude
        double L = 218.316 + 13.176396 * d;
        L = normalizeAngle(L);

        // Mean anomaly
        double M = 134.963 + 13.064993 * d;
        M = normalizeAngle(M) * RAD;

        // Mean distance (argument of latitude)
        double F = 93.272 + 13.229350 * d;
        F = normalizeAngle(F) * RAD;

        // Longitude
        double lambda = L + 6.289 * Math.sin(M);
        lambda = normalizeAngle(lambda) * RAD;

        // Latitude
        double beta = 5.128 * Math.sin(F);
        beta = beta * RAD;

        // Obliquity
        double epsilon = 23.439 * RAD;

        // Convert to equatorial
        double ra = Math.atan2(
                Math.sin(lambda) * Math.cos(epsilon) - Math.tan(beta) * Math.sin(epsilon),
                Math.cos(lambda));
        double dec = Math.asin(
                Math.sin(beta) * Math.cos(epsilon) + Math.cos(beta) * Math.sin(epsilon) * Math.sin(lambda));

        moon.setRa(Coordinates.normalizeAngle(ra));
        moon.setDec(dec);
        moon.setMagnitude(-12.6);
        moon.setColor(Color.rgb(200, 200, 200));

        // Calculate phase (0 = new, 0.5 = full)
        CelestialBody sun = calculateSun(date);
        double elongation = moon.getRa() - sun.getRa();
        double phase = (1 - Math.cos(elongation)) / 2.0;
        moon.setPhase(phase);

        return moon;
    }

    /**
     * Calculate planet position (simplified).
     * These are very approximate - suitable for general visualization only.
     */
    public static CelestialBody calculatePlanet(String name, Date date) {
        CelestialBody planet = new CelestialBody(name, CelestialBody.BodyType.PLANET);

        double jd = (date.getTime() / 86400000.0) + 2440587.5;
        double d = jd - 2451545.0;

        // Very simplified orbital elements (mean values)
        double L, a, e, i, omega, Omega;
        int color;
        double mag;

        switch (name) {
            case "Mercury":
                L = 252.25 + 4.092385 * d;
                a = 0.387098;
                e = 0.205635;
                mag = -0.4;
                color = Color.rgb(180, 180, 180);
                break;
            case "Venus":
                L = 181.98 + 1.602130 * d;
                a = 0.723330;
                e = 0.006772;
                mag = -4.4;
                color = Color.rgb(255, 230, 200);
                break;
            case "Mars":
                L = 355.43 + 0.524071 * d;
                a = 1.523688;
                e = 0.093405;
                mag = -2.0;
                color = Color.rgb(255, 100, 50);
                break;
            case "Jupiter":
                L = 34.35 + 0.083056 * d;
                a = 5.202603;
                e = 0.048498;
                mag = -2.7;
                color = Color.rgb(255, 200, 150);
                break;
            case "Saturn":
                L = 50.08 + 0.033371 * d;
                a = 9.536622;
                e = 0.055548;
                mag = 0.0;
                color = Color.rgb(255, 220, 150);
                break;
            default:
                // Default to a generic planet
                L = 0;
                a = 1.0;
                e = 0.0;
                mag = 0;
                color = Color.WHITE;
        }

        L = normalizeAngle(L) * RAD;

        // Very rough approximation - use mean longitude as RA
        // This is NOT accurate but gives a general sky position
        double ra = L;
        double dec = 0.0; // Approximate - planets near ecliptic

        planet.setRa(ra);
        planet.setDec(dec);
        planet.setMagnitude(mag);
        planet.setColor(color);

        return planet;
    }

    /**
     * Normalize angle to 0-360 degrees.
     */
    private static double normalizeAngle(double degrees) {
        double result = degrees % 360.0;
        if (result < 0)
            result += 360.0;
        return result;
    }
}
