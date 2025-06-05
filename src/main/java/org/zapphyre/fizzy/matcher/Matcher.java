package org.zapphyre.fizzy.matcher;

import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.zapphyre.fizzy.model.MatchDefinition;
import org.zapphyre.fizzy.model.MatchResult;
import org.zapphyre.fizzy.model.Run;
import org.zapphyre.fizzy.model.SyntheticKey;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Value
public class Matcher<T> {
    MatchDefinition<T> matchDefinition;
    List<SyntheticKey> knownKeys;

    static final int MIN_COUNT_THRESHOLD = 2;
    static final double FREQUENCY_WEIGHT = 0.6;
    static final double ORDER_WEIGHT = 0.4;
    static final int NOISE_RUN_LENGTH = 1;

    @Builder
            Matcher(MatchDefinition<T> matchDefinition) {
        this.matchDefinition = matchDefinition;
        this.knownKeys = matchDefinition.getKnownValidValues().stream()
                .map(this::computeSyntheticKey)
                .toList();
    }

    List<MatchResult> match(String input) {
        SyntheticKey inputKey = computeSyntheticKey(input);
        return IntStream.range(0, matchDefinition.getKnownValidValues().size())
                .mapToObj(i -> {
                    String knownString = matchDefinition.getKnownValidValues().get(i);
                    SyntheticKey knownKey = knownKeys.get(i);
                    double matchScore = computeMatchScore(inputKey, knownKey);
                    log.debug("DEBUG: Matching '{}' to known '{}': score={}\n",
                            input, knownString, matchScore * 100);
                    return MatchResult.of(knownString, matchScore * 100);
                })
                .sorted((a, b) -> Double.compare(b.getMatchPercentage(), a.getMatchPercentage()))
                .toList();
    }

    SyntheticKey computeSyntheticKey(String input) {
        List<Run> rawRuns = parseRuns(input);
        List<Run> filteredRuns = filterRuns(rawRuns);
        SyntheticKey key = buildSyntheticKey(filteredRuns);
        log.debug("DEBUG: Input '{}' filtered to runs: {}\n",
                input, filteredRuns.stream()
                        .map(r -> r.getDigit() + ":" + r.getLength())
                        .collect(Collectors.joining(", ")));
        return key;
    }

    List<Run> parseRuns(String input) {
        if (input == null || input.isEmpty()) {
            return List.of();
        }
        List<Run> runs = new ArrayList<>();
        char currentDigit = input.charAt(0);
        int currentRunLength = 1;

        for (int i = 1; i < input.length(); i++) {
            char c = input.charAt(i);
            if (!Character.isDigit(c)) {
                throw new IllegalArgumentException("Invalid character: " + c);
            }
            if (c == currentDigit) {
                currentRunLength++;
            } else {
                runs.add(Run.of(currentDigit, currentRunLength));
                currentDigit = c;
                currentRunLength = 1;
            }
        }
        runs.add(Run.of(currentDigit, currentRunLength));
        return List.copyOf(runs);
    }

    List<Run> filterRuns(List<Run> rawRuns) {
        int maxConsecutiveDrop = matchDefinition.getMaxConsecutiveDrop();
        return rawRuns.stream()
                .filter(run -> run.getLength() > NOISE_RUN_LENGTH &&
                        run.getLength() > maxConsecutiveDrop)
                .toList();
    }

    SyntheticKey buildSyntheticKey(List<Run> filteredRuns) {
        Map<Character, Integer> digitCounts = filteredRuns.stream()
                .collect(Collectors.groupingBy(
                        Run::getDigit,
                        Collectors.summingInt(Run::getLength)));
        Map<Character, List<Integer>> runLengths = filteredRuns.stream()
                .collect(Collectors.groupingBy(
                        Run::getDigit,
                        Collectors.mapping(Run::getLength, Collectors.toList())));
        List<Run> runSequence = List.copyOf(filteredRuns);
        return SyntheticKey.of(
                Collections.unmodifiableMap(digitCounts),
                Collections.unmodifiableMap(runLengths),
                runSequence);
    }

    double computeMatchScore(SyntheticKey inputKey, SyntheticKey knownKey) {
        double frequencyScore = computeFrequencyScore(inputKey, knownKey);
        double orderScore = computeOrderScore(inputKey, knownKey);
        log.debug("DEBUG: frequencyScore={}, orderScore={}\n", frequencyScore, orderScore);
        return FREQUENCY_WEIGHT * frequencyScore + ORDER_WEIGHT * orderScore;
    }

    double computeFrequencyScore(SyntheticKey inputKey, SyntheticKey knownKey) {
        if (inputKey.getDigitCounts().equals(knownKey.getDigitCounts()) &&
                inputKey.getRunLengths().equals(knownKey.getRunLengths())) {
            return 1.0;
        }
        double countScore = computeCountScore(inputKey, knownKey);
        double runScore = computeRunScore(inputKey, knownKey);
        double finalScore = (countScore + runScore) / 2;
        log.debug("DEBUG: countScore={}, runScore={}\n", countScore, runScore);
        return finalScore;
    }

    double computeCountScore(SyntheticKey inputKey, SyntheticKey knownKey) {
        double tolerance = matchDefinition.getFrequencyTolerancePercent() / 100.0;
        Set<Character> allDigits = new HashSet<>();
        allDigits.addAll(inputKey.getDigitCounts().keySet());
        allDigits.addAll(knownKey.getDigitCounts().keySet());

        return allDigits.stream()
                .filter(c -> {
                    int inputCount = inputKey.getDigitCounts().getOrDefault(c, 0);
                    int knownCount = knownKey.getDigitCounts().getOrDefault(c, 0);
                    return !(inputCount < MIN_COUNT_THRESHOLD && knownCount == 0) &&
                            !(knownCount < MIN_COUNT_THRESHOLD && inputCount == 0);
                })
                .mapToDouble(c -> {
                    int inputCount = inputKey.getDigitCounts().getOrDefault(c, 0);
                    int knownCount = knownKey.getDigitCounts().getOrDefault(c, 0);
                    double maxAllowed = knownCount * (1 + tolerance);
                    double minAllowed = Math.max(0, knownCount * (1 - tolerance));
                    double weight = Math.max(inputCount, knownCount);
                    if (inputCount >= Math.floor(minAllowed) && inputCount <= Math.ceil(maxAllowed)) {
                        return weight;
                    }
                    double deviation = Math.min(
                            Math.abs(inputCount - minAllowed),
                            Math.abs(inputCount - maxAllowed));
                    double maxDeviation = tolerance * knownCount;
                    return weight * (maxDeviation > 0 ? Math.max(0.0, 1.0 - (deviation / maxDeviation)) : 0.0);
                })
                .sum() / Math.max(1, allDigits.stream()
                .filter(c -> {
                    int inputCount = inputKey.getDigitCounts().getOrDefault(c, 0);
                    int knownCount = knownKey.getDigitCounts().getOrDefault(c, 0);
                    return !(inputCount < MIN_COUNT_THRESHOLD && knownCount == 0) &&
                            !(knownCount < MIN_COUNT_THRESHOLD && inputCount == 0);
                })
                .mapToDouble(c -> Math.max(
                        inputKey.getDigitCounts().getOrDefault(c, 0),
                        knownKey.getDigitCounts().getOrDefault(c, 0)))
                .sum());
    }

    double computeRunScore(SyntheticKey inputKey, SyntheticKey knownKey) {
        double tolerance = matchDefinition.getFrequencyTolerancePercent() / 100.0;
        Set<Character> allDigits = new HashSet<>();
        allDigits.addAll(inputKey.getDigitCounts().keySet());
        allDigits.addAll(knownKey.getDigitCounts().keySet());

        return allDigits.stream()
                .filter(c -> {
                    int inputCount = inputKey.getDigitCounts().getOrDefault(c, 0);
                    int knownCount = knownKey.getDigitCounts().getOrDefault(c, 0);
                    return !(inputCount < MIN_COUNT_THRESHOLD && knownCount == 0) &&
                            !(knownCount < MIN_COUNT_THRESHOLD && inputCount == 0);
                })
                .mapToDouble(c -> {
                    List<Integer> inputRuns = new ArrayList<>(inputKey.getRunLengths().getOrDefault(c, List.of()));
                    List<Integer> knownRuns = new ArrayList<>(knownKey.getRunLengths().getOrDefault(c, List.of()));
                    Collections.sort(inputRuns);
                    Collections.sort(knownRuns);
                    int maxRuns = Math.max(inputRuns.size(), knownRuns.size());
                    if (maxRuns == 0) return 0.0;
                    double runMatch = 0.0;
                    double weightSum = 0.0;
                    for (int i = 0; i < maxRuns; i++) {
                        int inputRun = i < inputRuns.size() ? inputRuns.get(i) : 0;
                        int knownRun = i < knownRuns.size() ? knownRuns.get(i) : 0;
                        double maxAllowed = knownRun * (1 + tolerance);
                        double minAllowed = Math.max(0, knownRun * (1 - tolerance));
                        double weight = Math.max(inputRun, knownRun);
                        if (inputRun >= Math.floor(minAllowed) && inputRun <= Math.ceil(maxAllowed)) {
                            runMatch += weight;
                        } else {
                            double deviation = Math.min(
                                    Math.abs(inputRun - minAllowed),
                                    Math.abs(inputRun - maxAllowed));
                            double maxDeviation = tolerance * knownRun;
                            runMatch += weight * (maxDeviation > 0 ? Math.max(0.0, 1.0 - (deviation / maxDeviation)) : 0.0);
                        }
                        weightSum += weight;
                    }
                    return weightSum > 0 ? runMatch / weightSum : 0.0;
                })
                .average()
                .orElse(0.0);
    }

    double computeOrderScore(SyntheticKey inputKey, SyntheticKey knownKey) {
        List<Run> inputRuns = inputKey.getRunSequence();
        List<Run> knownRuns = knownKey.getRunSequence();

        List<Run> filteredInputRuns = inputRuns.stream()
                .filter(r -> inputKey.getDigitCounts().getOrDefault(r.getDigit(), 0) >= MIN_COUNT_THRESHOLD ||
                        knownKey.getDigitCounts().getOrDefault(r.getDigit(), 0) >= MIN_COUNT_THRESHOLD)
                .toList();
        List<Run> filteredKnownRuns = knownRuns.stream()
                .filter(r -> knownKey.getDigitCounts().getOrDefault(r.getDigit(), 0) >= MIN_COUNT_THRESHOLD ||
                        inputKey.getDigitCounts().getOrDefault(r.getDigit(), 0) >= MIN_COUNT_THRESHOLD)
                .toList();

        if (filteredInputRuns.isEmpty() && filteredKnownRuns.isEmpty()) {
            return 1.0;
        }
        if (filteredInputRuns.isEmpty() || filteredKnownRuns.isEmpty()) {
            return 0.0;
        }

        String inputSeq = filteredInputRuns.stream()
                .map(r -> String.valueOf(r.getDigit()))
                .collect(Collectors.joining());
        String knownSeq = filteredKnownRuns.stream()
                .map(r -> String.valueOf(r.getDigit()))
                .collect(Collectors.joining());

        int editDistance = computeLevenshteinDistance(inputSeq, knownSeq);
        if (editDistance == 0) {
            return 1.0;
        }

        int maxLength = Math.max(inputSeq.length(), knownSeq.length());
        int maxEdits = Math.max(1, (int) Math.ceil(matchDefinition.getOrderEditDistanceRatio() * maxLength));
        double score = 1.0 - (double) editDistance / (maxLength + maxEdits);
        log.debug("DEBUG: editDistance={}, maxEdits={}, orderSeq='{}' vs '{}'\n",
                editDistance, maxEdits, inputSeq, knownSeq);
        return Math.max(0.0, score);
    }

    int computeLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), // Insertion, deletion
                        dp[i - 1][j - 1] + cost // Substitution
                );
                // Check for swap (Damerau-Levenshtein)
                if (i > 1 && j > 1 &&
                        s1.charAt(i - 1) == s2.charAt(j - 2) &&
                        s1.charAt(i - 2) == s2.charAt(j - 1)) {
                    dp[i][j] = Math.min(dp[i][j], dp[i - 2][j - 2] + 1);
                }
            }
        }
        return dp[s1.length()][s2.length()];
    }
}