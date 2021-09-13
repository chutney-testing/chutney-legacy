# language: en

Feature: Final task for registering final actions for a testcase

    Scenario: Register simple success task
        Given A simple scenario is saved
            Do http-post Post scenario to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/scenario/v2
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "title":"Final task success",
                    "scenario":{
                        "when":{
                            "sentence":"Final task is registered",
                            "implementation":{
                                "task":"{\n type: final \n inputs: {\n identifier: success \n what-for: Testing final task... \n} \n}"
                            }
                        },
                        "thens": []
                    }
                }
                """
                Take scenarioId ${#body}
            Do compare Assert HTTP status is 200
                With actual ${T(Integer).toString(#status)}
                With expected 200
                With mode equals
        When Last saved scenario is executed
            Do http-post Post scenario execution to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/ui/scenario/execution/v1/${#scenarioId}/ENV
                With timeout 5 s
                Take report ${#body}
            Do compare Assert HTTP status is 200
                With actual ${T(Integer).toString(#status)}
                With expected 200
                With mode equals
        Then The report status is SUCCESS
            Do compare
                With actual ${#json(#report, "$.report.status")}
                With expected SUCCESS
                With mode equals
        And The report contains a single node for final actions
            Do json-assert
                With document ${#report}
                With expected
                | $.report.steps[1].name | ... |
                | $.report.steps[2].name | $isNull |
        And The report contains a single final action execution
            Do json-assert
                With document ${#report}
                With expected
                | $.report.steps[1].steps[0].name | Finally action generated for Testing final task... |
                | $.report.steps[1].steps[0].type | success |
                | $.report.steps[1].steps[1] | $isNull |

    Scenario: Register multiple tasks with one complex, i.e. with inputs and strategy
        Given A complex scenario is saved
            Do http-post Post scenario to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/scenario/v2
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "title":"Final task success",
                    "scenario":{
                        "when":{
                            "sentence":"Final tasks are registered",
                            "subSteps":[
                                {
                                    "sentence":"Register an assertion",
                                    "implementation":{
                                        "task":"{\n type: final \n inputs: {\n identifier: compare \n inputs: {\n actual: aValue \n expected: aValue \n mode: equals \n} \n} \n}"
                                    }
                                },
                                {
                                    "sentence":"Register a fail with retry",
                                    "implementation":{
                                        "task":"{\n type: final \n inputs: {\n identifier: fail \n strategy-type: retry-with-timeout \n strategy-properties: {\n timeOut: 1500 ms \n retryDelay: 1 s \n} \n} \n}"
                                    }
                                },
                                {
                                    "sentence":"Register variable in context",
                                    "implementation":{
                                        "target": "CHUTNEY_LOCAL",
                                        "task":"{\n type: final \n inputs: {\n identifier: context-put \n inputs: {\n entries: {\n myKey: myValue \n} \n} \n} \n}"
                                    }
                                }
                            ]
                        },
                        "thens": []
                    }
                }
                """
                Take scenarioId ${#body}
            Do compare Assert HTTP status is 200
                With actual ${T(Integer).toString(#status)}
                With expected 200
                With mode equals
        When Last saved scenario is executed
            Do http-post Post scenario execution to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/ui/scenario/execution/v1/${#scenarioId}/ENV
                With timeout 5 s
                Take report ${#body}
            Do compare Assert HTTP status is 200
                With actual ${T(Integer).toString(#status)}
                With expected 200
                With mode equals
        Then The report status is FAILURE
            Do compare
                With actual ${#json(#report, "$.report.status")}
                With expected FAILURE
                With mode equals
        And The report contains the executions (in declaration reverse order) of all the final tasks action
            Do json-assert
                With document ${#report}
                With expected
                | $.report.steps[1].steps[0].type | context-put |
                | $.report.steps[1].steps[0].status | SUCCESS |
                | $.report.steps[1].steps[1].type | fail |
                | $.report.steps[1].steps[1].status | FAILURE |
                | $.report.steps[1].steps[1].strategy | retry-with-timeout |
                | $.report.steps[1].steps[2].type | compare |
                | $.report.steps[1].steps[2].status | SUCCESS |
