@mobile @interview
Feature: My Demo App catalog and webview

  Scenario: Products are sorted by price and the Commencis website is opened in the webview
    Given the catalog screen is visible
    When the products are sorted by price ascending
    Then the visible products should be listed from the cheapest to the most expensive
    When the 3rd product is opened
    Then the opened product should be the selected one
    When the WebView screen is opened from the menu
    And "https://www.commencis.com" is loaded in the webview
    Then the webview context and page should be opened
    When the menu is opened
    Then the menu should be displayed
