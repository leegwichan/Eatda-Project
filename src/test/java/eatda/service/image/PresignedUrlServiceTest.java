package eatda.service.image;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import eatda.controller.web.image.PresignedUrlRequest;
import eatda.controller.web.image.PresignedUrlResponse;
import eatda.exception.BusinessErrorCode;
import eatda.exception.BusinessException;
import eatda.service.BaseServiceTest;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PresignedUrlServiceTest extends BaseServiceTest {

    @Autowired
    private PresignedUrlService presignedUrlService;

    @Nested
    class GeneratePresignedUrl {

        @Test
        void Presigned_URL을_생성할_수_있다() {
            given(fileClient.generateUploadPresignedUrl(anyString(), any(Duration.class)))
                    .willReturn("https://s3.amazonaws.com/presigned-url");

            PresignedUrlRequest request = new PresignedUrlRequest(List.of(
                    new PresignedUrlRequest.FileDetail(1L, "image/png", 1000L)));

            PresignedUrlResponse response = presignedUrlService.generatePresignedUrl(request);

            assertAll(
                    () -> assertThat(response.urls()).hasSize(1),
                    () -> assertThat(response.urls().get(0).url()).isEqualTo("https://s3.amazonaws.com/presigned-url"),
                    () -> assertThat(response.urls().get(0).key()).startsWith("temp/"),
                    () -> assertThat(response.urls().get(0).contentType()).isEqualTo("image/png")
            );
        }

        @Test
        void 파일_정보가_비어있으면_예외를_던진다() {
            PresignedUrlRequest request = new PresignedUrlRequest(List.of());

            assertThatThrownBy(() -> presignedUrlService.generatePresignedUrl(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(BusinessErrorCode.INVALID_EMPTY_FILE_DETAILS.getMessage());
        }

        @Test
        void 허용되지_않는_이미지_타입이면_예외를_던진다() {
            PresignedUrlRequest request = new PresignedUrlRequest(List.of(
                    new PresignedUrlRequest.FileDetail(1L, "image/bmp", 1000L)));

            assertThatThrownBy(() -> presignedUrlService.generatePresignedUrl(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(BusinessErrorCode.INVALID_IMAGE_TYPE.getMessage());
        }

        @Test
        void 파일_크기가_초과하면_예외를_던진다() {
            long overMaxSize = 1024 * 1024 * 10 + 1;
            PresignedUrlRequest request = new PresignedUrlRequest(List.of(
                    new PresignedUrlRequest.FileDetail(1L, "image/png", overMaxSize)));

            assertThatThrownBy(() -> presignedUrlService.generatePresignedUrl(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(BusinessErrorCode.INVALID_MAX_FILE_SIZE.getMessage());
        }
    }
}
