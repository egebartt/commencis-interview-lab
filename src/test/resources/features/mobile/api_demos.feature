@mobile
Feature: Api Demos navigation

  Appium server ve bagli bir Android cihaz/emulator gerektirir.
  Kosum: .\mvnw.cmd clean verify "-Dcucumber.filter.tags=@mobile"

  Scenario: The Views menu is opened
    Given the Api Demos home screen is visible
    When the user opens the Views menu
    Then the Buttons option should be visible

  Scenario: A planet is selected from the Spinner dropdown
    Given the Api Demos home screen is visible
    When the user opens the Views menu
    And the user opens the Spinner screen
    And the user selects "Jupiter" from the planet dropdown
    Then the selected planet should be "Jupiter"

  Scenario: The monitored switch is turned on and off
    Given the Api Demos home screen is visible
    When the user opens the Views menu
    And the user opens the Switches screen
    Then the monitored switch should be off
    When the user taps the monitored switch
    Then the "Monitored switch is on" toast should be visible
    And the monitored switch should be on
    When the user taps the monitored switch
    Then the "Monitored switch is off" toast should be visible
    And the monitored switch should be off
