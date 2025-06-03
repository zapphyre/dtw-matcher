package org.zapphyre.fun;

import org.zapphyre.model.PolarCoords;
import reactor.core.publisher.Flux;

@FunctionalInterface
public interface MotionCaptor {

    CaptorControl onStream(Flux<PolarCoords> polarCoords);
}
