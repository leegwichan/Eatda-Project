# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

- **Build:** `./gradlew build`
- **Run all tests:** `./gradlew test`
- **Run single test class:** `./gradlew test --tests "eatda.service.store.StoreServiceTest"`
- **Run single test method:** `./gradlew test --tests "eatda.service.store.StoreServiceTest.testMethodName"`
- **Check (tests + static analysis):** `./gradlew check`
- **Coverage report:** `./gradlew jacocoTestReport` (output: `build/reports/jacoco/test/html/index.html`)

## Architecture

Spring Boot 3.5.0 / Java 21 / Gradle. Root package: `eatda` (group: `net.eatda`).

### Layered Architecture

```
Controller (+ Request/Response DTOs) → Service → Persistence → Repository → Domain (JPA Entities)
```

- **Controller** (`eatda.controller.{domain}`): REST endpoints. Always returns `ResponseEntity<T>`. Uses `@Validated` for bean validation.
- **Service** (`eatda.service.{domain}`): Business logic. `@Transactional(readOnly = true)` for reads, `@Transactional` for writes.
- **Persistence** (`eatda.persistence.{domain}`): Complex query logic with custom result objects (e.g., `StorePreviewResult`, `LoginResult`). Sits between Service and Repository when queries go beyond simple JPA methods.
- **Repository** (`eatda.repository.{domain}`): JPA repository interfaces extending `JpaRepository`.
- **Domain** (`eatda.domain.{domain}`): JPA entities with value objects. Base class `AuditingEntity` provides `createdAt`.

### Other Key Packages

- **`eatda.client`**: External API clients — `oauth` (Kakao login), `map` (Kakao Maps), `file` (S3 presigned URLs).
- **`eatda.web`** (aliased as `controller.web` in code): JWT (`JwtManager`), authentication argument resolvers.
- **`eatda.exception`**: `BusinessException` + `BusinessErrorCode` enum. Error codes are domain-prefixed (MEM, STO, CHE, AUTH, IMG, STY, MAP). `GlobalExceptionHandler` handles all exceptions.

### Domain Model

Member (OAuth-based) → Cheer (store appreciation, with images + tags) → Store (from Kakao Maps, with address/coordinates/district). Story (member experience about a store, with images).

## Code Conventions

- **Lombok everywhere**: `@Getter`, `@RequiredArgsConstructor`, `@Builder`. Constructor injection only — no `@Autowired` on fields.
- **DTOs**: Request/Response records live in controller packages. Never expose entities in API responses.
- **Error throwing**: `throw new BusinessException(BusinessErrorCode.STORE_NOT_FOUND);`

## Test Setup

- **Integration tests** using `@SpringBootTest(webEnvironment = RANDOM_PORT)` + RestAssured.
- **Base class**: `BaseControllerTest` provides test fixtures, mocked external clients (`OauthClient`, `MapClient`, `FileClient`), and JWT helpers (`accessToken()`, `refreshToken()`).
- **Database cleanup**: `@ExtendWith(DatabaseCleaner.class)` wipes DB between tests.
- **Test data**: Generator classes in `eatda.fixture` (e.g., `MemberGenerator`, `StoreGenerator`) create test entities via repositories.
- **DB**: H2 in-memory for tests, MySQL for production. Flyway manages migrations.

## API Documentation

SpringDoc OpenAPI + Spring REST Docs. `./gradlew openapi3` generates the spec. Swagger UI served from `src/main/resources/static/docs/`.
