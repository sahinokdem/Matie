Feature: Application service critical business rules

  Scenario: User cannot apply to own listing
    Given a listing owned by current user
    When the user creates an application for that listing
    Then a bad request error is thrown containing "cannot apply to your own listing"

  Scenario: User cannot create duplicate active application
    Given a listing owned by another user
    And an active application already exists for same listing and user
    When the user creates an application for that listing
    Then a bad request error is thrown containing "already have an active application"

  Scenario: Listing owner can accept an application
    Given an existing pending application
    When the listing owner updates the application status to ACCEPTED
    Then the application response status should be ACCEPTED

  Scenario: Non-owner cannot reject an application
    Given an existing pending application
    When a non-owner updates the application status to REJECTED
    Then a forbidden error is thrown containing "not authorized"

  Scenario: Applicant can cancel own application
    Given an existing pending application
    When the applicant updates the application status to CANCELLED
    Then the application response status should be CANCELLED

  Scenario: User can apply again if their previous application was REJECTED or CANCELLED
    Given a listing owned by another user
    And a previous application was REJECTED
    When the user creates an application for that listing
    Then the application response status should be PENDING

  Scenario: User can apply again after a CANCELLED application
    Given a listing owned by another user
    And a previous application was CANCELLED
    When the user creates an application for that listing
    Then the application response status should be PENDING

  Scenario: Manual status update to PENDING is forbidden
    Given an existing pending application
    When the applicant manually updates the application status to PENDING
    Then a bad request error is thrown containing "Manual update to PENDING status is not allowed"
