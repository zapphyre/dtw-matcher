package org.zapphyre.fizzy.matcher.build;

import org.zapphyre.fizzy.matcher.Matcher;
import org.zapphyre.fizzy.model.ToleranceConfig;

@FunctionalInterface
public interface ToleranceConfigurer<T> {
    Matcher<T> withTolerances(ToleranceConfig config);
}