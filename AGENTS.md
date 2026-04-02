# EatDa Project Developer & Agent Guidelines

This `AGENTS.md` file contains instructions for AI agents and developers operating in the EatDa monorepo, which consists of a `web` (Next.js/React) and `server` (Java 21/Spring Boot) directory.

---

## 1. Directory Structure
The repository is split into two primary applications:
*   `web/`: Frontend Next.js 15 App Router project using TypeScript, Tailwind CSS, Vitest, and Playwright.
*   `server/`: Backend Java 21 Spring Boot project using Gradle.

Always run commands in the respective `web/` or `server/` directory.

---

## 2. Web (Frontend) Guidelines

### Build, Lint, and Test Commands
All web commands must be run inside the `web/` directory.
*   **Install Dependencies:** `pnpm install`
*   **Dev Server:** `pnpm run dev` (or `pnpm run dev:msw` with MSW mock server)
*   **Build:** `pnpm run build`
*   **Lint:** `pnpm run lint` and `pnpm run format:check`
*   **Format Code:** `pnpm run format` (uses Prettier)
*   **Run All Unit Tests:** `pnpm run test` (uses Vitest)
*   **Run Single Test File:** `pnpm run test <file-name>.test.ts`
*   **Run Single Test Case:** `pnpm run test -t "test case name"`
*   **Run E2E Tests:** `pnpm dlx playwright test`

### Code Style Guidelines
*   **Frameworks:** Next.js 15 App Router, React 19, Tailwind CSS.
*   **TypeScript:** Strict typing is mandatory. Avoid `any`. Define interfaces and types in respective `_types/` folders.
*   **Architecture & Colocation:** Adhere to the colocation pattern. Keep related files close together.
    *   Use underscore prefixes for non-routable folders in `app/` (e.g., `_components/`, `_utils/`).
    *   Use Route Groups `(groupName)` to group related pages and domain logic.
    *   Shared logic across the app should reside in `src/components/`, `src/hooks/`, or `src/utils/`.
*   **Component Structure:**
    *   Separate logic from UI (Custom hooks for complex state).
    *   Prefer Server Components by default. Use `"use client"` only for interactivity, state, and browser APIs.
    *   Use functional components with explicit `React.FC` or direct function declarations.
*   **Data Validation:** Use Zod for schema validation (e.g., in `_schemas/`).
*   **Imports:** Group imports logically (React -> Next.js -> third-party -> local relative imports). Use path aliases (e.g., `@/components/`) where configured.
*   **Error Handling:** Use React Error Boundaries `error.tsx` for route-level errors. Handle API errors gracefully via try/catch and toast notifications or specific UI states.
*   **Naming Conventions:**
    *   Components & Files containing components: PascalCase (e.g., `Button.tsx`).
    *   Hooks: camelCase with `use` prefix (e.g., `useAuth.ts`).
    *   Constants & Utils: camelCase or UPPER_SNAKE_CASE (e.g., `formatDate.ts`, `MAX_RETRIES`).
    *   Types & Interfaces: PascalCase, consider appending `Type` or `Interface` if ambiguous, but generally just descriptive PascalCase (e.g., `User`, `ApiResponse`).

---

## 3. Server (Backend) Guidelines

### Build, Lint, and Test Commands
All server commands must be run inside the `server/` directory using the Gradle wrapper (`./gradlew`).
*   **Build Project:** `./gradlew build`
*   **Clean Build:** `./gradlew clean build`
*   **Compile Only:** `./gradlew classes`
*   **Run All Tests:** `./gradlew test`
*   **Run Single Test Class:** `./gradlew test --tests "eatda.service.store.StoreServiceTest"`
*   **Run Single Test Method:** `./gradlew test --tests "eatda.service.store.StoreServiceTest.testMethodName"`
*   **Run Tests with Coverage:** `./gradlew jacocoTestReport`

### Code Style Guidelines
*   **Language & Framework:** Java 21, Spring Boot.
*   **Formatting:** Follow the standard Java conventions (e.g., Google Java Style Guide or checkstyle rules if applied). Use spaces for indentation (usually 4 spaces).
*   **Annotations:** Prefer Lombok for boilerplate reduction (e.g., `@Getter`, `@Setter`, `@RequiredArgsConstructor`, `@Builder`, `@Slf4j`).
*   **Architecture (Layered):**
    *   `controller`: Handles HTTP requests, input validation, and maps responses. Keep thin.
    *   `service`: Contains core business logic.
    *   `repository`: Handles data access (Spring Data JPA).
    *   `domain` / `entity`: JPA entities and core domain models.
    *   `dto`: Data Transfer Objects for API requests/responses. Use record types where appropriate.
*   **Types & Immutability:** Utilize Java 21 features like `record` for immutable data carriers (DTOs). Use `final` where values shouldn't change.
*   **Dependency Injection:** Prefer constructor injection over field injection (`@Autowired`). Use Lombok's `@RequiredArgsConstructor`.
*   **Error Handling:**
    *   Use a global `@RestControllerAdvice` to handle exceptions.
    *   Throw custom business exceptions extending `RuntimeException` (e.g., `EntityNotFoundException`).
    *   Return consistent JSON error responses with meaningful HTTP status codes.
*   **Naming Conventions:**
    *   Classes: PascalCase (e.g., `StoreService`).
    *   Methods & Variables: camelCase (e.g., `calculateTotal()`).
    *   Constants: UPPER_SNAKE_CASE (e.g., `MAX_LOGIN_ATTEMPTS`).
    *   Database Tables & Columns: snake_case mapping.
*   **Testing:**
    *   Use JUnit 5 for tests.
    *   Write tests following the Arrange-Act-Assert (Given-When-Then) pattern.
    *   Use Mockito for mocking dependencies in unit tests.
    *   Suffix test classes with `Test` (e.g., `OrderServiceTest`).
    *   Test methods should clearly describe the behavior being tested (e.g., `shouldThrowExceptionWhenUserNotFound`).

---

## 4. General Git & PR Guidelines
*   **Commits:** Use Conventional Commits format (e.g., `feat: add new login page`, `fix: resolve null pointer exception in auth`).
*   **Pull Requests:** Provide a clear summary of changes, motivation, and link to relevant issue/ticket if applicable.
*   **Before pushing:** Always ensure code compiles, linters pass, and unit tests succeed in the respective `web/` or `server/` directory.
