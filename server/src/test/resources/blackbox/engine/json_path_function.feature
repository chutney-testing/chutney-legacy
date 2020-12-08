# language: en
Feature: Execution with jsonPath function

    Scenario: Scenario execution with simple json value extraction
        Given this scenario is saved
            Do http-post Post scenario to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/scenario/v2
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "title":"JSON scenario",
                    "scenario":{
                        "when":{
                            "sentence":"Put JSON value in context",
                            "implementation":{
                                "task":"{\n type: context-put \n inputs: {\n entries: {\n content: {\n field1: value1 \n} \n} \n} \n}"
                            }
                        },
                        "thens":[
                            {
                                "sentence":"Put value in context with JSON extraction",
                                "implementation":{
                                    "task":"{\n type: context-put \n inputs: {\n entries: {\n extracted: \"\${#json(#content, '$.field1')}\" \n} \n} \n}"
                                }
                            }
                        ]
                    }
                }
                """
                Take scenarioId ${#body}
            Do compare Assert HTTP status is 200
                With actual ${T(Integer).toString(#status)}
                With expected 200
                With mode equals
        When last saved scenario is executed
            Do http-post Post scenario execution to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/ui/scenario/execution/v1/${#scenarioId}/ENV
                Take report ${#body}
            Do compare Assert HTTP status is 200
                With actual ${T(Integer).toString(#status)}
                With expected 200
                With mode equals
        Then the extracted value is 'value1'
            Do compare
                With actual ${#json(#report, "$.report.steps[1].stepOutputs.extracted")}
                With expected value1
                With mode equals

    Scenario: Scenario execution with multiple json value extraction
        Given this scenario is saved
            Do http-post Post scenario to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/scenario/v2
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "title":"JSON scenario",
                    "scenario":{
                        "when":{
                            "sentence":"Put JSON value in context",
                            "implementation":{
                                "task":"{\n type: context-put \n inputs: {\n entries: {\n content: {\n field1: value1 \n field2: {\n field1: value1 \n} \n} \n} \n} \n}"
                            }
                        },
                        "thens":[
                            {
                                "sentence":"Put value in context with JSON extraction",
                                "implementation":{
                                    "task":"{\n type: context-put \n inputs: {\n entries: {\n extracted: \"\${#json(#content, '$..field1')}\" \n} \n} \n}"
                                }
                            }
                        ]
                    }
                }
                """
                Take scenarioId ${#body}
            Do compare Assert HTTP status is 200
                With actual ${T(Integer).toString(#status)}
                With expected 200
                With mode equals
        When last saved scenario is executed
            Do http-post Post scenario execution to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/ui/scenario/execution/v1/${#scenarioId}/ENV
                Take report ${#body}
            Do compare Assert HTTP status is 200
                With actual ${T(Integer).toString(#status)}
                With expected 200
                With mode equals
        Then the extracted value is '["value1","value1"]'
            Do compare
                With actual ${#json(#report, "$.report.steps[1].stepOutputs.extracted")}
                With expected ["value1","value1"]
                With mode equals

    Scenario: Scenario execution with json object value extraction
        Given this scenario is saved
            Do http-post Post scenario to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/scenario/v2
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "title":"JSON scenario",
                    "scenario":{
                        "when":{
                            "sentence":"Put JSON value in context",
                            "implementation":{
                                "task":"{\n type: context-put \n inputs: {\n entries: {\n content: {\n field1: value1 \n} \n} \n} \n}"
                            }
                        },
                        "thens":[
                            {
                                "sentence":"Put value in context with JSON extraction",
                                "implementation":{
                                    "task":"{\n type: context-put \n inputs: {\n entries: {\n extracted: \"\${#json(#content, '$')}\" \n} \n} \n}"
                                }
                            }
                        ]
                    }
                }
                """
                Take scenarioId ${#body}
            Do compare Assert HTTP status is 200
                With actual ${T(Integer).toString(#status)}
                With expected 200
                With mode equals
        When last saved scenario is executed
            Do http-post Post scenario execution to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/ui/scenario/execution/v1/${#scenarioId}/ENV
                Take report ${#body}
            Do compare Assert HTTP status is 200
                With actual ${T(Integer).toString(#status)}
                With expected 200
                With mode equals
        Then the extracted value is '{field1=value1}'
            Do compare
                With actual ${#json(#report, "$.report.steps[1].stepOutputs.extracted")}
                With expected {field1=value1}
                With mode equals
