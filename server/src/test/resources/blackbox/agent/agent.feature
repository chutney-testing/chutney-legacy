# language: en
@Agent
Feature: Agent test

Agent A (main) -> Agent B
We are the agent B. The agent A send us its network configuration in order we save it.

Scenario: We receive a network configuration to persist
    When network configuration with target FAKE_TARGET with url http://fake_url:1234/fake is received
    Then target FAKE_TARGET is saved locally
