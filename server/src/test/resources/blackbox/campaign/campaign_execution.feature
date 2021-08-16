# language: en
@Campaign
Feature:  Campaign execution

    Background:
        Given a scenario with name "scenario1" is stored
            Do http-post Post scenario to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/scenario/v2
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "title":"scenario1",
                    "scenario":{
                        "when":{
                            "sentence":"Just a success step",
                            "implementation":{
                                "task":"{\n type: success \n }"
                            }
                        },
                        "thens":[]
                    }
                }
                """
                Take scenario1Id ${#body}
            Do compare Assert HTTP status is 200
                With actual ${T(Integer).toString(#status)}
                With expected 200
                With mode equals
        And a scenario with name "scenario2" is stored
            Do http-post Post scenario to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/scenario/v2
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "title":"scenario2",
                    "scenario":{
                        "when":{
                            "sentence":"Just a success step",
                            "implementation":{
                                "task":"{\n type: success \n }"
                            }
                        },
                        "thens":[]
                    }
                }
                """
                Take scenario2Id ${#body}
            Do compare Assert HTTP status is 200
                With actual ${T(Integer).toString(#status)}
                With expected 200
                With mode equals

    Scenario: Execution by campaign id with 2 scenarios
        Given a campaign is stored
            Do http-post Post campaign to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/ui/campaign/v1
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "title":"campaign",
                    "description":"",
                    "scenarioIds":[ "${#scenario1Id}", "${#scenario2Id}" ],
                    "computedParameters":{},
                    "environment":"ENV",
                    "parallelRun": false,
                    "retryAuto": false,
                    "tags":[]
                }
                """
                Take campaignId ${#jsonPath(#body, "$.id")}
            Do compare Assert HTTP status is 200
                With actual ${T(Integer).toString(#status)}
                With expected 200
                With mode equals
        When this campaign is executed by id
            Do http-get
                On CHUTNEY_LOCAL
                With uri /api/ui/campaign/execution/v1/byID/${#campaignId}
                Take report ${#body}
            Do compare Assert HTTP status is 200
                With actual ${T(Integer).toString(#status)}
                With expected 200
                With mode equals
        Then the execution report is returned
            Do compare Check execution id not empty
                With actual ${#json(#report, "$.executionId").toString()}
                With expected 0
                With mode greater than
            Do compare Check status is SUCCESS
                With actual ${#json(#report, "$.status")}
                With expected SUCCESS
                With mode equals
            Do compare Check scenario ids
                With actual ${#json(#report, "$.scenarioExecutionReports[*].scenarioId").toString()}
                With expected ["${#scenario1Id}","${#scenario2Id}"]
                With mode equals
        And this execution report is stored in the campaign execution history
            Do http-get Request campaign from Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/ui/campaign/v1/${#campaignId}
                Take campaign ${#body}
            Do compare Assert execution is present
                With actual ${#json(#campaign, "$.campaignExecutionReports[0].executionId").toString()}
                With expected ${#json(#report, "$.executionId").toString()}
                With mode equals

    Scenario: Execution by campaign name with 1 scenario
        Given a campaign with name "campaignName" is stored
            Do http-post Post campaign to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/ui/campaign/v1
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "title":"campaignName",
                    "description":"",
                    "scenarioIds":[ "${#scenario1Id}" ],
                    "computedParameters":{},
                    "environment":"ENV",
                    "parallelRun": false,
                    "retryAuto": false,
                    "tags":[]
                }
                """
                Take campaignId ${#jsonPath(#body, "$.id")}
            Do compare Assert HTTP status is 200
                With actual ${T(Integer).toString(#status)}
                With expected 200
                With mode equals
        When this campaign is executed by name
            Do http-get
                On CHUTNEY_LOCAL
                With uri /api/ui/campaign/execution/v1/campaignName/ENV
                Take report ${#json(#body, "$[0]")}
            Do compare Assert HTTP status is 200
                With actual ${T(Integer).toString(#status)}
                With expected 200
                With mode equals
        Then the execution reports are returned
            Do compare Check execution id not empty
                With actual ${#json(#report, "$.executionId").toString()}
                With expected 0
                With mode greater than
            Do compare Check status is SUCCESS
                With actual ${#json(#report, "$.status")}
                With expected SUCCESS
                With mode equals
            Do compare Check scenario ids
                With actual ${#json(#report, "$.scenarioExecutionReports[*].scenarioId").toString()}
                With expected ["${#scenario1Id}"]
                With mode equals
        And this execution report is stored in the campaign execution history
            Do http-get Request campaign from Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/ui/campaign/v1/${#campaignId}
                Take campaign ${#body}
            Do compare Assert execution is present
                With actual ${#json(#campaign, "$.campaignExecutionReports[0].executionId").toString()}
                With expected ${#json(#report, "$.executionId").toString()}
                With mode equals

    Scenario: Execution for surefire of a campaign with 1 scenario
        Given a campaign with name "campaignSurefire" is stored
            On CHUTNEY_LOCAL
                With uri /api/ui/campaign/v1
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "title":"campaignSurefire",
                    "description":"",
                    "scenarioIds":[ "${#scenario1Id}" ],
                    "dataSet":{},
                    "environment":"ENV",
                    "parallelRun": false,
                    "retryAuto": false,
                    "tags":[]
                }
                """
                Take campaignId ${#jsonPath(#body, "$.id")}
            Do compare Assert HTTP status is 200
                With actual ${T(Integer).toString(#status)}
                With expected 200
                With mode equals
        When this campaign is executed for surefire
            Do http-get
                On CHUTNEY_LOCAL
                With uri /api/ui/campaign/execution/v1/campaignSurefire/surefire
                Take responseHeaders ${#headers}
            Do compare Assert HTTP status is 200
                With actual ${T(Integer).toString(#status)}
                With expected 200
                With mode equals
        Then the response is a non empty zip file
            Do compare
                With actual ${#responseHeaders.getContentType().toString()}
                With expected application/zip
                With mode equals
            Do compare
                With actual ${#responseHeaders.getContentDisposition().toString()}
                With expected attachment; filename="surefire-report.zip"
                With mode contains
            Do compare
                With actual ${T(Long).toString(#responseHeaders.getContentLength())}
                With expected 10
                With mode greater than

    Scenario: Execution by id of an unknown campaign
        When an unknown campaign is executed by id
            Do http-get
                On CHUTNEY_LOCAL
                With uri /api/ui/campaign/execution/v1/byID/666/ENV
        Then the campaign is not found
            Do compare Assert HTTP status is 404
                With actual ${T(Integer).toString(#status)}
                With expected 404
                With mode equals

    Scenario: Execution by name of an unknown campaign
        When an unknown campaign is executed by name
            Do http-get
                On CHUTNEY_LOCAL
                With uri /api/ui/campaign/execution/v1/unknownName
                Take report ${#body}
            Do compare Assert HTTP status is 200
                With actual ${T(Integer).toString(#status)}
                With expected 200
                With mode equals
        Then the campaign report is empty
            Do compare Assert HTTP status is 404
                With actual ${#report}
                With expected []
                With mode equals
