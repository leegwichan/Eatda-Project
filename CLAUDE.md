# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

EatDa is a restaurant discovery platform. Monorepo with two applications:
- `server/` — Java 21 + Spring Boot 3.5.0 backend (Gradle)
- `web/` — Next.js 15 + TypeScript frontend (pnpm)

**Always run commands from the respective `web/` or `server/` directory.**

## Common Commands

### Server (`server/`)
```bash
./gradlew build                  # Build (includes tests)
./gradlew clean build            # Clean build
./gradlew test                   # Run all tests
./gradlew test --tests "eatda.service.store.StoreServiceTest"           # Single test class
./gradlew test --tests "eatda.service.store.StoreServiceTest.methodName" # Single test method
./gradlew jacocoTestReport       # Test coverage
./gradlew classes                # Compile only
```

### Web (`web/`)
```bash
pnpm install                     # Install dependencies
pnpm run dev                     # Dev server (port 3000)
pnpm run dev:msw                 # Dev with mock service worker
pnpm run build                   # Production build
pnpm run lint                    # ESLint
pnpm run format:check            # Prettier check
pnpm run format                  # Auto-format
pnpm run test                    # Unit tests (Vitest)
pnpm run test <file>.test.ts     # Single test file
pnpm run test -t "test name"     # Single test case
pnpm dlx playwright test         # E2E tests
```

## Architecture

### Server — Layered Architecture
```
Controller → Service / Facade → Persistence → Repository → Domain
```
- **controller/**: Thin HTTP handlers organized by domain (auth, cheer, member, store, story)
- **service/**: Core business logic
- **facade/**: Orchestrates multiple services (e.g., `CheerRegisterFacade`)
- **persistence/**: Custom data access beyond Spring Data JPA (complex queries, projections)
- **repository/**: Spring Data JPA repositories
- **domain/**: JPA entities with rich Value Objects (`Coordinates`, `District`, `CheerTags`)
- **client/**: External integrations (OAuth, map API, S3 file upload)
- **exception/**: `GlobalExceptionHandler` (@RestControllerAdvice) with `BusinessException`

Key tech: Flyway migrations (MySQL), JWT auth (jjwt), REST Docs → OpenAPI/Swagger (`/docs/swagger`), AWS S3 + Parameter Store.

### Web — Next.js 15 App Router with Co-location
- **src/app/**: Route Groups `(domain)` for page organization
- **src/features/**: Domain-specific feature modules
- **src/shared/**: Cross-cutting utilities, components, hooks

Co-location pattern with underscore-prefixed non-routable folders:
- `_components/`, `_hooks/`, `_types/`, `_schemas/`, `_api/`, `_utils/`, `_constants/`

Key tech: TanStack Query + ky for data fetching, react-hook-form + Zod for forms, vanilla-extract for styling.

## Code Conventions

### Server (Java)
- Lombok: `@RequiredArgsConstructor` for constructor injection, `@Getter`, `@Builder`, `@Slf4j`
- Java records for immutable DTOs
- Tests: JUnit 5 + REST Assured, Given-When-Then pattern, `*Test` suffix

### Web (TypeScript)
- Strict TypeScript, no `any`
- Server Components by default, `"use client"` only when needed
- API calls: `_api/*.api.ts` (ky) + `_api/*.queries.ts` (TanStack Query)
- Style files: `*.css.ts` (vanilla-extract) co-located with components

### Git
- Conventional Commits: `feat:`, `fix:`, `refactor:`, `docs:`, etc.
- Ensure code compiles, lints pass, and tests succeed before pushing
