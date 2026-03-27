# EatDa Server Developer Guide

This document provides guidelines for AI agents and developers working on the EatDa Server repository.

## 1. Build, Lint, and Test

This project uses **Gradle** with **Java 21**.

### Build
- **Build Project:** `./gradlew build`
- **Clean Build:** `./gradlew clean build`
- **Compile Only:** `./gradlew classes`

### Test
- **Run All Tests:** `./gradlew test`
- **Run Single Test Class:** `./gradlew test --tests "eatda.service.store.StoreServiceTest"`
- **Run Single Test Method:** `./gradlew test --tests "eatda.service.store.StoreServiceTest.testMethodName"`
- **Generate Coverage Report:** `./gradlew jacocoTestReport` (Reports in `build/reports/jacoco/test/html/index.html`)

### Lint & Quality
- **Check Quality:** `./gradlew check` (Runs tests and static analysis)
- **SonarQube:** `./gradlew sonar` (Requires configuration)

## 2. Code Style & Conventions

### General
- **Language:** Java 21
- **Framework:** Spring Boot 3.5.0
- **Formatting:** Use 4 spaces for indentation. Follow standard Java conventions.
- **Lombok:** Use Lombok extensively (`@Getter`, `@RequiredArgsConstructor`, `@Builder`, etc.) to reduce boilerplate.
- **Dependency Injection:** Use Constructor Injection with `@RequiredArgsConstructor`. Avoid `@Autowired` on fields.

### Architecture
- **Layered Architecture:** Controller -> Service -> Repository -> Domain
- **DTOs:** Always use DTOs (Request/Response objects) for API communication. Never expose Entities directly in Controllers.
- **Entities:** JPA Entities in `eatda.domain`. Use `@Entity`, `@Id`, `@GeneratedValue`.

### Controller Layer (`eatda.controller`)
- **Annotation:** `@RestController`, `@RequestMapping`, `@RequiredArgsConstructor`.
- **Validation:** Use `@Validated` on the class and `@Valid` or validation annotations (`@NotNull`, `@Min`, etc.) on parameters/DTOs.
- **Return Type:** Always return `ResponseEntity<T>`.
- **Naming:** `*Controller` (e.g., `StoreController`).
- **Example:**
  ```java
  @RestController
  @RequiredArgsConstructor
  @Validated
  public class StoreController {
      private final StoreService storeService;

      @GetMapping("/api/stores/{storeId}")
      public ResponseEntity<StoreResponse> getStore(@PathVariable Long storeId) {
          return ResponseEntity.ok(storeService.getStore(storeId));
      }
  }
  ```

### Service Layer (`eatda.service`)
- **Annotation:** `@Service`, `@RequiredArgsConstructor`, `@Transactional`.
- **Transactions:** Use `@Transactional(readOnly = true)` for read operations and `@Transactional` for write operations.
- **Business Logic:** Encapsulate all business logic here.
- **Naming:** `*Service` (e.g., `StoreService`).

### Repository Layer (`eatda.repository`)
- **Interface:** Extend `JpaRepository<Entity, ID>`.
- **Naming:** `*Repository` (e.g., `StoreRepository`).

### Error Handling
- **Global Handler:** `GlobalExceptionHandler` handles exceptions globally.
- **Custom Exception:** Use `BusinessException` with `BusinessErrorCode` for logic errors.
- **Error Codes:** Define new error codes in `BusinessErrorCode` enum.
- **Example:**
  ```java
  throw new BusinessException(BusinessErrorCode.STORE_NOT_FOUND);
  ```

### Documentation
- **Swagger/OpenAPI:** API documentation is generated using SpringDoc (`org.springdoc`).
- **RestDocs:** Used for testing and generating snippets.

## 3. Database & AWS
- **Database:** MySQL (Production), H2 (Test). Managed via **Flyway**.
- **AWS:** S3 for image storage, Parameter Store for configuration.

## 4. Key Libraries
- **Spring Boot 3.5.x**
- **Spring Data JPA**
- **QueryDSL** (if applicable)
- **MapStruct** (if applicable for mapping)
- **Lombok**
- **H2 / MySQL Driver**
- **Flyway**
- **Spring Cloud AWS**

## 5. Directory Structure
```
src/main/java/eatda
  ├── client       # External API clients
  ├── config       # Configuration classes
  ├── controller   # REST Controllers & DTOs
  ├── domain       # JPA Entities & Domain logic
  ├── exception    # Global exception handling & Error codes
  ├── facade       # Complex business logic orchestrators
  ├── repository   # Data access layer
  ├── service      # Business logic
  └── EatdaApplication.java
```
