@api @interview
Feature: Contact List user lifecycle

  Scenario: A user is created, updated and deleted
    Given a random Contact List user is prepared
    When the user is created
    Then the user should be created successfully
    When the current user is requested
    Then the created user information should be returned
    When the user's first name and last name are changed to random values
    Then the user should be updated successfully
    When the current user is requested
    Then the updated user information should be returned
    When the current user is deleted
    Then the user should be deleted successfully
    Then the deleted user should no longer be accessible
