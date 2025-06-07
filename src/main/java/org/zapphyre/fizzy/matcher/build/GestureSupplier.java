package org.zapphyre.fizzy.matcher.build;

import reactor.core.Disposable;

import java.util.function.Consumer;

@FunctionalInterface
public interface GestureSupplier {

    Disposable gestureCb(Consumer<String> gesture);
}
