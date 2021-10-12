Feature: test the application

  @Generic
  Scenario: POST request to an invalid endpoint
    When I hit "https://localhost:9044/no_endpoint" with an empty POST request
    Then I should get a 404 response
    And The response payload contains the following text : "HTTP 404 Not Found"

  @Generic
  Scenario: POST request to correct endpoint with malformed JSON
    When I hit "https://localhost:9044/submission" with the a JSON string of "{bad, bad, bad}"
    Then I should get a 500 response
    And The response payload contains the following text : "Unable to process request"

  @Generic
  Scenario: POST request to correct endpoint with invalid json items, should fail deserialisation
    When I hit the service url "https://localhost:9044/submission" with the following json body
      | msg_id | "access-to-work.submission.new" |
      | bad_bad_field | "aaa123" |
      | date_submitted  | "2017-02-19T12:46:31Z" |
      | applicant      | {}                     |
      | data_capture      | {}                |
      | declaration       | "i declare ok"      |
      | tags              | []                |
    Then I should get a 400 response
    And The response payload contains the following text : "Payload contains invalid items"

  @Generic
  Scenario: POST request to correct endpoint with missing msg_id, should not get as far as deserialisation
    When I hit the service url "https://localhost:9044/submission" with the following json body
      | bad_bad_field | "aaa123" |
      | date_submitted  | "2017-02-19T12:46:31Z" |
      | applicant      | {}                     |
      | data_capture      | {}                |
      | declaration       | "i declare ok"      |
      | tags              | []                |
    Then I should get a 500 response
    And The response payload contains the following text : "Unable to process request"
