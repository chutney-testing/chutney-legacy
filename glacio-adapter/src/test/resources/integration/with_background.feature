# language: en
Feature: Feature with background

    Background:
        Given A background step
        And Another one with substeps
            First substep
            Second substep

    Scenario: A scenario with steps
        When Something happens
        Then Background step should be taken into account
