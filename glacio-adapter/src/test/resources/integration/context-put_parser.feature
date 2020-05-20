# language: en

Feature: Context-put feature
    Could execute context-put task as glacio feature

    Scenario: Define context-put tasks with specific parser
        When Need to put some variables directly in context
            Do success
        Then Use datatable
            Do put in context
            | var1 | value1 splitted |
            | var 2 | value2 |
        And Use substeps
            Execute Store variables
                var1 value1 splitted
                "var 2" value2

    Scenario: Define context-put tasks with default parser
        When Need to put some variables directly in context
            Do success
        Then Use default parsing syntax
            Run context-put my first put
                With entries
                | var1 | value1 splitted |
                | var 2 | value2 |
