package org.zapphyre.fizzy.matcher.build;


import org.zapphyre.fizzy.model.MatchDef;

import java.util.List;

@FunctionalInterface
public interface MatchDefSupplier<T> {
    ToleranceConfigurer<T> withMatchDefs(List<MatchDef<T>> matchDefs);
}
