package org.zapphyre.fizzy;

import lombok.Builder;
import org.zapphyre.fizzy.matcher.build.GestureSupplier;
import org.zapphyre.fizzy.matcher.build.GraphSnaper;
import org.zapphyre.fizzy.grid.Node;
import org.zapphyre.model.ENextNodeDirection;
import org.zapphyre.model.PolarCoords;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

@Builder
public class StickMotionMapper implements GraphSnaper {

    private Node root = defaultNode();
    private Node next = root;

    @Builder.Default
    int newNodeTreshold = 3_000;

    @Builder.Default
    int rotationDeltaDeg = 90;

    @Builder.Default
    int gestureDuration = 210;

    @Builder.Default
    private final String pathBegin = "0";

    Node defaultNode() {
        return new Node(newNodeTreshold, Math.toRadians(rotationDeltaDeg), 0, 0, ENextNodeDirection.CENTER, pathBegin);
    }

    public GestureSupplier pathCompose(Flux<PolarCoords> stream) {
        return supplier -> {
            Disposable windowDisp = stream.window(Duration.ofMillis(gestureDuration))
                    .flatMap(Flux::collectList)
                    .filter(List::isEmpty)
                    .filter(_ -> !next.getPath().equals(pathBegin))
                    .subscribe(q -> {
                        supplier.accept(next.getPath());
                        next = root = defaultNode();
                    });

            return stream.map(q -> next.directionFromTheta(q))
                    .map(q -> next.movement(q))
                    .map(x -> next = x)
                    .doOnComplete(windowDisp::dispose)
                    .subscribe();
        };
    }

    @Override
    public Node snap() {
        return root;
    }
}
