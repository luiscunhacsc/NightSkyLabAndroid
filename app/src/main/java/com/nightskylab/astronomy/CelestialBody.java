package com.nightskylab.astronomy;

/**
 * Represents a celestial body (star, planet, Sun, Moon).
 */
public class CelestialBody {
    public enum BodyType {
        STAR, SUN, MOON, PLANET
    }

    private String name;
    private BodyType type;
    private double ra; // Right Ascension in radians
    private double dec; // Declination in radians
    private double magnitude; // Visual magnitude
    private int color; // RGB color
    private double phase; // Moon phase (0-1), -1 for others

    public CelestialBody(String name, BodyType type) {
        this.name = name;
        this.type = type;
        this.phase = -1;
    }

    public String getName() {
        return name;
    }

    public BodyType getType() {
        return type;
    }

    public double getRa() {
        return ra;
    }

    public void setRa(double ra) {
        this.ra = ra;
    }

    public double getDec() {
        return dec;
    }

    public void setDec(double dec) {
        this.dec = dec;
    }

    public double getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(double magnitude) {
        this.magnitude = magnitude;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public double getPhase() {
        return phase;
    }

    public void setPhase(double phase) {
        this.phase = phase;
    }

    /**
     * Get radius for rendering based on magnitude.
     */
    public float getRenderRadius() {
        if (type == BodyType.SUN)
            return 12f;
        if (type == BodyType.MOON)
            return 10f;
        if (type == BodyType.PLANET)
            return 6f;

        // Stars: brightness based on magnitude (increased size for visibility)
        float base = 6.5f - (float) magnitude; // Increased from 5.0 to 6.5
        return Math.max(2.5f, Math.min(12f, base)); // Increased min/max
    }
}
