package org.zapphyre.grid;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.zapphyre.dtw.model.PolarCoords;

import java.util.HashMap;
import java.util.Map;

@Value
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@RequiredArgsConstructor
public class Node {

    int nodeLength;
    double maxAngleDeltaRadians;
    double rotationCoefficient = 0; // Shifts sectors by rotationCoefficient * 2π radians

    double begin;
    double initialTheta;

    @EqualsAndHashCode.Include
    ENextNodeDirection pathFromPrev;

    String path;

    Map<ENextNodeDirection, Node> nextNodes = new HashMap<>();

    public Node movement(DirectedCoords directedCoords) {
        double r = directedCoords.getCoords().getRadius();
        double theta = directedCoords.getCoords().getTheta();

        double relR = Math.abs(begin - r);
        System.out.printf("r=%.2f, theta=%.2f (%s)%n", r, theta, directionFromTheta(directedCoords.getCoords()).getDirection());
        // Compute angular difference
        double angleDiff = Math.min(Math.abs(theta - initialTheta), 2 * Math.PI - Math.abs(theta - initialTheta));

        if (relR > nodeLength || angleDiff > maxAngleDeltaRadians) {
            // Retrieve or create the next node
            return nextNodes.computeIfAbsent(directedCoords.getDirection(),
                    d -> new Node(nodeLength, maxAngleDeltaRadians, r, theta, d, path + d.ordinal()));
        }

        return this;
    }

    public DirectedCoords directionFromTheta(PolarCoords coords) {
        double r = coords.getRadius();
        double theta = coords.getTheta();

        DirectedCoords.DirectedCoordsBuilder builder = DirectedCoords
                .builder()
                .coords(coords);

        if (r < 1e-5) {
            return builder.direction(ENextNodeDirection.CENTER).build();
        }

        // Normalize theta to [0, 2π)
        double normalizedTheta = ((theta % (2 * Math.PI)) + (2 * Math.PI)) % (2 * Math.PI);
        double normalizedInitialTheta = ((initialTheta % (2 * Math.PI)) + (2 * Math.PI)) % (2 * Math.PI);

        // Compute angular difference
        double angleDiff = Math.min(Math.abs(normalizedTheta - normalizedInitialTheta),
                2 * Math.PI - Math.abs(normalizedTheta - normalizedInitialTheta));

        // Adjust for joystick: θ ≈ -π/2 (up) maps to NORTH
        double adjustedTheta = ((normalizedTheta + Math.PI / 2) % (2 * Math.PI) + (2 * Math.PI)) % (2 * Math.PI);

        // Apply rotation: shift sectors by rotationCoefficient * 360°
        double rotation = rotationCoefficient * 2 * Math.PI;
        double rotatedTheta = ((adjustedTheta - rotation) % (2 * Math.PI) + (2 * Math.PI)) % (2 * Math.PI);

        // Assign directions to 45° sectors (π/4 radians)
        if (rotatedTheta >= 0 && rotatedTheta < Math.PI / 4) {
            builder.direction(ENextNodeDirection.NORTH);
        } else if (rotatedTheta >= Math.PI / 4 && rotatedTheta < Math.PI / 2) {
            builder.direction(ENextNodeDirection.NORTH_EAST);
        } else if (rotatedTheta >= Math.PI / 2 && rotatedTheta < 3 * Math.PI / 4) {
            builder.direction(ENextNodeDirection.EAST);
        } else if (rotatedTheta >= 3 * Math.PI / 4 && rotatedTheta < Math.PI) {
            builder.direction(ENextNodeDirection.SOUTH_EAST);
        } else if (rotatedTheta >= Math.PI && rotatedTheta < 5 * Math.PI / 4) {
            builder.direction(ENextNodeDirection.SOUTH);
        } else if (rotatedTheta >= 5 * Math.PI / 4 && rotatedTheta < 3 * Math.PI / 2) {
            builder.direction(ENextNodeDirection.SOUTH_WEST);
        } else if (rotatedTheta >= 3 * Math.PI / 2 && rotatedTheta < 7 * Math.PI / 4) {
            builder.direction(ENextNodeDirection.WEST);
        } else {
            builder.direction(ENextNodeDirection.NORTH_WEST);
        }

        // Flip direction if returning toward center
        if (r < begin && angleDiff <= maxAngleDeltaRadians) {
            switch (builder.build().getDirection()) {
                case NORTH:
                    builder.direction(ENextNodeDirection.SOUTH);
                    break;
                case SOUTH:
                    builder.direction(ENextNodeDirection.NORTH);
                    break;
                case EAST:
                    builder.direction(ENextNodeDirection.WEST);
                    break;
                case WEST:
                    builder.direction(ENextNodeDirection.EAST);
                    break;
                case NORTH_EAST:
                    builder.direction(ENextNodeDirection.SOUTH_WEST);
                    break;
                case SOUTH_WEST:
                    builder.direction(ENextNodeDirection.NORTH_EAST);
                    break;
                case NORTH_WEST:
                    builder.direction(ENextNodeDirection.SOUTH_EAST);
                    break;
                case SOUTH_EAST:
                    builder.direction(ENextNodeDirection.NORTH_WEST);
                    break;
                default:
                    // CENTER unchanged
                    break;
            }
        }

        return builder.build();
    }
}