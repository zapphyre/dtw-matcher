package org.zapphyre.fizzy.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class MatchDef<T> {
    T key;
    List<String> knownValues;
}
