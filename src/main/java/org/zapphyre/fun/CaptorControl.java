package org.zapphyre.fun;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CaptorControl {

    CaptorDestroy destroy;
    GraphSniper snap;
}
