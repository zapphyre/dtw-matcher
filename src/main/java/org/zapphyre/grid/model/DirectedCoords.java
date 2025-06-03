package org.zapphyre.grid.model;

import lombok.Builder;
import lombok.Value;
import org.zapphyre.model.PolarCoords;

@Value
@Builder
public class DirectedCoords {

    PolarCoords coords;
    ENextNodeDirection direction;
}
