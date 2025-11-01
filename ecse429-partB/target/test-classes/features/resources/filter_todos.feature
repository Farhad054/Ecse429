Feature: Search and filter
  As a user I want to filter todos by status and title so that I can find items quickly.

  Background:
    Given the API is running
    And a clean slate for this scenario
    And I create a todo titled "S2-C" with doneStatus "false"
    And I create a todo titled "S2-D" with doneStatus "true"

  @normal
  Scenario: Filter by doneStatus
    When I GET path "/todos" with query "doneStatus=false"
    Then every item in the response has doneStatus "false"

  @alternate
  Scenario: Filter by title and status
    When I GET path "/todos" with query "doneStatus=true&title=S2-D"
    Then the response contains exactly 1 item titled "S2-D"

  @error
  Scenario: Unknown filter parameter is tolerated
    When I GET path "/todos" with query "foo=bar"
    Then the response status is 200
