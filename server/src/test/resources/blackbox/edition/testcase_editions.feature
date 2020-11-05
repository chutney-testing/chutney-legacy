# language: en
@TestCaseEdition
Feature: Support testcase editions

    Scenario: Request testcase edition
        Given paloma requests an edition on an existing testcase
        And robert requests an edition on the same testcase
        When admin consults the current editions of this testcase
        Then paloma,robert are seen as current editors

    Scenario: Request for a second time testcase edition
        Given paloma requests an edition on an existing testcase
        When paloma requests an edition on the same testcase
        Then the edition received is the first one

    Scenario: End testcase edition
        Given paloma requests an edition on an existing testcase
        When paloma ends its edition
        Then paloma cannot be seen as current editor

    Scenario: Edition time to live
        Given paloma requests an edition on an existing testcase
        When edition lasts beyond defined ttl
        Then paloma cannot be seen as current editor
