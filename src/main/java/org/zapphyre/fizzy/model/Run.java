package org.zapphyre.fizzy.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Run {
    char digit;
    int length;

    public static Run of(char digit, int length) {
        return Run.builder().digit(digit).length(length).build();
    }
}