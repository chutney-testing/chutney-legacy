# language: en
Feature: Assertions Task test

Scenario: Execution by UI controller
    Given this scenario is saved
"""
{
    "title": "this is scenario !",
    "tags": [],
    "executions": [],
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
                            "document": "${#test_int}",
                            "expected": {
                                "$.status.code": 200.0,
                                "$.status.reason": "OK"
                            }
                        }
                    }, {
                        "name": "Xml assert",
                        "type": "xml-assert",
                        "inputs": {
                            "document": "${#test_xml}",
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
                            "document": "${#test_xml_ns}",
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
    Then the report status is SUCCESS

Scenario: All in one assertions
    Given this scenario is saved
"""
    {
        "title": "post scenario !",
        "tags": [],
        "executions": [],
        "content":
        '''
    {
    	"scenario": {
    		"name": "Init variables",
    		"steps": [{
                    "name": "Put Values in context",
    				"type": "context-put",
    				"inputs": {
    					"entries": {
    						"test_json": "{\"status\":{\"code\":\"200\",\"reason\":\"OK\"}}",
    						"test_int": "{\"status\":{\"code\":200.0,\"reason\":\"OK\"}}",
    						"test_json2": "{\"test\":{\"status\":{\"code\":\"200\",\"reason\":\"OK\"}}}",
    						"test_string": "Sky is the limite",
    						"test_xml": "<test><xml><is>boring</is></xml></test>"
    					}
    				}
    			}, {
                    "name": "Simple asserts",
    				"type": "assert",
    				"inputs": {
    					"asserts":
    					[{
    							"assert-true": "${1 == 1}"
    						}, {
    							"assert-true": "${!false}"
    						}
    					]
    				}
    			}, {
    				"name": "Json assert",
    				"type": "json-assert",
    				"inputs": {
    					"document": "${#test_json}",
    					"expected": {
    						"$.status.code": "200",
    						"$.status.reason": "OK",
    						"$.status.not_exist": null
    					}
    				}
    			}, {
    				"name": "Json compare",
    				"type": "json-compare",
    				"inputs": {
    					"document1": "${#test_json}",
    					"document2": "${#test_json2}",
    					"comparingPaths": {
    						"$.status": "$.test.status",
    						"$.status.code": "$.test.status.code"
    					}
    				}
    			}, {
    				"name": "Xml assert",
					"type": "xml-assert",
					"inputs": {
						"document": "${#test_xml}",
						"expected": {
							"/test/xml/is//text()": "boring"
						}
					}
				},{
                    "name": "Json assert",
                    "type": "json-assert",
                    "inputs": {
                        "document": "${#test_int}",
                        "expected": {
                            "$.status.code": 200.0
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
    Then the report status is SUCCESS

Scenario: Test xsd tasks
    Given this scenario is saved
"""
{
    "title": "this is scenario !",
    "tags": [],
    "executions": [],
    "content":
    '''
       {"scenario":
           {
                "name": "xsd task",
                "steps": [
                {
                    "name": "validate employee xml with employee xsd",
                    "type": "xsd-validation",
                    "inputs": {
                        "xml": "<?xml version=\"1.0\"?><Employee xmlns=\"https://www.chutneytesting.com/Employee\"><name>Pankaj</name><age>29</age><role>Java Developer</role><gender>Male</gender></Employee>",
                        "xsd": "/blackbox/xsd_samples/employee.xsd"
                    }
                }

                ]
           }
       }
    '''
}
"""
    When last saved scenario is executed
    Then the report status is SUCCESS
