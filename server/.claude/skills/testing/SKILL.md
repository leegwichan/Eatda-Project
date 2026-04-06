name: writing-test
description: Java/Spring 서비스의 테스트 작성

Write tests for the specified source code following the project's established test conventions.

## Inputs

- **target**: The source class or method to test (e.g., `StoreService`, `StoreController#getStore`)
- **type** (optional): `controller` | `service` | `repository` | `domain` | `document`. Auto-detected from target class if omitted.

## Instructions

### 1. Determine Test Type & Base Class

| Target Layer | Base Class | WebEnvironment | Key Annotation |
|---|---|---|---|
| Controller (`eatda.controller.*`) | `BaseControllerTest` | `RANDOM_PORT` | `@ExtendWith(DatabaseCleaner.class)` |
| Service (`eatda.service.*`) | `BaseServiceTest` | `NONE` | `@ExtendWith(DatabaseCleaner.class)` |
| Persistence (`eatda.persistence.*`) | `BasePersistenceTest` | `NONE` | `@ExtendWith(DatabaseCleaner.class)` |
| Repository (`eatda.repository.*`) | `BaseRepositoryTest` | N/A | `@DataJpaTest` |
| Domain (`eatda.domain.*`) | None (plain JUnit) | N/A | N/A |
| Document (`eatda.document.*`) | `BaseDocumentTest` | `RANDOM_PORT` | `@ExtendWith({RestDocumentationExtension.class, MockitoExtension.class})` |

### 2. Test File Location

Mirror the source package structure under `src/test/java/`:
```
src/main/java/eatda/service/store/StoreService.java
  → src/test/java/eatda/service/store/StoreServiceTest.java
```

### 3. Test Class Structure

```java
class {ClassName}Test extends {BaseClass} {

    @Autowired
    private {TargetClass} {targetField};  // Service/Repository tests only

    @Nested
    class {MethodName} {    // One @Nested class per public method

        @Test
        void 한글로_테스트_의도를_설명한다() {
            // Arrange - Generator로 테스트 데이터 생성
            // Act - 테스트 대상 메서드 실행
            // Assert - AssertJ/JUnit으로 검증
        }

        @Test
        void 예외_케이스를_검증한다() {
            // ...
        }
    }
}
```

### 4. Test Method Naming

- Korean descriptive names with underscores: `회원_정보를_조회할_수_있다`
- Patterns:
  - Success: `{기능}을_할_수_있다`, `{결과}를_반환한다`
  - Exception: `{조건}이면_예외가_발생한다`, `존재하지_않는_{객체}이면_예외를_던진다`
  - Filtering: `{조건}으로_필터링하여_조회할_수_있다`

### 5. Test Data Setup

Use generator classes from `eatda.fixture`:
- `memberGenerator.generate("socialId")` - basic member
- `memberGenerator.generateRegisteredMember("nickname", "email", "socialId", "phone")` - full member
- `storeGenerator.generate("kakaoId", "lotNumberAddress")` - basic store
- `storeGenerator.generate("kakaoId", "address", District.GANGNAM, StoreCategory.KOREAN)` - full store
- `cheerGenerator.generateCommon(member, store)` - cheer
- `cheerTagGenerator.generate(cheer, List.of(CheerTagName.INSTAGRAMMABLE))` - tags
- `storyGenerator.generate(member, "kakaoId", "storeName")` - story
- `cheerImageGenerator.generate(cheer)` / `storyImageGenerator.generate(story)` - images

All generators are available as `protected` fields in base test classes. Do NOT create entities manually with `new` — always use generators (except in domain unit tests).

### 6. Controller Test Pattern (RestAssured)

```java
class {Controller}Test extends BaseControllerTest {

    @Nested
    class {EndpointMethod} {

        @Test
        void 성공_케이스() {
            // Arrange
            Member member = memberGenerator.generate("111");
            Store store = storeGenerator.generate("kakaoId", "address");

            // Act & Assert
            {ResponseType} response = given()
                    .header(HttpHeaders.AUTHORIZATION, accessToken(member))  // if auth required
                    .contentType(ContentType.JSON)                           // if request body
                    .body(request)                                           // if request body
                    .queryParam("key", "value")                              // if query params
                    .pathParam("id", store.getId())                          // if path params
                    .when()
                    .get("/api/endpoint/{id}")
                    .then()
                    .statusCode(200)
                    .extract().as({ResponseType}.class);

            assertAll(
                    () -> assertThat(response.field1()).isEqualTo(expected1),
                    () -> assertThat(response.field2()).isEqualTo(expected2)
            );
        }

        @Test
        void 인증되지_않은_사용자는_401을_반환한다() {
            given()
                    .when()
                    .get("/api/endpoint")
                    .then()
                    .statusCode(401);
        }
    }
}
```

### 7. Persistence Test Pattern

```java
class {Persistence}Test extends BasePersistenceTest {

    @Autowired
    private {PersistenceClass} {persistenceField};

    @Nested
    class {MethodName} {

        @Test
        void 정상_동작한다() {
            Member member = memberGenerator.generateRegisteredMember("커찬", "ac@kakao.com", "123", "01012341235");
            // ... setup

            {ReturnType} result = {persistenceField}.{method}(args);

            assertAll(
                    () -> assertThat(result.field()).isEqualTo(expected)
            );
        }

        @Test
        void 조건에_해당하면_예외를_던진다() {
            // ... setup for exception branch

            assertThatThrownBy(() -> {persistenceField}.{method}(args))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(BusinessErrorCode.{ERROR_CODE}.getMessage());
        }
    }
}
```

### 8. Service Test Pattern

```java
class {Service}Test extends BaseServiceTest {

    @Autowired
    private {ServiceClass} {serviceField};

    @Nested
    class {MethodName} {

        @Test
        void 정상_동작한다() {
            Member member = memberGenerator.generate("123");
            // ... setup

            {ReturnType} result = {serviceField}.{method}(args);

            assertAll(
                    () -> assertThat(result.field()).isEqualTo(expected)
            );
        }

        @Test
        void 존재하지_않으면_예외를_던진다() {
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> {serviceField}.{method}(invalidArgs));

            assertThat(exception.getErrorCode())
                    .isEqualTo(BusinessErrorCode.{ERROR_CODE});
        }
    }
}
```

### 9. Domain Unit Test Pattern

```java
class {Entity}Test {

    private static final Member DEFAULT_MEMBER = new Member("socialId", "email@kakao.com", "nickname");
    private static final Store DEFAULT_STORE = Store.builder()
            .kakaoId("kakaoId").name("name")
            .lotNumberAddress("address").roadNameAddress("road")
            .latitude(37.0).longitude(127.0)
            .district(District.GANGNAM).category(StoreCategory.KOREAN)
            .build();

    @Nested
    class Validate {

        @ParameterizedTest
        @NullAndEmptySource
        void 필드가_비어있으면_예외를_던진다(String value) {
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> new {Entity}(/* args with invalid value */));

            assertThat(exception.getErrorCode())
                    .isEqualTo(BusinessErrorCode.{ERROR_CODE});
        }

        @Test
        void 정상적으로_생성된다() {
            assertThatCode(() -> new {Entity}(/* valid args */))
                    .doesNotThrowAnyException();
        }
    }
}
```

### 10. Repository Test Pattern

```java
class {Repository}Test extends BaseRepositoryTest {

    @Autowired
    private {RepositoryClass} {repositoryField};

    @Nested
    class {QueryMethodName} {

        @Test
        void 조건에_맞는_결과를_조회한다() {
            // Arrange with generators
            Store store = storeGenerator.generate("1235", "address", District.SEONGBUK, StoreCategory.KOREAN);

            // Act
            List<Store> actual = {repositoryField}.{method}(args);

            // Assert
            assertThat(actual).map(Store::getId)
                    .containsExactlyInAnyOrder(store.getId());
        }
    }
}
```

### 11. Assertion Conventions

- Use **AssertJ** (`assertThat`) as the primary assertion library
- Use **`assertAll()`** from JUnit 5 when verifying multiple fields of a response
- Use **`assertThrows()`** for exception testing, then verify error code with `assertThat`
- Use **`assertThatCode(...).doesNotThrowAnyException()`** for no-exception verification
- Use **`@ParameterizedTest`** with `@NullAndEmptySource` for null/empty validation tests

### 12. Static Imports

Always include these as needed:
```java
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
```

### 13. Rules

- **Read the source class first** before writing any test. Understand every public method, its parameters, return type, and possible exceptions.
- **One `@Nested` class per public method** being tested.
- **Each layer tests only its own logic** — test the happy case and edge cases (if/branch) of the method itself. Assume sub-methods (called methods from lower layers) pass their happy case. Sub-method edge cases are tested in the sub-method's own test class. However, if the method uses **private helpers**, their branches count as the method's own edge cases and must be tested here.
- **Never mock repositories or services within integration tests** — use generators to set up real data.
- **External clients (OauthClient, MapClient, FileClient) are already mocked** in base classes via `@MockitoBean`.
- **Do not add `@Transactional` to test classes or methods** — `DatabaseCleaner` handles isolation.
- **Run the test** after writing: `./gradlew test --tests "eatda.{package}.{TestClass}"` to verify it compiles and passes.
