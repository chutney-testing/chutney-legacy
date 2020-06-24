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

    Scenario: See on sleep
        When something
            Do sleep for 1 sec (softly:)
        Then it keeps going to the next step
            Do success

    Scenario: Simple http get
        When Do http-get Request chutney-testing github page
            On GITHUB_API
            With uri /orgs/chutney-testing
            With timeout 2000 s
            With headers
            | X-Extra-Header | An extra header |
            Take statusOk ${200 == #status}
            Take jsonBody ${#json(#body, '$')}
            Keep headersString ${#headers}
        Then Execute debug
