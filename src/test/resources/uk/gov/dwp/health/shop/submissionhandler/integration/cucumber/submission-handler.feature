Feature: test the main functionality of the application

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

  Scenario: POST request to correct endpoint with unknown msg_id should be rejected
    When I hit the service url "https://localhost:9044/submission" with the following json body
      | msg_id | "does-not-exist.submission.new" |
      | ref | "aaa123" |
      | date_submitted  | "2017-02-19T12:46:31Z" |
      | applicant      | {}                     |
      | data_capture      | {}                |
      | declaration       | "i declare ok"      |
      | tags              | []                |
    Then I should get a 500 response
    And The response payload contains the following text : "Unable to process request"

  Scenario: POST request to correct endpoint with missing json content, should fail validation
    When I hit the service url "https://localhost:9044/submission" with the following json body
      | msg_id | "access-to-work.submission.new" |
      | ref | "aaa123" |
      | date_submitted  | "2017-02-19T12:46:31Z" |
      | applicant      | {}                     |
      | data_capture      | {}                |
      | declaration       | "i declare ok"      |
      | tags              | []                |
    Then I should get a 400 response
    And The response payload contains the following text : "Payload contains invalid items"

  Scenario: POST request to correct endpoint with valid json and bad content - date submitted - should fail validation
    When I hit the service url "https://localhost:9044/submission" with the following json body
      | msg_id | "access-to-work.submission.new" |
      | ref | "aaa123" |
      | date_submitted  | "2017-02-39T12:46:31Z" |
      | applicant      | {"forenames": "aa", "surname": "bb", "dob": "2000-02-29", "residence_address": {"lines": ["line 1", "line 2"],"premises": "at home","postcode": "ls6 4pt"},"contact_options": [{"method": "email","data": "an.email@address.co.uk","preferred": true}],"representative": {"full_name": "john andrew smith","relationship": "father","email": "email@server.com","tel": "1234"}} |
      | data_capture      | {"someTextItem": "this is text", "someIntValue": 99} |
      | declaration       | "i declare ok"      |
      | tags              | []                |
    Then I should get a 400 response
    And The response payload contains the following text : "Payload contains invalid items"

  Scenario: POST request to correct endpoint with valid json and empty required field - forename - should fail validation
    When I hit the service url "https://localhost:9044/submission" with the following json body
      | msg_id | "access-to-work.submission.new" |
      | ref | "aaa123" |
      | date_submitted  | "2017-02-19T12:46:31Z" |
      | applicant      | {"forenames": "", "surname": "bb", "dob": "2000-02-29", "residence_address": {"lines": ["line 1", "line 2"],"premises": "at home","postcode": "ls6 4pt"},"contact_options": [{"method": "email","data": "an.email@address.co.uk","preferred": true}],"representative": {"full_name": "john andrew smith","relationship": "father","email": "email@server.com","tel": "1234"}} |
      | data_capture      | {"someTextItem": "this is text", "someIntValue": 99} |
      | declaration       | "i declare ok"      |
      | tags              | []                |
    Then I should get a 400 response
    And The response payload contains the following text : "Payload contains invalid items"

  Scenario: POST request to correct endpoint with valid json and null required field - forename - should fail validation
    When I hit the service url "https://localhost:9044/submission" with the following json body
      | msg_id | "access-to-work.submission.new" |
      | ref | "aaa123" |
      | date_submitted  | "2017-02-19T12:46:31Z" |
      | applicant      | {"forenames": null, "surname": "bb", "dob": "2000-02-29", "residence_address": {"lines": ["line 1", "line 2"],"premises": "at home","postcode": "ls6 4pt"},"contact_options": [{"method": "email","data": "an.email@address.co.uk","preferred": true}],"representative": {"full_name": "john andrew smith","relationship": "father","email": "email@server.com","tel": "1234"}} |
      | data_capture      | {"someTextItem": "this is text", "someIntValue": 99} |
      | declaration       | "i declare ok"      |
      | tags              | []                |
    Then I should get a 400 response
    And The response payload contains the following text : "Payload contains invalid items"

  Scenario: POST request to correct endpoint with valid content with no SNS topic to publish to
    And I hit the service url "https://localhost:9044/submission" with the following json body
      | msg_id | "access-to-work.submission.new" |
      | ref | "aaa123" |
      | date_submitted  | "2017-02-19T12:46:31Z" |
      | applicant      | {"forenames": "aa", "surname": "bb", "dob": "2000-02-29", "residence_address": {"lines": ["line 1", "line 2"],"premises": "at home","postcode": "ls6 4pt"},"contact_options": [{"method": "email","data": "an.email@address.co.uk","preferred": true}],"representative": {"full_name": "john andrew smith","relationship": "father","email": "email@server.com","tel": "1234"}} |
      | data_capture      | {"someTextItem": "this is text", "someIntValue": 99} |
      | declaration       | "i declare ok"      |
      | tags              | []                |
    Then I should get a 500 response
    And The response payload contains the following text : "Unable to process request"

  Scenario: POST request to correct endpoint with valid content and listen to the wrong queue
    Given I create an sns topic named "sub-handler-topic"
    And I create a queue called "test.queue.1" binding to topic "sub-handler-topic" with filter policy "test.key.1"
    And I hit the service url "https://localhost:9044/submission" with the following json body
      | msg_id | "access-to-work.submission.new" |
      | ref | "aaa123" |
      | date_submitted  | "2017-02-19T12:46:31Z" |
      | applicant      | {"forenames": "aa", "surname": "bb", "dob": "2000-02-29", "residence_address": {"lines": ["line 1", "line 2"],"premises": "at home","postcode": "ls6 4pt"},"contact_options": [{"method": "email","data": "an.email@address.co.uk","preferred": true}],"representative": {"full_name": "john andrew smith","relationship": "father","email": "email@server.com","tel": "1234"}} |
      | data_capture      | {"someTextItem": "this is text", "someIntValue": 99} |
      | declaration       | "i declare ok"      |
      | tags              | []                |
    Then I should get a 200 response
    And The response payload contains a valid return UUID correlationId
    And There were no messages on queue "test.queue.1"

  Scenario: POST request to correct endpoint with valid ACCESS TO WORK content and it is a SUCCESS!
    Given I create an sns topic named "sub-handler-topic"
    And I create a catch all subscription for queue name "test.queue.2" binding to topic "sub-handler-topic"
    When I hit the service url "https://localhost:9044/submission" with the following json body
      | msg_id | "access-to-work.submission.new" |
      | ref | "asa123" |
      | date_submitted  | "2017-02-19T12:46:31Z" |
      | applicant      | {"forenames": "aa", "surname": "bb", "dob": "2000-02-29", "residence_address": {"lines": ["line 1", "line 2"],"premises": "at home","postcode": "ls6 4pt"},"contact_options": [{"method": "email","data": "an.email@address.co.uk","preferred": true}],"representative": {"full_name": "john andrew smith","relationship": "father","email": "email@server.com","tel": "1234"}} |
      | data_capture      | {"someTextItem": "this is text", "someIntValue": 99} |
      | declaration       | "i declare ok"      |
      | tags              | []                |
    Then I should get a 200 response
    And The response payload contains a valid return UUID correlationId
    Then a message is successfully removed from the queue, there were a total of 1 messages on queue "test.queue.2"
    And The received message has a routing key "incoming.submission.atw"
    And The message body content is an AtwSubmissionItem with reference "asa123"
    Then I read the mongo database and there are 1 entries
    And There is an entry containing the reference "asa123" and the original submitted time
    And The mongo entry with ref "asa123" has matching contents to the original submitted item

  Scenario: POST request to correct endpoint with valid NEW STYLE ESA content and it is a SUCCESS!
    Given I create an sns topic named "sub-handler-topic"
    And I create a queue called "test-queue-3" binding to topic "sub-handler-topic" with filter policy "incoming.submission.esa"
    When I hit the service url "https://localhost:9044/submission" with the following json body taken from file "src/test/resources/submission-template-esa.json" with msg_id "new-style-esa.submission.new" and submission time of now plus 0 days
    Then I should get a 200 response
    And The response payload contains a valid return UUID correlationId

    Then a message is successfully removed from the queue, there were a total of 1 messages on queue "test-queue-3"
    And The received message has a routing key "incoming.submission.esa"
    And The message body content is an AtwSubmissionItem with reference "2PJE5M"
    Then I read the mongo database and there are 1 entries
    And There is an entry containing the reference "2PJE5M" and the original submitted time
    And The mongo entry with ref "2PJE5M" has matching contents to the original submitted item

  Scenario: POST request to correct endpoint with valid NEW STYLE ESA with submitted in the future is a FAILURE!
    Given I create an sns topic named "sub-handler-topic"
    And I create a queue called "test.queue.4" binding to topic "sub-handler-topic" with filter policy "incoming.submission.esa"
    When I hit the service url "https://localhost:9044/submission" with the following json body taken from file "src/test/resources/submission-template-esa.json" with msg_id "new-style-esa.submission.new" and submission time of now plus 5 days
    Then I should get a 400 response

  Scenario: POST request to correct endpoint with valid ACCESS TO WORK with submitted 5 days in the future is a SUCCESS!
    Given I create an sns topic named "sub-handler-topic"
    And I create a queue called "atw.queue.1" binding to topic "sub-handler-topic" with filter policy "incoming.submission.atw"
    When I hit the service url "https://localhost:9044/submission" with the following json body taken from file "src/test/resources/submission-template-atw.json" with msg_id "access-to-work.submission.new" and submission time of now plus 5 days
    Then I should get a 200 response
    And The response payload contains a valid return UUID correlationId
    Then a message is successfully removed from the queue, there were a total of 1 messages on queue "atw.queue.1"
    And The received message has a routing key "incoming.submission.atw"
    And The message body content is an AtwSubmissionItem with reference "2PJE5M"
    Then I read the mongo database and there are 1 entries
    And There is an entry containing the reference "2PJE5M" and the original submitted time
    And The mongo entry with ref "2PJE5M" has matching contents to the original submitted item

  Scenario: POST request to correct endpoint with valid ACCESS TO WORK with submitted is 6 days in the future is a FAILURE!
    Given I create an sns topic named "sub-handler-topic"
    And I create a queue called "atw.queue.2" binding to topic "sub-handler-topic" with filter policy "incoming.submission.atw"
    When I hit the service url "https://localhost:9044/submission" with the following json body taken from file "src/test/resources/submission-template-atw.json" with msg_id "access-to-work.submission.new" and submission time of now plus 6 days
    Then I should get a 400 response

  Scenario: POST request to correct endpoint with allowable blank content - relationship - and it is a SUCCESS with the serialised blank content removed!
    Given I create an sns topic named "sub-handler-topic"
    And I create a catch all subscription for queue name "atw.queue.3" binding to topic "sub-handler-topic"
    When I hit the service url "https://localhost:9044/submission" with the following json body
      | msg_id | "access-to-work.submission.new" |
      | ref | "aaa1234" |
      | date_submitted  | "2017-02-19T12:46:31Z" |
      | applicant      | {"forenames": "aa", "surname": "bb", "dob": "2000-02-29", "residence_address": {"lines": ["line 1", "line 2"],"premises": "at home","postcode": "ls6 4pt"},"contact_options": [{"method": "email","data": "an.email@address.co.uk","preferred": true}],"representative": {"full_name": "john andrew smith","relationship": "","email": "some@server.com","tel": "1234"}} |
      | data_capture      | {"someTextItem": "this is text", "someIntValue": 99} |
      | declaration       | "i declare ok"      |
      | tags              | []                |
    Then I should get a 200 response
    And The response payload contains a valid return UUID correlationId
    Then a message is successfully removed from the queue, there were a total of 1 messages on queue "atw.queue.3"
    And The received message has a routing key "incoming.submission.atw"
    And The message body content is an AtwSubmissionItem with reference "aaa1234"
    And The following message body json items are present
      | "relationship"    |
    Then I read the mongo database and there are 1 entries
    And There is an entry containing the reference "aaa1234" and the original submitted time
    And The mongo entry with ref "aaa1234" has matching contents to the original submitted item

  Scenario: POST request to correct endpoint with allowable optional fields - where at least one must be present, rep & contact - and it is a FAILURE
    Given I create an sns topic named "sub-handler-topic"
    And I create a catch all subscription for queue name "atw.queue.4" binding to topic "sub-handler-topic"
    When I hit the service url "https://localhost:9044/submission" with the following json body
      | msg_id | "access-to-work.submission.new" |
      | ref | "aaa123" |
      | date_submitted  | "2017-02-19T12:46:31Z" |
      | applicant      | {"forenames": "aa", "surname": "test", "dob": "2000-02-29", "residence_address": {"lines": ["line 1", "line 2"],"premises": "at home","postcode": "ls6 4pt"}} |
      | data_capture      | {"someTextItem": "this is text", "someIntValue": 99} |
      | declaration       | "i declare ok"      |
      | tags              | []                |
    Then I should get a 200 response

  Scenario: POST request to correct endpoint with allowable missing - representative - and it is a SUCCESS with the serialised blank content removed!
    Given I create an sns topic named "sub-handler-topic"
    And I create a catch all subscription for queue name "atw.queue.5" binding to topic "sub-handler-topic"
    When I hit the service url "https://localhost:9044/submission" with the following json body
      | msg_id | "access-to-work.submission.new" |
      | ref | "abc123" |
      | date_submitted  | "2017-02-19T12:46:31Z" |
      | applicant      | {"forenames": "aa",  "surname": "bb", "dob": "2000-02-29", "residence_address": {"lines": ["line 1", "line 2"],"premises": "at home","postcode": "ls6 4pt"},"contact_options": [{"method": "email","data": "an.email@address.co.uk","preferred": true}]} |
      | data_capture      | {"someTextItem": "this is text", "someIntValue": 99} |
      | declaration       | "i declare ok"      |
      | tags              | []                |
    Then I should get a 200 response
    And The response payload contains a valid return UUID correlationId
    Then a message is successfully removed from the queue, there were a total of 1 messages on queue "atw.queue.5"
    And The received message has a routing key "incoming.submission.atw"
    And The message body content is an AtwSubmissionItem with reference "abc123"
    And The following message body json items are missing
      | "representative" |
    Then I read the mongo database and there are 1 entries
    And There is an entry containing the reference "abc123" and the original submitted time
    And The mongo entry with ref "abc123" has matching contents to the original submitted item

  Scenario: POST request to correct endpoint with allowable missing - contact_option - and it is a SUCCESS with the serialised blank content removed!
    Given I create an sns topic named "sub-handler-topic"
    And I create a catch all subscription for queue name "atw.queue.6" binding to topic "sub-handler-topic"
    When I hit the service url "https://localhost:9044/submission" with the following json body
      | msg_id | "access-to-work.submission.new" |
      | ref | "bbb321" |
      | date_submitted  | "2017-02-19T12:46:31Z" |
      | applicant      | {"forenames": "aa",  "surname": "bb", "dob": "2000-02-29", "residence_address": {"lines": ["line 1", "line 2"],"premises": "at home","postcode": "ls6 4pt"},"representative": {"full_name": "john andrew smith","relationship": "","email": "some@server.com","tel": "1234"}} |
      | data_capture      | {"someTextItem": "this is text", "someIntValue": 99} |
      | declaration       | "i declare ok"      |
      | tags              | []                |
    Then I should get a 200 response
    And The response payload contains a valid return UUID correlationId
    Then a message is successfully removed from the queue, there were a total of 1 messages on queue "atw.queue.6"
    And The received message has a routing key "incoming.submission.atw"
    And The message body content is an AtwSubmissionItem with reference "bbb321"
    And The following message body json items are missing
      | "contact_option" |
    Then I read the mongo database and there are 1 entries
    And There is an entry containing the reference "bbb321" and the original submitted time
    And The mongo entry with ref "bbb321" has matching contents to the original submitted item

  Scenario: POST request to correct endpoint with more missing contents - tags - and it is a FAILURE
    Given I create an sns topic named "sub-handler-topic"
    And I create a catch all subscription for queue name "atw.queue.7" binding to topic "sub-handler-topic"
    When I hit the service url "https://localhost:9044/submission" with the following json body
      | msg_id | "access-to-work.submission.new" |
      | ref | "aaa123" |
      | date_submitted  | "2017-02-19T12:46:31Z" |
      | applicant      | {"forenames": "aa", "surname": "test", "dob": "2000-02-29", "residence_address": {"lines": ["line 1", "line 2"],"premises": "at home","postcode": "ls6 4pt"},"contact_options": [{"method": "email","data": "an.email@address.co.uk","preferred": true}],"representative": {"full_name": "john andrew smith","relationship": "","email": "some@server.com","tel": "1234"}} |
      | data_capture      | {"someTextItem": "this is text", "someIntValue": 99} |
      | declaration       | "i declare ok"      |
      | tags              | [null]                |
    Then I should get a 400 response
    And The response payload contains the following text : "Payload contains invalid items"

  Scenario: POST two requests with duplicate reference to correct endpoint - and the first is successful but the second fails!
    Given I create an sns topic named "sub-handler-topic"
    And I create a catch all subscription for queue name "atw.queue.8" binding to topic "sub-handler-topic"
    When I hit the service url "https://localhost:9044/submission" with the following json body
      | msg_id | "access-to-work.submission.new" |
      | ref | "aaa1234" |
      | date_submitted  | "2017-02-19T12:46:31Z" |
      | applicant      | {"forenames": "aa",  "surname": "bb", "dob": "2000-02-29", "residence_address": {"lines": ["line 1", "line 2"],"premises": "at home","postcode": "ls6 4pt"},"contact_options": [{"method": "email","data": "an.email@address.co.uk","preferred": true}],"representative": {"full_name": "john andrew smith","relationship": "","email": "some@server.com","tel": "1234"}} |
      | data_capture      | {"someTextItem": "this is text", "someIntValue": 99} |
      | declaration       | "i declare ok"      |
      | tags              | []                |
    Then I should get a 200 response
    And The response payload contains a valid return UUID correlationId
    Then a message is successfully removed from the queue, there were a total of 1 messages on queue "atw.queue.8"
    And The received message has a routing key "incoming.submission.atw"
    And The message body content is an AtwSubmissionItem with reference "aaa1234"
    Then I read the mongo database and there are 1 entries
    And There is an entry containing the reference "aaa1234" and the original submitted time
    And The mongo entry with ref "aaa1234" has matching contents to the original submitted item
    When I hit the service url "https://localhost:9044/submission" with the following json body
      | msg_id | "access-to-work.submission.new" |
      | ref | "aaa1234" |
      | date_submitted  | "2017-02-28T12:46:31Z" |
      | applicant      | {"forenames": "ab",  "surname": "bb", "dob": "2000-02-29", "residence_address": {"lines": ["line 1", "line 2"],"premises": "at home","postcode": "ls6 4pt"},"contact_options": [{"method": "email","data": "an.email@address.co.uk","preferred": true}],"representative": {"full_name": "john andrew smith","relationship": "","email": "some@server.com","tel": "1234"}} |
      | data_capture      | {"someTextItem": "this is text", "someIntValue": 99} |
      | declaration       | "i declare ok"      |
      | tags              | []                |
    Then I should get a 409 response
    And The response payload contains the following text : "Unable to process request"

  Scenario: POST two requests with different reference to correct endpoint - the first is successful and the second is also successful!
    Given I create an sns topic named "sub-handler-topic"
    And I create a catch all subscription for queue name "atw.queue.9" binding to topic "sub-handler-topic"
    When I hit the service url "https://localhost:9044/submission" with the following json body
      | msg_id | "access-to-work.submission.new" |
      | ref | "aaa1234" |
      | date_submitted  | "2017-02-19T12:46:31Z" |
      | applicant      | {"forenames": "aa", "surname": "bb", "dob": "2000-02-29", "residence_address": {"lines": ["line 1", "line 2"],"premises": "at home","postcode": "ls6 4pt"},"contact_options": [{"method": "email","data": "an.email@address.co.uk","preferred": true}],"representative": {"full_name": "john andrew smith","relationship": "","email": "some@server.com","tel": "1234"}} |
      | data_capture      | {"someTextItem": "this is text", "someIntValue": 99} |
      | declaration       | "i declare ok"      |
      | tags              | []                |
    Then I should get a 200 response
    And The response payload contains a valid return UUID correlationId
    Then a message is successfully removed from the queue, there were a total of 1 messages on queue "atw.queue.9"
    And The received message has a routing key "incoming.submission.atw"
    And The message body content is an AtwSubmissionItem with reference "aaa1234"
    Then I read the mongo database and there are 1 entries
    And There is an entry containing the reference "aaa1234" and the original submitted time
    And The mongo entry with ref "aaa1234" has matching contents to the original submitted item
    When I hit the service url "https://localhost:9044/submission" with the following json body
      | msg_id | "access-to-work.submission.new" |
      | ref | "aab1234" |
      | date_submitted  | "2017-02-28T12:46:31Z" |
      | applicant      | {"forenames": "ab", "surname": "bb",  "dob": "2000-02-29", "residence_address": {"lines": ["line 1", "line 2"],"premises": "at home","postcode": "ls6 4pt"},"contact_options": [{"method": "email","data": "an.email@address.co.uk","preferred": true}],"representative": {"full_name": "john andrew smith","relationship": "","email": "some@server.com","tel": "1234"}} |
      | data_capture      | {"someTextItem": "this is text", "someIntValue": 99} |
      | declaration       | "i declare ok"      |
      | tags              | []                |
    Then I should get a 200 response
    And The response payload contains a valid return UUID correlationId
    Then a message is successfully removed from the queue, there were a total of 1 messages on queue "atw.queue.9"
    And The received message has a routing key "incoming.submission.atw"
    And The message body content is an AtwSubmissionItem with reference "aab1234"
    Then I read the mongo database and there are 2 entries
    And There is an entry containing the reference "aab1234" and the original submitted time
    And The mongo entry with ref "aab1234" has matching contents to the original submitted item
