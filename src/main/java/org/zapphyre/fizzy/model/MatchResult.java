package org.zapphyre.fizzy.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MatchResult<T> {
    String knownString;
    double matchPercentage;
    T key;
}