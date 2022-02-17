# language: en
@TestCaseEdition
Feature: Support testcase editions

    Background:
        Given A start date
            Do put in context
            | startDate         | ${#now().toInstant()}                        |
            | isoFormatter      | ${#isoDateFormatter('instant')} |
        And An existing testcase
            Do http-post
                On CHUTNEY_LOCAL
                With uri /api/scenario/v2
                With headers
                | Content-Type  | application/json;charset=UTF-8 |
                With body
                """
                {
                    "title": "titre",
                    "scenario":{
                        "when":{},
                        "thens":[]
                    }
                }
                """
                Take testcaseId ${#body}
                Validate httpStatusCode_200 ${#status == 200}

    Scenario: Request testcase edition
        Given paloma requests an edition on an existing testcase
            Do http-post
                On CHUTNEY_LOCAL_NO_USER
                With uri /api/v1/editions/testcases/${#testcaseId}
                With headers
                | Authorization | Basic ${T(java.util.Base64).getEncoder().encodeToString(("paloma:paloma").getBytes())} |
                Validate httpStatusCode_200 ${#status == 200}
        And robert requests an edition on the same testcase
            Do http-post
                On CHUTNEY_LOCAL_NO_USER
                With uri /api/v1/editions/testcases/${#testcaseId}
                With headers
                | Authorization | Basic ${T(java.util.Base64).getEncoder().encodeToString(("robert:robert").getBytes())} |
                Validate httpStatusCode_200 ${#status == 200}
        When admin consults the current editions of this testcase
            Do http-get
                On CHUTNEY_LOCAL_NO_USER
                With uri /api/v1/editions/testcases/${#testcaseId}
                With headers
                | Authorization | Basic ${T(java.util.Base64).getEncoder().encodeToString(("admin:admin").getBytes())} |
                Take currentEditions ${#body}
                Validate httpStatusCode_200 ${#status == 200}
        Then paloma and robert are seen as current editors
            Do json-assert Check paloma's edition
                With document ${#jsonSerialize(#json(#currentEditions, "$[?(@.editionUser=='paloma')]").get(0))}
                With expected
                | $.testCaseId       | ${#testcaseId}                                   |
                | $.testCaseVersion  | 1                                                |
                | $.editionStartDate | $isAfterDate:${#isoFormatter.format(#startDate)} |
            Do json-assert Check robert's edition
                With document ${#jsonSerialize(#json(#currentEditions, "$[?(@.editionUser=='robert')]").get(0))}
                With expected
                | $.testCaseId       | ${#testcaseId}                                   |
                | $.testCaseVersion  | 1                                                |
                | $.editionStartDate | $isAfterDate:${#isoFormatter.format(#startDate)} |

    Scenario: Request for a second time testcase edition
        Given paloma requests an edition on an existing testcase
            Do http-post
                On CHUTNEY_LOCAL_NO_USER
                With uri /api/v1/editions/testcases/${#testcaseId}
                With headers
                | Authorization | Basic ${T(java.util.Base64).getEncoder().encodeToString(("paloma:paloma").getBytes())} |
                Take firstEdition ${#body}
                Validate httpStatusCode_200 ${#status == 200}
        When paloma requests an edition on the same testcase
            Do http-post
                On CHUTNEY_LOCAL_NO_USER
                With uri /api/v1/editions/testcases/${#testcaseId}
                With headers
                | Authorization | Basic ${T(java.util.Base64).getEncoder().encodeToString(("paloma:paloma").getBytes())} |
                Take secondEdition ${#body}
                Validate httpStatusCode_200 ${#status == 200}
        Then the edition received is the first one
            Do json-compare
                With document1 ${#firstEdition}
                With document2 ${#secondEdition}

    Scenario: End testcase edition
        Given paloma requests an edition on an existing testcase
            Do http-post
                On CHUTNEY_LOCAL_NO_USER
                With uri /api/v1/editions/testcases/${#testcaseId}
                With headers
                | Authorization | Basic ${T(java.util.Base64).getEncoder().encodeToString(("paloma:paloma").getBytes())} |
                Validate httpStatusCode_200 ${#status == 200}
        When paloma ends its edition
            Do http-delete
                On CHUTNEY_LOCAL_NO_USER
                With uri /api/v1/editions/testcases/${#testcaseId}
                With headers
                | Authorization | Basic ${T(java.util.Base64).getEncoder().encodeToString(("paloma:paloma").getBytes())} |
                Validate httpStatusCode_200 ${#status == 200}
        Then paloma cannot be seen as current editor
            Consults the current editions of this testcase
                Do http-get
                    On CHUTNEY_LOCAL
                    With uri /api/v1/editions/testcases/${#testcaseId}
                    Take currentEditions ${#body}
                    Validate httpStatusCode_200 ${#status == 200}
            Do json-assert Check paloma's edition inexistence
                With document ${#currentEditions}
                With expected
                | $[?(@.editionUser=='paloma')] | $isNull |

    Scenario: Edition time to live
        Given paloma requests an edition on an existing testcase
            Do http-post
                On CHUTNEY_LOCAL_NO_USER
                With uri /api/v1/editions/testcases/${#testcaseId}
                With headers
                | Authorization | Basic ${T(java.util.Base64).getEncoder().encodeToString(("paloma:paloma").getBytes())} |
                Validate httpStatusCode_200 ${#status == 200}
        When edition lasts beyond defined ttl
            Do wait for 2500 ms
        Then paloma cannot be seen as current editor
            Consults the current editions of this testcase
                Do http-get
                    On CHUTNEY_LOCAL
                    With uri /api/v1/editions/testcases/${#testcaseId}
                    Take currentEditions ${#body}
                    Validate httpStatusCode_200 ${#status == 200}
            Do json-assert Check paloma's edition inexistence
                With document ${#currentEditions}
                With expected
                | $[?(@.editionUser=='paloma')] | $isNull |
