@mobile @interview
Feature: Interview mobile example

  Scenario: Views menu is opened with an Interview Page Object
    Given the Interview home screen is visible
    When the user opens Views on the Interview page
    Then the Interview Buttons option should be visible
