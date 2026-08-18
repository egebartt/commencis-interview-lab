@mobile
Feature: Views Controls form

  Views > Controls > 1. Light Theme ekranindaki form kontrolleri.
  Dogrulamalar formun ic tutarliligina bakar: radio grubunda tek secim, toggle etiketinin
  durumla eslesmesi gibi.

  Background:
    Given the Controls screen is open

  Scenario: Text, checkbox, radio and toggle work together
    When the user types "Commencis" into the text field
    Then the first checkbox should be unchecked
    When the user taps the first checkbox
    And the user selects the second radio button
    And the user taps the first toggle
    Then the text field should contain "Commencis"
    And the first checkbox should be checked
    And only the second radio button should be selected
    And the first toggle should be on and labelled "ON"

  Scenario: The selected planet is shown in the dropdown
    Then the selected Controls planet should be "Mercury"
    When the user selects "Jupiter" from the Controls planet dropdown
    Then the selected Controls planet should be "Jupiter"

  Scenario: The disabled Save button stays disabled
    Then the enabled Save button should be clickable
    And the disabled Save button should not be clickable
