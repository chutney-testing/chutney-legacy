package com.chutneytesting.task.assertion.placeholder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlaceholderAsserterUtils {

    private static final List<PlaceholderAsserter> asserters = new ArrayList<>();

    static {
        asserters.add(new IsNullAsserter());
        asserters.add(new NotNullAsserter());
        asserters.add(new ContainsAsserter());
        asserters.add(new BeforeDateAsserter());
        asserters.add(new AfterDateAsserter());
        asserters.add(new MatchesStringAsserter());
    }

    public static final Optional<PlaceholderAsserter> getAsserterMatching(Object toMatch) {
        if (toMatch == null) {
            return Optional.of(new IsNullAsserter());
        }
        return asserters.stream().filter(a -> a.canApply(toMatch.toString())).findFirst();
    }
}
