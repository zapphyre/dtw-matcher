package org.zapphyre.fizzy;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.zapphyre.model.PolarCoords;
import org.zapphyre.model.error.GestureTimeoutException;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@Value
@RequiredArgsConstructor
public class GesturizerAdapter<T> {
    Gesturizer gesturizer;
    Function<T, PolarCoords> polarCoordsMapper;

    @NonFinal
    Disposable gestureCb = Mono::empty;

    public Consumer<MonoSink<String>> finishAfterFirst(Flux<T> polarStream) {
        return q -> {
            ScheduledFuture<?> timeout = Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                log.debug("Polar stream timeout");
                q.error(new GestureTimeoutException("Gesture timeout after 4 sec"));
            }, 4, TimeUnit.SECONDS);

            q.onDispose(() -> timeout.cancel(true));

            gestureCb = gesturizer
                    .pathCompose(polarStream.map(polarCoordsMapper))
                    .gestureCb(q::success);
        };
    }

    public void dispose() {
        gestureCb.dispose();
    }
}
