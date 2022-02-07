# language: en
Feature: Engine tasks exposition

    Scenario Outline: Retrieve task <task-id> from all tasks and by identifier
        When Request engine for all declared tasks
            Do http-get
                On CHUTNEY_LOCAL
                With uri /api/task/v1
                Take allTasks ${#body}
                Validate httpStatusCode_200 ${#status == 200}
        And Request engine for task <task-id>
            Do http-get
                On CHUTNEY_LOCAL
                With uri /api/task/v1/<task-id>
                Take task ${#body}
                Validate httpStatusCode_200 ${#status == 200}
        Then Its inputs are present in both responses
            Do json-compare Assert inputs from all tasks
                With document1 ${#allTasks}
                With document2 <inputs>
                With comparingPaths
                | $[*][?(@.identifier == '<task-id>')].inputs | $ |
            Do json-compare Assert inputs from task alone
                With document1 ${#task}
                With document2 <inputs>
                With comparingPaths
                | $.inputs | $[0] |

        Examples:
            | task-id | inputs                                                                                                                                        |
            | debug   | [[{"name": "filters","type": "java.util.List"}]]                                                                                              |
            | assert  | [[{"name": "asserts","type": "java.util.List"}]]                                                                                              |
            | compare | [[{"name": "actual","type": "java.lang.String"},{"name": "expected","type": "java.lang.String"},{"name": "mode","type": "java.lang.String"}]] |
