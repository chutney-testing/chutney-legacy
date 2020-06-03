# language: en

Feature: Debug feature
    Could execute debug task

    Scenario: Simple Debug
        Given Run debug direct debug
        When I want to have one substep
            Execute debug substep debug
        Then I want to have more multiple substeps
            It's a first substep
                Do debug
            It's a second substep
                Do: debug
