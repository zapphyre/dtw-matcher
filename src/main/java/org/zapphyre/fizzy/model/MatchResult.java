package org.zapphyre.fizzy.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MatchResult {
    String knownString;
    double matchPercentage;

    public static MatchResult of(String knownString, double matchPercentage) {
        return MatchResult.builder()
                .knownString(knownString)
                .matchPercentage(matchPercentage)
                .build();
    }
}