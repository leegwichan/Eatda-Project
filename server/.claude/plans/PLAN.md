### 2. `server/.../FileClient.java` — presigned URL 호스트 치환 로직 추가

**파일**: `server/src/main/java/eatda/client/file/FileClient.java`

변경 내용:
- `s3.presigned-base-url` 프로퍼티 주입 (기본값: 빈 문자열)
- `generateUploadPresignedUrl()` 메서드에서 URL 생성 후 호스트 치환

```java
// 생성자에 추가
@Value("${s3.presigned-base-url:}") String presignedBaseUrl

// generateUploadPresignedUrl() 내부, return 전:
String url = s3Presigner.presignPutObject(presignRequest).url().toString();
if (presignedBaseUrl != null && !presignedBaseUrl.isBlank()) {
    URI originalUri = URI.create(url);
    String originalBase = originalUri.getScheme() + "://" + originalUri.getAuthority();
    url = url.replace(originalBase, presignedBaseUrl);
}
return url;
```
