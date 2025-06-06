package org.zapphyre.fizzy.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class MatchDefinition<T> {
    List<String> knownValidValues;
    Result<T> resultTemplate;
}
