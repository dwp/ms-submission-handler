# ms-submission-handler

The API will be the primary input service for all 'submissions' of data to SHOP.  It is a standard java dropwizard application with the following dependencies.

**Prerequisites**

This project requires the [Java Cryptography Extension (JCE)](http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html) to be installed  

The full api is detailed in `spec-api.yml` in Swagger format (v2).  The build is standard maven `mvn clean package` with the OWASP dependency checker built into the 'test' phase

**build & run**

    mvn clean package

to build the application and then running the application using 

    java -jar <path-to-jar-file> server <path-to-config-yml>
        
**encryption (message & mongo)**

The service will use the `data-cryptography` and the `messaging-utility` dependencies to encrypt data for persistence to Mongo and for propagation to RabbitMQ respectively.  The relevant configuration items for this functionality are:-

    mongoCryptoConfiguration  
    mqCryptoConfiguration
        
which are `uk.gov.dwp.health.crypto.CryptoConfig` classes and are built according to the specification detailed in the README for `data-cryptography` (https://gitlab.itsshared.net/health-pdu/shared/govuk-data-cryptography).

* `dataKeyRequestId` : This is a mandatory property, the unique key ID or the Amazon Resource Name (ARN) of the CMK, or the alias name or ARN of an alias that refers to the CMK.  This will likely be an alias as more than one is required per instance (one for messaging, one for mongo).  An example would be `alias/ExampleAliasName`.
* `encryptionType` : This is an optional property.  If missing it will default to AES_256
* `awsRegion` : This is an optional property.  If missing it will default to EU_WEST_2 (London)
* `kmsEndpointOverride` : This is an optional parameter and will override the default endpoint for aws region to point to a specific KMS server instance (and is useful for testing against mock kms server implementations)

A valid `dataKeyRequestId` is the unique key ID or the Amazon Resource Name (ARN) of the CMK, or the alias name or ARN of an alias that refers to the CMK. Examples:

* Unique key ID: `1234abcd-12ab-34cd-56ef-1234567890ab`
* CMK ARN: `arn:aws:kms:us-east-2:111122223333:key/1234abcd-12ab-34cd-56ef-1234567890ab`
* Alias name: `alias/ExampleAlias`
* Alias ARN: `arn:aws:kms:us-east-2:111122223333:alias/ExampleAlias`

**environmental dependencies**

The incoming JSON object will be validated against a JSON schema document.  This has been created using the `spec-api.yaml` as the base document and being passed through the `apis` project (health-pdu/apis) in order to generate a list of all the api endpoints for a service and their respective schema validation.

`/submission`
=

This endpoint will deserialise the incoming JSON and validate the contents before encrypting the entire payload and pushing as a new object to a MongoDB collection.

    {
        "msg_id": "atw.submission.new", 
        "ref": "aa123_string_reference",
        "date_submitted": "2017-02-19T12:46:31Z",
        "applicant": {
            "forenames": "john",
            "surname": "doe",
            "dob": "2000-02-29",
            "residence_address": {
                "lines": ["line 1", "line 2"],
                "premises": "at home",
                "postcode": "ls6 4pt"
            },
            "contact_options": [{
                    "method": "email",
                    "data": "an.email@address.co.uk",
                    "preferred": true
                }
            ],
            "representative": {
                "full_name": "john andrew smith",
                "relationship": "father",
                "email": "email@server.com",
                "tel": "1234"
            }
        },
        "data_capture": {
            "pojo": "object"
        },
        "declaration": "I confirm",
        "tags": []
    }

**msg_id** must be passed into the service in order to identify which configuration to use to verify, encode and propagate.

The data object is saved to Mongo using the configuration elements to set the instance, database and collection.

    mongoDbUrl: https://localhost:9897
    mongoDatabase: incomingData
    mongoCollectionName: submissions

The collection format is:-

* `date_submitted` : (EpochMillis) Converted to the number of milliseconds from the epoch of 1970-01-01T00:00:00Z
* `ref` : String reference
* `encrypted_message` : The encrypted payload data (kms encrypted)
* `hash` : the 'data key' returned back from KMS (along with the encrypted payload)

eg. *Document{{_id=5a0308a93204210ff0ee21be, date_submitted=1487508391000, ref=aa123, encrypted_message=encrypted-serialised-object, hash=asdfghjkjhgfds1234567898765432}}*
    
### Return Status

**Success** is returned when the payload is successfully validated, encrypted and saved to Mongo.

* Invalid json input data or validation - `BAD_REQUEST (400)`
* Exceptions during processing - `INTERNAL_SERVER_ERROR (500)`
* Success - `SC_OK (200)`

### Continuous Integration (CI) Pipeline

For general information about the CI pipeline on this repository please see documentation at: https://confluence.service.dwpcloud.uk/x/_65dCg

**Pipeline Invocation**

This CI Pipeline now replaces the Jenkins Build CI Process for the `ms-submission-handler`.

Gitlab CI will automatically invoke a pipeline run when pushing to a feature branch (this can be prevented using `[skip ci]` in your commit message if not required).

When a feature branch is merged into `develop` it will automatically start a `develop` pipeline and build the required artifacts.

For production releases please see the release process documented at: https://confluence.service.dwpcloud.uk/pages/viewpage.action?spaceKey=DHWA&title=SRE
A production release requires a manual pipeline (to be invoked by an SRE) this is only a release function. 
Production credentials are required.

**Local Cucumber Tests**

To execute the Cucumber Tests locally the `docker-compose.yml` file can be utilised. This has been written to allow usage by both the pipeline as well as local invocation.

To run the Cucumber Tests locally please run the following command:
```
docker-compose up --exit-code-from cucumber-tests
```

This will start the following services:
- `localstack-init` - This is used to create 2 KMS Keys and corresponding alias's.
- `localstack`      - This will start the mock KMS, SQS, SNS and s3 services.
- `mongo-db`        - Utilised during the running of cucumber tests.
- `cucumber-tests`  - Service to run required integration tests.

Due to the orchestration within `docker-compose.yml` there is no need to stop any running services after the tests have completed as this functionality is built in.

**localdev Usage**

There is no change to the usage of localdev. The gitlab CI Build process create artifacts using the same naming convention as the old (no longer utilised) Jenkins CI Build process.

Therefore please continue to use `branch-develop` or `branch-f-*` (depending on branch name) for proving any feature changes.

**Docker Build**

With this CI docker build, docker uses the embedded base configuration which holds all required environment variables.
See /src/main/properties/base-config.yml for details of the environment variables that can be provided and what the default values are.

Please note however when using in conjunction with localdev, the base config is overridden with embedded config held within the localdev project and so these environment variables are not required.

**Access**

While this repository is open internally for read, no one has write access to this repository by default.
To obtain access to this repository please contact #ask-health-platform within slack and a member will grant the appropriate level of access.
