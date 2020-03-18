# language: en

Feature: Context-put feature
    Could execute context-put task as glacio feature

    Scenario: Map inputs with context-put task
        When Need to put some variables directly in context
            Do success
        Then Use this for not too long values
            Execute add to context var1 value1
            Execute store in context var1 value1 "var 2" "value2 splitted" "var3 " value3
        And Use datatable otherwise
            Do put in context
            | var1 | value1 splitted | var 2 | value2 |
            | var3 | value 3 |
        And Use direct substep
            Execute store variables
                var1 value1 var2 value2
                "var 3" value 3
                var4 value4 splitted
        And Use any to agregate
            Execute put var1 value1 "var 2" "value2 splitted"
            | var3 | value3 splitted | var 2 | value2 |
            | var3 | value 3 |
            Do store var1 value1 "var 2" "value2 splitted"
                var1 value1 var2 value2
                "var 3" value 3
