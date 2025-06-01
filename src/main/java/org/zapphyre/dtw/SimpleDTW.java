package org.zapphyre.dtw;


import org.zapphyre.dtw.model.PolarCoords;

import java.util.List;

public class SimpleDTW {
    private final DistanceFunction distanceFunction;

    public SimpleDTW(DistanceFunction distanceFunction) {
        this.distanceFunction = distanceFunction;
    }

    public double compute(List<PolarCoords> series1, List<PolarCoords> series2) {
        int n = series1.size();
        int m = series2.size();
        double[][] dtw = new double[n + 1][m + 1];

        // Initialize first row and column
        for (int i = 0; i <= n; i++) {
            dtw[i][0] = Double.POSITIVE_INFINITY;
        }
        for (int j = 0; j <= m; j++) {
            dtw[0][j] = Double.POSITIVE_INFINITY;
        }
        dtw[0][0] = 0.0;

        // Fill DTW matrix
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                double cost = distanceFunction.calcDistance(
                        new double[]{series1.get(i - 1).getRadius(), series1.get(i - 1).getTheta()},
                        new double[]{series2.get(j - 1).getRadius(), series2.get(j - 1).getTheta()}
                );
                dtw[i][j] = cost + Math.min(
                        dtw[i - 1][j],    // Insertion
                        Math.min(dtw[i][j - 1],  // Deletion
                                dtw[i - 1][j - 1]) // Match
                );
            }
        }
        return dtw[n][m];
    }
}
