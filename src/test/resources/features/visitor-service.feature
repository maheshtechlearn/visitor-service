Feature: Visitor Management

  Scenario: Add a new visitor
    Given a visitor named "Jane Doe" with contact number "0987654321" and email "jane.doe@example.com"
    And the visitor has a purpose "Conference"
    When the visitor is added
    Then the visitor should be added successfully

  Scenario: Retrieve an existing visitor
    Given a visitor with ID from the first scenario exists
    When the user requests the visitor
    Then the visitor should be returned with name "Jane Doe"

  Scenario: Update a visitor
    Given a visitor with ID from the first scenario exists
    When the user updates the visitor's name to "Jane Smith"
    Then the visitor's name should be updated to "Jane Smith"

  Scenario: Delete a visitor
    Given a visitor with ID from the first scenario exists
    When the user deletes the visitor
    Then the visitor should no longer exist

  Scenario: Get all visitors sorted by check-in
    Given visitors exist in the system
    When the user requests all visitors sorted by check-in
    Then the visitors should be returned in order of check-in time and ID

  Scenario: Get approved visitors
    Given visitors exist in the system
    When the user requests approved visitors
    Then only approved visitors should be returned

  Scenario: Group visitors by purpose
    Given visitors exist in the system
    When the user requests visitors grouped by purpose
    Then visitors should be grouped by their purpose with null purposes last

  Scenario: Calculate total visit duration
    Given visitors exist in the system
    When the user requests the total visit duration
    Then the total duration should be calculated and returned

  Scenario: Get unique contact numbers
    Given visitors exist in the system
    When the user requests unique contact numbers
    Then unique contact numbers should be returned


