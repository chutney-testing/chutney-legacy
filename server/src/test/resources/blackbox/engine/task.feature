# language: en
Feature: Engine tasks exposition

    Scenario Outline: Retrieve task <task-id> from all tasks and by identifier
        When Request engine for all declared tasks
            Do http-get
                On CHUTNEY_LOCAL
                With uri /api/task/v1
                Take allTasks ${#body}
            Do compare Assert HTTP status is 200
                With actual ${#status}
                With expected 200
                With mode equals
        And Request engine for task <task-id>
            Do http-get
                On CHUTNEY_LOCAL
                With uri /api/task/v1/<task-id>
                Take task ${#body}
            Do compare Assert HTTP status is 200
                With actual ${#status}
                With expected 200
                With mode equals
        Then Its inputs are present in both responses
            Do json-compare Assert inputs from all tasks
                With document1 ${#json(#allTasks, "$[*][?(@.identifier == '<task-id>')].inputs").toString()}
                With document2 ${#json('<inputs>', "$").toString()}
                With comparingPaths
                | $[0] | $ |
            Do json-compare Assert inputs from all tasks
                With document1 ${#json(#task, "$.inputs").toString()}
                With document2 ${#json('<inputs>', "$").toString()}
                With comparingPaths
                | $ | $ |

        Examples:
            | task-id | inputs                                                                                                                                      |
            | debug   | [{"name": "filters","type": "java.util.List"}]                                                                                              |
            | assert  | [{"name": "asserts","type": "java.util.List"}]                                                                                              |
            | compare | [{"name": "actual","type": "java.lang.String"},{"name": "expected","type": "java.lang.String"},{"name": "mode","type": "java.lang.String"}] |
