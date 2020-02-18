# language: en
# TODO - Find a simple way to test Selenium tasks
@Ignore
@Selenium
Feature: Selenium Task test

    Scenario: Get url test
        Given a scenario with following selenium actions is saved
        """
        {
            name: Access UI
            type: selenium-get
            inputs: {
                web-driver: "${#webDriver}"
                value: https://localhost:##SERVER_PORT##
            }
        }
        {
            name: Verify that we have handles to play with
            type: assert
            inputs: {
                asserts: [
                    {
                        assert-true: "${#outputGet.length() > 1}"
                    }
                    {
                        assert-true: "${#webDriver.getWindowHandles().size() == 1}"
                    }
                ]
            }
        }
        """
        When last saved scenario is executed
        Then the report status is SUCCESS

    Scenario: Play with tabs (Get in new window, Close and SwitchTo test)
        Given a scenario with following selenium actions is saved
        """
        {
            name: Access UI
            type: selenium-get
            inputs: {
                web-driver: "${#webDriver}"
                value: https://localhost:##SERVER_PORT##
            }
            outputs: {
                firstWindowId: "${#outputGet}"
            }
        }
        {
            name: Switch to current window
            type: selenium-switch-to
            inputs: {
                web-driver: "${#webDriver}"
            }
        }
        {
            name: Access UI in a new tab
            type: selenium-get
            inputs: {
                web-driver: "${#webDriver}"
                selector: new_window
                value: https://localhost:##SERVER_PORT##
            }
            outputs: {
                secondWindowId: "${#outputGet}"
            }
        }
        {
            name: Check we have two tabs with correct window handles
            type: assert
            inputs: {
                asserts: [
                    {
                        assert-true: "${#webDriver.getWindowHandles().size() == 2}"
                    }
                    {
                        assert-true: "${#webDriver.getWindowHandles().contains(#firstWindowId)}"
                    }
                    {
                        assert-true: "${#webDriver.getWindowHandles().contains(#secondWindowId)}"
                    }
                ]
            }
        }
        {
            name: Close the second tab
            type: selenium-close
            inputs: {
                web-driver: "${#webDriver}"
            }
        }
        {
            name: Switch to first tab
            type: selenium-switch-to
            inputs: {
                web-driver: "${#webDriver}"
                switchType: Window
                selector: "${#firstWindowId}"
            }
        }
        {
            name: Check we have only one tab with correct window handle
            type: assert
            inputs: {
                asserts: [
                    {
                        assert-true: "${#webDriver.getWindowHandles().size() == 1}"
                    }
                    {
                        assert-true: "${#webDriver.getWindowHandles().contains(#firstWindowId)}"
                    }
                ]
            }
        }
        """
        When last saved scenario is executed
        Then the report status is SUCCESS

    Scenario: Click test
        Given a scenario with following selenium actions is saved
        """
        {
            name: Access UI
            type: selenium-get
            inputs: {
                web-driver: "${#webDriver}"
                value: https://localhost:##SERVER_PORT##
            }
        }
        {
            name: Log in
            type: selenium-click
            inputs: {
                web-driver: "${#webDriver}"
                selector: "//icg-login//button[@type='submit']"
                by: xpath
            }
        }
        """
        When last saved scenario is executed
        Then the report status is SUCCESS

    Scenario: Get text test
        Given a scenario with following selenium actions is saved
        """
        {
            name: Access UI
            type: selenium-get
            inputs: {
                web-driver: "${#webDriver}"
                value: https://localhost:##SERVER_PORT##
            }
        }
        {
            name: Get validate button text from login page
            type: selenium-get-text
            inputs: {
                web-driver: "${#webDriver}"
                selector: "//icg-login//button[@type='submit']/span"
                by: xpath
                wait: 2
            }
        }
        {
            name: Check text is not empty
            type: assert
            inputs: {
                asserts: [
                    {
                        assert-true: "${#outputGet.length() > 0}"
                    }
                ]
            }
        }
        """
        When last saved scenario is executed
        Then the report status is SUCCESS

    Scenario: Send Text test
        Given a scenario with following selenium actions is saved
        """
        {
            name: Access UI
            type: selenium-get
            inputs: {
                web-driver: "${#webDriver}"
                value: https://localhost:##SERVER_PORT##
            }
        }
        {
            name: Fill in username for login page
            type: selenium-send-keys
            inputs: {
                web-driver: "${#webDriver}"
                selector: "//input[@name='username']"
                by: xpath
                value: MY_ID
                wait: 2
            }
        }
        """
        When last saved scenario is executed
        Then the report status is SUCCESS

    Scenario: Hover on menu and click on one of its items test
        Given a scenario with following selenium actions is saved
        """
        {
            name: Access UI
            type: selenium-get
            inputs: {
                web-driver: "${#webDriver}"
                value: https://localhost:##SERVER_PORT##
            }
        }
        {
            name: Fill in username for login page
            type: selenium-send-keys
            inputs: {
                web-driver: "${#webDriver}"
                selector: "//input[@name='username']"
                by: xpath
                value: user
                wait: 2
            }
        }
        {
            name: Fill in username for login page
            type: selenium-send-keys
            inputs: {
                web-driver: "${#webDriver}"
                selector: "//input[@name='password']"
                by: xpath
                value: user
            }
        }
        {
            name: Log in
            type: selenium-click
            inputs: {
                web-driver: "${#webDriver}"
                selector: "//icg-login//button[@type='submit']"
                by: xpath
            }
        }
        {
            name: Hover on Admin menu and click on the Target item
            type: selenium-hover-then-click
            inputs: {
                web-driver: "${#webDriver}"
                selector: "//button[text()='Admin']"
                menuItemSelector: "//icg-main-menu/nav/ul/ul/div/div/icg-menu-item[2]/li/a[text() = ' Targets ']"
                by: xpath
                wait: 5
            }
        }
        """
        When last saved scenario is executed
        Then the report status is SUCCESS

    Scenario: Scroll to an element
        Given a scenario with following selenium actions is saved
        """
        {
            name: Access UI
            type: selenium-get
            inputs: {
                web-driver: "${#webDriver}"
                value: https://localhost:##SERVER_PORT##
            }
        }
        {
            name: Fill in username for login page
            type: selenium-send-keys
            inputs: {
                web-driver: "${#webDriver}"
                selector: "//input[@name='username']"
                by: xpath
                wait: 2
                value: user
            }
        }
        {
            name: Fill in username for login page
            type: selenium-send-keys
            inputs: {
                web-driver: "${#webDriver}"
                selector: "//input[@name='password']"
                by: xpath
                value: user
            }
        }
        {
            name: Log in
            type: selenium-click
            inputs: {
                web-driver: "${#webDriver}"
                selector: "//icg-login//button[@type='submit']"
                by: xpath
            }
        }
        {
            name: Hover on Admin menu and click on the Documentation item
            type: selenium-hover-then-click
            inputs: {
                web-driver: "${#webDriver}"
                selector: "//button[text()='Admin']"
                by: xpath
                wait: 5
            }
        }
        {
            name: click on the Documentation item
            type: selenium-click
            inputs: {
                web-driver: "${#webDriver}"
                selector: "//icg-main-menu/nav/ul/ul/div/div/icg-menu-item[5]/li/a[text() = ' Documentation ']"
                by: xpath
            }
        }
        {
            name: Scroll to Retry section
            type: selenium-scroll-to
            inputs: {
                web-driver: "${#webDriver}"
                selector: "_retry_avec_timeout"
                by: id
                wait: 10
            }
        }
        """
        When last saved scenario is executed
        Then the report status is SUCCESS
