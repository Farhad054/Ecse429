Feature: Create todo
  As a user I want to add a todo so that I can track a new task.

  Background:
    Given the API is running
    And a clean slate for this scenario

  @normal
  Scenario Outline: Create a todo with valid data
    When I create a todo titled "<title>" with doneStatus "<done>" and description "<desc>"
    Then the response status is 200 or 201
    And I remember the created id
    And GET that id returns title "<title>" and doneStatus "<done>"
    And GET that id shows description "<desc>"

    Examples:
      | title | done  | desc        |
      | S2-A  | false | first item  |
      | S2-B  | true  | urgent work |

  @alternate
  Scenario: Create with minimal required fields
    When I create a todo titled "Minimal" with doneStatus "false" and description ""
    Then the response status is 200 or 201
    And I remember the created id
    And GET that id returns title "Minimal" and doneStatus "false"

  @error
  Scenario: Reject create without title
    When I create a todo titled "" with doneStatus "false" and description "x"
    Then the response status is 4xx
