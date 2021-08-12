# language: en

Feature: Sleep feature
    Could execute sleep task as glacio feature

    Scenario: Define sleep tasks with specific parser
        When The night is out there
            Do success
        Then I rest for short time periods
            Do: sleep for 100 ms
            Execute success
            Do rest during 120 ms
        And I wait for some more short periods of time
            Execute await 110 ms
            Do success
            Execute pause during 120 ms
            Do success
            Execute: Wait for 100 ms
