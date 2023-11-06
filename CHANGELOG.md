# 5.0.0 (06-11-2023)

* upgrade dropwizard dependency ([355a7d88](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/355a7d88))

# 4.9.0 (11-10-2023)

* Add support for new Access to Work application submission

# 4.8.0 (19-01-2023)

* Add reason no ssp
* Update dockerfile user

#4.7.0 (10-03-2022)

* Update database compatibility from mongo v4 to mongo v5

#4.6.1 (02-02-2022)

* Updates Jar packaging strategy
* Updates Dropwizard

##4.6.0 (07-12-2021)

* Updates validation in common-case-application-classes
* Prepare ms-submission-handler for Mongo v5 upgrade

##4.5.0 (20-10-2021)

* initial open sourcing of ms-submission-handler
* dependency / security updates
* add open source pipeline fragment

## Combined changelog for release prior to 4.5.0

### Bug Fixes

* **atlas:** adding in a configurable switch for 'sslInvalidHostNameAllowed' to cater for the certificate not having the original hostname in Mongo Atlas. ([4eee64c](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/4eee64c))
* **bundle:** only bundle application.yml to the jar file and leave the json test files to be deployed locally.  If required in the bundle then add another entry with filtering false to bundle them unmodified ([3698ce1](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/3698ce1))
* **certs:** created new 5yr certificates that are only used for the src/test setup ([c2132c0](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/c2132c0))
* **ci:** fix typo in Dockerfile ([c4107c0](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/c4107c0))
* **ci:** update Dockerfile ([83868bb](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/83868bb))
* **class-cast:** remediate the Checkmarx medium risk for unchecked class cast by forcing the Class.forName to subclass to Payload.class ([0fb51f5](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/0fb51f5))
* **dependency:** updated message-utility to v2.1.3 ([4d63c4a](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/4d63c4a))
* **env:** revert change to BUILD_NUMBER as env.BUILD_NUMBER is actually correct ([2c24326](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/2c24326))
* **events:** apply the latest events version to (correctly) handle SNS publishing / SQS comsumption ([92ea697](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/92ea697))
* fix time validation ([a948731](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/a948731))
* fragment version ([dd01c28](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/dd01c28))
* **hpi-168:** updated messages to use persistent delivery modes ([4531cd3](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/4531cd3))
* **lint:** apply standard formatting and address pmd and checkstyle issues ([e57f707](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/e57f707))
* **mongo:**  fixed support for mongo+srv URI naming and renamed the configuraiton item to be URI instead of URL.  Also looking at replaced Fongo as this does not work with the newest mongo version at the moment (https://github.com/fakemongo/fongo/issues/316) ([537b687](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/537b687))
* **mongo:** if Mongo goes away the injected MongoClient becomes stale and fails from that point forwards.  Create/Close connections as required ([4d802f4](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/4d802f4))
* **mongo:** remove Fongo as it does not (yet) support Mongo 3.7.0 which we must use in order to support the +srv URI functionality.  Using embedded-mongo instead for all database interactions ([1b31db6](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/1b31db6))
* **naming:** ajust the main class for ATW to reflect the usage, with config updates ([9ebd5b3](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/9ebd5b3))
* **naming:** change the projectName to be more general across Health ([83695df](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/83695df))
* **owasp:** add suppression for slf4j until next release is out ([e9360d9](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/e9360d9))
* **owasp:** fix jetty vulnerability ([13a3470](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/13a3470))
* **owasp:** update vulnerable packages ([b001927](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/b001927))
* **owasp:** updated jackson-databind and removed unused suppressions ([8957cc1](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/8957cc1))
* **owasp:** updated owasp vulnerabilities that can be excluded ([5fc327a](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/5fc327a))
* **packages:** fix package names to HealthPDU standard ([3f69b82](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/3f69b82))
* **rabbit:** include truststore/keystore configuration parmaeters for rabbitmq connection ([e8a2c05](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/e8a2c05))
* **rabbitmq:** fix rabbitmq static issues ([340ea3d](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/340ea3d))
* **rabbit:** use the latest version of rabbitmq library that will reuse a single connection ([ecc0e8f](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/ecc0e8f))
* removed validation for either contact or rep required. These are not mandatory fields ([dc22381](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/dc22381))
* **sonar:** address sonarqube issues ([9d677f1](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/9d677f1))
* **sonar:** remov unused dependencies from classes ([f66f831](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/f66f831))
* **strict:** important parameter to EnvironmentVariableSubstitutor in order to support default values for unset environment variables ([64ac035](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/64ac035))
* **typo:** change the schema validation from yml to json ([c6330aa](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/c6330aa))
* **typo:** fix typo in events spec ([12df751](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/12df751))
* update dev certs and bump dependency versions ([fb82996](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/fb82996))
* **URL:** in order to support the seed-list naming convention for mongo clusters 'mongodb+srv://user:password[@mongodb](https://github.com/mongodb).com' the URL needed to be updated to java.net.URI.  SSL will be enabled if the truststore file name is NOT null in the configuration ([c8964db](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/c8964db))
* **variable:** correct the enviornment variable from jenkins to BUILD_NUMBER ([b370553](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/b370553))
* **versions:** updated versions to carry fixes for logging and updated OWASP dependencies ([f54cc89](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/f54cc89))
* **vuln:** address owasp vulnerability ([29567d3](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/29567d3))
* **vuln:** address snakeyml vulnerability ([35b7067](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/35b7067))


### Features

* add new endpoint for service info ([45d5da6](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/45d5da6))
* add tests and refactor ([ad1f180](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/ad1f180))
* add variable for jenkins build number ([36d84d2](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/36d84d2))
* **ci:** add Dockerfile ([abdbce3](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/abdbce3))
* **ci:** add sonar-project.properties file ([8cb51c2](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/8cb51c2))
* **dev:** on-going development ([9ea5bf0](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/9ea5bf0))
* ensure no duplicate application reference with unique key constraint in DB: ATW-25 ([c1e5aef](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/c1e5aef))
* **env-var:** allow environment variable substitution for the application ([ab78b52](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/ab78b52))
* **esa:** new additions for ESA that implement the absolute minimum to be able to accept and propagate a message ([61b4f90](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/61b4f90))
* **format:** run through standard formatter and created a BaseItem interface to use in item classes that are not of type Payload ([aee2990](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/aee2990))
* **ha-1314:** added a build time to the application properties ([a0f76ca](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/a0f76ca))
* HPI-252 modified config for many input, modified tests. Surname mandatory in submission item ([065aa70](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/065aa70))
* HPI-252 split JSON file per event type ([c4ee068](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/c4ee068))
* **ignore:** set class to ignore unknown as msg_id is not a member of this ([0ef3159](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/0ef3159))
* implement new crypto work in submission handler. First pass ([05a6e79](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/05a6e79))
* **imports:** correctly set imports for no wildcards ([7bead43](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/7bead43))
* **logging:** Alter default log level to INFO, rather than DEBUG ([13f388b](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/13f388b))
* **mongo:** ingore the SAN certificate error when connecting to Mongo Atlas by allowing certificates names that do not match the hostname ([54f84eb](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/54f84eb))
* move version path back to top of class ([0e04b9d](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/0e04b9d))
* **properties:** first pass at deciding how to identify the incoming event and pick from the configuration.  decided on msg_id ([d86921e](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/d86921e))
* refactor for review comment ([e752f94](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/e752f94))
* refactor from review comments ([179da96](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/179da96))
* refactor yml file ([9b1acdd](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/9b1acdd))
* remove info file and add properties to existing yml file ([433f110](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/433f110))
* **services:** new signiture and tests to pick the relevant entry from configuration and use it in the resource depending on value ([a4d4189](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/a4d4189))
* update log to provide readable data instead of object reference ([903ff0d](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/903ff0d))
* **updates:** updated pom entries for JAVA11 and OWASP coverage ([087f106](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/087f106))
* **validatation:** continued adding of the validation and specification for the incoming model.  once the model is complete everything should flow easily through the remaining services.  ATWDC-1228 ([5a31e09](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/5a31e09))
* **validate:** addition validation to EsaSubmissionItem for real property validation ([9f3b1bb](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/9f3b1bb))
* **validation:** creating the model and validators for the input structure (with tests) ([6374bc3](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/6374bc3))
* **version:** removed Rabbit dependencies and added in SNS/SQS ([4e60fe1](https://gitlab.com/dwp/health/shared-components/components/ms-submission-handler/commit/4e60fe1))
