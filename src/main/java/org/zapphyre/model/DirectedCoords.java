package org.zapphyre.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DirectedCoords {

    PolarCoords coords;
    ENextNodeDirection direction;
}
