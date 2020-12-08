# language: en
Feature: Assertions Task test

    Scenario: Execution by UI controller
        Given this scenario is saved
            Do http-post Post scenario to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/scenario/v2
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "title":"Init variables",
                    "scenario":{
                        "when":{
                            "sentence":"Put values in context",
                            "implementation":{
                                "task":"{\n type: context-put \n inputs: {\n entries: {\n test_int: {\"status\":{\"code\":200.0,\"reason\":\"OK\"}} \n test_xml: <test><xml attr=\"attributeValue\"><status><code>200.0</code><reason>OK</reason></status></xml></test> \n test_xml_ns: <tt:test xmlns:tt=\"http://test.org\" xmlns=\"http://test.org\"><xml attr=\"attributeValue\"><status xmlns=\"http://simple.org\"><code>200.0</code><reason>OK</reason></status></xml></test> \n} \n} \n}"
                            }
                        },
                        "thens":[
                            {
                                "sentence":"Json assert",
                                "implementation":{
                                    "task":"{\n type: json-assert \n inputs: {\n document: \${#test_int} \n expected: {\n $.status.code: 200.0 \n $.status.reason: OK \n} \n} \n}"
                                }
                            },
                            {
                                "sentence":"Xml assert",
                                "implementation":{
                                    "task":"{\n type: xml-assert \n inputs: {\n document: \${#test_xml} \n expected: {\n /test/xml/@attr: attributeValue \n /test/xml/status/code: \"200.0\" \n /test/xml/status/reason: OK \n} \n} \n}"
                                }
                            },
                            {
                                "sentence":"Xml assert with namespace",
                                "implementation":{
                                    "task":"{\n type: xml-assert \n inputs: {\n document: \${#test_xml_ns} \n expected: {\n /test/xml/@attr: attributeValue \n /test/xml/status/code: \"200.0\" \n /test/xml/status/reason: OK \n} \n} \n}"
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
        Then the report status is SUCCESS
            Do compare
                With actual ${#json(#report, "$.report.status")}
                With expected SUCCESS
                With mode equals

    Scenario: All in one assertions
        Given this scenario is saved
            Do http-post Post scenario to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/scenario/v2
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "title":"Init variables",
                    "scenario":{
                        "when":{
                            "sentence":"Put values in context",
                            "implementation":{
                                "task":"{\n type: context-put \n inputs: {\n entries: {\n test_json: {\"status\":{\"code\":\"200\",\"reason\":\"OK\"}} \n test_int: {\"status\":{\"code\":200.0,\"reason\":\"OK\"}} \n test_json2: {\"test\":{\"status\":{\"code\":\"200\",\"reason\":\"OK\"}}} \n test_string: Sky is the limit \n test_xml: <test><xml><is>boring</is></xml></test> \n} \n} \n}"
                            }
                        },
                        "thens":[
                            {
                                "sentence":"Simple asserts",
                                "implementation":{
                                    "task":"{\n type: assert \n inputs: {\n asserts: [{\n assert-true: \${1 == 1} \n} \n {\n assert-true: \${!false} \n}] \n} \n}"
                                }
                            },
                            {
                                "sentence":"Json assert",
                                "implementation":{
                                    "task":"{\n type: json-assert \n inputs: {\n document: \${#test_json} \n expected: {\n $.status.code: \"200\" \n $.status.reason: OK \n $.status.not_exist: null \n} \n} \n}"
                                }
                            },
                            {
                                "sentence":"Json compare",
                                "implementation":{
                                    "task":"{\n type: json-compare \n inputs: {\n document1: \${#test_json} \n document2: \${#test_json2} \n comparingPaths: {\n $.status: $.test.status \n $.status.code: $.test.status.code \n} \n} \n}"
                                }
                            },
                            {
                                "sentence":"Xml assert",
                                "implementation":{
                                    "task":"{\n type: xml-assert \n inputs: {\n document: \${#test_xml} \n expected: {\n /test/xml/is//text(): boring \n} \n} \n}"
                                }
                            },
                            {
                                "sentence":"Json assert",
                                "implementation":{
                                    "task":"{\n type: json-assert \n inputs: {\n document: \${#test_int} \n expected: {\n $.status.code: 200.0 \n} \n} \n}"
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
        Then the report status is SUCCESS
            Do compare
                With actual ${#json(#report, "$.report.status")}
                With expected SUCCESS
                With mode equals

    Scenario: Test xsd tasks
        Given this scenario is saved
            Do http-post Post scenario to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/scenario/v2
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "title":"Xsd task",
                    "scenario":{
                        "when":{
                            "sentence":"Validate employee xml with employee xsd",
                            "implementation":{
                                "task":"{\n type: xsd-validation \n inputs: {\n xsd: /blackbox/xsd_samples/employee.xsd \n xml: <?xml version=\"1.0\"?><Employee xmlns=\"https://www.chutneytesting.com/Employee\"><name>Pankaj</name><age>29</age><role>Java Developer</role><gender>Male</gender></Employee> \n} \n}"
                            }
                        },
                        "thens":[]
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
