package org.zapphyre.fizzy;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.zapphyre.model.PolarCoords;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.util.function.Consumer;
import java.util.function.Function;

@Value
@RequiredArgsConstructor
public class GesturizerAdapter<T> {
    Gesturizer gesturizer;
    Function<T, PolarCoords> polarCoordsMapper;

    @NonFinal
    Disposable gestureCb = Mono::empty;

    public Consumer<MonoSink<String>> finishAfterFirst(Flux<T> polarStream) {
        return q ->
                gestureCb = gesturizer.pathCompose(polarStream.map(polarCoordsMapper)).gestureCb(q::success);
    }

    public void dispose() {
        gestureCb.dispose();
    }
}
