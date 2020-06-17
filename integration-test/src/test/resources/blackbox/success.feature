# language: en
Feature: Success feature
    Could execute success task

    Scenario: Direct Success
        When Run debug direct success

    Scenario: Substeps Success
        Given Run debug direct success
        When I want to have one substep
            Execute success substep success
        Then I want to have more multiple substeps
            It's a first substep
            Do success
            It's a second substep
            Do: success
