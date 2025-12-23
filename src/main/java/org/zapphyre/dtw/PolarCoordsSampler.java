package org.zapphyre.dtw;

import org.zapphyre.model.PolarCoords;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class PolarCoordsSampler {
    public static class TimedPolarCoords {
        final PolarCoords coords;
        final long timestampMs;

        public TimedPolarCoords(PolarCoords coords, long timestampMs) {
            this.coords = coords;
            this.timestampMs = timestampMs;
        }
    }

    public static List<PolarCoords> interpolate(List<TimedPolarCoords> input, Duration sampleInterval) {
        if (input.size() < 2) {
            return input.stream().map(t -> t.coords).toList();
        }

        List<PolarCoords> result = new ArrayList<>();
        long startTime = input.getFirst().timestampMs;
        long endTime = input.getLast().timestampMs;
        long intervalMs = sampleInterval.toMillis();

        for (long t = startTime; t <= endTime; t += intervalMs) {
            // Find the two points to interpolate between
            TimedPolarCoords prev = null;
            TimedPolarCoords next = null;
            for (TimedPolarCoords point : input) {
                if (point.timestampMs <= t) {
                    prev = point;
                } else {
                    next = point;
                    break;
                }
            }

            if (prev == null || next == null) {
                continue; // Skip if no valid interval
            }

            // Linear interpolation
            double fraction = (double) (t - prev.timestampMs) / (next.timestampMs - prev.timestampMs);
            double r = prev.coords.getRadius() + fraction * (next.coords.getRadius() - prev.coords.getRadius());
            double theta = prev.coords.getTheta() + fraction * (next.coords.getTheta() - prev.coords.getTheta());
            // Normalize theta to [0, 2π)
            theta = theta % (2 * Math.PI);
            if (theta < 0) {
                theta += 2 * Math.PI;
            }

            result.add(PolarCoords.builder().radius(r).theta(theta).build());
        }

        return result;
    }
}