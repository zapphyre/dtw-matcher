package org.zapphyre.dtw.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;

@With
@Value
@Builder
public class PolarCoords {
    double radius;
    double theta;

    public boolean isZero() {
        return radius == 0 && theta == 0;
    }
}
