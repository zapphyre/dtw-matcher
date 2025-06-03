package org.zapphyre;

import org.zapphyre.fun.CaptorControl;
import org.zapphyre.fun.GraphSniper;
import org.zapphyre.fun.MotionCaptor;
import org.zapphyre.grid.Node;
import org.zapphyre.grid.model.DirectedCoords;
import reactor.core.Disposable;

public class StickMotionMapper implements GraphSniper {

    Node root, next;

    MotionCaptor captor = stream -> {
        Disposable subscribe = stream.map(q -> next.directionFromTheta(q))
                .distinctUntilChanged(DirectedCoords::getDirection)
                .map(q -> next.movement(q))
                .map(x -> next = x)
                .subscribe(q -> {
                    System.out.println("key: " + next.getPath());
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
