# language: en

Feature: Final action for registering final actions for a testcase

    Scenario: Register simple success action
        Given A simple scenario is saved
            Do http-post Post scenario to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/scenario/v2
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "title":"Final action success",
                    "scenario":{
                        "when":{
                            "sentence":"Final action is registered",
                            "implementation":{
                                "task":"{\n type: final \n inputs: {\n type: success \n name: Testing final action... \n} \n}"
                            }
                        },
                        "thens": []
                    }
                }
                """
                Take scenarioId ${#body}
                Validate httpStatusCode_200 ${#status == 200}
        When Last saved scenario is executed
            Do http-post Post scenario execution to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/ui/scenario/execution/v1/${#scenarioId}/ENV
                With timeout 5 s
                Take report ${#body}
                Validate httpStatusCode_200 ${#status == 200}
        Then The report status is SUCCESS
            Do compare
                With actual ${#json(#report, "$.report.status")}
                With expected SUCCESS
                With mode equals
        And The report contains a single node for final actions
            Do json-assert
                With document ${#report}
                With expected
                | $.report.steps[1].name | TearDown |
                | $.report.steps[2].name | $isNull |
        And The report contains a single final action execution
            Do json-assert
                With document ${#report}
                With expected
                | $.report.steps[1].steps[0].name | Testing final action... |
                | $.report.steps[1].steps[0].type | success |
                | $.report.steps[1].steps[1] | $isNull |

    Scenario: Register multiple actions with one complex, i.e. with inputs and strategy
        Given A complex scenario is saved
            Do http-post Post scenario to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/scenario/v2
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "title":"Final action success",
                    "scenario":{
                        "when":{
                            "sentence":"Final actions are registered",
                            "subSteps":[
                                {
                                    "sentence":"Register an assertion",
                                    "implementation":{
                                        "task":"{\n type: final \n inputs: {\n type: compare \n name: An assertion \n inputs: {\n actual: aValue \n expected: aValue \n mode: equals \n} \n} \n}"
                                    }
                                },
                                {
                                    "sentence":"Register a fail with retry",
                                    "implementation":{
                                        "task":"{\n type: final \n inputs: {\n type: fail \n name: I'm no good \n strategy-type: retry-with-timeout \n strategy-properties: {\n timeOut: 1500 ms \n retryDelay: 1 s \n} \n} \n}"
                                    }
                                },
                                {
                                    "sentence":"Register variable in context",
                                    "implementation":{
                                        "target": "CHUTNEY_LOCAL",
                                        "task":"{\n type: final \n inputs: {\n type: context-put \n name: Put myKey \n inputs: {\n entries: {\n myKey: myValue \n} \n} validations: {\n putOk: \${#myKey == 'myValue'} \n} \n} \n}"
                                    }
                                }
                            ]
                        },
                        "thens": []
                    }
                }
                """
                Take scenarioId ${#body}
                Validate httpStatusCode_200 ${#status == 200}
        When Last saved scenario is executed
            Do http-post Post scenario execution to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/ui/scenario/execution/v1/${#scenarioId}/ENV
                With timeout 5 s
                Take report ${#body}
                Validate httpStatusCode_200 ${#status == 200}
        Then The report status is FAILURE
            Do compare
                With actual ${#json(#report, "$.report.status")}
                With expected FAILURE
                With mode equals
        And The report contains the executions (in declaration reverse order) of all the final actions action
            Do json-assert
                With document ${#report}
                With expected
                | $.report.steps[1].steps[0].type | compare |
                | $.report.steps[1].steps[0].status | SUCCESS |
                | $.report.steps[1].steps[1].type | fail |
                | $.report.steps[1].steps[1].status | FAILURE |
                | $.report.steps[1].steps[1].strategy | retry-with-timeout |
                | $.report.steps[1].steps[2].type | context-put |
                | $.report.steps[1].steps[2].status | SUCCESS |

    Scenario: Register final action with validations on outputs
        Given a target
            Do http-post Create environment and target
                On CHUTNEY_LOCAL
                With uri /api/v2/environment
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "name": "ENV_FINAL",
                    "description": "",
                    "targets": [
                        {
                            "name": "test_http",
                            "url": "${#target.rawUri()}",
                            "properties": [
                                { "key" : "keyStore", "value": "${#escapeJson(#resourcePath("blackbox/keystores/client.jks"))}" },
                                { "key" : "keyStorePassword", "value": "client" },
                                { "key" : "keyPassword", "value": "client" },
                                { "key" : "username", "value": "admin" },
                                { "key" : "password", "value": "admin" }
                            ]
                        }
                    ]
                }
                """
                Validate httpStatusCode_200 ${#status == 200}
        And A scenario is saved
            Do http-post Post scenario to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/scenario/v2
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "title":"Final action success",
                    "scenario":{
                        "when":{
                            "sentence":"Final action are registered",
                            "subSteps":[
                                {
                                    "sentence":"Register action providing outputs",
                                    "implementation":{
                                        "target": "CHUTNEY_LOCAL",
                                        "task":"{\n type: final \n target: test_http \n inputs: {\n type: http-get \n name: Get user \n inputs: {\n uri: /api/v1/user \n timeout: 5 sec \n} \n validations: { \n http_OK: \${ #status == 200 } \n} \n} \n}"
                                    }
                                }
                            ]
                        },
                        "thens": []
                    }
                }
                """
                Take scenarioId ${#body}
                Validate httpStatusCode_200 ${#status == 200}
        When Last saved scenario is executed
            Do http-post Post scenario execution to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/ui/scenario/execution/v1/${#scenarioId}/ENV_FINAL
                With timeout 5 s
                Take report ${#body}
                Validate httpStatusCode_200 ${#status == 200}
        Then The report contains the executions (in declaration reverse order) of all the final actions action
            Do json-assert
                With document ${#report}
                With expected
                | $.report.steps[0].steps[0].evaluatedInputs.validations.http_OK | \${ #status == 200 } |
                | $.report.steps[1].steps[0].type | http-get |
                | $.report.steps[1].steps[0].status | SUCCESS |
