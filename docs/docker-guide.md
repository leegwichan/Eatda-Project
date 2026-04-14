# Docker 실행 가이드

## 사전 준비

[Docker Desktop](https://www.docker.com/products/docker-desktop/)을 다운로드하여 설치합니다.

설치 후 터미널에서 정상 설치를 확인합니다:
```bash
docker --version
docker compose version
```

## 실행

프로젝트 루트 디렉토리에서 아래 명령어를 실행합니다:

```bash
docker compose up -d
```

초기 실행 시 이미지 다운로드로 인해 시간이 다소 소요될 수 있습니다.
모든 서비스가 정상적으로 시작되면 http://localhost:3000 으로 접속할 수 있습니다.

### 서비스 구성

| 서비스 | 설명 | 포트 |
|--------|------|------|
| **web** | Next.js 프론트엔드 | [localhost:3000](http://localhost:3000) |
| **server** | Spring Boot 백엔드 API | [localhost:8080](http://localhost:8080) |
| **localstack** | AWS S3 로컬 에뮬레이터 (이미지 저장) | localhost:4566 |

서비스 간 의존 관계는 다음과 같습니다:

```
web → server → localstack
```

`localstack`이 healthy 상태가 된 후 `server`가 시작되고, `server`가 healthy 상태가 된 후 `web`이 시작됩니다.

### 서비스 상태 확인

```bash
docker compose ps
```

모든 서비스의 STATUS가 `Up` 또는 `Up (healthy)`이면 정상입니다.

### 로그 확인

```bash
# 전체 로그
docker compose logs

# 특정 서비스 로그 (실시간)
docker compose logs -f server
docker compose logs -f web
```

## 종료

```bash
docker compose down
```

## 이미지 업데이트

최신 이미지로 업데이트하려면 다음 명령어를 실행합니다:

```bash
docker compose pull
docker compose up -d
```

## 환경 변수

Docker Compose 실행 시 환경 변수를 통해 설정을 변경할 수 있습니다.

| 환경 변수 | 설명 | 기본값 |
|-----------|------|--------|
| `KAKAO_API_KEY` | 카카오 REST API 키 ([발급 방법](kakao-api-setup.md)) | `unused-in-dev-mode` |
| `JWT_SECRET_KEY` | JWT 서명 키 | 자동 생성 (로컬 개발용) |
| `SESSION_PASSWORD` | 세션 암호화 키 | 자동 생성 (로컬 개발용) |

환경 변수를 설정하여 실행하는 방법:

**PowerShell:**
```powershell
$env:KAKAO_API_KEY="발급받은_API_KEY"; docker compose up -d
```

**Git Bash / Mac / Linux:**
```bash
KAKAO_API_KEY=발급받은_API_KEY docker compose up -d
```
