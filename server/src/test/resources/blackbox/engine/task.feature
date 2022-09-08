# language: en
Feature: Engine actions exposition

    Scenario Outline: Retrieve action <action-id> from all actions and by identifier
        When Request engine for all declared actions
            Do http-get
                On CHUTNEY_LOCAL
                With uri /api/action/v1
                Take allTasks ${#body}
                Validate httpStatusCode_200 ${#status == 200}
        And Request engine for action <action-id>
            Do http-get
                On CHUTNEY_LOCAL
                With uri /api/action/v1/<action-id>
                Take action ${#body}
                Validate httpStatusCode_200 ${#status == 200}
        Then Its inputs are present in both responses
            Do json-compare Assert inputs from all actions
                With document1 ${#allTasks}
                With document2 <inputs>
                With comparingPaths
                | $[*][?(@.identifier == '<action-id>')].inputs | $ |
            Do json-compare Assert inputs from action alone
                With document1 ${#action}
                With document2 <inputs>
                With comparingPaths
                | $.inputs | $[0] |

        Examples:
            | action-id | inputs                                                                                                                                        |
            | debug   | [[{"name": "filters","type": "java.util.List"}]]                                                                                              |
            | assert  | [[{"name": "asserts","type": "java.util.List"}]]                                                                                              |
            | compare | [[{"name": "actual","type": "java.lang.String"},{"name": "expected","type": "java.lang.String"},{"name": "mode","type": "java.lang.String"}]] |
