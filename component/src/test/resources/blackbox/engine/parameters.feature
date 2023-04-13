# language: en
@Parameters
Feature: Replace scenario parameters with data set or global var values

    Background:
        Given global variables defined in global_var
            Do http-post Post global variable to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/ui/globalvar/v1/global_var
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "message": "{ \"simple\": { \"word\": \"a_word\", \"line\": \"one line\", \"multiline\": \"My half empty glass,\\nI will fill your empty half.\\nNow you are half full.\" }, \"escape\": { \"quote\": \"line with quote \\\"\", \"backslash\": \"line with backslash \\\\\", \"slash\": \"line with slash as url http://host:port/path\", \"apostrophe\": \"line with apostrophe '\" } }"
                }
                """
                Validate httpStatusCode_200 ${#status == 200}

    Scenario: Execute composable testcase with global vars
        Given A context-put composable action component wrapper
            Do http-post Post component to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/steps/v1
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "name": "put entry [**key** -- **value**] in context",
                    "usage": null,
                    "task": "{\"identifier\":\"context-put\",\"target\":\"\",\"hasTarget\":false,\"mapInputs\":[{\"name\":\"entries\",\"values\":[{\"key\":\"**key**\",\"value\":\"**value**\"}]}],\"listInputs\":[],\"inputs\":[],\"outputs\":[]}",
                    "steps": [],
                    "parameters": [
                        {
                            "key": "key",
                            "value": ""
                        },
                        {
                            "key": "value",
                            "value": ""
                        }
                    ],
                    "strategy": {
                        "type": "Default",
                        "parameters": {
                        }
                    },
                    "computedParameters": [],
                    "tags": []
                }
                """
                Take contextPutWrapperComponentId ${#body}
                Validate httpStatusCode_200 ${#status == 200}
        And A assert-equals composable action component wrapper
            Do http-post Post component to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/steps/v1
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "name": "Asssert [**actual**] equals [**expected**]",
                    "usage": null,
                    "task": "{\"identifier\":\"compare\",\"target\":\"\",\"hasTarget\":false,\"mapInputs\":[],\"listInputs\":[],\"inputs\":[{\"name\":\"actual\",\"value\":\"**actual**\"},{\"name\":\"expected\",\"value\":\"**expected**\"},{\"name\":\"mode\",\"value\":\"equals\"}],\"outputs\":[]}",
                    "steps": [],
                    "parameters": [
                        {
                            "key": "actual",
                            "value": ""
                        },
                        {
                            "key": "expected",
                            "value": ""
                        }
                    ],
                    "strategy": {
                        "type": "Default",
                        "parameters": {
                        }
                    },
                    "computedParameters": [],
                    "tags": []
                }
                """
                Take assertEqualsWrapperComponentId ${#body}
                Validate httpStatusCode_200 ${#status == 200}
        And a composable testcase
            Do http-post Post scenario to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/scenario/component-edition
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "title": "TestCase composable with parameters for global vars",
                    "description": "",
                    "scenario": {
                        "parameters": [
                            {
                                "key": "testcase parameter quote",
                                "value": "**escape.quote**"
                            },
                            {
                                "key": "testcase parameter apostrophe",
                                "value": "**escape.apostrophe**"
                            }
                        ],
                        "componentSteps": [
                            {
                                "id": "${#contextPutWrapperComponentId}",
                                "name": "put entry [**key** -- **value**] in context",
                                "usage": null,
                                "task": null,
                                "steps": [],
                                "parameters": [
                                    {
                                        "key": "key",
                                        "value": ""
                                    },
                                    {
                                        "key": "value",
                                        "value": ""
                                    }
                                ],
                                "strategy": {
                                    "type": "Default",
                                    "parameters": {
                                    }
                                },
                                "computedParameters": [
                                    {
                                        "key": "key",
                                        "value": "slash"
                                    },
                                    {
                                        "key": "value",
                                        "value": "**escape.slash**"
                                    }
                                ],
                                "tags": []
                            },
                            {
                                "id": "${#assertEqualsWrapperComponentId}",
                                "name": "Asssert [**actual**] equals [**expected**]",
                                "usage": null,
                                "task": null,
                                "steps": [],
                                "parameters": [
                                    {
                                        "key": "actual",
                                        "value": ""
                                    },
                                    {
                                        "key": "expected",
                                        "value": ""
                                    }
                                ],
                                "strategy": {
                                    "type": "Default",
                                    "parameters": {
                                    }
                                },
                                "computedParameters": [
                                    {
                                        "key": "actual",
                                        "value": "\${#slash}"
                                    },
                                    {
                                        "key": "expected",
                                        "value": "line with slash as url http:\/\/host:port\/path"
                                    }
                                ],
                                "tags": []
                            },
                            {
                                "id": "${#contextPutWrapperComponentId}",
                                "name": "put entry [**key** -- **value**] in context",
                                "usage": null,
                                "task": null,
                                "steps": [],
                                "parameters": [
                                    {
                                        "key": "key",
                                        "value": ""
                                    },
                                    {
                                        "key": "value",
                                        "value": ""
                                    }
                                ],
                                "strategy": {
                                    "type": "Default",
                                    "parameters": {
                                    }
                                },
                                "computedParameters": [
                                    {
                                        "key": "key",
                                        "value": "apostrophe"
                                    },
                                    {
                                        "key": "value",
                                        "value": "**testcase parameter apostrophe**"
                                    }
                                ],
                                "tags": []
                            },
                            {
                                "id": "${#assertEqualsWrapperComponentId}",
                                "name": "Asssert [**actual**] equals [**expected**]",
                                "usage": null,
                                "task": null,
                                "steps": [],
                                "parameters": [
                                    {
                                        "key": "actual",
                                        "value": ""
                                    },
                                    {
                                        "key": "expected",
                                        "value": ""
                                    }
                                ],
                                "strategy": {
                                    "type": "Default",
                                    "parameters": {
                                    }
                                },
                                "computedParameters": [
                                    {
                                        "key": "actual",
                                        "value": "\${#apostrophe}"
                                    },
                                    {
                                        "key": "expected",
                                        "value": "line with apostrophe '"
                                    }
                                ],
                                "tags": []
                            },
                            {
                                "id": "${#contextPutWrapperComponentId}",
                                "name": "put entry [**key** -- **value**] in context",
                                "usage": null,
                                "task": null,
                                "steps": [],
                                "parameters": [
                                    {
                                        "key": "key",
                                        "value": ""
                                    },
                                    {
                                        "key": "value",
                                        "value": ""
                                    }
                                ],
                                "strategy": {
                                    "type": "Default",
                                    "parameters": {
                                    }
                                },
                                "computedParameters": [
                                    {
                                        "key": "key",
                                        "value": "quote"
                                    },
                                    {
                                        "key": "value",
                                        "value": "**testcase parameter quote**"
                                    }
                                ],
                                "tags": []
                            },
                            {
                                "id": "${#assertEqualsWrapperComponentId}",
                                "name": "Asssert [**actual**] equals [**expected**]",
                                "usage": null,
                                "task": null,
                                "steps": [],
                                "parameters": [
                                    {
                                        "key": "actual",
                                        "value": ""
                                    },
                                    {
                                        "key": "expected",
                                        "value": ""
                                    }
                                ],
                                "strategy": {
                                    "type": "Default",
                                    "parameters": {
                                    }
                                },
                                "computedParameters": [
                                    {
                                        "key": "actual",
                                        "value": "\${#quote}"
                                    },
                                    {
                                        "key": "expected",
                                        "value": "line with quote \""
                                    }
                                ],
                                "tags": []
                            },
                            {
                                "id": "${#contextPutWrapperComponentId}",
                                "name": "put entry [**key** -- **value**] in context",
                                "usage": null,
                                "task": null,
                                "steps": [],
                                "parameters": [
                                    {
                                        "key": "key",
                                        "value": ""
                                    },
                                    {
                                        "key": "value",
                                        "value": ""
                                    }
                                ],
                                "strategy": {
                                    "type": "Default",
                                    "parameters": {
                                    }
                                },
                                "computedParameters": [
                                    {
                                        "key": "key",
                                        "value": "backslash"
                                    },
                                    {
                                        "key": "value",
                                        "value": "**escape.backslash**"
                                    }
                                ],
                                "tags": []
                            },
                            {
                                "id": "${#assertEqualsWrapperComponentId}",
                                "name": "Asssert [**actual**] equals [**expected**]",
                                "usage": null,
                                "task": null,
                                "steps": [],
                                "parameters": [
                                    {
                                        "key": "actual",
                                        "value": ""
                                    },
                                    {
                                        "key": "expected",
                                        "value": ""
                                    }
                                ],
                                "strategy": {
                                    "type": "Default",
                                    "parameters": {
                                    }
                                },
                                "computedParameters": [
                                    {
                                        "key": "actual",
                                        "value": "\${#backslash}"
                                    },
                                    {
                                        "key": "expected",
                                        "value": "line with backslash \\"
                                    }
                                ],
                                "tags": []
                            }
                        ]
                    },
                    "computedParameters": [],
                    "tags": []
                }
                """
                Take scenarioId ${#body}
                Validate httpStatusCode_200 ${#status == 200}
        When last saved scenario is executed
            Do http-post Post scenario execution to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/ui/component/execution/v1/${#scenarioId}/ENV
                With timeout 5 s
                Take report ${#body}
                Validate httpStatusCode_200 ${#status == 200}
        Then the report status is SUCCESS
            Do compare
                With actual ${#json(#report, "$.report.status")}
                With expected SUCCESS
                With mode equals
