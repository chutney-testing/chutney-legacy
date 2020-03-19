Feature: Assertions Task test

    Scenario: Execution by UI controller
        Given this scenario is saved
            Execute http-post
                On CHUTNEY_DEV <host>
                With uri /api/scenario/v2/raw/
                With headers
                | Content-Type | application/json |
                With body
                """
                {
                    "title": "this is scenario !",
                    "tags": [],
                    "content":
                    '''
                       {"scenario":
                           {
                                "name": "Init variables",
                                "steps": [{
                                        "name": "Put Values in context",
                                        "type": "context-put",
                                        "inputs": {
                                            "entries": {
                                                "test_int": "{\"status\":{\"code\":200.0,\"reason\":\"OK\"}}",
                                                "test_xml": "<test><xml attr=\"attributeValue\"><status><code>200.0</code><reason>OK</reason></status></xml></test>"
                                                "test_xml_ns": "<tt:test xmlns:tt=\"http://test.org\" xmlns=\"http://test.org\"><xml attr=\"attributeValue\"><status xmlns=\"http://simple.org\"><code>200.0</code><reason>OK</reason></status></xml></test>"
                                            }
                                        }
                                    }, {
                                        "name": "Json assert",
                                        "type": "json-assert",
                                        "inputs": {
                                            "document": "${'${#test_int'}}",
                                            "expected": {
                                                "$.status.code": 200.0,
                                                "$.status.reason": "OK"
                                            }
                                        }
                                    }, {
                                        "name": "Xml assert",
                                        "type": "xml-assert",
                                        "inputs": {
                                            "document": "${'${#test_xml'}}",
                                            "expected": {
                                                "/test/xml/@attr": "attributeValue",
                                                "/test/xml/status/code": "200.0",
                                                "/test/xml/status/reason": "OK"
                                            }
                                        }
                                    }, {
                                        "name": "Xml assert with namespaces",
                                        "type": "xml-assert",
                                        "inputs": {
                                            "document": "${'${#test_xml_ns'}}",
                                            "expected": {
                                                "/test/xml/@attr": "attributeValue",
                                                "/test/xml/status/code": "200.0",
                                                "/test/xml/status/reason": "OK"
                                            }
                                        }
                                    }
                                ]
                           }
                       }
                    '''
                }
                """
        When last saved scenario is executed
            Execute http-post
                On CHUTNEY_DEV <host>
                With uri /api/ui/scenario/execution/v1/${#body}
                With body {}
        Then the report status is SUCCESS
            Do compare
                With mode equals
                With actual ${#jsonPath(#body, "$.status")}
                With expected SUCCESS
