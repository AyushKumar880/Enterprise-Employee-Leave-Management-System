# Project instructions for the Antigravity agent

This is a Spring Boot 3 / Java 17 / Spring Data JPA / MySQL backend called
**Employee Leave Management System**.

## Architecture
Controller -> Service -> Repository -> JPA/Hibernate -> Database
(see README.md for the full explanation)

## How to run it
- Default profile "dev" uses an in-memory H2 database — no setup needed.
  Run: `mvn spring-boot:run`
- To use real MySQL instead, edit `src/main/resources/application-mysql.properties`
  with real credentials, then run:
  `mvn spring-boot:run -Dspring-boot.run.profiles=mysql`

## What I want help with in this workspace
- Build the project, fix any compile errors, and confirm it starts successfully.
- Hit the REST endpoints (curl or the built-in browser) to confirm CRUD and the
  leave apply/approve/reject flow work end to end.
- If asked to add features (e.g. Swagger/OpenAPI docs, JWT auth, a React frontend,
  email notifications on approval), follow the existing layered structure:
  new entity fields go in entity/, new business rules go in service/, new
  endpoints go in controller/.
- Keep validation in the service layer and DTOs, not in controllers.
- Do not remove the "dev" H2 profile — it's what makes the project runnable
  without external setup.
