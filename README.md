[![CI](https://github.com/DanielWillian/cooperative-meetings/actions/workflows/main.yaml/badge.svg?branch=main)](https://github.com/DanielWillian/cooperative-meetings/actions?query=workflow%3ACI+branch%3Amain)

cooperative-meetings is a demo app exposing REST APIs for managing meetings of a cooperative.

It uses OpenAPI as specification and implements it using Spring Boot.

## Requirements
Compiling, running and testing the application have different requirements.

To compile the app:

- Java SDK 11, openjdk works
- Maven, (recommended v3.6.0 or higher)

To run the app jar:

- Java JRE 11, openjdk works

To run integration tests:

- Java SDK 11, openjdk works
- Maven, (recommended v3.6.0 or higher)
- Docker, v19.03.8 or more recent is fine
- Docker Compose, v1.25.5 or more recent is fine

Conveniently there are utilities that automate running integration tests, these require:

- Be able to execute Bourne shell script, /bin/sh
- GNU Make, v4.1 or more recent is fine

## Building
To create the app jar run `mvn clean install`. This creates the jar at `target` of `:web-app` module
as well as a zip at `target` of `:integration-test` containing the jar and a docker compose file
to deploy a playground with PostgreSQL.

## Testing
To run the integration tests execute:

```
$ make deploy-test
```

It will compile the jar if needed, deploy the playground app, run integration tests
and undeploy the application at the end.

