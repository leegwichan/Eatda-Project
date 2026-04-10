# 잇다 - 맛집 탐색에 도움을 주는 참여형 맛집 플랫폼

## 실행 방법

### 1. Docker 설치

[Docker Desktop](https://www.docker.com/products/docker-desktop/)을 다운로드하여 설치합니다.

설치 후 터미널에서 정상 설치를 확인합니다:
```bash
docker --version
docker compose version
```

### 2. 실행

프로젝트 루트 디렉토리에서 아래 명령어를 실행합니다:

```bash
docker compose up -d
```

실행 후 http://localhost:3000 으로 접속할 수 있습니다.

### 3. KAKAO API KEY 설정 (선택)

카카오 로그인, 지도 등 카카오 API 기능을 사용하려면 API 키가 필요합니다.

#### 발급 방법

> TODO: 발급 방법 작성 예정

#### 실행 명령어

**PowerShell:**
```powershell
$env:KAKAO_API_KEY="발급받은_API_KEY"; docker compose up -d
```

**Git Bash / Mac / Linux:**
```bash
KAKAO_API_KEY=발급받은_API_KEY docker compose up -d
```

## 서비스 소개
<img width="100%" alt="image" src="https://github.com/user-attachments/assets/f7b2f5da-1be1-4314-ac90-39056640892b" />
<img width="100%" alt="image" src="https://github.com/user-attachments/assets/e680df5e-9a14-4510-ac90-e74367bc843c" />
<img width="100%" alt="image" src="https://github.com/user-attachments/assets/e9e8d201-e8f8-4d50-b185-3250fabc04c1" />
<img width="100%" alt="image" src="https://github.com/user-attachments/assets/d5de2046-a628-40ad-9386-e98d1fe151d8" />
