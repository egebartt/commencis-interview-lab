@mobile @smoke
Feature: Api Demos navigation

  Appium server ve bagli bir Android cihaz/emulator gerektirir.
  Kosum: .\mvnw.cmd clean verify -Pcucumber "-Dcucumber.filter.tags=@mobile and @smoke"

  Ilk uc senaryo is dili adimlarini (Page Object) kullanir; son uc senaryo ayni akislari
  sayfa adi + element adi ile yazilmis genel adimlarla tekrarlar.

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

  Scenario: The Views menu is opened with generic steps
    * Wait for element "ACCESSIBILITY_MENU" in "Api Demos Page"
    * Click to element "VIEWS_MENU" in "Api Demos Page"
    * Verify element "BUTTONS_OPTION" exists in "Api Demos Page"
    * Navigate back
    * Verify element "ACCESSIBILITY_MENU" exists in "Api Demos Page"

  Scenario: A planet is selected with generic steps
    * Click to element "VIEWS_MENU" in "Api Demos Page"
    * Scroll to element "SPINNER_OPTION" and click in "Api Demos Page"
    * Click to element "PLANET_DROPDOWN" in "Api Demos Page"
    * Click to element with text "Jupiter"
    * Check if element "PLANET_DROPDOWN_VALUE" has text "Jupiter" in "Api Demos Page"
    * Save text of element "PLANET_DROPDOWN_VALUE" as "selectedPlanet" in "Api Demos Page"
    * Check if element "PLANET_DROPDOWN_VALUE" contains text "${ctx:selectedPlanet}" in "Api Demos Page"

  Scenario: The monitored switch is toggled with generic steps
    * Click to element "VIEWS_MENU" in "Api Demos Page"
    * Scroll to element "SWITCHES_OPTION" and click in "Api Demos Page"
    * Check element "MONITORED_SWITCH" is not checked in "Api Demos Page"
    * Click to element "MONITORED_SWITCH" in "Api Demos Page"
    * Toast message "Monitored switch is on" should be visible
    * Check element "MONITORED_SWITCH" is checked in "Api Demos Page"
