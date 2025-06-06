package org.zapphyre.fizzy.model;

import lombok.Value;

@Value(staticConstructor = "of")
public class Run {
    char digit;
    int length;
}