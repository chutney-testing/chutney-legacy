# language: en
Feature: Feature with multiple non executable steps

    Scenario: First scenario
        When We try something
            First substep of action step
            Second substep of action step
                First substep of second substep of action step
            Third substep of action step
                First substep of third substep of action step
                Second substep of third substep of action step
        Then An assert is ok
            First substep of assert step
                First substep of first substep of assert step
                Second substep of first substep of assert step
            Second substep of assert step
                First substep of second substep of assert step
            Third substep of assert step
