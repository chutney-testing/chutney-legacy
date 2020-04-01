# language: en

Feature: Context-put feature
    Could execute context-put task as glacio feature

    Scenario: Store all sorts of variables
        When Need to put some variables directly in context
            Do success
        Then Use default parsing syntax
            Run (context-put) my first put
                With entries
                | key1 | value 1 |
                | key2 | value 2 |
            Run (sleep) my sleep
                With duration 1 s
            Run (context-put) my first put
                With entries
                | key3 | value 3 |
                | key4 | ${1+1+1+1} |

