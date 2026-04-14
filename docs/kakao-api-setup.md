# 카카오 API 키 설정

카카오 로그인, 지도 등 카카오 API 기능을 사용하려면 API 키가 필요합니다.
키 없이도 앱은 정상적으로 실행되며, 새로운 가게 검색 & 응원/스토리 등록 기능만 지원하지 않습니다.

## 발급 방법

1. [Kakao Developers](https://developers.kakao.com/)에 로그인합니다.
2. **내 애플리케이션** > **애플리케이션 추가하기**로 앱을 생성합니다.
3. 생성된 앱의 **앱 키** 탭에서 **REST API 키**를 복사합니다.

## 적용 방법

발급받은 API 키를 환경 변수로 전달하여 실행합니다.

**PowerShell:**
```powershell
$env:KAKAO_API_KEY="발급받은_API_KEY"; docker compose up -d
```

**Git Bash / Mac / Linux:**
```bash
KAKAO_API_KEY=발급받은_API_KEY docker compose up -d
```
