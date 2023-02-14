# language: en
@TestCaseEditionMetadata
Feature: Support testcase edition metadata

    Background:
        Given A start date
            Do put in context
            | startDate         | ${#now().toInstant()}           |
            | isoFormatter      | ${#isoDateFormatter('instant')} |

    Scenario: Consult new composable testcase metadata
        Given robert has created a composable testcase with metadata
            Do http-post
                On CHUTNEY_LOCAL_NO_USER
                With uri /api/scenario/component-edition
                With headers
                | Content-Type  | application/json;charset=UTF-8                                                         |
                | Authorization | Basic ${T(java.util.Base64).getEncoder().encodeToString(("robert:robert").getBytes())} |
                With body
                """
                {
                    "title": "titre",
                    "description": "a testcase",
                    "tags": [ "first", "second" ],
                    "author": "notCreator",
                    "creationDate": "2020-01-01T12:00:03Z",
                    "updateDate": "2020-02-02T12:00:03Z",
                    "version": 111,
                    "scenario":{
                        "when":{},
                        "thens":[]
                    }
                }
                """
                Take testcaseId ${#body}
                Validate httpStatusCode_200 ${#status == 200}
        When admin consult it
            Do http-get
                On CHUTNEY_LOCAL_NO_USER
                With uri /api/scenario/component-edition/${#testcaseId}
                With headers
                | Authorization | Basic ${T(java.util.Base64).getEncoder().encodeToString(("admin:admin").getBytes())} |
                Validate httpStatusCode_200 ${#status == 200}
        Then Check testcase metadata
            Do json-assert
                With document ${#body}
                With expected
                | $.title        | titre                                            |
                | $.description  | a testcase                                       |
                | $.tags         | ${#json('["FIRST","SECOND"]', '$')}              |
                | $.creationDate | $isEqualDate:2020-01-01T12:00:03Z                |
                | $.author       | robert                                           |
                | $.updateDate   | $isAfterDate:${#isoFormatter.format(#startDate)} |
                | $.version      | 1                                                |

    Scenario: Consult composable testcase metadata after update
        Given robert has created a testcase with metadata
            Do http-post
                On CHUTNEY_LOCAL_NO_USER
                With uri /api/scenario/component-edition
                With headers
                | Content-Type  | application/json;charset=UTF-8                                                         |
                | Authorization | Basic ${T(java.util.Base64).getEncoder().encodeToString(("robert:robert").getBytes())} |
                With body
                """
                {
                    "title": "titre",
                    "description": "a testcase",
                    "tags": [ "first", "second" ],
                    "author": "notCreator",
                    "creationDate": "2020-01-01T12:00:03Z",
                    "updateDate": "2020-02-02T12:00:03Z",
                    "version": 111,
                    "scenario":{
                        "when":{},
                        "thens":[]
                    }
                }
                """
                Take testcaseId ${#body}
                Validate httpStatusCode_200 ${#status == 200}
        And paloma has updated it with metadata
            Do http-post
                On CHUTNEY_LOCAL_NO_USER
                With uri /api/scenario/component-edition
                With headers
                | Content-Type  | application/json;charset=UTF-8                                                         |
                | Authorization | Basic ${T(java.util.Base64).getEncoder().encodeToString(("paloma:paloma").getBytes())} |
                With body
                """
                {
                    "id": "${#testcaseId}",
                    "title": "new Title",
                    "description": "new description",
                    "tags": [ "second", "third" ],
                    "author": "notEditor",
                    "creationDate": "2020-06-01T14:00:00Z",
                    "updateDate": "2001-01-01T00:00:00Z",
                    "version": 1,
                    "scenario":{
                        "when":{},
                        "thens":[]
                    }
                }
                """
                Validate httpStatusCode_200 ${#status == 200}
        When admin consult it
            Do http-get
                On CHUTNEY_LOCAL_NO_USER
                With uri /api/scenario/component-edition/${#testcaseId}
                With headers
                | Authorization | Basic ${T(java.util.Base64).getEncoder().encodeToString(("admin:admin").getBytes())} |
                Validate httpStatusCode_200 ${#status == 200}
        Then Check testcase metadata
            Do json-assert
                With document ${#body}
                With expected
                | $.title        | new Title                                        |
                | $.description  | new description                                  |
                | $.tags         | ${#json('["SECOND","THIRD"]', '$')}              |
                | $.creationDate | $isEqualDate:2020-01-01T12:00:03Z                |
                | $.author       | paloma                                           |
                | $.updateDate   | $isAfterDate:${#isoFormatter.format(#startDate)} |
                | $.version      | 2                                                |

    Scenario: Update composable testcase with wrong version
        Given robert has created a composable testcase with metadata
            Do http-post
                On CHUTNEY_LOCAL_NO_USER
                With uri /api/scenario/component-edition
                With headers
                | Content-Type  | application/json;charset=UTF-8                                                         |
                | Authorization | Basic ${T(java.util.Base64).getEncoder().encodeToString(("robert:robert").getBytes())} |
                With body
                """
                {
                    "title": "titre",
                    "description": "a testcase",
                    "tags": [ "first", "second" ],
                    "author": "notCreator",
                    "creationDate": "2020-01-01T12:00:03Z",
                    "updateDate": "2020-02-02T12:00:03Z",
                    "version": 111,
                    "scenario":{
                        "when":{},
                        "thens":[]
                    }
                }
                """
                Take testcaseId ${#body}
                Validate httpStatusCode_200 ${#status == 200}
        When paloma updates it with wrong version
            Do http-post
                On CHUTNEY_LOCAL_NO_USER
                With uri /api/scenario/component-edition
                With headers
                | Content-Type  | application/json;charset=UTF-8                                                         |
                | Authorization | Basic ${T(java.util.Base64).getEncoder().encodeToString(("paloma:paloma").getBytes())} |
                With body
                """
                {
                    "id": "${#testcaseId}",
                    "title": "new Title",
                    "description": "new description",
                    "tags": [ "second", "third" ],
                    "author": "notEditor",
                    "creationDate": "2020-06-01T14:00:00Z",
                    "updateDate": "2001-01-01T00:00:00Z",
                    "version": 999,
                    "scenario":{
                        "when":{},
                        "thens":[]
                    }
                }
                """
        Then request fails with error status 404
            Do compare
                With actual ${#status}
                With expected 404
                With mode equals
        And message contains "version [666] not found"
            Do compare
                With actual ${#json(#body, '$')}
                With expected version [999] not found
                With mode contains
