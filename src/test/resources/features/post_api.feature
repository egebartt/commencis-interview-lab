@api @smoke
Feature: Posts API

  Cihaz gerektirmeyen API senaryolari. Adimlar PostApi client'ini cagirir.

  Scenario: An existing post is fetched
    When post 1 is requested
    Then the response status should be 200
    And the response field "id" should be "1"

  Scenario: A new post is created from a JSON file
    Given the request body is loaded from "testdata/create-post.json"
    When the post is created
    Then the response status should be 201
    And the response field "title" should be "Commencis interview lab"
    And the response field "id" should not be null
