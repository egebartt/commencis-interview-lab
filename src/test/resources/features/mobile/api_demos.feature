@mobile @smoke
Feature: Api Demos navigation

  Appium server ve bagli bir Android cihaz/emulator gerektirir.
  Kosum: .\mvnw.cmd clean verify -Pcucumber "-Dcucumber.filter.tags=@mobile and @smoke"

  Scenario: The Views menu is opened
    Given the Api Demos home screen is visible
    When the user opens the Views menu
    Then the Buttons option should be visible
