@mobile
Feature: Views Controls form

  Views > Controls > 1. Light Theme ekranindaki form kontrolleri.
  Dogrulamalar formun ic tutarliligina bakar: radio grubunda tek secim, toggle etiketinin
  durumla eslesmesi gibi.

  # Ortak acilis adimi bilerek Background'a alinmadi, her senaryoda tekrar ediliyor:
  # Bar2 Report plugin'i (0.9.0) Cucumber JSON'daki "background" element'ini ayri bir senaryo
  # sayiyor ve tek senaryoluk raporda iki kayit gorunce run manifest'ini yazamiyor
  # ("Run record could not be saved"). Plugin duzelince Background'a geri donulebilir.

  Scenario: Text, checkbox, radio and toggle work together
    Given the Controls screen is open
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
    Given the Controls screen is open
    Then the selected Controls planet should be "Mercury"
    When the user selects "Jupiter" from the Controls planet dropdown
    Then the selected Controls planet should be "Jupiter"

  Scenario: The disabled Save button stays disabled
    Given the Controls screen is open
    Then the enabled Save button should be clickable
    And the disabled Save button should not be clickable
