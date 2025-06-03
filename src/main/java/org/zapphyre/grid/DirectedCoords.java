package org.zapphyre.grid;

import lombok.Builder;
import lombok.Value;
import org.zapphyre.dtw.model.PolarCoords;

@Value
@Builder
public class DirectedCoords {

    PolarCoords coords;
    ENextNodeDirection direction;
}
