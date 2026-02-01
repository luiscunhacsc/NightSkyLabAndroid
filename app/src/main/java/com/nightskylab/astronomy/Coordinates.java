package com.nightskylab.astronomy;

/**
 * Coordinate system conversions and astronomical calculations.
 */
public class Coordinates {
    private static final double RAD = Math.PI / 180.0;
    private static final double DEG = 180.0 / Math.PI;

    /**
     * Represents horizontal coordinates (altitude and azimuth).
     */
    public static class HorizontalCoords {
        public double altitude; // radians, -90 to +90
        public double azimuth; // radians, 0 to 2π (0 = North, π/2 = East)

        public HorizontalCoords(double altitude, double azimuth) {
            this.altitude = altitude;
            this.azimuth = azimuth;
        }
    }

    /**
     * Represents equatorial coordinates (right ascension and declination).
     */
    public static class EquatorialCoords {
        public double ra; // radians, 0 to 2π
        public double dec; // radians, -90 to +90

        public EquatorialCoords(double ra, double dec) {
            this.ra = ra;
            this.dec = dec;
        }
    }

    /**
     * Convert equatorial coordinates to horizontal coordinates.
     * 
     * @param ra     Right ascension in radians
     * @param dec    Declination in radians
     * @param lst    Local sidereal time in radians
     * @param latRad Observer latitude in radians
     * @return Horizontal coordinates (alt/az)
     */
    public static HorizontalCoords equatorialToHorizontal(double ra, double dec, double lst, double latRad) {
        // Hour angle
        double ha = lst - ra;

        double sinDec = Math.sin(dec);
        double cosDec = Math.cos(dec);
        double sinLat = Math.sin(latRad);
        double cosLat = Math.cos(latRad);
        double cosHA = Math.cos(ha);

        // Altitude
        double sinAlt = sinDec * sinLat + cosDec * cosLat * cosHA;
        double alt = Math.asin(Math.max(-1.0, Math.min(1.0, sinAlt)));

        // Azimuth
        double cosAz = (sinDec - sinAlt * sinLat) / (Math.cos(alt) * cosLat);
        double az = Math.acos(Math.max(-1.0, Math.min(1.0, cosAz)));

        if (Math.sin(ha) > 0) {
            az = (Math.PI * 2) - az;
        }

        return new HorizontalCoords(alt, az);
    }

    /**
     * Calculate Greenwich Mean Sidereal Time.
     * 
     * @param dateMillis Date/time in milliseconds since epoch
     * @return GMST in hours (0 to 24)
     */
    public static double calculateGMST(long dateMillis) {
        // Convert to Julian Date
        double jd = (dateMillis / 86400000.0) + 2440587.5;

        // Days since J2000.0
        double d = jd - 2451545.0;

        // GMST at 0h UT
        double gmst = 18.697374558 + 24.06570982441908 * d;

        // Normalize to 0-24 hours
        gmst = gmst % 24.0;
        if (gmst < 0)
            gmst += 24.0;

        return gmst;
    }

    /**
     * Calculate Local Sidereal Time.
     * 
     * @param dateMillis Date/time in milliseconds since epoch
     * @param lonDeg     Observer longitude in degrees (East positive)
     * @return LST in radians
     */
    public static double calculateLST(long dateMillis, double lonDeg) {
        double gmst = calculateGMST(dateMillis);
        double lst = gmst + (lonDeg / 15.0); // Convert longitude to hours
        lst = lst % 24.0;
        if (lst < 0)
            lst += 24.0;
        return lst * 15.0 * RAD; // Convert to radians
    }

    /**
     * Normalize angle to 0-2π range.
     */
    public static double normalizeAngle(double angleRad) {
        double result = angleRad % (2 * Math.PI);
        if (result < 0)
            result += 2 * Math.PI;
        return result;
    }

    /**
     * Convert degrees to radians.
     */
    public static double toRadians(double degrees) {
        return degrees * RAD;
    }

    /**
     * Convert radians to degrees.
     */
    public static double toDegrees(double radians) {
        return radians * DEG;
    }
}
