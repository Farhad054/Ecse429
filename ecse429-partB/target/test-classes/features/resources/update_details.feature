Feature: Update details
  As a user I want to change a todo's title and description so that I can correct or clarify it.

  Background:
    Given the API is running
    And a clean slate for this scenario
    And I create a todo titled "Edit-Me" with doneStatus "false" and remember its id

  @normal
  Scenario: Full update with PUT
    When I PUT to that id with JSON:
      """
      {"title":"Edited","doneStatus":false,"description":"changed"}
      """
    Then the response status is 200
    And GET that id returns title "Edited" and doneStatus "false"
    And GET that id shows description "changed"

  @alternate
  Scenario: Partial amend with POST
    When I POST to that id with JSON:
      """
      {"description":"patched"}
      """
    Then the response status is 200
    And GET that id shows description "patched"

  @error
  Scenario: Partial PUT without title is rejected
    When I PUT to that id with JSON:
      """
      {"description":"no title"}
      """
    Then the response status is 4xx
