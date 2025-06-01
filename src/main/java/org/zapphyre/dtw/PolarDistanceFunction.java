package org.zapphyre.dtw;

public class PolarDistanceFunction implements DistanceFunction {
    @Override
    public double calcDistance(double[] point1, double[] point2) {
        double r1 = point1[0], theta1 = point1[1];
        double r2 = point2[0], theta2 = point2[1];

        // Normalize theta to handle periodicity (e.g., 0 and 2π are equivalent)
        double deltaTheta = Math.abs(theta1 - theta2);
        deltaTheta = Math.min(deltaTheta, 2 * Math.PI - deltaTheta);

        // Convert to Cartesian for Euclidean distance
        double x1 = r1 * Math.cos(theta1);
        double y1 = r1 * Math.sin(theta1);
        double x2 = r2 * Math.cos(theta2);
        double y2 = r2 * Math.sin(theta2);

        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }
}