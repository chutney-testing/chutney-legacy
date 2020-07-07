# language: en

Feature: Strategy parser feature
    Could execute tasks using execution strategies, i.e. soft assert, retry or default

    Scenario: Fails softly
        When a step fails (softly:)
            Do fail
        Then it keeps going to the next step
            Do success

    Scenario: Fails softly, again
        When A step (softly:) fails
            Do fail
        Then it keeps going to the next step
            Do success

    Scenario: Fails softly on executable steps
        When A step fails
            Do fail (softly:)
        Then it keeps going to the next step
            Do success

    Scenario: Fails softly without peeking other comments as strategies
        When (softly:) Failing a (non-catchable(: comment in between) step (with another parenthesized comment)
            Do fail (softly:)
        Then it keeps going to the next step
            Do success

    Scenario: Gracefully fallback to default strategy when type is unknown
        When A step fails (unknown-strat:)
            Do fail
        Then it keeps going to the next step
            Do success

    Scenario: Use a strategy with a specific parser
        When using a specific parser like Sleep
            Do sleep for 1 sec (softly:)
        Then the strategy doesn't affect parameters parsing
            Do success

    Scenario: Use a strategy with the default parser
        When Do fail (retry: every 5s for 3s)
        Then it fails 3 times
