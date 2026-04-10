Feature: Listing management critical scenarios

  Background:
    Given listing API test context is initialized

  # SCENARIO SET 1: Room Available (House Owner)
  Scenario: Successful room-available listing creation
    Given I am authenticated as listing owner
    And I have a valid room-available request payload
    And listing service returns a room-available listing response
    When I POST to "/api/v1/listings/room-available"
    Then the response status should be 201
    And the response listingType should be "ROOM_AVAILABLE"

  Scenario: Validation fails when address is missing for room-available
    Given I am authenticated as listing owner
    And I have a room-available request payload without address
    When I POST to "/api/v1/listings/room-available"
    Then the response status should be 400

  Scenario: Validation fails when rentAmount is negative for room-available
    Given I am authenticated as listing owner
    And I have a room-available request payload with invalid rentAmount
    When I POST to "/api/v1/listings/room-available"
    Then the response status should be 400

  Scenario: Non-owner cannot update room listing
    Given I am authenticated as non-owner user
    And listing service rejects room update with forbidden
    And I have a valid room update payload
    When I PUT to "/api/v1/listings/{id}"
    Then the response status should be 403

  Scenario: Non-owner user receives 403 when trying to delete someone else's listing
    Given I am authenticated as non-owner user
    And listing service rejects delete with forbidden
    When I DELETE to "/api/v1/listings/{id}"
    Then the response status should be 403

  # SCENARIO SET 2: Roommate Wanted (Looking for Room)
  Scenario: Successful roommate-wanted listing creation
    Given I am authenticated as listing owner
    And I have a valid roommate-wanted request payload
    And listing service returns a roommate-wanted listing response
    When I POST to "/api/v1/listings/roommate-wanted"
    Then the response status should be 201
    And the response listingType should be "ROOMMATE_WANTED"

  Scenario: Roommate-wanted accepts null house fields
    Given I am authenticated as listing owner
    And I have a valid roommate-wanted request payload
    And listing service returns a roommate-wanted listing response
    When I POST to "/api/v1/listings/roommate-wanted"
    Then the response status should be 201
    And the response should not contain field "address"
    And the response should not contain field "rentAmount"

  Scenario: Filter listings by roommate-wanted type
    Given listing service returns only roommate-wanted listings for type filter
    When I GET "/api/v1/listings?type=ROOMMATE_WANTED"
    Then the response status should be 200
    And all response listing types should be "ROOMMATE_WANTED"

  # SCENARIO SET 3: Photo Management
  Scenario: Owner can add photo to listing
    Given I am authenticated as listing owner
    And listing service returns a created listing photo
    And I have a valid add-photo payload
    When I POST to "/api/v1/listings/{id}/photos"
    Then the response status should be 201
    And the response photoUrl should be "https://cdn.example.com/photo-1.jpg"

  Scenario: Photos are returned in correct display order
    Given listing service returns listing details with ordered photos
    When I GET "/api/v1/listings/{id}"
    Then the response status should be 200
    And the response photos should be ordered by displayOrder

  Scenario: Owner can remove photo from listing
    Given I am authenticated as listing owner
    And listing service allows removing listing photo
    When I DELETE to "/api/v1/listings/{id}/photos/{photoId}"
    Then the response status should be 204

  Scenario: Non-owner cannot add or remove photo
    Given I am authenticated as non-owner user
    And listing service rejects photo operations with forbidden
    And I have a valid add-photo payload
    When I POST to "/api/v1/listings/{id}/photos"
    Then the response status should be 403
    When I DELETE to "/api/v1/listings/{id}/photos/{photoId}"
    Then the response status should be 403

  Scenario: Soft-deleted listing photo operations are handled correctly
    Given I am authenticated as listing owner
    And listing service allows soft delete but photo operations return not found
    And I have a valid add-photo payload
    When I DELETE to "/api/v1/listings/{id}"
    Then the response status should be 204
    When I POST to "/api/v1/listings/{id}/photos"
    Then the response status should be 404

  # SCENARIO SET 4: Complex Business Rules
  Scenario: Pagination returns size and next-page metadata
    Given listing service has 25 active listings and page size 10
    When I GET "/api/v1/listings?size=10&page=0"
    Then the response status should be 200
    And the response content size should be 10
    And the response should indicate next page exists

  Scenario: Soft deleted listing does not appear in active results
    Given listing service excludes soft-deleted listings from default query
    When I GET "/api/v1/listings"
    Then the response status should be 200
    And the response content size should be 2

  Scenario: City filter returns only Izmir listings
    Given listing service has listings in Izmir and Istanbul
    When I GET "/api/v1/listings?city=Izmir"
    Then the response status should be 200
    And the response content size should be 2
