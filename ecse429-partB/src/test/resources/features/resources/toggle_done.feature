Feature: Toggle completion
  As a user I want to mark a todo done or undone so that its status reflects reality.

  Background:
    Given the API is running
    And a clean slate for this scenario
    And I create a todo titled "Toggle-Me" with doneStatus "false" and remember its id

  @normal
  Scenario: Mark a todo as done
    When I POST to that id with JSON:
      """
      {"doneStatus": true}
      """
    Then the response status is 200
    And GET that id shows doneStatus "true"

  @alternate
  Scenario: Mark a todo as undone
    When I POST to that id with JSON:
      """
      {"doneStatus": false}
      """
    Then the response status is 200
    And GET that id shows doneStatus "false"

  @error
  Scenario: Toggle a non-existent id returns an error
    When I POST to id "99999" with JSON:
      """
      {"doneStatus": true}
      """
    Then the response status is 4xx
