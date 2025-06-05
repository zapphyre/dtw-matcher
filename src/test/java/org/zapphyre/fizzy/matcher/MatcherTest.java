package org.zapphyre.fizzy.matcher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zapphyre.fizzy.model.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MatcherTest {

    private Matcher<String> matcher;
    private MatchDefinition<String> definition;
    
    @BeforeEach
    void setUp() {
        definition = MatchDefinition.<String>builder()
                .knownValidValues(List.of("8888000000000222222200000044444"))
                .resultTemplate(Result.of(null))
                .frequencyTolerancePercent(10.0)
                .orderEditDistanceRatio(0.1)
                .maxConsecutiveDrop(2)
                .build();
        matcher = Matcher.<String>builder()
                .matchDefinition(definition)
                .build();
    }

    @Test
    void testMatcher() {
        List<String> knownValues = List.of(
                "888800000000022228822200000044444",
                "8888000000000222222200000044444",
                "888880000000002222222220000000044444",
                "888800000000022222222200000044444",
                "88888800002222222200004444444"
        );
        Result<String> resultTemplate = Result.of(null);
        MatchDefinition<String> definition = MatchDefinition.<String>builder()
                .knownValidValues(knownValues)
                .resultTemplate(resultTemplate)
                .frequencyTolerancePercent(20.0)
                .orderEditDistanceRatio(0.8)
                .maxConsecutiveDrop(2)
                .build();

        Matcher<String> matcher = Matcher.<String>builder()
                .matchDefinition(definition)
                .build();

        String input = "8888000000000222222200001110044444"; // Extra "88" inserted
        List<MatchResult> results = matcher.match(input);
    }

    @Test
    void parseRuns_emptyInput_returnsEmptyList() {
        List<Run> runs = matcher.parseRuns("");
        assertTrue(runs.isEmpty());
    }

    @Test
    void parseRuns_validInput_parsesCorrectRuns() {
        List<Run> runs = matcher.parseRuns("88811122");
        List<Run> expected = List.of(
                Run.of('8', 3),
                Run.of('1', 3),
                Run.of('2', 2)
        );
        assertEquals(expected, runs);
    }

    @Test
    void parseRuns_invalidCharacter_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> matcher.parseRuns("888a22"));
    }

    @Test
    void filterRuns_maxConsecutiveDropAndNoise_filtersCorrectly() {
        List<Run> rawRuns = List.of(
                Run.of('8', 3),
                Run.of('1', 2),
                Run.of('2', 1),
                Run.of('3', 4)
        );
        List<Run> filtered = matcher.filterRuns(rawRuns);
        List<Run> expected = List.of(
                Run.of('8', 3),
                Run.of('3', 4)
        );
        assertEquals(expected, filtered);
    }

    @Test
    void filterRuns_noFiltering_neededWhenAllRunsValid() {
        List<Run> rawRuns = List.of(
                Run.of('8', 3),
                Run.of('0', 5)
        );
        List<Run> filtered = matcher.filterRuns(rawRuns);
        assertEquals(rawRuns, filtered);
    }

    @Test
    void buildSyntheticKey_emptyRuns_returnsEmptyKey() {
        SyntheticKey key = matcher.buildSyntheticKey(List.of());
        assertTrue(key.getDigitCounts().isEmpty());
        assertTrue(key.getRunLengths().isEmpty());
        assertTrue(key.getRunSequence().isEmpty());
    }

    @Test
    void buildSyntheticKey_validRuns_buildsCorrectKey() {
        List<Run> runs = List.of(
                Run.of('8', 3),
                Run.of('0', 5)
        );
        SyntheticKey key = matcher.buildSyntheticKey(runs);
        assertEquals(Map.of('8', 3, '0', 5), key.getDigitCounts());
        assertEquals(Map.of('8', List.of(3), '0', List.of(5)), key.getRunLengths());
        assertEquals(runs, key.getRunSequence());
    }

    @Test
    void computeSyntheticKey_filtersAndBuildsCorrectly() {
        SyntheticKey key = matcher.computeSyntheticKey("88811222");
        Map<Character, Integer> expectedCounts = Map.of('8', 3, '2', 3); // Updated to include 2:3
        Map<Character, List<Integer>> expectedRunLengths = Map.of('8', List.of(3), '2', List.of(3));
        List<Run> expectedSequence = List.of(Run.of('8', 3), Run.of('2', 3));
        assertEquals(expectedCounts, key.getDigitCounts());
        assertEquals(expectedRunLengths, key.getRunLengths());
        assertEquals(expectedSequence, key.getRunSequence());
    }

    @Test
    void computeCountScore_identicalCounts_scoresOne() {
        SyntheticKey key = matcher.computeSyntheticKey("88880000");
        double score = matcher.computeCountScore(key, key);
        assertEquals(1.0, score, 0.001);
    }

    @Test
    void computeCountScore_withinTolerance_scoresHigh() {
        SyntheticKey inputKey = matcher.computeSyntheticKey("88880000"); // 8:4, 0:4
        SyntheticKey knownKey = matcher.computeSyntheticKey("888880000"); // 8:5, 0:4
        double score = matcher.computeCountScore(inputKey, knownKey);
        assertTrue(score > 0.8, "Score should be high: " + score);
    }

    @Test
    void computeRunScore_identicalRuns_scoresOne() {
        SyntheticKey key = matcher.computeSyntheticKey("88880000");
        double score = matcher.computeRunScore(key, key);
        assertEquals(1.0, score, 0.001);
    }

    @Test
    void computeRunScore_withinTolerance_scoresHigh() {
        SyntheticKey inputKey = matcher.computeSyntheticKey("88880000"); // 8:4, 0:4
        SyntheticKey knownKey = matcher.computeSyntheticKey("888880000"); // 8:5, 0:4
        double score = matcher.computeRunScore(inputKey, knownKey);
        assertTrue(score > 0.8, "Score should be high: " + score);
    }

    @Test
    void computeFrequencyScore_identicalKeys_scoresOne() {
        SyntheticKey key = matcher.computeSyntheticKey("88880000");
        double score = matcher.computeFrequencyScore(key, key);
        assertEquals(1.0, score, 0.001);
    }

    @Test
    void computeOrderScore_identicalSequences_scoresOne() {
        SyntheticKey key = matcher.computeSyntheticKey("888800002222");
        double score = matcher.computeOrderScore(key, key);
        assertEquals(1.0, score, 0.001);
    }

    @Test
    void computeOrderScore_singleEdit_scoresHigh() {
        SyntheticKey inputKey = matcher.computeSyntheticKey("8888000022221"); // 1 filtered out
        SyntheticKey knownKey = matcher.computeSyntheticKey("888800002222");
        double score = matcher.computeOrderScore(inputKey, knownKey);
        assertEquals(1.0, score, 0.001); // Filtered sequences identical
    }

    @Test
    void computeLevenshteinDistance_identicalStrings_returnsZero() {
        int distance = matcher.computeLevenshteinDistance("802", "802");
        assertEquals(0, distance);
    }

    @Test
    void computeLevenshteinDistance_singleEdit_returnsOne() {
        int distance = matcher.computeLevenshteinDistance("802", "820");
        assertEquals(1, distance);
    }

    @Test
    void match_exactMatch_scoresHundredPercent() {
        String input = "8888000000000222222200000044444";
        List<MatchResult> results = matcher.match(input);
        assertEquals(1, results.size());
        assertEquals(100.0, results.get(0).getMatchPercentage(), 0.001);
    }

    @Test
    void match_withNoiseAndDrop_scoresHigh() {
        String input = "8888110000000222222200000044444"; // 11 dropped
        List<MatchResult> results = matcher.match(input);
        assertEquals(1, results.size());
        assertTrue(results.get(0).getMatchPercentage() > 90.0, "Score: " + results.get(0).getMatchPercentage());
    }
}
