# language: en
Feature: Execution with jsonPath function

Scenario: Scenario execution with simple json value extraction
    Given this scenario is saved
"""
    {
        "title": "post scenario !",
        "tags": [],
        "executions": [],
        "content":
        '''
    {
        "scenario":
        {
            "name": "json scenario",
            "steps":
            [
                {
                   "name": "Put JSON value in context",
                   "type":"context-put",
                   "inputs":{
                      "entries":{
                        "content":"{\"field1\": \"value1\"}"
                      }
                   }
                },
                {
                   "name": "Put value in context with JSON extraction",
                   "type":"context-put",
                   "inputs": {
                      "entries": {
                            "extracted": "${#json(#content, '$.field1')}"
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
    Then the extracted value is 'Output: (extracted) : (value1)'

Scenario: Scenario execution with multiple json value extraction
    Given this scenario is saved
    """
    {
        "title": "post scenario !",
        "tags": [],
        "executions": [],
        "content":
        '''
    {
        "scenario":
        {
            "name": "json scenario",
            "steps":
            [
                {
                   "name": "Put JSON value in context",
                   "type":"context-put",
                   "inputs":{
                      "entries":{
                        "content": "{\"field1\": \"value1\",\"field2\": {\"field1\": \"value1\"}}"
                      }
                   }
                },
                {
                   "name": "Put value in context with JSON extraction",
                   "type":"context-put",
                   "inputs":{
                      "entries": {
                        "extracted": "${#json(#content, '$..field1')}"
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
    Then the extracted value is 'Output: (extracted) : (["value1","value1"])'

Scenario: Scenario execution with json object value extraction
    Given this scenario is saved
"""
    {
        "title": "post scenario !",
        "tags": [],
        "executions": [],
        "content":
        '''
    {
        "scenario":
        {
            "name": "json scenario",
            "steps":
            [
                {
                   "name": "Put JSON value in context",
                   "type":"context-put",
                   "inputs":{
                      "entries": {
                        "content":"{\"field1\": \"value1\"}"
                      }
                   }
                },
                {
                   "name": "Put value in context with JSON extraction",
                   "type":"context-put",
                   "inputs":{
                      "entries": {
                        "extracted": "${#json(#content, '$')}"
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
    Then the extracted value is 'Output: (extracted) : ({field1=value1})'
