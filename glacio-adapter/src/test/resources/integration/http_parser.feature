# language: en

Feature: HTTP feature
    Could execute http task as glacio feature

    Scenario: Simple http get
        When Do http-get Request chutney-testing github page
            On GITHUB_API
            With uri /orgs/chutney-testing
            With timeout 2000 s
            With headers
            | X-Extra-Header | An extra header |
            Take statusOk ${200 == #status}
            Take jsonBody ${#json(#body, '$')}
            Keep headersString ${#headers}
        Then Execute debug
