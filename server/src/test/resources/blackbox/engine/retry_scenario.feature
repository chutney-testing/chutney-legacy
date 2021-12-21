# language: en
Feature: Execution retry strategy

Scenario: Retry should stop after success assertion
    Given this scenario is saved
        Do http-post Post scenario to Chutney instance
            On CHUTNEY_LOCAL
            With uri /api/scenario/v2
            With headers
            | Content-Type | application/json;charset=UTF-8 |
            With body
            """
            {
                "title":"Test scenario",
                "scenario":{
                    "when":{
                        "sentence":"Set stop date",
                        "implementation":{
                            "task":"{\n type: context-put \n inputs: {\n entries: {\n dateTimeFormat: ss \n secondsPlus5: \${#localDateFormatter(#dateTimeFormat).format(#now().plusSeconds(5))} \n} \n} \n}"
                        }
                    },
                    "thens":[
                        {
                            "sentence":"Assertion",
                            "strategy": {
                                "type": "retry-with-timeout",
                                "parameters": {
                                    "timeOut": "15 sec",
                                    "retryDelay": "1 sec"
                                }
                            },
                            "subSteps":[
                                {
                                    "sentence":"Set current date",
                                    "implementation":{
                                        "task":"{\n type: context-put \n inputs: {\n entries: {\n currentSeconds: \${#localDateFormatter(#dateTimeFormat).format(#now())} \n} \n} \n}"
                                    }
                                },
                                {
                                    "sentence":"Check current date get to stop date",
                                    "implementation":{
                                        "task":"{\n type: string-assert \n inputs: {\n document: \${#secondsPlus5} \n expected: \${T(java.lang.String).format('%02d', new Integer(#currentSeconds) + 1)} \n} \n}"
                                    }
                                }
                            ]
                        }
                    ]
                }
            }
            """
            Take scenarioId ${#body}
        Do compare Assert HTTP status is 200
            With actual ${#status}
            With expected 200
            With mode equals
    When last saved scenario is executed
        Do http-post Post scenario execution to Chutney instance
            On CHUTNEY_LOCAL
            With uri /api/ui/scenario/execution/v1/${#scenarioId}/ENV
            With timeout 10 s
            Take report ${#body}
        Do compare Assert HTTP status is 200
            With actual ${#status}
            With expected 200
            With mode equals
    Then the report status is SUCCESS
        Do compare
            With actual ${#json(#report, "$.report.status")}
            With expected SUCCESS
            With mode equals
