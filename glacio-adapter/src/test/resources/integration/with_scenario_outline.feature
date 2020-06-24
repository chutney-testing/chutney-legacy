# language: en
Feature: Feature with scenario outline

    Scenario Outline: A scenario with steps
        Given A step outline <name>
        When Parse this step <name> with substeps
            First substep with param <param1>
            Second substep with param <param2>
        Then Multiple scenarios <number> should be parsed

        Examples:
            | name   | param1  | param2  | number |
            | first  | value11 | value12 | 1      |
            | second | value21 | value22 | 2      |
