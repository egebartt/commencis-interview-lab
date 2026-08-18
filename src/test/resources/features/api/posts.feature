@api
Feature: Posts API

  Adres, govde ve parametreler senaryodan gelir; endpoint basina Java sinifi yazilmaz.
  Base URL config.properties icindeki api.base.url degerinden okunur.

  Kosum: .\mvnw.cmd clean verify

  Scenario: An existing post is fetched
    When I send GET to "/posts/1"
    Then the response status should be 200
    And the response time should be under 10000 ms
    And the response field "id" should be "1"

  Scenario: A new post is created from a JSON file
    Given the request body from file "testdata/create-post.json"
    When I send POST to "/posts"
    Then the response status should be 201
    And the response fields should be:
      | title  | Commencis interview lab |
      | userId | 7                       |
    And the response field "id" should not be null

  Scenario: The body is written directly in the scenario
    Given the request headers:
      | X-Request-Id | commencis-interview |
    And the request body:
      """
      {
        "userId": 7,
        "title": "Inline body",
        "body": "Senaryonun icine yazildi"
      }
      """
    When I send POST to "/posts"
    Then the response status should be 201
    And the response field "title" should be "Inline body"

  Scenario: A value from the first response is used in the second request
    When I send GET to "/posts/1"
    Then the response status should be 200
    And I save response field "id" as "postId"
    Given the request body:
      """
      { "title": "Updated from context", "userId": 7 }
      """
    When I send PUT to "/posts/${ctx:postId}"
    Then the response status should be 200
    And the response field "title" should be "Updated from context"

  Scenario: Query parameters are sent without hand-built URLs
    Given the query params:
      | postId | 1 |
    When I send GET to "/comments"
    Then the response status should be 200
    And the response field "[0].postId" should be "1"

  Scenario: A scenario-specific base url overrides the configuration
    Given the base url is "https://jsonplaceholder.typicode.com"
    When I send GET to "/users/1"
    Then the response status should be 200
    And the response field "username" should be "Bret"

  Scenario: A record is partially updated
    Given the request body:
      """
      { "title": "Partially updated interview post" }
      """
    When I send PATCH to "/posts/1"
    Then the response status should be 200
    And the response field "title" should be "Partially updated interview post"

  Scenario: A record is deleted
    When I send DELETE to "/posts/1"
    Then the response status should be 200

  Scenario: A missing record returns not found
    When I send GET to "/posts/999999"
    Then the response status should be 404

  # Veri odakli kosum icin ayri bir CSV katmani gerekmez: Cucumber'in kendi Examples tablosu.
  Scenario Outline: Posts are fetched for several ids
    When I send GET to "/posts/<id>"
    Then the response status should be 200
    And the response field "userId" should be "<userId>"

    Examples:
      | id | userId |
      | 1  | 1      |
      | 12 | 2      |
      | 25 | 3      |
