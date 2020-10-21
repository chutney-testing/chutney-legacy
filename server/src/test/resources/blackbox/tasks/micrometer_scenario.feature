# language: en

@Micrometer
Feature: Micrometer Tasks test

    Background:
        Given an existing target chutney_local on local server

    Scenario: Micrometer counter meter
        Given this scenario is saved
        """
        {
            "title": "micrometer counter scenario",
            "tags": [],
            "executions": [],
            "content":
            '''
            {
                "scenario": {
                    "name": "micrometer counter scenario",
                    "steps": [
                        {
                           "name": "Create counter meter",
                           "type": "micrometer-counter",
                           "inputs":{
                              "name": "my.counter",
                              "tags": [
                                "myTagKey", "myTagValue"
                              ]
                           },
                           "outputs":{
                              "myCounter": "${#micrometerCounter}"
                           }
                        },
                        {
                            "name": "Check counter creation",
                            "steps": [
                                {
                                    "name": "Request for counter meter",
                                    "type": "http-get",
                                    "target": "chutney_local",
                                    "inputs":{
                                        "uri": "/actuator/metrics/my.counter"
                                    }
                                },
                                {
                                    "name": "Check counter value",
                                    "type": "compare",
                                    "inputs":{
                                        "mode": "equals",
                                        "actual": "${#json(#body, \"$.measurements[?(@.statistic=='COUNT')].value\").toString()}",
                                        "expected": "[0.0]"
                                    }
                                },
                                {
                                    "name": "Check counter tags",
                                    "type": "compare",
                                    "inputs":{
                                        "mode": "equals",
                                        "actual": "${#json(#body, \"$.availableTags[?(@.tag=='myTagKey')].values[0]\").toString()}",
                                        "expected": "[\"myTagValue\"]"
                                    }
                                }
                            ]
                        },
                        {
                            "name": "Increment my counter",
                            "type": "micrometer-counter",
                            "inputs":{
                                "counter": "${#myCounter}",
                                "increment": "2"
                            }
                        },
                        {
                            "name": "Check counter value",
                            "steps": [
                                {
                                    "name": "Request for counter meter",
                                    "type": "http-get",
                                    "target": "chutney_local",
                                    "inputs":{
                                        "uri": "/actuator/metrics/my.counter"
                                    }
                                },
                                {
                                    "name": "Check counter value",
                                    "type": "compare",
                                    "inputs":{
                                        "mode": "equals",
                                        "actual": "${#json(#body, \"$.measurements[?(@.statistic=='COUNT')].value\").toString()}",
                                        "expected": "[2.0]"
                                    }
                                }
                            ]
                        }
                    ]
                }
            }
            '''
        }
        """
        When last saved scenario is executed
        Then the report status is SUCCESS

    Scenario: Micrometer timer meter
        Given this scenario is saved
        """
        {
            "title": "micrometer timer scenario",
            "tags": [],
            "executions": [],
            "content":
            '''
            {
                "scenario": {
                    "name": "micrometer timer scenario",
                    "steps": [
                        {
                           "name": "Create timer meter",
                           "type": "micrometer-timer",
                           "inputs":{
                              "name": "my.timer",
                              "tags": [
                                "myTagKey", "myTagValue"
                              ]
                           },
                           "outputs":{
                              "myTimer": "${#micrometerTimer}"
                           }
                        },
                        {
                            "name": "Check timer creation",
                            "steps": [
                                {
                                    "name": "Request for timer meter",
                                    "type": "http-get",
                                    "target": "chutney_local",
                                    "inputs":{
                                        "uri": "/actuator/metrics/my.timer"
                                    }
                                },
                                {
                                    "name": "Check timer count value",
                                    "type": "compare",
                                    "inputs":{
                                        "mode": "equals",
                                        "actual": "${#json(#body, \"$.measurements[?(@.statistic=='COUNT')].value\").toString()}",
                                        "expected": "[0.0]"
                                    }
                                },
                                {
                                    "name": "Check timer tags",
                                    "type": "compare",
                                    "inputs":{
                                        "mode": "equals",
                                        "actual": "${#json(#body, \"$.availableTags[?(@.tag=='myTagKey')].values[0]\").toString()}",
                                        "expected": "[\"myTagValue\"]"
                                    }
                                }
                            ]
                        },
                        {
                            "name": "Update my timer",
                            "type": "micrometer-timer",
                            "inputs":{
                                "timer": "${#myTimer}",
                                "record": "5 s"
                            }
                        },
                        {
                            "name": "Check timer value",
                            "steps": [
                                {
                                    "name": "Request for timer meter",
                                    "type": "http-get",
                                    "target": "chutney_local",
                                    "inputs":{
                                        "uri": "/actuator/metrics/my.timer"
                                    }
                                },
                                {
                                    "name": "Check timer count value",
                                    "type": "compare",
                                    "inputs":{
                                        "mode": "equals",
                                        "actual": "${#json(#body, \"$.measurements[?(@.statistic=='COUNT')].value\").toString()}",
                                        "expected": "[1.0]"
                                    }
                                },
                                {
                                    "name": "Check timer total time value",
                                    "type": "compare",
                                    "inputs":{
                                        "mode": "equals",
                                        "actual": "${#json(#body, \"$.measurements[?(@.statistic=='TOTAL_TIME')].value\").toString()}",
                                        "expected": "[5.0]"
                                    }
                                },
                                {
                                    "name": "Check timer max value",
                                    "type": "compare",
                                    "inputs":{
                                        "mode": "equals",
                                        "actual": "${#json(#body, \"$.measurements[?(@.statistic=='MAX')].value\").toString()}",
                                        "expected": "[5.0]"
                                    }
                                }
                            ]
                        }
                    ]
                }
            }
            '''
        }
        """
        When last saved scenario is executed
        Then the report status is SUCCESS

    Scenario: Micrometer timer meter with start and stop
        Given this scenario is saved
        """
        {
            "title": "micrometer timer start stop scenario",
            "tags": [],
            "executions": [],
            "content":
            '''
            {
                "scenario": {
                    "name": "micrometer timer start stop scenario",
                    "steps": [
                        {
                           "name": "Create timer meter",
                           "type": "micrometer-timer",
                           "inputs":{
                              "name": "my.timer.start.stop",
                              "tags": [
                                "myTagKey", "myTagValue"
                              ]
                           },
                           "outputs":{
                              "myTimer": "${#micrometerTimer}"
                           }
                        },
                        {
                           "name": "Start a timing sample",
                           "type": "micrometer-timer-start",
                           "outputs":{
                              "myTimingSample": "${#micrometerTimerSample}"
                           }
                        },
                        {
                            "name": "Sleep for a second",
                            "type": "sleep",
                            "inputs":{
                                "duration": "1100 ms"
                            }
                        },
                        {
                           "name": "Stop a timing sample",
                           "type": "micrometer-timer-stop",
                           "inputs":{
                              "sample": "${#micrometerTimerSample}",
                              "timer": "${#myTimer}"
                           }
                        },
                        {
                            "name": "Check timer value",
                            "steps": [
                                {
                                    "name": "Request for timer meter",
                                    "type": "http-get",
                                    "target": "chutney_local",
                                    "inputs":{
                                        "uri": "/actuator/metrics/my.timer.start.stop"
                                    }
                                },
                                {
                                    "name": "Check timer count value",
                                    "type": "compare",
                                    "inputs":{
                                        "mode": "equals",
                                        "actual": "${#json(#body, \"$.measurements[?(@.statistic=='COUNT')].value\").toString()}",
                                        "expected": "[1.0]"
                                    }
                                },
                                {
                                    "name": "Check timer total time value",
                                    "type": "compare",
                                    "inputs":{
                                        "mode": "greater than",
                                        "actual": "${#json(#body, \"$.measurements[?(@.statistic=='TOTAL_TIME')].value\").get(0).toString()}",
                                        "expected": "1"
                                    }
                                },
                                {
                                    "name": "Check timer max value",
                                    "type": "compare",
                                    "inputs":{
                                        "mode": "greater than",
                                        "actual": "${#json(#body, \"$.measurements[?(@.statistic=='MAX')].value\").get(0).toString()}",
                                        "expected": "1"
                                    }
                                }
                            ]
                        }
                    ]
                }
            }
            '''
        }
        """
        When last saved scenario is executed
        Then the report status is SUCCESS

    Scenario: Micrometer gauge meter
        Given this scenario is saved
        """
        {
            "title": "micrometer gauge scenario",
            "tags": [],
            "executions": [],
            "content":
            '''
            {
                "scenario": {
                    "name": "micrometer gauge scenario",
                    "steps": [
                        {
                           "name": "Create gauge meter",
                           "type": "micrometer-gauge",
                           "inputs":{
                              "name": "my.gauge",
                              "tags": [
                                "myTagKey", "myTagValue"
                              ],
                              "gaugeObject": "${new java.util.ArrayList()}"
                           },
                           "outputs":{
                             "myGaugeObject": "${#micrometerGaugeObject}"
                           }
                        },
                        {
                            "name": "Check gauge creation",
                            "steps": [
                                {
                                    "name": "Request for gauge meter",
                                    "type": "http-get",
                                    "target": "chutney_local",
                                    "inputs":{
                                        "uri": "/actuator/metrics/my.gauge"
                                    }
                                },
                                {
                                    "name": "Check gauge value",
                                    "type": "compare",
                                    "inputs":{
                                        "mode": "equals",
                                        "actual": "${#json(#body, \"$.measurements[?(@.statistic=='VALUE')].value\").toString()}",
                                        "expected": "[0.0]"
                                    }
                                },
                                {
                                    "name": "Check gauge tags",
                                    "type": "compare",
                                    "inputs":{
                                        "mode": "equals",
                                        "actual": "${#json(#body, \"$.availableTags[?(@.tag=='myTagKey')].values[0]\").toString()}",
                                        "expected": "[\"myTagValue\"]"
                                    }
                                }
                            ]
                        },
                        {
                            "name": "Update gauge object",
                            "type": "success",
                            "outputs":{
                                "noop": "${#myGaugeObject.add(new Object())}"
                            }
                        },
                        {
                            "name": "Check gauge value",
                            "steps": [
                                {
                                    "name": "Request for gauge meter",
                                    "type": "http-get",
                                    "target": "chutney_local",
                                    "inputs":{
                                        "uri": "/actuator/metrics/my.gauge"
                                    }
                                },
                                {
                                    "name": "Check gauge value",
                                    "type": "compare",
                                    "inputs":{
                                        "mode": "equals",
                                        "actual": "${#json(#body, \"$.measurements[?(@.statistic=='VALUE')].value\").toString()}",
                                        "expected": "[1.0]"
                                    }
                                }
                            ]
                        }
                    ]
                }
            }
            '''
        }
        """
        When last saved scenario is executed
        Then the report status is SUCCESS

    Scenario: Micrometer distribution summary meter
        Given this scenario is saved
        """
        {
            "title": "micrometer distribution summary scenario",
            "tags": [],
            "executions": [],
            "content":
            '''
            {
                "scenario": {
                    "name": "micrometer distribution summary scenario",
                    "steps": [
                        {
                           "name": "Create distribution meter",
                           "type": "micrometer-summary",
                           "inputs":{
                              "name": "my.summary",
                              "tags": [
                                "myTagKey", "myTagValue"
                              ]
                           },
                           "outputs":{
                              "mySummary": "${#micrometerSummary}"
                           }
                        },
                        {
                            "name": "Check distribution creation",
                            "steps": [
                                {
                                    "name": "Request for distribution meter",
                                    "type": "http-get",
                                    "target": "chutney_local",
                                    "inputs":{
                                        "uri": "/actuator/metrics/my.summary"
                                    }
                                },
                                {
                                    "name": "Check distribution value",
                                    "type": "compare",
                                    "inputs":{
                                        "mode": "equals",
                                        "actual": "${#json(#body, \"$.measurements[?(@.statistic=='TOTAL')].value\").toString()}",
                                        "expected": "[0.0]"
                                    }
                                },
                                {
                                    "name": "Check distribution value",
                                    "type": "compare",
                                    "inputs":{
                                        "mode": "equals",
                                        "actual": "${#json(#body, \"$.measurements[?(@.statistic=='COUNT')].value\").toString()}",
                                        "expected": "[0.0]"
                                    }
                                },
                                {
                                    "name": "Check distribution tags",
                                    "type": "compare",
                                    "inputs":{
                                        "mode": "equals",
                                        "actual": "${#json(#body, \"$.availableTags[?(@.tag=='myTagKey')].values[0]\").toString()}",
                                        "expected": "[\"myTagValue\"]"
                                    }
                                }
                            ]
                        },
                        {
                            "name": "Update my distribution",
                            "type": "micrometer-summary",
                            "inputs":{
                                "distributionSummary": "${#mySummary}",
                                "record": "2"
                            }
                        },
                        {
                            "name": "Check distribution value",
                            "steps": [
                                {
                                    "name": "Request for distribution meter",
                                    "type": "http-get",
                                    "target": "chutney_local",
                                    "inputs":{
                                        "uri": "/actuator/metrics/my.summary"
                                    }
                                },
                                {
                                    "name": "Check distribution value",
                                    "type": "compare",
                                    "inputs":{
                                        "mode": "equals",
                                        "actual": "${#json(#body, \"$.measurements[?(@.statistic=='TOTAL')].value\").toString()}",
                                        "expected": "[2.0]"
                                    }
                                },
                                {
                                    "name": "Check distribution value",
                                    "type": "compare",
                                    "inputs":{
                                        "mode": "equals",
                                        "actual": "${#json(#body, \"$.measurements[?(@.statistic=='COUNT')].value\").toString()}",
                                        "expected": "[1.0]"
                                    }
                                }
                            ]
                        }
                    ]
                }
            }
            '''
        }
        """
        When last saved scenario is executed
        Then the report status is SUCCESS
