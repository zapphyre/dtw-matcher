package org.zapphyre.fizzy.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class MatchDef<T> {
    Result<T> result;
    List<String> knownValues;
}
