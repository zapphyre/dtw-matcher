package org.zapphyre.fizzy;

import lombok.Builder;
import lombok.Value;
import org.zapphyre.fizzy.grid.Node;
import org.zapphyre.fizzy.matcher.build.GestureSupplier;
import org.zapphyre.model.ENextNodeDirection;
import org.zapphyre.model.PolarCoords;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Value
@Builder
public class Gesturizer {

    @Builder.Default
    int newNodeTreshold = 3_000;
    @Builder.Default
    int rotationDeltaDeg = 90;
    @Builder.Default
    int gestureDuration = 210;
    @Builder.Default
    String pathBegin = "0";

    public static Gesturizer withDefaults() {
        return Gesturizer.builder().build();
    }

    Node defaultNode() {
        return new Node(newNodeTreshold, Math.toRadians(rotationDeltaDeg), 0, 0, ENextNodeDirection.CENTER, pathBegin);
    }

    public GestureSupplier pathCompose(Flux<PolarCoords> stream) {
        final AtomicReference<Node> next = new AtomicReference<>(defaultNode());

        return supplier -> {
            Disposable windowDisp = stream.window(Duration.ofMillis(gestureDuration))
                    .flatMap(Flux::collectList)
                    .filter(List::isEmpty)
                    .filter(_ -> !next.get().getPath().equals(pathBegin))
                    .subscribe(q -> {
                        supplier.accept(next.get().getPath());
                        next.set(defaultNode());
                    });

            return stream.map(q -> next.get().directionFromTheta(q))
                    .map(q -> next.get().movement(q))
                    .doOnComplete(windowDisp::dispose)
                    .doOnCancel(windowDisp::dispose)
                    .subscribe(next::set);
        };
    }
}
