# language: en

Feature: HTTP feature
    Could execute http task as glacio feature

    Scenario: Simple http get
        When Do (http-get) Request google search page
            On google
            With uri /actuator/info
            With timeout 1000 s
            With headers
            | X-MGN-Header | MGN MGNopâze^jaezMGN MGN |
            statusOk ${200 == #status}
            jsonBody ${#json(#body, '$')}
            headersString ${#headers}
        Then Execute debug

 Scenario: mgn
        When toto
        Then Do something
    With simple simple_value
    With list of simple values
    | value1 | value2 | value3 |
    With list of list of simple values
    | value1 | value2 | value3 |
    | value1 | value2 | value3 |
    | value1 | value2 | value3 |
    With map of simple values
    |      |
    | key1 | value1 |
    | key2 | value2 |
    | key3 | value3 |
    With map of list of simple values
    |      |
    | key1 | value1 | value1' |
    | key2 | value2 | value2' |
    | key3 | value3 | value3' |
    With map of map of simple values
    | key1 |
    |      | subKey1 | subValue1 |
    |      | subKey2 | subValue2 |
    | key2 |
    |      | subKey1 | subValue1 |
    |      | subKey2 | subValue2 |

    |      | subKey1   | subKey2    |
    | key1 | subValue1 | subValue1' |
    | key2 |         |           |
    |      | subKey1 | subValue1 |
    |      | subKey2 | subValue2 |
