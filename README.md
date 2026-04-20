# Account API

A RESTful Account Management Service built with Java 25, Spring Boot, Liquibase, and H2.

## Overview

Manages `Account` entities with CRUD endpoints, request validation, schema versioning via Liquibase, and an OpenAPI/Swagger UI.

## Tech stack

- **Java** 25 (LTS)
- **Gradle** (wrapper)
- **Spring Boot** 4.0.5 — Web MVC, Data JPA, Validation
- **Liquibase** — database schema versioning
- **H2** — in-memory database for development and tests
- **springdoc-openapi** — OpenAPI 3 / Swagger UI
- **Lombok** — boilerplate reduction on the JPA entity
- **JUnit 5**, **Mockito**, **AssertJ**, **MockMvc** — testing

## Prerequisites

- JDK 25 installed (a matching toolchain is declared in `build.gradle`; Gradle will attempt auto-provisioning if available)

## Build & run

```bash
./gradlew build          # compile, run tests, package
./gradlew bootRun        # start the application on http://localhost:8080
./gradlew test           # unit + integration tests
```

## Endpoints

| Method | Path             | Description         | Success |
|--------|------------------|---------------------|---------|
| POST   | `/accounts`      | Create account      | 201 + `Location` |
| GET    | `/accounts/{id}` | Fetch by id         | 200 |
| PUT    | `/accounts/{id}` | Full update         | 200 |
| DELETE | `/accounts/{id}` | Delete              | 204 |

### Request / response shape

```json
POST /accounts
{
  "name": "Alice",
  "phoneNr": "+3725551234"
}

201 Created
Location: /accounts/1
{
  "id": 1,
  "name": "Alice",
  "phoneNr": "+3725551234",
  "createdDtime": "2026-04-17T10:00:00.000000Z",
  "modifiedDtime": "2026-04-17T10:00:00.000000Z"
}
```

### Validation

The assignment description under /docs states for only the field phoneNr, that it is optional, 
therefore I presume name and the rest of the fields must have a non null value.

Also there are no prescriptions uniqueness constraints. Neither that name should be unique, 
nor that phoneNr nor that name + phoneNr.

I took the liberty to add a uniqueness constraint for phoneNr, because "nimemakse" was mentioned and since that implies 
that only one account can be tied to a phone number. I thought this would make the assignment slightly more realistic 
and reflect some consideration of the domain. Beyond that, I did not introduce any additional constraints, so as to stay 
aligned with the task description.

- `name` — required, non-blank
- `phoneNr` — optional; when present must match `^\+?[1-9]\d{6,14}$` (E.164-ish)

### Error format

```json
{
  "code": "VALIDATION_FAILED",
  "message": "Request validation failed",
  "errors": [
    { "field": "name", "message": "must not be blank" }
  ]
}
```

Codes: `NOT_FOUND`, `VALIDATION_FAILED`.

## Interactive docs

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## H2 console

Enabled at http://localhost:8080/h2-console with:

- JDBC URL: `jdbc:h2:mem:account;DB_CLOSE_DELAY=-1`
- User: `sa`
- Password: *(empty)*

Because the database is in-memory, Liquibase runs every startup against a fresh schema — there is no persisted `DATABASECHANGELOG` between runs.

## Database schema

Managed by Liquibase under `src/main/resources/db/changelog/`.

- `db.changelog-master.yaml` — includes all changelogs under `changes/`
- `changes/001-create-account.yaml` — creates the `account` table

## Project layout

```
src/main/java/ee/stivka/account/
├── AccountApiApplication.java
├── api/              # REST layer (controller, DTOs, exception handler)
├── config/           # JPA auditing configuration
├── domain/           # JPA entity
├── repository/       # Spring Data repositories
└── service/          # Business logic + domain exceptions
```

## Tests

- `AccountServiceTest` — unit tests with Mockito
- `AccountControllerIT` — integration tests with `@SpringBootTest` + MockMvc covering CRUD, validation, 404s, and OpenAPI availability
