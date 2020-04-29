# language: en
@HttpServerTask
Feature: HTTP server Task test

Scenario: Http post request local server endpoint
    Given a target local_server with url https://localhost:8443/ with security
        |keyStorePath|blackbox/security/client.jks|
        |keyStorePassword|client|
    And existing truststore blackbox/security/truststore.jks
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
                   "name": "Start HTTPS server",
                   "type":"https-server-start",
                   "target": "local_server",
                   "inputs":{
                      "port": "8443",
                      "truststore-path": "%%trustStoreAbsolutePath%%",
                      "truststore-password": "truststore"
                   }
                },
                {
                   "name": "Make POST request",
                   "type":"http-post",
                   "target": "local_server",
                   "inputs":{
                      "uri": "/test",
                      "body": "cool buddy"
                   }
                },
                {
                   "name": "Listens to POST requests",
                   "type":"https-listener",
                   "inputs":{
                      "https-server": "${#httpsServer}"
                      "uri": "/test",
                      "verb": "POST",
                      "expected-message-count": "1"
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
