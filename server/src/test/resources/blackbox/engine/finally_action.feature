# language: en

Feature: Finally actions

Scenario: Step of a type self registering as Finally Action does not create an infinite loop
    Given this scenario is saved
        Do http-post Post scenario to Chutney instance
            On CHUTNEY_LOCAL
            With uri /api/scenario/v2
            With headers
            | Content-Type | application/json;charset=UTF-8 |
            With body
            """
            {
                "title":"Success scenario",
                "scenario":{
                    "when":{
                        "sentence":"Do something to register finally action",
                        "implementation":{
                            "action":"{\n type: self-registering-finally \n }"
                        }
                    },
                    "thens":[]
                }
            }
            """
            Take scenarioId ${#body}
            Validate httpStatusCode_200 ${#status == 200}
    When last saved scenario is executed
        Do http-post Post scenario execution to Chutney instance
            On CHUTNEY_LOCAL
            With uri /api/ui/scenario/execution/v1/${#scenarioId}/ENV
            Take report ${#body}
            Validate httpStatusCode_200 ${#status == 200}
    Then the report status is SUCCESS
        Do compare
            With actual ${#json(#report, "$.report.status")}
            With expected SUCCESS
            With mode equals
