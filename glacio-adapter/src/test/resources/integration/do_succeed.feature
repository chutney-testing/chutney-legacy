# language: en

Feature: Good feature

    Scenario: Direct Success
        When Do success
        Then Execute success

    Scenario: Substeps Success
        When something is good
            Do success
        Then it is very good
            Execute success

    Scenario: Substeps with task hint Success
        When something is good
            Do (success) a nice thing
        Then it is very good
            Execute (success) a good thing
