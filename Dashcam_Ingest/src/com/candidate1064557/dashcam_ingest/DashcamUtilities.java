package com.candidate1064557.dashcam_ingest;

/**
 * Dashcam utility methods used in the module
 */
public class DashcamUtilities {

    static final double radius = 6371000.0;
    static final double eps = 0.01;

    public DashcamUtilities() {

    }

    private static double toRadians(double ang) {
        return (Math.PI / 180D) * ang;
    }

    public static double getHaversineDistance(double x1, double y1, double x2, double y2) {
        double deltaLat = toRadians(y2 - y1);
        double deltaLon = toRadians(x2 - x1);
        double lat1 = toRadians(y1);
        double lat2 = toRadians(y2);
        double hav = Math.sin(deltaLat / 2D) * Math.sin(deltaLat / 2D)
                + Math.sin(deltaLon / 2D) * Math.sin(deltaLon / 2D) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2D * Math.asin(Math.sqrt(hav));
        return c * radius;

    }

    // Check whether coordinate is a valid location AND not a special null value (like (0,0) or (180,0))
    public static boolean isValidTrackCoordinate(double x, double y) {
        double xAbs = Math.abs(x);
        double yAbs = Math.abs(y);
        boolean xInvalidRange = xAbs >= (180.0 - eps) || xAbs <= eps;
        boolean yInvalidRange = yAbs >= (90.0 - eps) || yAbs <= eps;
        return isCoordinateInBounds(xAbs, yAbs) && (!(xInvalidRange && yInvalidRange));
    }

    // Check whether coordinate pair is a valid location
    public static boolean isCoordinateInBounds(double x, double y) {
        double xAbs = Math.abs(x);
        double yAbs = Math.abs(y);
        return xAbs <= 180.0 && yAbs <= 90.0;
    }
}
