# language: en

Feature: HTTP feature
    Could execute http task as glacio feature

    Scenario: Simple http get
        When Do (http-get) Request google search page
            On google <url>
            With uri /actuator/info
            With timeout 1000 s
            With headers
            | X-MGN-Header | MGN MGNopâze^jaezMGN MGN |
            statusOk ${200 == #status}
            jsonBody ${#json(#body, '$')}
            headersString ${#headers}
        Then Execute debug
