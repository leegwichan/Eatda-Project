package eatda.client.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import eatda.exception.BusinessErrorCode;
import eatda.exception.BusinessException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CopyObjectResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

class LocalStackFileClientTest {

    private S3Client s3Client;
    private S3Presigner s3Presigner;
    private LocalStackFileClient fileClient;

    @BeforeEach
    void setUp() {
        this.s3Client = mock(S3Client.class);
        this.s3Presigner = mock(S3Presigner.class);
        this.fileClient = new LocalStackFileClient(
                s3Client, "test-bucket", s3Presigner, "http://localhost:4566", "http://localstack:4566");
    }

    @Nested
    class GetImageUrl {

        @Test
        void externalUrl과_bucket과_이미지_경로를_조합하여_URL을_반환한다() {
            String actual = fileClient.getImageUrl("cheer/123/image.jpg");

            assertThat(actual).isEqualTo("http://localhost:4566/test-bucket/cheer/123/image.jpg");
        }
    }

    @Nested
    class GenerateUploadPresignedUrl {

        @Test
        void internalUrl을_externalUrl로_치환하여_반환한다() throws MalformedURLException {
            String fileKey = "test-file-key.jpg";
            String originalUrl = "http://localstack:4566/test-bucket/test-file-key.jpg";
            doReturn(mockPresignedRequest(originalUrl)).when(s3Presigner)
                    .presignPutObject(any(PutObjectPresignRequest.class));

            String actual = fileClient.generateUploadPresignedUrl(fileKey, Duration.ofMinutes(10));

            assertThat(actual).isEqualTo("http://localhost:4566/test-bucket/test-file-key.jpg");
        }

        @Test
        void internalUrl이_포함되지_않으면_원본_URL을_그대로_반환한다() throws MalformedURLException {
            String fileKey = "test-file-key.jpg";
            String expected = "https://s3.amazonaws.com/test-bucket/test-file-key.jpg";
            doReturn(mockPresignedRequest(expected)).when(s3Presigner)
                    .presignPutObject(any(PutObjectPresignRequest.class));

            String actual = fileClient.generateUploadPresignedUrl(fileKey, Duration.ofMinutes(10));

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void 문제가_생길_경우_서비스_에러_처리를_한다() {
            String fileKey = "test-file-key.jpg";
            doThrow(SdkClientException.create("Presigned URL generation failed"))
                    .when(s3Presigner).presignPutObject(any(PutObjectPresignRequest.class));

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> fileClient.generateUploadPresignedUrl(fileKey, Duration.ofMinutes(10)));

            assertThat(exception.getErrorCode()).isEqualTo(BusinessErrorCode.PRESIGNED_URL_GENERATION_FAILED);
        }

        private PresignedPutObjectRequest mockPresignedRequest(String url) throws MalformedURLException {
            PresignedPutObjectRequest request = mock(PresignedPutObjectRequest.class);
            doReturn(new URL(url)).when(request).url();
            return request;
        }
    }

    @Nested
    class MoveFiles {

        @Test
        void 임시_파일들을_영구_위치로_이동한다() {
            String domainName = "cheer";
            long domainId = 123L;
            List<String> tempImageKeys = List.of("temp/temp1.jpg", "temp/temp2.jpg");

            doReturn(CopyObjectResponse.builder().build()).when(s3Client).copyObject(any(CopyObjectRequest.class));
            doReturn(DeleteObjectsResponse.builder().build()).when(s3Client)
                    .deleteObjects(any(DeleteObjectsRequest.class));

            FileMovingResult result = fileClient.moveFiles(domainName, domainId, tempImageKeys);

            assertThat(result.findNewPath("temp/temp1.jpg")).isEqualTo("cheer/123/temp1.jpg");
            assertThat(result.findNewPath("temp/temp2.jpg")).isEqualTo("cheer/123/temp2.jpg");
        }

        @Test
        void 파일_복사_중_실패하면_롤백_후_예외를_던진다() {
            String domainName = "cheer";
            long domainId = 123L;
            List<String> tempImageKeys = List.of("temp/temp1.jpg", "temp/temp2.jpg");

            doReturn(CopyObjectResponse.builder().build())
                    .doThrow(SdkClientException.create("copy failed"))
                    .when(s3Client).copyObject(any(CopyObjectRequest.class));
            doReturn(DeleteObjectsResponse.builder().build()).when(s3Client)
                    .deleteObjects(any(DeleteObjectsRequest.class));

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> fileClient.moveFiles(domainName, domainId, tempImageKeys));

            assertThat(exception.getErrorCode()).isEqualTo(BusinessErrorCode.FAIL_TEMP_IMAGE_PROCESS);
        }
    }

    @Nested
    class DeleteFiles {

        @Test
        void 파일_경로_목록으로_삭제_요청을_보낸다() {
            doReturn(DeleteObjectsResponse.builder().build()).when(s3Client)
                    .deleteObjects(any(DeleteObjectsRequest.class));

            fileClient.deleteFiles(List.of("cheer/123/image1.jpg", "cheer/123/image2.jpg"));

            verify(s3Client).deleteObjects(any(DeleteObjectsRequest.class));
        }

        @Test
        void 빈_목록이면_삭제_요청을_보내지_않는다() {
            fileClient.deleteFiles(List.of());

            verify(s3Client, never()).deleteObjects(any(DeleteObjectsRequest.class));
        }
    }
}
