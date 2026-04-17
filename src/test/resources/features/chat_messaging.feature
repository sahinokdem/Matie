Feature: Chat and Messaging system between users

  Background:
    Given chat API test context is initialized
    And a listing exists in the system

  # SCENARIO SET 1: Automatic Conversation Creation
  Scenario: Conversation is automatically created when application is accepted
    Given a pending application exists for the listing
    And I am authenticated as the listing owner
    When I update the application status to "ACCEPTED"
    Then the application status should be "ACCEPTED"
    And a new conversation should be created between the owner and the applicant
    And the conversation should be linked to the listing

  # SCENARIO SET 2: Message Sending and Receiving
  Scenario: Participant can successfully send a message in a conversation
    Given a conversation exists between "User A" and "User B"
    And I am authenticated as "User A"
    When I POST a message "Hello, is the room still available?" to the conversation
    Then the response status should be 201
    And the message content should match "Hello, is the room still available?"
    And the message sender should be "User A"

  # SCENARIO SET 3: Security & Authorization (Critical for Interviews)
  Scenario: Unauthorized user cannot send a message to someone else's conversation
    Given a conversation exists between "User A" and "User B"
    And I am authenticated as "User C" (Unauthorized)
    When I attempt to POST a message to that conversation
    Then the response status should be 403
    And the error message should contain "not a participant"

  Scenario: Unauthorized user cannot view someone else's message history
    Given a conversation exists between "User A" and "User B"
    And I am authenticated as "User C"
    When I attempt to GET messages for that conversation
    Then the response status should be 403

  # SCENARIO SET 4: Conversation Management & UX
  Scenario: User can fetch their list of active conversations
    Given I am authenticated as a user with 3 active conversations
    When I GET "/api/v1/conversations"
    Then the response status should be 200
    And the list should contain 3 conversations
    And each conversation should show a preview of the last message

  Scenario: Fetching message history with pagination
    Given a conversation exists with 25 messages
    And I am authenticated as a participant
    When I GET messages for the conversation with page size 10
    Then the response status should be 200
    And the message list size should be 10
    And the response should indicate that a next page exists
