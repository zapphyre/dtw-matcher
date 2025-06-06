package org.zapphyre.fizzy.matcher;

import lombok.Builder;
import lombok.Value;
import org.zapphyre.fizzy.matcher.build.ToleranceConfigurer;
import org.zapphyre.fizzy.model.MatchDef;
import org.zapphyre.fizzy.model.MatchResult;
import org.zapphyre.fizzy.model.Run;
import org.zapphyre.fizzy.model.ToleranceConfig;

import java.util.*;
import java.util.stream.Collectors;

@Value
@Builder
public class Matcher<T> {
    List<MatchDef<T>> matchDefs;
    ToleranceConfig toleranceConfig;

    public static <T> ToleranceConfigurer<T> create(List<MatchDef<T>> matchDefs) {
        return config -> Matcher.<T>builder()
                .matchDefs(matchDefs)
                .toleranceConfig(config)
                .build();
    }

    public List<MatchResult<T>> match(String input) {
        List<Run> inputRuns = filterRuns(parseRuns(input));

        return matchDefs.stream()
                .flatMap(def -> def.getKnownValues().stream()
                        .map(known -> {
                            List<Run> knownRuns = filterRuns(parseRuns(known));
                            double score = computeMatchScore(inputRuns, knownRuns);
                            return MatchResult.<T>builder()
                                    .knownString(known)
                                    .result(def.getResult())
                                    .matchPercentage(score * 100)
                                    .build();
                        }))
                .sorted(Comparator.comparingDouble(MatchResult<T>::getMatchPercentage).reversed())
                .toList();
    }

     List<Run> parseRuns(String input) {
        if (input == null || input.isEmpty()) return List.of();

        List<Run> runs = new ArrayList<>();
        char current = input.charAt(0);
        int count = 1;

        for (int i = 1; i < input.length(); i++) {
            char c = input.charAt(i);
            if (!Character.isDigit(c)) throw new IllegalArgumentException("Invalid character: " + c);

            if (c == current) {
                count++;
            } else {
                runs.add(Run.of(current, count));
                current = c;
                count = 1;
            }
        }
        runs.add(Run.of(current, count));
        return runs;
    }

     List<Run> filterRuns(List<Run> runs) {
        return runs.stream()
                .filter(run -> run.getLength() > toleranceConfig.getMaxConsecutiveDrop())
                .toList();
    }

     double computeMatchScore(List<Run> input, List<Run> known) {
        double freqScore = computeFrequencyScore(input, known);
        double orderScore = computeOrderScore(input, known);

        return (1 - toleranceConfig.getOrderEditDistanceRatio()) * freqScore +
                toleranceConfig.getOrderEditDistanceRatio() * orderScore;
    }

     double computeFrequencyScore(List<Run> input, List<Run> known) {
        Map<Character, Integer> inputCounts = computeCounts(input);
        Map<Character, Integer> knownCounts = computeCounts(known);

        Set<Character> allDigits = new HashSet<>(inputCounts.keySet());
        allDigits.addAll(knownCounts.keySet());

        double tolerance = toleranceConfig.getFrequencyTolerancePercent() / 100.0;

        double totalWeight = 0;
        double matchWeight = 0;

        for (char digit : allDigits) {
            int inCount = inputCounts.getOrDefault(digit, 0);
            int knCount = knownCounts.getOrDefault(digit, 0);
            int max = Math.max(inCount, knCount);

            double maxAllowed = knCount * (1 + tolerance);
            double minAllowed = knCount * (1 - tolerance);

            boolean withinRange = inCount >= Math.floor(minAllowed) && inCount <= Math.ceil(maxAllowed);
            matchWeight += withinRange ? max : max * Math.max(0.0, 1.0 - Math.abs(inCount - knCount) / (knCount * tolerance + 1));
            totalWeight += max;
        }

        return totalWeight > 0 ? matchWeight / totalWeight : 1.0;
    }

     Map<Character, Integer> computeCounts(List<Run> runs) {
        Map<Character, Integer> counts = new HashMap<>();
        for (Run run : runs) {
            counts.merge(run.getDigit(), run.getLength(), Integer::sum);
        }
        return counts;
    }

     double computeOrderScore(List<Run> input, List<Run> known) {
        String s1 = input.stream().map(r -> String.valueOf(r.getDigit())).collect(Collectors.joining());
        String s2 = known.stream().map(r -> String.valueOf(r.getDigit())).collect(Collectors.joining());

        if (s1.equals(s2)) return 1.0;

        int dist = computeLevenshtein(s1, s2);
        int maxLen = Math.max(s1.length(), s2.length());
        int allowedDist = Math.max(1, (int) Math.ceil(toleranceConfig.getOrderEditDistanceRatio() * maxLen));

        return Math.max(0.0, 1.0 - ((double) dist / (maxLen + allowedDist)));
    }

     int computeLevenshtein(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= s2.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[s1.length()][s2.length()];
    }
}
