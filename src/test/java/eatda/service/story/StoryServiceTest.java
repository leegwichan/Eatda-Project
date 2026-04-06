package eatda.service.story;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import eatda.client.file.FileMovingResult;
import eatda.client.map.MapClientStoreSearchResult;
import eatda.controller.story.StoriesDetailResponse;
import eatda.controller.story.StoriesResponse;
import eatda.controller.story.StoryImageResponse;
import eatda.controller.story.StoryRegisterImage;
import eatda.controller.story.StoryRegisterRequest;
import eatda.controller.story.StoryRegisterResponse;
import eatda.controller.story.StoryResponse;
import eatda.domain.member.Member;
import eatda.domain.store.District;
import eatda.domain.store.Store;
import eatda.domain.store.StoreCategory;
import eatda.domain.store.StoreSearchResult;
import eatda.domain.story.Story;
import eatda.exception.BusinessErrorCode;
import eatda.exception.BusinessException;
import eatda.service.BaseServiceTest;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class StoryServiceTest extends BaseServiceTest {

    @Nested
    @Transactional
    class RegisterStory {

        private Member member;
        private StoreSearchResult storeSearchResult;

        @BeforeEach
        void setUp() {
            member = memberGenerator.generate("12345");
            storeSearchResult = new StoreSearchResult(
                    "123", StoreCategory.KOREAN, "02-755-5232", "곱창", "http://place.map.kakao.com/123",
                    "서울시 강남구 역삼동 123-45",
                    "서울시 강남구 사사로 3길 12-24",
                    District.GANGNAM, 37.5665, 126.9780);
        }

        @Test
        void 스토리_등록에_성공한다() {
            StoryRegisterRequest request =
                    new StoryRegisterRequest("농민백암순대 본점", "123", "미쳤다 여기", List.of());
            given(mapClient.searchStores(anyString()))
                    .willReturn(List.of(
                            new MapClientStoreSearchResult("123", "FD6", "음식점 > 한식 > 국밥", "010-1234-1234", "농민백암순대 본점",
                                    "https://yapp.co.kr", "서울 강남구 대치동 896-33", "서울 강남구 선릉로86길 40-4", 37.5d, 127.0d),
                            new MapClientStoreSearchResult("456", "FD6", "음식점 > 한식 > 국밥", "010-1234-1234", "농민백암순대 시청점",
                                    "http://yapp.kr", "서울 중구 북창동 19-4", null, 37.5d, 127.0d)
                    ));
            given(fileClient.moveFiles(any(String.class), anyLong(), anyList()))
                    .willReturn(new FileMovingResult(Collections.emptyMap()));

            var response = storyService.registerStory(request, member.getId());

            Story savedStory = storyRepository.findById(response.storyId()).orElseThrow();
            assertAll(
                    () -> assertThat(savedStory.getMember().getId()).isEqualTo(member.getId()),
                    () -> assertThat(savedStory.getStoreKakaoId()).isEqualTo("123"),
                    () -> assertThat(savedStory.getStoreName()).isEqualTo("농민백암순대 본점"),
                    () -> assertThat(savedStory.getStoreRoadAddress()).isEqualTo("서울 강남구 선릉로86길 40-4"),
                    () -> assertThat(savedStory.getStoreLotNumberAddress()).isEqualTo("서울 강남구 대치동 896-33"),
                    () -> assertThat(savedStory.getStoreCategory()).isEqualTo(StoreCategory.KOREAN),
                    () -> assertThat(savedStory.getDescription()).isEqualTo("미쳤다 여기"),
                    () -> assertThat(savedStory.getImages()).isEmpty()
            );
        }

        @Test
        void 이미지_이동_중_실패하면_스토리를_삭제한다() {
            StoryRegisterRequest request =
                    new StoryRegisterRequest("농민백암순대 본점", "123", "미쳤다 여기",
                            List.of(new StoryRegisterImage("temp-key-1", 1L, "image/jpeg", 1000L)));
            given(mapClient.searchStores(anyString()))
                    .willReturn(List.of(
                            new MapClientStoreSearchResult("123", "FD6", "음식점 > 한식 > 국밥", "010-1234-1234", "농민백암순대 본점",
                                    "https://yapp.co.kr", "서울 강남구 대치동 896-33", "서울 강남구 선릉로86길 40-4", 37.5d, 127.0d)
                    ));
            given(fileClient.moveFiles(any(String.class), anyLong(), anyList()))
                    .willThrow(new BusinessException(BusinessErrorCode.FAIL_TEMP_IMAGE_PROCESS));

            assertThatThrownBy(() -> storyService.registerStory(request, member.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(BusinessErrorCode.FAIL_TEMP_IMAGE_PROCESS.getMessage());
            assertThat(storyRepository.count()).isZero();
        }

        @Test
        void 스토리_등록_시_이미지도_함께_저장된다() {
            StoryRegisterImage image2 =
                    new StoryRegisterImage("temp-key-2", 2L, "image/jpeg", 2000L);
            StoryRegisterImage image1 =
                    new StoryRegisterImage("temp-key-1", 1L, "image/jpeg", 1000L);
            StoryRegisterRequest request =
                    new StoryRegisterRequest("농민백암순대 본점", "123", "미쳤다 여기", List.of(image2, image1));
            given(mapClient.searchStores(anyString()))
                    .willReturn(List.of(
                            new MapClientStoreSearchResult("123", "FD6", "음식점 > 한식 > 국밥", "010-1234-1234", "농민백암순대 본점",
                                    "https://yapp.co.kr", "서울 강남구 대치동 896-33", "서울 강남구 선릉로86길 40-4", 37.5d, 127.0d),
                            new MapClientStoreSearchResult("456", "FD6", "음식점 > 한식 > 국밥", "010-1234-1234", "농민백암순대 시청점",
                                    "http://yapp.kr", "서울 중구 북창동 19-4", null, 37.5d, 127.0d)
                    ));
            given(fileClient.moveFiles(any(String.class), anyLong(), anyList()))
                    .willReturn(new FileMovingResult(
                            Map.of("temp-key-1", "permanent/path/1", "temp-key-2", "permanent/path/2")
                    ));

            StoryRegisterResponse response = storyService.registerStory(request, member.getId());

            Story savedStory = storyRepository.findById(response.storyId()).orElseThrow();

            assertAll(
                    () -> assertThat(savedStory.getImages()).hasSize(2),
                    () -> assertThat(savedStory.getImages()).extracting(img -> img.getOrderIndex())
                            .containsExactly(1L, 2L),
                    () -> assertThat(savedStory.getImages()).extracting(img -> img.getImageKey())
                            .containsExactly("permanent/path/1", "permanent/path/2")
            );
        }
    }

    @Nested
    class GetStoryPreviews {

        @Test
        void 스토리_목록을_조회할_수_있다() {
            Member member = memberGenerator.generate("12345");
            Story story1 = storyGenerator.generate(member, "1", "곱창집");
            storyImageGenerator.generate(story1, "key1-2", 2L, "image/png", 200L);
            storyImageGenerator.generate(story1, "key1-1", 1L, "image/png", 100L);

            Story story2 = storyGenerator.generate(member, "2", "순대국밥집");

            var response = storyService.getStoryPreviews(5);

            assertThat(response.stories()).hasSize(2)
                    .extracting(StoriesResponse.StoryPreview::storyId)
                    .containsExactly(story2.getId(), story1.getId());

            StoriesResponse.StoryPreview storyPreview1 = response.stories().stream()
                    .filter(p -> p.storyId() == story1.getId())
                    .findFirst().orElseThrow();

            assertThat(storyPreview1.images()).hasSize(2)
                    .isSortedAccordingTo(Comparator.comparingLong(StoryImageResponse::orderIndex));
            assertThat(storyPreview1.images().get(0).orderIndex()).isEqualTo(1L);
        }
    }

    @Nested
    class GetStory {

        private Member member;
        private Story story;

        @BeforeEach
        void setUp() {
            member = memberGenerator.generate("99999");
            story = storyGenerator.generate(member, "123456", "진또곱창집");
        }

        @Test
        void 스토리_상세_정보를_조회할_때_스토어ID가_없으면_NULL로_반환된다() {
            StoryResponse response = storyService.getStory(story.getId());

            assertAll(
                    () -> assertThat(response.storeId()).isNull(),
                    () -> assertThat(response.storeKakaoId()).isEqualTo("123456")
            );
        }

        @Test
        void 스토리_상세_정보를_조회할_때_스토어ID가_있으면_해당_값을_반환한다() {
            Store store = storeRepository.save(
                    storeGenerator.generate("123456", "진또곱창집"));

            StoryResponse response = storyService.getStory(story.getId());

            assertAll(
                    () -> assertThat(response.storeId()).isEqualTo(store.getId()),
                    () -> assertThat(response.storeKakaoId()).isEqualTo("123456")
            );
        }

    }

    @Nested
    class GetStoriesDetails {

        @Test
        void 카카오ID로_스토리_목록을_조회할_수_있다() {
            Member member = memberGenerator.generate("99999");
            String kakaoId = "123456";

            Story story1 = storyGenerator.generate(member, kakaoId, "진또곱창집");
            storyImageGenerator.generate(story1, "key1-2", 2L, "image/jpeg", 200L);
            storyImageGenerator.generate(story1, "key1-1", 1L, "image/jpeg", 100L);

            Story story2 = storyGenerator.generate(member, kakaoId, "진또곱창집");
            storyGenerator.generate(member, "other-id", "다른집");

            var response = storyService.getStoriesDetails(kakaoId, 5);

            assertThat(response.stories()).hasSize(2)
                    .extracting(StoriesDetailResponse.StoryDetailResponse::storyId)
                    .containsExactly(story2.getId(), story1.getId());

            StoriesDetailResponse.StoryDetailResponse detailResponse1 = response.stories().stream()
                    .filter(d -> d.storyId() == story1.getId())
                    .findFirst().orElseThrow();

            assertThat(detailResponse1.images()).hasSize(2)
                    .isSortedAccordingTo(Comparator.comparingLong(StoryImageResponse::orderIndex));
            assertThat(detailResponse1.images().get(0).orderIndex()).isEqualTo(1L);

            StoriesDetailResponse.StoryDetailResponse detailResponse2 = response.stories().stream()
                    .filter(d -> d.storyId() == story2.getId())
                    .findFirst().orElseThrow();
            assertThat(detailResponse2.images()).isEmpty();
        }
    }

    @Nested
    class GetStoriesByMemberId {

        @Test
        void 회원_ID로_스토리_목록을_조회할_수_있다() {
            Member member = memberGenerator.generate("12345");
            LocalDateTime startAt = LocalDateTime.of(2025, 7, 23, 10, 0);
            Story story1 = storyGenerator.generate(member, "123456", "곱창집", startAt);
            Story story2 = storyGenerator.generate(member, "123457", "순대국밥집", startAt.plusDays(1));

            var response = storyService.getStoriesByMemberId(member.getId(), 0, 5);

            assertAll(
                    () -> assertThat(response.stories()).hasSize(2),
                    () -> assertThat(response.stories().get(0).id()).isEqualTo(story2.getId()),
                    () -> assertThat(response.stories().get(0).storeName()).isEqualTo(story2.getStoreName()),
                    () -> assertThat(response.stories().get(1).id()).isEqualTo(story1.getId()),
                    () -> assertThat(response.stories().get(1).storeName()).isEqualTo(story1.getStoreName())
            );
        }

        @Test
        void 회원_ID로_스토리_목록을_페이지네이션할_수_있다() {
            Member member = memberGenerator.generate("12345");
            LocalDateTime startAt = LocalDateTime.of(2025, 7, 23, 10, 0);
            Story story1 = storyGenerator.generate(member, "123456", "곱창집", startAt);
            Story story2 = storyGenerator.generate(member, "123457", "순대국밥집", startAt.plusDays(1));
            Story story3 = storyGenerator.generate(member, "123458", "김밥집", startAt.plusDays(2));

            var response = storyService.getStoriesByMemberId(member.getId(), 1, 2);

            assertAll(
                    () -> assertThat(response.stories()).hasSize(1),
                    () -> assertThat(response.stories().get(0).id()).isEqualTo(story1.getId()),
                    () -> assertThat(response.stories().get(0).storeName()).isEqualTo(story1.getStoreName())
            );
        }

        @Test
        void 회원_ID로_스토리_목록을_조회할_때_존재하지_않는_ID를_요청하면_빈_목록을_반환한다() {
            long nonExistentMemberId = 999L;

            var response = storyService.getStoriesByMemberId(nonExistentMemberId, 0, 5);

            assertThat(response.stories()).isEmpty();
        }
    }
}
