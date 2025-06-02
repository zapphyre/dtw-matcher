package org.zapphyre.grid;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.HashMap;
import java.util.Map;

@Value
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@RequiredArgsConstructor
public class Node {

    int nodeLength;
    double begin;

    @EqualsAndHashCode.Include
    ENextNodeDirection pathFromPrev;

    Map<ENextNodeDirection, Node> nextNodes = new HashMap<>();

    public Node movement(DirectedCoords directedCoords) {
        double r = directedCoords.getCoords().getRadius();
        double theta = directedCoords.getCoords().getTheta();

        if (r + begin > nodeLength) {

            // Retrieve or create the next node
            return nextNodes.computeIfAbsent(directedCoords.getDirection(),
                    d -> {
                        return new Node(nodeLength, r, d);
                    });
        }

        return this;
    }

    public static DirectedCoords directionFromTheta(PolarCoords coords) {
        double theta = coords.getTheta();
        double normalized = ((theta % (2 * Math.PI)) + (2 * Math.PI)) % (2 * Math.PI);

        DirectedCoords.DirectedCoordsBuilder builder = DirectedCoords
                .builder()
                .coords(coords);

        if (normalized >= Math.PI / 4 && normalized < 3 * Math.PI / 4) {
            builder.direction(ENextNodeDirection.NORTH);
        } else if (normalized >= 3 * Math.PI / 4 && normalized < 5 * Math.PI / 4) {
            builder.direction(ENextNodeDirection.WEST);
        } else if (normalized >= 5 * Math.PI / 4 && normalized < 7 * Math.PI / 4) {
            builder.direction(ENextNodeDirection.SOUTH);
        } else {
            builder.direction(ENextNodeDirection.EAST);
        }

        return builder.build();
    }

}
