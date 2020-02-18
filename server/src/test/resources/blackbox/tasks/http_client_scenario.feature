# language: en
@HttpTask
Feature: HTTP Task test

Scenario: Http get request wrong url
    Given an existing target test with url http://localhost:12345
    And this scenario is saved
"""
    {
        "title": "post scenario !",
        "tags": [],
        "executions": [],
        "content":
        '''
    {
        "scenario": {
            "name": "http client failure get",
            "steps": [
                {
                   "name": "Make failed GET request",
                   "type":"http-get",
                   "target": "test",
                   "inputs":{
                      "uri": "notused"
                   }
                }
            ]
        }
    }
    '''
    }
"""
    When last saved scenario is executed
    Then the report status is FAILURE

Scenario: Http delete request wrong url
    Given an existing target test with url http://localhost:12345
    And this scenario is saved
"""
    {
        "title": "post scenario !",
        "tags": [],
        "executions": [],
        "content":
        '''
    {
        "scenario": {
            "name": "http client failure delete",
            "steps": [
                {
                   "name": "Make failed DELETE request",
                   "type":"http-delete",
                   "target": "test",
                   "inputs":{
                      "uri": "notused"
                   }
                }
            ]
        }
    }
    '''
    }
"""
    When last saved scenario is executed
    Then the report status is FAILURE

Scenario: Http post request wrong url
    Given an existing target test with url http://localhost:12345
    And this scenario is saved
"""
    {
        "title": "post scenario !",
        "tags": [],
        "executions": [],
        "content":
        '''
    {
        "scenario": {
            "name": "http client failure post",
            "steps": [
                {
                   "name": "Make failed POST request",
                   "type":"http-post",
                   "target": "test",
                   "inputs":{
                      "uri": "notused",
                      "body": "cool buddy"
                   }
                }
            ]
        }
    }
    '''
    }
"""
    When last saved scenario is executed
    Then the report status is FAILURE

Scenario: Http put request wrong url
    Given an existing target test with url http://localhost:12345
    And this scenario is saved
"""
    {
        "title": "post scenario !",
        "tags": [],
        "executions": [],
        "content":
        '''
    {
        "scenario": {
            "name": "http client failure put",
            "steps": [
                {
                   "name": "Make failed PUT request",
                   "type":"http-put",
                   "target": "test",
                   "inputs":{
                      "uri": "notused",
                      "body": "cool buddy"
                   }
                }
            ]
        }
    }
    '''
    }
"""
    When last saved scenario is executed
    Then the report status is FAILURE

Scenario: Http get request local valid endpoint
    Given an existing target test on local server
    And this scenario is saved
"""
    {
        "title": "post scenario !",
        "tags": [],
        "executions": [],
        "content":
        '''
    {
        "scenario": {
            "name": "http client get",
            "steps": [
                {
                   "name": "Make GET request",
                   "type":"http-get",
                   "target": "test",
                   "inputs":{
                      "uri": "/actuator/health",
                      "headers": {
                            "X--API-VERSION": "1.0"
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


Scenario: Http post request local valid endpoint
    Given an app providing an http endpoint such as POST on uri /mock/post
    And a configured target test_mock for an app
    And this scenario is saved
"""
    {
        "title": "post scenario !",
        "tags": [],
        "executions": [],
        "content":
        '''
        {
            "scenario": {
                "name": "http client post",
                "steps": [
                    {
                       "name": "Make POST request",
                       "type":"http-post",
                       "target": "test_mock",
                       "inputs":{
                          "uri": "/mock/post",
                          "body": {
							selector : "$.selector[?(@.metadata.id == '1234')]"
						    },
						  "timeout": 3000 ms
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

Scenario: Http post request local valid endpoint
    Given an app providing an http endpoint such as POST on uri /mock/post
    And a configured target test_mock for an app
    And this scenario is saved
"""
    {
        "title": "post scenario !",
        "tags": [],
        "executions": [],
        "content":
        '''
    {
        "scenario": {
            "name": "http client post",
            "steps": [
                {
                   "name": "Make POST request",
                   "type":"http-post",
                   "target": "test_mock",
                   "inputs":{
                      "uri": "/mock/post",
                      "body": "cool buddy"
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

Scenario: Http put request local valid endpoint
    Given an app providing an http endpoint such as PUT on uri /mock/put
    And a configured target test_mock for an app
    And this scenario is saved
"""
    {
        "title": "post scenario !",
        "tags": [],
        "executions": [],
        "content":
        '''
    {
        "scenario": {
            "name": "http client put",
            "steps": [
                {
                   "name": "Make PUT request",
                   "type":"http-put",
                   "target": "test_mock",
                   "inputs":{
                      "uri": "/mock/put",
                      "body": "cool buddy"
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

Scenario: Http put request local valid endpoint
    Given an app providing an http endpoint such as DELETE on uri /mock/delete
    And a configured target test_mock for an app
    And this scenario is saved
"""
    {
        "title": "post scenario !",
        "tags": [],
        "executions": [],
        "content":
        '''
    {
        "scenario": {
            "name": "http client delete",
            "steps": [
                {
                   "name": "Make DELETE request",
                   "type":"http-delete",
                   "target": "test_mock",
                   "inputs":{
                      "uri": "/mock/delete",
                      "body": "cool buddy"
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
