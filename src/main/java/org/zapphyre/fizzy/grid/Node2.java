package org.zapphyre.fizzy.grid;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.zapphyre.model.DirectedCoords;
import org.zapphyre.model.ENextNodeDirection;
import org.zapphyre.model.PolarCoords;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Value
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@RequiredArgsConstructor
public class Node2 {

    private static final double TWO_PI = 2 * Math.PI;
    private static final double PI_4 = Math.PI / 4;
    private static final double PI_2 = Math.PI / 2;
    private static final double DEAD_ZONE_RADIUS = 100.0; // Threshold for inward movement
    private static final double THETA_HYSTERESIS = Math.PI / 180; // ~1° to ignore noise

    // Sector definitions: {direction, startTheta (radians), ordinal}
    private static final Object[][] SECTORS = {
            {ENextNodeDirection.NORTH, 0.0, 0},
            {ENextNodeDirection.EAST, PI_2, 2},
            {ENextNodeDirection.SOUTH, Math.PI, 4},
            {ENextNodeDirection.WEST, 3 * PI_2, 6},
    };

    // Opposite direction mapping
    private static final Map<ENextNodeDirection, ENextNodeDirection> OPPOSITES = new HashMap<>();
    static {
        OPPOSITES.put(ENextNodeDirection.NORTH, ENextNodeDirection.SOUTH);
        OPPOSITES.put(ENextNodeDirection.SOUTH, ENextNodeDirection.NORTH);
        OPPOSITES.put(ENextNodeDirection.EAST, ENextNodeDirection.WEST);
        OPPOSITES.put(ENextNodeDirection.WEST, ENextNodeDirection.EAST);
        OPPOSITES.put(ENextNodeDirection.CENTER, ENextNodeDirection.CENTER);
    }

    int nodeLength;
    double maxAngleDeltaRadians;
    double rotationCoefficient = 0; // Shifts sectors by rotationCoefficient * 2π radians

    double begin;
    double initialTheta;

    @EqualsAndHashCode.Include
    ENextNodeDirection pathFromPrev;

    String path;

    Map<ENextNodeDirection, Node2> nextNodes = new HashMap<>();

    public Node2 movement(DirectedCoords directedCoords) {
        double r = directedCoords.getCoords().getRadius();
        double theta = directedCoords.getCoords().getTheta();

        // Validate inputs
        if (r < 0) {
            throw new IllegalArgumentException("Radius cannot be negative: " + r);
        }

        double relR = Math.abs(begin - r);
        DirectedCoords directionResult = directionFromTheta(directedCoords.getCoords());
        log.debug("r={}, theta={} ({}), begin={}, angleDiff={}",
                r, theta, directionResult.getDirection(), begin, computeAngleDiff(theta, initialTheta));

        // Compute angular difference
        double angleDiff = computeAngleDiff(theta, initialTheta);

        if (relR > nodeLength || angleDiff > maxAngleDeltaRadians) {
            return nextNodes.computeIfAbsent(directedCoords.getDirection(),
                    d -> new Node2(nodeLength, maxAngleDeltaRadians, r, theta, d, path + d.ordinal()));
        }

        return this;
    }

    public DirectedCoords directionFromTheta(PolarCoords coords) {
        double r = coords.getRadius();
        double theta = coords.getTheta();

        // Validate radius
        if (r < 0) {
            throw new IllegalArgumentException("Radius cannot be negative: " + r);
        }

        DirectedCoords.DirectedCoordsBuilder builder = DirectedCoords.builder().coords(coords);

        // Handle center
        if (r < 1e-5) {
            return builder.direction(ENextNodeDirection.CENTER).build();
        }

        // Normalize theta to [0, 2π)
        double normalizedTheta = ((theta % TWO_PI) + TWO_PI) % TWO_PI;
        double normalizedInitialTheta = ((initialTheta % TWO_PI) + TWO_PI) % TWO_PI;

        // Compute angular difference
        double angleDiff = computeAngleDiff(normalizedTheta, normalizedInitialTheta);

        // Adjust for joystick: θ ≈ -π/2 (up) maps to NORTH
        double adjustedTheta = ((normalizedTheta + PI_2 + TWO_PI) % TWO_PI);

        // Apply rotation
        double rotation = rotationCoefficient * TWO_PI;
        double rotatedTheta = ((adjustedTheta - rotation + TWO_PI) % TWO_PI);

        // Find base direction
        ENextNodeDirection direction = ENextNodeDirection.CENTER;
        for (Object[] sector : SECTORS) {
            double start = (double) sector[1];
            if (rotatedTheta >= start && rotatedTheta < start + PI_4) {
                direction = (ENextNodeDirection) sector[0];
                break;
            }
        }
        if (direction == ENextNodeDirection.CENTER) {
            // Handle wrap-around for NORTH_WEST
            double start = (double) SECTORS[SECTORS.length - 1][1];
            if (rotatedTheta >= start && rotatedTheta < TWO_PI) {
                direction = (ENextNodeDirection) SECTORS[SECTORS.length - 1][0];
            }
        }

        builder.direction(direction);

        // Apply inward movement flip only if conditions are met
        if (r < begin - DEAD_ZONE_RADIUS && angleDiff <= maxAngleDeltaRadians + THETA_HYSTERESIS) {
            direction = OPPOSITES.getOrDefault(direction, direction);
            builder.direction(direction);
        }

        // Debugging output
        log.debug("DEBUG: r={}, theta={}, normalizedTheta={}, adjustedTheta={}, rotatedTheta={}, angleDiff={}, begin={}, direction={}",
                r, theta, normalizedTheta, adjustedTheta, rotatedTheta, angleDiff, begin, direction);

        return builder.build();
    }

    private double computeAngleDiff(double theta1, double theta2) {
        double diff = Math.abs(theta1 - theta2);
        return Math.min(diff, TWO_PI - diff);
    }
}