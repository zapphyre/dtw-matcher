package org.zapphyre;

import org.zapphyre.fun.CaptorControl;
import org.zapphyre.fun.GraphSnaper;
import org.zapphyre.fun.MotionCaptor;
import org.zapphyre.grid.Node;
import org.zapphyre.grid.model.DirectedCoords;
import org.zapphyre.grid.model.ENextNodeDirection;
import org.zapphyre.model.PolarCoords;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

public class StickMotionMapper implements GraphSnaper {

    private Node root = defaultNode();
    private Node next = root;

    Node defaultNode() {
        return new Node(3_000, Math.toRadians(90), 0, 0, ENextNodeDirection.CENTER, "0");
    }

    public Flux<String> pathCompose(Flux<PolarCoords> stream) {
        StringBuilder directions = new StringBuilder();

        Disposable windowDisp = stream.window(Duration.ofMillis(210))
                .flatMap(Flux::collectList)
                .filter(List::isEmpty)
                .subscribe(q -> {
                    next = root = defaultNode();
                    directions.setLength(0);
                });

        return stream.map(q -> next.directionFromTheta(q))
//                .doOnDiscard(DirectedCoords.class, q -> System.out.println("Discarding " + q))
                .map(q -> next.movement(q))
                .map(x -> next = x)
//                .distinctUntilChanged(Node::getPathFromPrev)
                .map(q -> directions.append(q.getPathFromPrev().ordinal()))
                .map(q -> q.toString())

//                .doOnComplete(windowDisp::dispose)
//                .map(Node::getPath);
                ;
    }

    public Disposable captor(Flux<PolarCoords> stream) {
        return stream.map(q -> next.directionFromTheta(q))
                .distinctUntilChanged(DirectedCoords::getDirection)
                .map(q -> next.movement(q))
                .subscribe(x -> next = x);
    }

    public MotionCaptor captor = stream -> {
        Disposable subscribe = stream.map(q -> next.directionFromTheta(q))
                .distinctUntilChanged(DirectedCoords::getDirection)
                .map(q -> next.movement(q))
                .map(x -> next = x)
                .subscribe(q -> {
//                    System.out.println("key: " + next.getPath());
                });

        return CaptorControl.builder()
                .snap(this)
                .destroy(subscribe::dispose)
                .build();
    };

    @Override
    public Node snap() {
        return root;
    }
}
