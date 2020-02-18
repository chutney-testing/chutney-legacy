# language: en
@HttpJms
Feature: HTTP then JMS test

Scenario: Http forward to jms server test
    Given a dyanmic target local_server with url https://localhost with security
        |keyStorePath|blackbox/security/client.jks|
        |keyStorePassword|client|
    And existing truststore blackbox/security/truststore.jks
    And a dynamic target jms_broker with url vm://localhost with properties
        | java.naming.factory.initial | org.apache.activemq.jndi.ActiveMQInitialContextFactory |
    And a mock jms endpoint with reference port jms_broker_port with host localhost
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
                   "name": "Start https server",
                   "type":"https-server-start",
                   "target": "local_server",
                   "inputs":{
                      "port": "##local_server_port##",
                      "truststore-path": "%%trustStoreAbsolutePath%%",
                      "truststore-passowrd": "truststore"
                   }
                },
                {
                   "name": "Post request to local server",
                   "type":"http-post",
                   "target": "local_server",
                   "inputs":{
                      "uri": "/test",
                      "body": "cool buddy"
                   },
                   "strategy":{
                        "type": "retry-with-timeout",
                        "parameters":{
                            "retryDelay": "2 sec",
                            "timeOut": "10 sec",
                        }
                    }
                },
                {
                   "name": "Listen for POST requests",
                   "type":"https-listener",
                   "inputs":{
                      "https-server": "${#httpsServer}"
                      "uri": "/test",
                      "verb": "POST",
                      "expected-message-count": "1"
                   }
                },
                {
                   "name": "Send JMS to broker",
                   "type":"jms-sender",
                   "target": "jms_broker",
                   "inputs":{
                      "destination": "dynamicQueues/test",
                      "body": "${#requests[0].bodyAsString}"
                      "headers" : "${#extractHeadersAsMap(#requests[0])}"
                   }
                },
                {
                   "name": "Listen to JMS broker",
                   "type":"jms-listener",
                   "target": "jms_broker",
                   "inputs":{
                      "destination": "dynamicQueues/test"
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
