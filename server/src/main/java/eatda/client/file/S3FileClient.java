package eatda.client.file;

import eatda.exception.BusinessErrorCode;
import eatda.exception.BusinessException;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Slf4j
@Component
@ConditionalOnProperty(name = "local-docker.enabled", havingValue = "false", matchIfMissing = true)
public class S3FileClient implements FileClient {

    private static final String PATH_DELIMITER = "/";
    private final S3Client s3Client;
    private final String bucket;
    private final S3Presigner s3Presigner;
    private final String cdnBaseUrl;

    public S3FileClient(S3Client s3Client,
                        @Value("${spring.cloud.aws.s3.bucket}") String bucket,
                        S3Presigner s3Presigner,
                        @Value("${cdn.base-url}") String cdnBaseUrl) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.s3Presigner = s3Presigner;
        this.cdnBaseUrl = cdnBaseUrl;
    }

    @Override
    public String getImageUrl(String imagePath) {
        return cdnBaseUrl + "/" + imagePath;
    }

    @Override
    public String generateUploadPresignedUrl(String fileKey, Duration signatureDuration) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileKey)
                .build();
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .putObjectRequest(putObjectRequest)
                .signatureDuration(signatureDuration)
                .build();

        try {
            return s3Presigner.presignPutObject(presignRequest).url().toString();
        } catch (SdkException exception) {
            throw new BusinessException(BusinessErrorCode.PRESIGNED_URL_GENERATION_FAILED);
        }
    }

    @Override
    public FileMovingResult moveFiles(String domainName, long domainId, List<String> beforePaths) {
        Map<String, String> moveResult = new HashMap<>();

        try {
            for (String beforePath : beforePaths) {
                String fileName = extractFileName(beforePath);
                String afterPath = domainName + PATH_DELIMITER + domainId + PATH_DELIMITER + fileName;
                copyObject(beforePath, afterPath);
                moveResult.put(beforePath, afterPath);
            }
            deleteFiles(beforePaths);
        } catch (SdkException sdkException) {
            log.error("S3 파일 이동 중 실패. 롤백 수행. moveResult={}", moveResult, sdkException);
            deleteFiles(moveResult.values());
            throw new BusinessException(BusinessErrorCode.FAIL_TEMP_IMAGE_PROCESS);
        }
        return new FileMovingResult(moveResult);
    }

    private String extractFileName(String fullName) {
        int index = fullName.lastIndexOf(PATH_DELIMITER);
        return index == -1 ? fullName : fullName.substring(index + 1);
    }

    private void copyObject(String beforePath, String afterPath) {
        CopyObjectRequest copyReq = CopyObjectRequest.builder()
                .sourceBucket(bucket)
                .sourceKey(beforePath)
                .destinationBucket(bucket)
                .destinationKey(afterPath)
                .build();
        s3Client.copyObject(copyReq);
    }

    @Override
    public void deleteFiles(Collection<String> paths) {
        if (paths.isEmpty()) {
            return;
        }

        List<ObjectIdentifier> keysToDelete = paths.stream()
                .map(path -> ObjectIdentifier.builder().key(path).build())
                .toList();
        DeleteObjectsRequest multiDeleteRequest = DeleteObjectsRequest.builder()
                .bucket(bucket)
                .delete(Delete.builder().objects(keysToDelete).build())
                .build();
        s3Client.deleteObjects(multiDeleteRequest);
    }
}
