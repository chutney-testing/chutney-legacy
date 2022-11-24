# language: en

Feature: Strategy parser feature
    Could execute actions using execution strategies, i.e. soft assert, retry or default

    Scenario: Fails softly
        When a step fails (softly:)
            Do fail
        Then it keeps going to the next step
            Do success

    Scenario: Fails softly, again
        When a step (softly:) fails
            Do fail
        Then it keeps going to the next step
            Do success

    Scenario: Fails softly on executable steps
        When a step fails softly
            Do fail (softly:)
        Then it keeps going to the next step
            Do success

    Scenario: Fails softly without peeking other comments as strategies
        When (softly:) failing a (non-catchable(: comment in between) step (with another parenthesized comment)
            Do fail (softly:)
        Then it keeps going to the next step
            Do success

    Scenario: Gracefully fallback to default strategy when type is unknown
        When a step succeeds (unknown-strat:)
            Do success

    Scenario: Use a strategy with a specific parser
        When using a specific parser like Sleep
            Do sleep for 1 sec (softly:)
        Then the strategy doesn't affect parameters parsing
            Do success

    Scenario: Use a strategy with the default parser
        When Do fail (retry: every 5 ms for 15 ms)
        Then it fails 3 times
