package org.zapphyre.dtw;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class Main {

    public static void main(String[] args) {

//        AtomicReference<List<PolarCoords>> referencePattern = new AtomicReference<>();
//        AxisEventFactory.rightStickStream().polarProducer(eventProducer.getWorker())
//                .transform(flux -> PolarCoordsSampler.samplePolarCoords(flux, Duration.ofMillis(50)))
//                .doOnNext(coord -> log.debug("Right stick raw: {}", coord))
//                .window(Duration.ofSeconds(1))
//                .flatMap(Flux::collectList)
//                .filter(list -> {
//                    boolean passes = list.size() >= 1;
//                    log.debug("Filter size>=1: passes={}, size={}", passes, list.size());
//                    return passes;
//                })
//                .doOnNext(pattern -> log.info("Right stick pattern: {} points", pattern.size()))
//                .subscribe(
//                        pattern -> {
//                            referencePattern.set(pattern);
//                            log.info("New reference pattern captured: {} points", pattern.size());
//                        },
//                        error -> log.error("Right stick error", error),
//                        () -> log.info("Right stick completed")
//                );
//
//        SimpleDTW dtw = new SimpleDTW(new PolarDistanceFunction());
//
//        // Process left stick and compare with the latest reference pattern
//        AxisEventFactory.leftStickStream().polarProducer(eventProducer.getWorker())
//                .transform(flux -> PolarCoordsSampler.samplePolarCoords(flux, Duration.ofMillis(50)))
////                .doOnNext(coord -> log.debug("Left stick raw: {}", coord))
////                .windowUntil(coord -> coord.getRadius() < 0.05 && Math.abs(coord.getTheta()) < 0.05, false)
////                .flatMap(window -> window
////                        .take(Duration.ofSeconds(2))
////                        .collectList()
////                        .doOnNext(list -> log.debug("Left stick window: size={}, content={}", list.size(), list)))
//                .window(Duration.ofSeconds(1))
//                .flatMap(Flux::collectList)
//                .filter(list -> {
//                    boolean passes = list.size() >= 1;
////                    log.debug("Filter size>=1: passes={}, size={}", passes, list.size());
//                    return passes;
//                })
//                .doOnNext(gesture -> log.info("Left stick gesture: {} points", gesture.size()))
//                .filter(gesture -> {
//                    boolean passes = referencePattern.get() != null && !referencePattern.get().isEmpty();
//                    log.debug("Filter reference available: passes={}", passes);
//                    return passes;
//                })
//                .flatMap(gesture -> {
//                    List<PolarCoords> ref = referencePattern.get();
//                    double distance = dtw.compute(gesture, ref);
//                    String result = distance < 1.5 ?
//                            "Match found! DTW Distance: " + distance :
//                            "No match. DTW Distance: " + distance;
//                    log.debug("DTW computed: distance={}", distance);
//                    return Mono.just(result);
//                })
//                .doOnNext(result -> log.debug("Before subscribe: result={}", result))
//                .subscribe(
//                        result -> log.info("Result: {}", result),
//                        error -> log.error("Left stick error", error),
//                        () -> log.info("Left stick completed")
//                );
//    }
//
//
//    public class PolarCoordsSampler {
//        public static Flux<PolarCoords> samplePolarCoords(Flux<PolarCoords> source, Duration sampleInterval) {
//            return source
//                    .timestamp() // Add timestamps (ms since epoch)
//                    .map(timed -> new org.asmus.dtw.PolarCoordsSampler.TimedPolarCoords(timed.getT2(), timed.getT1()))
//                    .bufferTimeout(100, Duration.ofSeconds(2)) // Buffer up to 100 points or 2s
//                    .flatMapIterable(buffer -> interpolate(buffer, sampleInterval))
//                    ;
////                    .doOnNext(coord -> log.debug("Sampled coord: r={}, theta={}", coord.getRadius(), coord.getTheta()));
//        }

//        root = next = new Node(3_000, 0, ENextNodeDirection.CENTER);
//
//        AxisEventFactory.leftStickStream().polarProducer(eventProducer.getWorker())
//                .take(Duration.ofSeconds(4))
//                .doOnComplete(() -> {
//                    System.out.println(root);
//                })
//                .map(Node::directionFromTheta)
//                .distinctUntilChanged(DirectedCoords::getDirection)
//                .map(q -> next.movement(q))
//                .subscribe(x -> next = x);
    }
}
