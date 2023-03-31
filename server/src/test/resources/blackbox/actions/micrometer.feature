# language: en

@Micrometer
Feature: Micrometer Tasks test

    Background:
        Given A target for chutney instance under test
            Do put in context
            | micrometerEnv | MICROMETER_ENV_${#generate().randomInt(99999)} |
            Do http-post Create environment and target
                On CHUTNEY_LOCAL
                With uri /api/v2/environment
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "name": "${#micrometerEnv}",
                    "description": "",
                    "targets": [
                        {
                            "name": "chutney_local",
                            "url": "${#target.rawUri()}",
                            "properties": [
                                { "key" : "user", "value": "admin" },
                                { "key" : "password", "value": "admin" }
                            ]
                        }
                    ]
                }
                """
                Validate httpStatusCode_200 ${#status == 200}

    Scenario: Micrometer counter meter
        Given this scenario is saved
            Do http-post Post scenario to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/scenario/v2
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "title":"micrometer counter scenario",
                    "scenario":{
                        "givens":[
                            {
                                "sentence": "An existing counter",
                                "subSteps":[
                                    {
                                        "sentence": "Create counter meter",
                                        "implementation":{
                                            "task":"{\n type: micrometer-counter \n inputs: {\n name: my.counter \n tags: [ 'myTagKey', 'myTagValue' ] \n} \n outputs: {\n myCounter: \${#micrometerCounter} \n} \n}"
                                        }
                                    },
                                    {
                                        "sentence": "Check counter creation",
                                        "subSteps":[
                                            {
                                                "sentence": "Request for counter meter",
                                                "implementation":{
                                                    "task":"{\n type: http-get \n target: chutney_local \n inputs: {\n uri: /actuator/metrics/my.counter \n} \n}"
                                                }
                                            },
                                            {
                                                "sentence": "Check counter value",
                                                "implementation":{
                                                    "task":"{\n type: compare \n inputs: {\n actual: \${#json(#body, \"$.measurements[?(@.statistic=='COUNT')].value\").toString()} \n expected: '[0.0]' \n mode: equals \n} \n}"
                                                }
                                            },
                                            {
                                                "sentence": "Check counter tags",
                                                "implementation":{
                                                    "task":"{\n type: compare \n inputs: {\n actual: \${#json(#body, \"$.availableTags[?(@.tag=='myTagKey')].values[0]\").toString()} \n expected: '[\"myTagValue\"]' \n mode: equals \n} \n}"
                                                }
                                            }
                                        ]
                                    }
                                ]
                            }
                        ],
                        "when":{
                            "sentence": "Increment my counter",
                            "implementation":{
                                "task":"{\n type: micrometer-counter \n inputs: {\n counter: \${#myCounter} \n increment: '2' \n} \n}"
                            }
                        },
                        "thens":[
                            {
                                "sentence": "Request for counter meter",
                                "implementation":{
                                    "task":"{\n type: http-get \n target: chutney_local \n inputs: {\n uri: /actuator/metrics/my.counter \n} \n}"
                                }
                            },
                            {
                                "sentence": "Check counter value",
                                "implementation":{
                                    "task":"{\n type: compare \n inputs: {\n actual: \${#json(#body, \"$.measurements[?(@.statistic=='COUNT')].value\").toString()} \n expected: '[2.0]' \n mode: equals \n} \n}"
                                }
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
                With uri /api/ui/scenario/execution/v1/${#scenarioId}/${#micrometerEnv}
                With timeout 5 s
                Take report ${#body}
                Validate httpStatusCode_200 ${#status == 200}
        Then the report status is SUCCESS
            Do compare
                With actual ${#json(#report, "$.report.status")}
                With expected SUCCESS
                With mode equals

    Scenario: Micrometer timer meter
        Given this scenario is saved
            Do http-post Post scenario to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/scenario/v2
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "title":"micrometer timer scenario",
                    "scenario":{
                        "givens":[
                            {
                                "sentence": "An existing timer",
                                "subSteps":[
                                    {
                                        "sentence": "Create timer meter",
                                        "implementation":{
                                            "task":"{\n type: micrometer-timer \n inputs: {\n name: my.timer \n tags: [ 'myTagKey', 'myTagValue' ] \n} \n outputs: {\n myTimer: \${#micrometerTimer} \n} \n}"
                                        }
                                    },
                                    {
                                        "sentence": "Check timer creation",
                                        "subSteps":[
                                            {
                                                "sentence": "Request for timer meter",
                                                "implementation":{
                                                    "task":"{\n type: http-get \n target: chutney_local \n inputs: {\n uri: /actuator/metrics/my.timer \n} \n}"
                                                }
                                            },
                                            {
                                                "sentence": "Check timer count value",
                                                "implementation":{
                                                    "task":"{\n type: compare \n inputs: {\n actual: \${#json(#body, \"$.measurements[?(@.statistic=='COUNT')].value\").toString()} \n expected: '[0.0]' \n mode: equals \n} \n}"
                                                }
                                            },
                                            {
                                                "sentence": "Check timer tags",
                                                "implementation":{
                                                    "task":"{\n type: compare \n inputs: {\n actual: \${#json(#body, \"$.availableTags[?(@.tag=='myTagKey')].values[0]\").toString()} \n expected: '[\"myTagValue\"]' \n mode: equals \n} \n}"
                                                }
                                            }
                                        ]
                                    }
                                ]
                            }
                        ],
                        "when":{
                            "sentence": "Update my timer",
                            "implementation":{
                                "task":"{\n type: micrometer-timer \n inputs: {\n timer: \${#myTimer} \n record: '5 s' \n} \n}"
                            }
                        },
                        "thens":[
                            {
                                "sentence": "Request for timer meter",
                                "implementation":{
                                    "task":"{\n type: http-get \n target: chutney_local \n inputs: {\n uri: /actuator/metrics/my.timer \n} \n}"
                                }
                            },
                            {
                                "sentence": "Check timer count value",
                                "implementation":{
                                    "task":"{\n type: compare \n inputs: {\n actual: \${#json(#body, \"$.measurements[?(@.statistic=='COUNT')].value\").toString()} \n expected: '[1.0]' \n mode: equals \n} \n}"
                                }
                            },
                            {
                                "sentence": "Check timer total time value",
                                "implementation":{
                                    "task":"{\n type: compare \n inputs: {\n actual: \${#json(#body, \"$.measurements[?(@.statistic=='TOTAL_TIME')].value\").toString()} \n expected: '[5.0]' \n mode: equals \n} \n}"
                                }
                            },
                            {
                                "sentence": "Check timer max value",
                                "implementation":{
                                    "task":"{\n type: compare \n inputs: {\n actual: \${#json(#body, \"$.measurements[?(@.statistic=='MAX')].value\").toString()} \n expected: '[5.0]' \n mode: equals \n} \n}"
                                }
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
                With uri /api/ui/scenario/execution/v1/${#scenarioId}/${#micrometerEnv}
                With timeout 5 s
                Take report ${#body}
                Validate httpStatusCode_200 ${#status == 200}
        Then the report status is SUCCESS
            Do compare
                With actual ${#json(#report, "$.report.status")}
                With expected SUCCESS
                With mode equals

    Scenario: Micrometer timer meter with start and stop
        Given this scenario is saved
            Do http-post Post scenario to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/scenario/v2
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "title":"micrometer timer start stop scenario",
                    "scenario":{
                        "givens":[
                            {
                                "sentence": "An existing timer",
                                "implementation":{
                                    "task":"{\n type: micrometer-timer \n inputs: {\n name: my.timer.start.stop \n tags: [ 'myTagKey', 'myTagValue' ] \n} \n outputs: {\n myTimer: \${#micrometerTimer} \n} \n}"
                                }
                            }
                        ],
                        "when":{
                            "sentence": "Start and stop a timing sample",
                            "subSteps":[
                                {
                                    "sentence": "Start a timing sample",
                                    "implementation":{
                                        "task":"{\n type: micrometer-timer-start \n outputs: {\n myTimingSample: \${#micrometerTimerSample} \n} \n}"
                                    }
                                },
                                {
                                    "sentence": "Sleep for a second",
                                    "implementation":{
                                        "task":"{\n type: sleep \n inputs: {\n duration: 1100 ms \n} \n}"
                                    }
                                },
                                {
                                    "sentence": "Stop the timing sample",
                                    "implementation":{
                                        "task":"{\n type: micrometer-timer-stop \n inputs: {\n sample: \${#micrometerTimerSample} \n timer: \${#myTimer} \n} \n}"
                                    }
                                }
                            ]
                        },
                        "thens":[
                            {
                                "sentence": "Request for timer meter",
                                "implementation":{
                                    "task":"{\n type: http-get \n target: chutney_local \n inputs: {\n uri: /actuator/metrics/my.timer.start.stop \n} \n}"
                                }
                            },
                            {
                                "sentence": "Check timer count value",
                                "implementation":{
                                    "task":"{\n type: compare \n inputs: {\n actual: \${#json(#body, \"$.measurements[?(@.statistic=='COUNT')].value\").toString()} \n expected: '[1.0]' \n mode: equals \n} \n}"
                                }
                            },
                            {
                                "sentence": "Check timer total time value",
                                "implementation":{
                                    "task":"{\n type: compare \n inputs: {\n actual: \"\${#json(#body, \\\"$.measurements[?(@.statistic=='TOTAL_TIME')].value\\\").get(0).toString()}\" \n expected: '1' \n mode: greater than \n} \n}"
                                }
                            },
                            {
                                "sentence": "Check timer max value",
                                "implementation":{
                                    "task":"{\n type: compare \n inputs: {\n actual: \"\${#json(#body, \\\"$.measurements[?(@.statistic=='MAX')].value\\\").get(0).toString()}\" \n expected: '1' \n mode: greater than \n} \n}"
                                }
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
                With uri /api/ui/scenario/execution/v1/${#scenarioId}/${#micrometerEnv}
                With timeout 5 s
                Take report ${#body}
                Validate httpStatusCode_200 ${#status == 200}
        Then the report status is SUCCESS
            Do compare
                With actual ${#json(#report, "$.report.status")}
                With expected SUCCESS
                With mode equals

    Scenario: Micrometer gauge meter
        Given this scenario is saved
            Do http-post Post scenario to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/scenario/v2
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "title":"micrometer gauge scenario",
                    "scenario":{
                        "givens":[
                            {
                                "sentence": "An existing gauge",
                                "subSteps":[
                                    {
                                        "sentence": "Create gauge meter",
                                        "implementation":{
                                            "task":"{\n type: micrometer-gauge \n inputs: {\n name: my.gauge \n tags: [ 'myTagKey', 'myTagValue' ] \n gaugeObject: \${new java.util.ArrayList()} \n} \n outputs: {\n myGaugeObject: \${#micrometerGaugeObject} \n} \n}"
                                        }
                                    },
                                    {
                                        "sentence": "Check gauge creation",
                                        "subSteps":[
                                            {
                                                "sentence": "Request for counter meter",
                                                "implementation":{
                                                    "task":"{\n type: http-get \n target: chutney_local \n inputs: {\n uri: /actuator/metrics/my.gauge \n} \n}"
                                                }
                                            },
                                            {
                                                "sentence": "Check gauge value",
                                                "implementation":{
                                                    "task":"{\n type: compare \n inputs: {\n actual: \${#json(#body, \"$.measurements[?(@.statistic=='VALUE')].value\").toString()} \n expected: '[0.0]' \n mode: equals \n} \n}"
                                                }
                                            },
                                            {
                                                "sentence": "Check gauge tags",
                                                "implementation":{
                                                    "task":"{\n type: compare \n inputs: {\n actual: \${#json(#body, \"$.availableTags[?(@.tag=='myTagKey')].values[0]\").toString()} \n expected: '[\"myTagValue\"]' \n mode: equals \n} \n}"
                                                }
                                            }
                                        ]
                                    }
                                ]
                            }
                        ],
                        "when":{
                            "sentence": "Update gauge object",
                            "implementation":{
                                "task":"{\n type: success \n outputs: {\n noop: \${#myGaugeObject.add(new Object())} \n} \n}"
                            }
                        },
                        "thens":[
                            {
                                "sentence": "Request for counter meter",
                                "implementation":{
                                    "task":"{\n type: http-get \n target: chutney_local \n inputs: {\n uri: /actuator/metrics/my.gauge \n} \n}"
                                }
                            },
                            {
                                "sentence": "Check gauge value",
                                "implementation":{
                                    "task":"{\n type: compare \n inputs: {\n actual: \${#json(#body, \"$.measurements[?(@.statistic=='VALUE')].value\").toString()} \n expected: '[1.0]' \n mode: equals \n} \n}"
                                }
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
                With uri /api/ui/scenario/execution/v1/${#scenarioId}/${#micrometerEnv}
                With timeout 5 s
                Take report ${#body}
                Validate httpStatusCode_200 ${#status == 200}
        Then the report status is SUCCESS
            Do compare
                With actual ${#json(#report, "$.report.status")}
                With expected SUCCESS
                With mode equals

    Scenario: Micrometer distribution summary meter
        Given this scenario is saved
            Do http-post Post scenario to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/scenario/v2
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "title":"micrometer distribution summary scenario",
                    "scenario":{
                        "givens":[
                            {
                                "sentence": "An existing distribution",
                                "subSteps":[
                                    {
                                        "sentence": "Create distribution meter",
                                        "implementation":{
                                            "task":"{\n type: micrometer-summary \n inputs: {\n name: my.summary \n tags: [ 'myTagKey', 'myTagValue' ] \n} \n outputs: {\n mySummary: \${#micrometerSummary} \n} \n}"
                                        }
                                    },
                                    {
                                        "sentence": "Check distribution creation",
                                        "subSteps":[
                                            {
                                                "sentence": "Request for distribution meter",
                                                "implementation":{
                                                    "task":"{\n type: http-get \n target: chutney_local \n inputs: {\n uri: /actuator/metrics/my.summary \n} \n}"
                                                }
                                            },
                                            {
                                                "sentence": "Check distribution total value",
                                                "implementation":{
                                                    "task":"{\n type: compare \n inputs: {\n actual: \${#json(#body, \"$.measurements[?(@.statistic=='TOTAL')].value\").toString()} \n expected: '[0.0]' \n mode: equals \n} \n}"
                                                }
                                            },
                                            {
                                                "sentence": "Check distribution count value",
                                                "implementation":{
                                                    "task":"{\n type: compare \n inputs: {\n actual: \${#json(#body, \"$.measurements[?(@.statistic=='COUNT')].value\").toString()} \n expected: '[0.0]' \n mode: equals \n} \n}"
                                                }
                                            },
                                            {
                                                "sentence": "Check distribution tags",
                                                "implementation":{
                                                    "task":"{\n type: compare \n inputs: {\n actual: \${#json(#body, \"$.availableTags[?(@.tag=='myTagKey')].values[0]\").toString()} \n expected: '[\"myTagValue\"]' \n mode: equals \n} \n}"
                                                }
                                            }
                                        ]
                                    }
                                ]
                            }
                        ],
                        "when":{
                            "sentence": "Update my distribution",
                            "implementation":{
                                "task":"{\n type: micrometer-summary \n inputs: {\n distributionSummary: \${#mySummary} \n record: '2' \n} \n}"
                            }
                        },
                        "thens":[
                            {
                                "sentence": "Request for distribution meter",
                                "implementation":{
                                    "task":"{\n type: http-get \n target: chutney_local \n inputs: {\n uri: /actuator/metrics/my.summary \n} \n}"
                                }
                            },
                            {
                                "sentence": "Check distribution total value",
                                "implementation":{
                                    "task":"{\n type: compare \n inputs: {\n actual: \${#json(#body, \"$.measurements[?(@.statistic=='TOTAL')].value\").toString()} \n expected: '[2.0]' \n mode: equals \n} \n}"
                                }
                            },
                            {
                                "sentence": "Check distribution count value",
                                "implementation":{
                                    "task":"{\n type: compare \n inputs: {\n actual: \${#json(#body, \"$.measurements[?(@.statistic=='COUNT')].value\").toString()} \n expected: '[1.0]' \n mode: equals \n} \n}"
                                }
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
                With uri /api/ui/scenario/execution/v1/${#scenarioId}/${#micrometerEnv}
                With timeout 5 s
                Take report ${#body}
                Validate httpStatusCode_200 ${#status == 200}
        Then the report status is SUCCESS
            Do compare
                With actual ${#json(#report, "$.report.status")}
                With expected SUCCESS
                With mode equals
