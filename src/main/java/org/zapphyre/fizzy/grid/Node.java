package org.zapphyre.fizzy.grid;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.zapphyre.model.DirectedCoords;
import org.zapphyre.model.ENextNodeDirection;
import org.zapphyre.model.PolarCoords;

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

        DirectedCoords.DirectedCoordsBuilder builder = DirectedCoords.builder()
                .coords(coords);

        if (r < 1e-5) {
            return builder.direction(ENextNodeDirection.CENTER).build();
        }

        // Normalize theta and initialTheta to [0, 2π)
        double normalizedTheta = ((theta % (2 * Math.PI)) + (2 * Math.PI)) % (2 * Math.PI);
        double normalizedInitialTheta = ((initialTheta % (2 * Math.PI)) + (2 * Math.PI)) % (2 * Math.PI);

        // Compute angular difference to determine movement direction
        double angleDiff = Math.abs(normalizedTheta - normalizedInitialTheta);
        if (angleDiff > Math.PI) {
            angleDiff = 2 * Math.PI - angleDiff;
        }

        // Determine base direction based on normalized theta
        if (normalizedTheta >= Math.PI / 4 && normalizedTheta < 3 * Math.PI / 4) {
            builder.direction(ENextNodeDirection.SOUTH); // Swapped to fix up = SOUTH issue
        } else if (normalizedTheta >= 3 * Math.PI / 4 && normalizedTheta < 5 * Math.PI / 4) {
            builder.direction(ENextNodeDirection.WEST);
        } else if (normalizedTheta >= 5 * Math.PI / 4 && normalizedTheta < 7 * Math.PI / 4) {
            builder.direction(ENextNodeDirection.NORTH); // Swapped to fix down = NORTH
        } else {
            builder.direction(ENextNodeDirection.EAST);
        }

        // If returning toward center (smaller radius than begin), use opposite direction
        if (r < begin && angleDiff < maxAngleDeltaRadians) {
            builder.direction(
                    switch (builder.build().getDirection()) {
                        case NORTH -> ENextNodeDirection.SOUTH;
                        case SOUTH -> ENextNodeDirection.NORTH;
                        case WEST -> ENextNodeDirection.EAST;
                        case EAST -> ENextNodeDirection.WEST;
                        default -> builder.build().getDirection(); // CENTER unchanged
                    }
            );
        }

        return builder.build();
    }
}