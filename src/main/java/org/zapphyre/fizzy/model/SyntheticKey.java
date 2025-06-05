package org.zapphyre.fizzy.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
@Builder
public class SyntheticKey {
    Map<Character, Integer> digitCounts;
    Map<Character, List<Integer>> runLengths;
    List<Run> runSequence;

    public static SyntheticKey of(Map<Character, Integer> digitCounts,
                                  Map<Character, List<Integer>> runLengths,
                                  List<Run> runSequence) {
        return SyntheticKey.builder()
                .digitCounts(digitCounts)
                .runLengths(runLengths)
                .runSequence(runSequence)
                .build();
    }
}