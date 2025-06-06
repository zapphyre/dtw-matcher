package org.zapphyre.fizzy.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ToleranceConfig {
    double frequencyTolerancePercent;
    double orderEditDistanceRatio;
    int maxConsecutiveDrop;
}