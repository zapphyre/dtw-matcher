package org.zapphyre.fizzy.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class MatchDefinition<T> {
    List<String> knownValidValues;
    Result<T> resultTemplate;
    double frequencyTolerancePercent; // e.g., 10.0 for ±10%
    double orderEditDistanceRatio; // e.g., 0.1 for 10% of sequence length
    int maxConsecutiveDrop; // Max consecutive digits to drop (e.g., 2)
}
