package org.zapphyre.fizzy.matcher;

import org.junit.jupiter.api.Test;
import org.zapphyre.fizzy.matcher.build.ToleranceConfigurer;
import org.zapphyre.fizzy.model.MatchDef;
import org.zapphyre.fizzy.model.MatchResult;
import org.zapphyre.fizzy.model.Result;
import org.zapphyre.fizzy.model.ToleranceConfig;

import java.util.List;

public class MatcherITest {

    @Test
    void testMatcher() {
        List<String> knownValues = List.of(
//                "2344888800000000022228822200000044444",
//                "234555558888000000000222222200000044444",
//                "2322222888880000000002222222220000000044444",
//                "123888800000000022222222200000044444",
//                "65688888800002222222200004444444"
                "00000000022222220000110044444"
        );
        Result<String> resultTemplate = Result.of("totok");

        MatchDef<String> matchDef = MatchDef.<String>builder()
                .knownValues(knownValues)
                .result(resultTemplate)
                .build();

        ToleranceConfig toleranceConfig = ToleranceConfig.builder()
                .frequencyTolerancePercent(10.0)
                .orderEditDistanceRatio(0.2)
                .maxConsecutiveDrop(2)
                .build();

        ToleranceConfigurer<String> forKnownValuesMatcher = Matcher.create(List.of(matchDef));

        Matcher<String> stringMatcher = forKnownValuesMatcher.withTolerances(toleranceConfig);


        String input = "888800000000022222220000110044444";
        List<MatchResult<String>> match = stringMatcher.match(input);
    }

}
