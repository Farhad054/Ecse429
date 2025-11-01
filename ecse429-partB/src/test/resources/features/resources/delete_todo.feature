Feature: Delete a todo
  As a user I want to delete a todo so that I can remove items I no longer need.

  Background:
    Given the API is running
    And a clean slate for this scenario
    And I create a todo titled "Delete-Me" with doneStatus "false" and remember its id

  @normal
  Scenario: Delete then verify it is gone
    When I DELETE that id
    Then the response status is 200 or 204
    And GET that id returns 404

  @alternate
  Scenario: Delete then recreate with same title
    When I DELETE that id
    Then the response status is 200 or 204
    And I create a todo titled "Delete-Me" with doneStatus "false" and description ""
    Then the response status is 200 or 201

  @error
  Scenario: Delete non-existent id is an error
    When I DELETE id "99999"
    Then the response status is 4xx
