# language: en

Feature: Sleep feature
    Could execute sleep task as glacio feature

    Scenario: Simple inputs with sleep task
        When The night is out there
            I rest for short time periods
                Do sleep for 1 sec
                Execute success
                Do rest during 200 ms
            I wait for some more short periods of time
                Execute await 300 ms
                Do success
                Execute stand by during 200 ms
                Do success
                Execute stop for 200 ms
