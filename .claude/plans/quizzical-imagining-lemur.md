# Dev Login 백엔드 구현 계획

## Context

Docker Hub public 이미지로 배포 시 Kakao OAuth 없이 로그인할 수 있는 dev-login 기능이 필요하다. `DEV_LOGIN_ENABLED=true` 환경변수로 활성화하며, 클라이언트가 socialId/email/nickname을 직접 전달하여 회원 생성 및 로그인한다.

---

## 변경 파일 목록

| 파일 | 작업 |
|------|------|
| `src/main/java/eatda/config/DevLoginProperties.java` | **신규** - 설정 클래스 |
| `src/main/java/eatda/controller/auth/DevLoginRequest.java` | **신규** - 요청 DTO |
| `src/main/java/eatda/controller/auth/DevAuthController.java` | **신규** - dev-login 엔드포인트 |
| `src/main/resources/application-local-docker.yml` | **수정** - dev-login 설정 + OAuth 더미값 |

---

## 구현 상세

### 1. DevLoginProperties (신규)

`src/main/java/eatda/config/DevLoginProperties.java`

```java
@ConfigurationProperties(prefix = "dev-login")
public record DevLoginProperties(boolean enabled) {}
```

- `dev-login.enabled` 값을 바인딩 (기본값 false)

### 2. DevLoginRequest (신규)

`src/main/java/eatda/controller/auth/DevLoginRequest.java`

```java
public record DevLoginRequest(String socialId, String email, String nickname) {}
```

- 클라이언트가 socialId, email, nickname을 직접 전달

### 3. DevAuthController (신규)

`src/main/java/eatda/controller/auth/DevAuthController.java`

- `@ConditionalOnProperty(name = "dev-login.enabled", havingValue = "true")`로 조건부 등록
- `@EnableConfigurationProperties(DevLoginProperties.class)`로 설정 바인딩
- `POST /api/auth/dev-login` 엔드포인트

**로직 흐름** (기존 `AuthController.login()`과 동일한 패턴):
1. `DevLoginRequest`에서 socialId, email, nickname 추출
2. `new Member(socialId, email, nickname)` 생성
3. `memberPersistence.login(member)` 호출 → `LoginResult` (기존 upsert 로직 재사용)
4. `MemberResponse` 생성
5. `jwtManager.issueAccessToken()` / `issueRefreshToken()` 발급
6. `LoginResponse(token, member)` 반환 (201 Created)

**재사용하는 기존 코드:**
- `MemberPersistence.login(Member)` — `src/main/java/eatda/persistence/member/MemberPersistence.java:39`
- `JwtManager.issueAccessToken()` / `issueRefreshToken()` — `src/main/java/eatda/controller/web/jwt/JwtManager.java`
- `LoginResponse`, `TokenResponse`, `MemberResponse` — 기존 DTO 그대로 사용

### 4. application-local-docker.yml 수정

`src/main/resources/application-local-docker.yml`

추가할 설정:
```yaml
dev-login:
  enabled: ${DEV_LOGIN_ENABLED:false}
```

OAuth/Kakao 더미값 설정 (환경변수 없을 때 기본값 제공):
```yaml
oauth:
  client-id: ${OAUTH_CLIENT_ID:unused-in-dev-mode}

kakao:
  api-key: ${KAKAO_API_KEY:unused-in-dev-mode}
```

이렇게 하면 `OAUTH_CLIENT_ID`와 `KAKAO_API_KEY` 환경변수 없이도 앱이 정상 기동된다.

---

## 설계 결정 사항

- **별도 컨트롤러 사용**: `@ConditionalOnProperty`로 빈 자체를 조건부 등록하면, `dev-login.enabled=false`일 때 엔드포인트가 아예 존재하지 않아 프로덕션 안전성이 높다.
- **AuthService를 거치지 않음**: AuthService는 OauthClient에 의존하므로, DevAuthController에서 MemberPersistence와 JwtManager를 직접 사용한다. 기존 AuthController.login()의 토큰 발급 패턴을 그대로 따른다.

---

## 검증 방법

1. `./gradlew test` — 기존 테스트가 깨지지 않는지 확인
2. `./gradlew build` — 빌드 성공 확인
3. Docker 환경에서 `DEV_LOGIN_ENABLED=true`로 기동 후:
   - `POST /api/auth/dev-login` 호출 → 201 + LoginResponse 확인
   - 같은 socialId로 재호출 → `isSignUp: false` 확인
   - `DEV_LOGIN_ENABLED` 미설정 시 → 404 확인
