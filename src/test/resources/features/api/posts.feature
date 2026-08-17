@api @smoke
Feature: Posts API

  Adres, govde ve parametreler senaryodan gelir; endpoint basina Java sinifi yazilmaz.
  Base URL aktif ortam dosyasindan okunur (config/env/<environment>.properties).

  Scenario: An existing post is fetched
    When I send GET to "/posts/1"
    Then the response status should be 200
    And the response time should be under 10000 ms
    And the response field "id" should be "1"

  Scenario: A new post is created from a JSON file
    Given the request body from json "testdata/json/create-post.json"
    When I send POST to "/posts"
    Then the response status should be 201
    And the response fields should be:
      | title  | Commencis interview lab |
      | userId | 7                       |
    And the response field "id" should not be null

  Scenario: A post is created from a CSV row and the result is stored
    Given the request body from csv "testdata/csv/posts.csv" where "case" is "happy_path"
    When I send POST to "/posts"
    Then the response status should be 201
    And I save the response to csv "created-posts.csv" with fields "id,title,userId"

  Scenario: The body is written directly in the scenario
    Given the request headers:
      | X-Request-Id | commencis-interview |
    And the request body:
      """
      {
        "userId": 7,
        "title": "Inline body",
        "body": "Written in the scenario"
      }
      """
    When I send POST to "/posts"
    Then the response status should be 201
    And the response field "title" should be "Inline body"

  Scenario: A value from the first response is used in the second request
    When I send GET to "/posts/1"
    Then the response status should be 200
    And I save response field "id" as "postId"
    And the request body from table:
      | title  | Updated from context |
      | userId | 7                    |
    When I send PUT to "/posts/${ctx:postId}"
    Then the response status should be 200
    And the response field "title" should be "Updated from context"

  Scenario: Query parameters are sent without hand-built URLs
    Given the query params:
      | postId | 1 |
    When I send GET to "/comments"
    Then the response status should be 200
    And the response field "[0].postId" should be "1"

  Scenario: A scenario-specific base url overrides the environment
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

  Scenario: A record is deleted with a manual header
    Given the request headers:
      | X-Request-Id | interview-delete-1 |
    When I send DELETE to "/posts/1"
    Then the response status should be 200

  Scenario: A full url is used instead of the configured base url
    When I send GET to "https://jsonplaceholder.typicode.com/posts/1"
    Then the response status should be 200
    And the response field "id" should be "1"

  Scenario: A missing record returns not found
    When I send GET to "/posts/999999"
    Then the response status should be 404
