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
                Validate httpStatusCode_200 ${#status == 200}

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
                        "testcase parameter quote": "**unused**",
                        "testcase parameter apostrophe": "**unused**"
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
                                                "apostrophe": "**escape.apostrophe**"
                                            }
                                        }
                                    }
                                },
                                {
                                    "implementation": {
                                        "type": "context-put",
                                        "inputs": {
                                            "entries": {
                                                "quote": "**escape.quote**"
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
                Validate httpStatusCode_200 ${#status == 200}
        When last saved scenario is executed
            Do http-post Post scenario execution to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/ui/scenario/execution/v1/${#scenarioId}/ENV
                With timeout 5 s
                Take report ${#body}
                Validate httpStatusCode_200 ${#status == 200}
        Then the report status is SUCCESS
            Do compare
                With actual ${#json(#report, "$.report.status")}
                With expected SUCCESS
                With mode equals
