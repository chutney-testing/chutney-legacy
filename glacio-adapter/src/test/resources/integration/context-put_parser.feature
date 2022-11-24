# language: en

Feature: Context-put feature
    Could execute context-put action as glacio feature

    Scenario: Define context-put action with specific parser
        When Need to put some variables directly in context
            Do success
        Then Use datatable
            Do put in context
            | var1 | value1 split |
            | var 2 | value2 |
        And Use substeps
            Execute Store variables
                var1 value1 split
                "var 2" value2

    Scenario: Define context-put action with default parser
        When Need to put some variables directly in context
            Do success
        Then Use default parsing syntax
            Run context-put my first put
                With entries
                | var1 | value1 split |
                | var 2 | value2 |
                Validate assertion ${'value1 split'.equals(#var1)}
