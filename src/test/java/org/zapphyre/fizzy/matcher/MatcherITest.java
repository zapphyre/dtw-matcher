package org.zapphyre.fizzy.matcher;

import org.junit.jupiter.api.Test;
import org.zapphyre.fizzy.matcher.build.ToleranceConfigurer;
import org.zapphyre.fizzy.model.MatchDef;
import org.zapphyre.fizzy.model.MatchResult;
import org.zapphyre.fizzy.model.Result;
import org.zapphyre.fizzy.model.ToleranceConfig;

import java.util.List;

public class MatcherITest {

    private final ToleranceConfig config = ToleranceConfig.builder()
            .frequencyTolerancePercent(20.0)
            .orderEditDistanceRatio(0.5)
            .maxConsecutiveDrop(1)
            .build();

    private final MatchDef<String> matchDef = MatchDef.<String>builder()
            .result(Result.of("test"))
            .knownValues(List.of("1112233", "444555"))
            .build();

    private final Matcher<String> matcher = Matcher.<String>create(List.of(matchDef))
            .withTolerances(config);

    @Test
    void testMatcher() {
        List<String> knownValues = List.of(
                "888800000000022228822200000044444",
                "8888000000000222222200000044444",
                "888880000000002222222220000000044444",
                "888800000000022222222200000044444",
                "88888800002222222200004444444"
        );
        Result<String> resultTemplate = Result.of("totok");

        MatchDef<String> matchDef = MatchDef.<String>builder()
                .knownValues(knownValues)
                .result(resultTemplate)
                .build();

        ToleranceConfig toleranceConfig = ToleranceConfig.builder()
                .frequencyTolerancePercent(20.0)
                .orderEditDistanceRatio(0.8)
                .maxConsecutiveDrop(2)
                .build();

        ToleranceConfigurer<String> forKnownValuesMatcher = Matcher.create(List.of(matchDef));

        Matcher<String> stringMatcher = forKnownValuesMatcher.withTolerances(toleranceConfig);


        String input = "888800000000022222220000110044444";
        List<MatchResult<String>> match = stringMatcher.match(input);
    }

}
