# Google Stax Server

## Table of Contents

- [Running the application](#running-the-application)
- [Prerequisites for manual setup](#prerequisites-for-manual-setup)
- [Getting Started](#getting-started)
- [Running the Application](#running-the-application)
- [Running Tests](#running-tests)
- [API Documentation](#api-documentation)
- [App specifics](docs/README.md)
- [License](#license)

## Running the application

You can checkout the instructions on how to directly run the application within a container [here](docs/LOCAL_SETUP.md)

## Prerequisites for manual setup

Before you begin, ensure you have met the following requirements:

- Java Development Kit (JDK) 17 or later
- Maven 3.9.6 or later
- Mysql 8.x
- gcloud CLI (for pubsub emulator)

## Getting Started

1. Configure the following ENV VARIABLES:
   ```
   // Encryption key for storing sensitive information in the database
   export AES_SECRET_KEY=defaultaessecret
   // JWT Authentication signing key
   export TOKEN_SIGNING_KEY=default_token_signing_key
   // Terms of Service authentication signing key
   export TOS_TOKEN_SIGNING_KEY=default_tos_token_signing_key
   // By default the application won't required configured google authentication settings.
   export AUTH_ENABLED=false
   // If authentication is enabled, please provide googe client id and secret for authentication
   export GOOGLE_CLIENT_ID=no_need_when_auth_enabled_false
   export GOOGLE_CLIENT_SECRET=no_need_when_auth_enabled_false

   // Database connection settings
   export JDBC_DATABASE_URL="jdbc:mysql://localhost:3306/your_db_name"
   export JDBC_DATABASE_USERNAME="your_db_user"
   export JDBC_DATABASE_PASSWORD="your_db_password"

   // Spring configuration profile
   export PROFILE_ACTIVE=local

   // GCP Project id - for local running with pubsub emulator it could be random string
   export GCP_PROJECT_ID=test

   // Settings for restricting user emails usage. Default settings - no restrictions
   export AUTH_ALLOWLIST_DOMAINS=localhost
   export AUTH_ALLOWLIST_GROUP_IDS=AllUsers

   // Terms of service type (the application supports different kinds of ToS). Can be left as is
   export TOS_TYPE=tt

   // System user defaults
   export SYSTEM_USER_FIRST_NAME=Stax
   export SYSTEM_USER_LAST_NAME=System User
   export SYSTEM_USER_EMAIL=planck-java-server@google.com
   ```

2. Build the project:
   ```
   mvn clean install
   ```

## Running pubsub locally

Currently, the "local" profile supports running of GCP pubsub emulator.
1. Set the active spring profile to `local`
2. Download the needed components for gcloud sdk (needed only once):

`gcloud components install beta pubsub-emulator`

3. Run the pubsub emulator:

`gcloud beta emulators pubsub start --host-port=localhost:8085`

4. Start the application normally. You should see such a message in the log, indicating that it is connected to the emulator:
2025-05-08 10:35:06 [DEBUG] i.g.n.s.i.g.netty.NettyClientHandler - [id: 0x8cfa63ce, L:/127.0.0.1:65332 - R:localhost/127.0.0.1:8085] OUTBOUND SETTINGS: ack=false settings={ENABLE_PUSH=0, MAX_CONCURRENT_STREAMS=0, INITIAL_WINDOW_SIZE=1048576, MAX_HEADER_LIST_SIZE=8192}

## Running the Application

To run the application locally, use the following command:

    mvn spring-boot:run

The application will start running at `http://localhost:8080`.

## Running Tests

This project uses JUnit for testing. To run the tests, you can use the following commands:

1. Run all tests:
   ```
   mvn clean test
   ```
2. Run a specific test class:
   ```
   mvn test -Dtest=TestClassName
   ```
   For example:
   ```
   mvn test -Dtest=com.planck.planck.domain.workbook.WorkbookServiceTest
   ```
3. Run a specific test method:
   ```
   mvn test -Dtest=TestClassName#testMethodName
   ```
   For example:
   ```
   mvn test -Dtest=com.planck.planck.domain.workbook.WorkbookServiceTest#testCreateWorkbook_Success
   ```
4. Run tests with coverage report (using JaCoCo):
   ```
   mvn clean test org.jacoco:jacoco-maven-plugin:prepare-agent verify org.jacoco:jacoco-maven-plugin:report
   ```
5. Before build, please run the command for format

   ```
   mvn com.theoryinpractise:googleformatter-maven-plugin:format
   ```

## App specifics
We are handling Enums and Tagging in a specific way - for more info please check out this [doc](docs/README.md)