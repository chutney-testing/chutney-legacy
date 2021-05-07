# language: en

Feature: Roles declarations and users associations

    Scenario: Declare a new role with its authorizations
        Given A new role
            Do context-put
                With entries
                | newRole | ${#jsonPath('{"name": "NEW_ROLE", "rights":["SCENARIO_READ", "CAMPAIGN_READ", "COMPONENT_READ"]}', '$')} |
        When Add it to current authorizations
            Ask for current roles
                Do http-get
                    On CHUTNEY_LOCAL
                    With uri /api/v1/authorizations
                    Take currentAuthorizations ${#body}
                Do compare Assert HTTP status is 200
                    With actual ${#status}
                    With expected 200
                    With mode equals
            Add the new role
                Do http-post Post authorizations to Chutney instance
                    On CHUTNEY_LOCAL
                    With uri /api/v1/authorizations
                    With headers
                    | Content-Type | application/json;charset=UTF-8 |
                    With body
                    """
                    {
                        "roles": ${#jsonSerialize(#jsonPath(#currentAuthorizations, '$.roles').appendElement(#newRole))},
                        "authorizations": ${#jsonPath(#currentAuthorizations, '$.authorizations')}
                    }
                    """
                Do compare Assert HTTP status is 200
                    With actual ${#status}
                    With expected 200
                    With mode equals
        Then It must be read back
            Ask for current roles
                Do http-get
                    On CHUTNEY_LOCAL
                    With uri /api/v1/authorizations
                    Take readAuthorizations ${#body}
                Do compare Assert HTTP status is 200
                    With actual ${#status}
                    With expected 200
                    With mode equals
            Do json-assert Validate the new role existence
                With document ${#readAuthorizations}
                With expected
                | $.roles[?(@.name=='NEW_ROLE')].rights | $value:${#jsonPath(#newRole, '$.rights')} |
