package org.zapphyre.fizzy.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;

@Value
@Builder
public class Result<T> {
    @With T value;

    public static <T> Result<T> of(T value) {
        return Result.<T>builder().value(value).build();
    }
}
