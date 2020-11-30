# language: en
@Parameters
Feature: Replace scenario parameters with data set or global var values

    Background:
        Given global variables defined in global_var
            Do http-post Post scenario to Chutney instance
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
            Do compare Assert HTTP status is 200
                With actual ${T(Integer).toString(#status)}
                With expected 200
                With mode equals

    Scenario: Execute raw scenario with global vars
        Given an existing testcase written in old format
            Do sql
                On CHUTNEY_DB
                With statements
                | SELECT nextval('SCENARIO_SEQ') |
                Take scenarioId ${#recordResult.get(0).rows.get(0).get(0)}
            Do sql
                On CHUTNEY_DB
                With statements
                | INSERT INTO SCENARIO (CONTENT_VERSION, ID, TITLE, DESCRIPTION, CONTENT, TAGS, CREATION_DATE, DATASET, ACTIVATED, UPDATE_DATE, VERSION) VALUES ('v1.0', ${#scenarioId}, 'Raw testcase with parameters for global vars', null, '{ "scenario" : { "name": "Raw scenario with parameters for global vars", "gwtType": "ROOT_STEP", "steps": [{ "name": "Putting variables with sensitive characters in context", "gwtType": "WHEN", "steps": [{ "type": "context-put", "inputs": { "entries": { "slash": "**escape.slash**" }}}, { "type": "context-put", "inputs": { "entries": { "apostrophe": "**testcase parameter apostrophe**" }}}, { "type": "context-put", "inputs": { "entries": { "quote": "**testcase parameter quote**" }}}, { "type": "context-put", "inputs": { "entries": { "backslash": "**escape.backslash**" }}}]}, { "name": "Context contains correct value of those variables", "gwtType": "THEN", "steps": [{ "type": "compare", "inputs": { "mode": "equals", "actual": "\${#slash}", "expected": "line with slash as url http:\/\/host:port\/path" }}, { "type": "compare", "inputs": { "mode": "equals", "actual": "\${#apostrophe}", "expected": "line with apostrophe ''" }}, { "type": "compare", "inputs": { "mode": "equals", "actual": "\${#quote}", "expected": "line with quote \"" }}, { "type": "compare", "inputs": { "mode": "equals", "actual": "\${#backslash}", "expected": "line with backslash \\" }}]}]}}', null, CURRENT_TIMESTAMP(6), '{ "testcase parameter quote": "**escape.quote**", "testcase parameter apostrophe": "**escape.apostrophe**" }', TRUE, CURRENT_TIMESTAMP(6), 1)  |
            Do compare Assert sql insert success
                With actual ${T(Long).toString(#recordResult.get(0).affectedRows)}
                With expected 1
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
        Then the report status is SUCCESS
            Do compare
                With actual ${#json(#report, "$.report.status")}
                With expected SUCCESS
                With mode equals

    Scenario: Execute gwt scenario with global vars
        Given a testcase written with GWT form
            Do http-post Post scenario to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/scenario/v2
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "title": "GWT testcase with parameters for global vars",
                    "tags": [],
                    "computedParameters": {
                        "testcase parameter quote": "**escape.quote**",
                        "testcase parameter apostrophe": "**escape.apostrophe**"
                    },
                    "scenario": {
                        "when": {
                            "sentence": "Putting variables with sensitive characters in context",
                            "subSteps": [
                                {
                                    "implementation": {
                                        "type": "context-put",
                                        "inputs": {
                                            "entries": {
                                                "slash": "**escape.slash**"
                                            }
                                        }
                                    }
                                },
                                {
                                    "implementation": {
                                        "type": "context-put",
                                        "inputs": {
                                            "entries": {
                                                "apostrophe": "**testcase parameter apostrophe**"
                                            }
                                        }
                                    }
                                },
                                {
                                    "implementation": {
                                        "type": "context-put",
                                        "inputs": {
                                            "entries": {
                                                "quote": "**testcase parameter quote**"
                                            }
                                        }
                                    }
                                },
                                {
                                    "implementation": {
                                        "type": "context-put",
                                        "inputs": {
                                            "entries": {
                                                "backslash": "**escape.backslash**"
                                            }
                                        }
                                    }
                                }
                            ]
                        },
                        "thens": [
                            {
                                "sentence": "Context contains correct value of those variables",
                                "subSteps": [
                                    {
                                        "implementation": {
                                            "type": "compare",
                                            "inputs": {
                                                "mode": "equals",
                                                "actual": "\${#slash}",
                                                "expected": "line with slash as url http:\/\/host:port\/path"
                                            }
                                        }
                                    },
                                    {
                                        "implementation": {
                                            "type": "compare",
                                            "inputs": {
                                                "mode": "equals",
                                                "actual": "\${#apostrophe}",
                                                "expected": "line with apostrophe '"
                                            }
                                        }
                                    },
                                    {
                                        "implementation": {
                                            "type": "compare",
                                            "inputs": {
                                                "mode": "equals",
                                                "actual": "\${#quote}",
                                                "expected": "line with quote \""
                                            }
                                        }
                                    },
                                    {
                                        "implementation": {
                                            "type": "compare",
                                            "inputs": {
                                                "mode": "equals",
                                                "actual": "\${#backslash}",
                                                "expected": "line with backslash \\"
                                            }
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
        Then the report status is SUCCESS
            Do compare
                With actual ${#json(#report, "$.report.status")}
                With expected SUCCESS
                With mode equals

    Scenario: Execute composable testcase with global vars
        Given A context-put composable task component wrapper
            Do http-post Post scenario to Chutney instance
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
            Do compare Assert HTTP status is 200
                With actual ${T(Integer).toString(#status)}
                With expected 200
                With mode equals
        And A assert-equals composable task component wrapper
            Do http-post Post scenario to Chutney instance
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
            Do compare Assert HTTP status is 200
                With actual ${T(Integer).toString(#status)}
                With expected 200
                With mode equals
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
            Do compare Assert HTTP status is 200
                With actual ${T(Integer).toString(#status)}
                With expected 200
                With mode equals
        When last saved scenario is executed
            Do http-post Post scenario execution to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/ui/component/execution/v1/${#scenarioId}/ENV
                Take report ${#body}
            Do compare Assert HTTP status is 200
                With actual ${T(Integer).toString(#status)}
                With expected 200
                With mode equals
        Then the report status is SUCCESS
            Do compare
                With actual ${#json(#report, "$.report.status")}
                With expected SUCCESS
                With mode equals
