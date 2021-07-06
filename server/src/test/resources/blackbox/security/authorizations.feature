# language: en

Feature: Roles declarations and users associations

    Scenario: Declare a new role with its authorizations
        Given A new role
            Do context-put
                With entries
                | newRole | ${#jsonPath('{"name": "NEW_ROLE", "rights":["SCENARIO_READ", "CAMPAIGN_READ", "COMPONENT_READ"]}', "$")} |
        When Add it to current roles and authorizations
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
                Do http-post Post roles and authorizations to Chutney instance
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
            Ask for current roles and authorizations
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
                | $.roles[?(@.name=='NEW_ROLE')].rights | $value:${#jsonPath(#newRole, "$.rights")} |

    Scenario: Add and remove user to/from an existing role
        Given A role not given to user
            Do context-put
                With entries
                | userName | user |
                | roleNameWithNoUser | NO_USER_ROLE |
                Take roleUserAuthorizations ${#jsonPath('{"name": "'+#roleNameWithNoUser+'", "users":["'+#userName+'"]}', "$")}
            Ask for current roles and authorizations
                Do http-get
                    On CHUTNEY_LOCAL
                    With uri /api/v1/authorizations
                    Take currentAuthorizations ${#body}
                Do compare Assert HTTP status is 200
                    With actual ${#status}
                    With expected 200
                    With mode equals
            Do json-assert Validate the role
                With document ${#currentAuthorizations}
                With expected
                | $.roles[?(@.name=='${#roleNameWithNoUser}')] | $isNotNull |
                | $.authorizations[?(@.name=='${#roleNameWithNoUser}')].users | $isEmpty |
                Take roleAuthorizations ${#jsonPath(#currentAuthorizations, "$.roles[?(@.name=='"+#roleNameWithNoUser+"')].rights[*]")}
        When Add user to role
            Do http-post Post roles and authorizations to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/v1/authorizations
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body
                """
                {
                    "roles": ${#jsonPath(#currentAuthorizations, '$.roles')},
                    "authorizations": ${#jsonSerialize(#jsonPath(#currentAuthorizations, "$.authorizations[?(@.name!='"+#roleNameWithNoUser+"')]").appendElement(#roleUserAuthorizations))}
                }
                """
            Do compare Assert HTTP status is 200
                With actual ${#status}
                With expected 200
                With mode equals
        Then It must be read back
            Ask for current roles and authorizations
                Do http-get
                    On CHUTNEY_LOCAL
                    With uri /api/v1/authorizations
                    Take readAuthorizations ${#body}
                Do compare Assert HTTP status is 200
                    With actual ${#status}
                    With expected 200
                    With mode equals
            Do json-assert Validate the user existence
                With document ${#readAuthorizations}
                With expected
                | $.authorizations[?(@.name=='${#roleNameWithNoUser}')].users[0] | $value:${#userName} |
        And Check user authority
            Do http-get
                On CHUTNEY_LOCAL_NO_USER
                With headers
                | Content-Type  | application/json;charset=UTF-8                                                             |
                | Authorization | Basic ${T(java.util.Base64).getEncoder().encodeToString(("user:user").getBytes())} |
                With uri /api/v1/user
            Do compare
                With actual ${#status}
                With expected 200
                With mode equals
            Do json-assert
                With document ${#body}
                With expected
                | $.authorizations | ${#roleAuthorizations} |
        When Remove user from role
            Do http-post Post roles and authorizations to Chutney instance
                On CHUTNEY_LOCAL
                With uri /api/v1/authorizations
                With headers
                | Content-Type | application/json;charset=UTF-8 |
                With body ${#currentAuthorizations}
            Do compare Assert HTTP status is 200
                With actual ${#status}
                With expected 200
                With mode equals
        And Check user authority
            Do http-get
                On CHUTNEY_LOCAL_NO_USER
                With headers
                | Content-Type  | application/json;charset=UTF-8                                                             |
                | Authorization | Basic ${T(java.util.Base64).getEncoder().encodeToString(("user:user").getBytes())} |
                With uri /api/v1/user
            Do compare
                With actual ${#status}
                With expected 200
                With mode equals
            Do json-assert
                With document ${#body}
                With expected
                | $.authorizations | $isEmpty |
