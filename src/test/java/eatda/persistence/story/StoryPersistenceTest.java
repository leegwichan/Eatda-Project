package eatda.persistence.story;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import eatda.client.file.FileMovingResult;
import eatda.controller.story.StoryRegisterImage;
import eatda.controller.story.StoryRegisterRequest;
import eatda.domain.member.Member;
import eatda.domain.store.District;
import eatda.domain.store.Store;
import eatda.domain.store.StoreCategory;
import eatda.domain.store.StoreSearchResult;
import eatda.domain.story.Story;
import eatda.domain.story.StoryImage;
import eatda.persistence.BasePersistenceTest;
import eatda.repository.story.StoryImageRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class StoryPersistenceTest extends BasePersistenceTest {

    @Autowired
    private StoryPersistence storyPersistence;

    @Autowired
    private StoryImageRepository storyImageRepository;

    @Nested
    class GetStoryDetail {

        @Test
        void 스토리_상세를_조회할_수_있다() {
            Member member = memberGenerator.generateRegisteredMember("커찬", "ac@kakao.com", "123", "01012341235");
            Store store = storeGenerator.generate("1235", "서울시 강남구 역삼동 123-45");
            Story story = storyGenerator.generate(member, store.getKakaoId(), "맛집");
            storyImageGenerator.generate(story, "image.png", 1L);

            StoryDetailResult actual = storyPersistence.getStoryDetail(story.getId());

            assertAll(
                    () -> assertThat(actual.story().getId()).isEqualTo(story.getId()),
                    () -> assertThat(actual.member().getId()).isEqualTo(member.getId()),
                    () -> assertThat(actual.storeId()).isEqualTo(store.getId()),
                    () -> assertThat(actual.images()).hasSize(1)
            );
        }

    }

    @Nested
    class GetStoryPreviewsByMemberId {

        @Test
        void 멤버의_스토리_미리보기를_조회할_수_있다() {
            Member member = memberGenerator.generateRegisteredMember("커찬", "ac@kakao.com", "123", "01012341235");
            Story story = storyGenerator.generate(member, "1235", "맛집");
            storyImageGenerator.generate(story, "image.png", 1L);

            List<StoryPreviewResult> actual = storyPersistence.getStoryPreviewsByMemberId(member.getId(), 0, 10);

            assertAll(
                    () -> assertThat(actual).hasSize(1),
                    () -> assertThat(actual.get(0).story().getId()).isEqualTo(story.getId()),
                    () -> assertThat(actual.get(0).images()).hasSize(1)
            );
        }

        @Test
        void 스토리가_없으면_빈_리스트를_반환한다() {
            Member member = memberGenerator.generateRegisteredMember("커찬", "ac@kakao.com", "123", "01012341235");

            List<StoryPreviewResult> actual = storyPersistence.getStoryPreviewsByMemberId(member.getId(), 0, 10);

            assertThat(actual).isEmpty();
        }
    }

    @Nested
    class GetRecentStoryPreviews {

        @Test
        void 최신_스토리_미리보기를_조회할_수_있다() {
            Member member = memberGenerator.generateRegisteredMember("커찬", "ac@kakao.com", "123", "01012341235");
            LocalDateTime baseTime = LocalDateTime.of(2023, 10, 1, 12, 0);
            Story story1 = storyGenerator.generate(member, "1235", "맛집1", baseTime);
            Story story2 = storyGenerator.generate(member, "1236", "맛집2", baseTime.plusHours(1));
            storyImageGenerator.generate(story1, "image1.png", 1L);

            List<StoryPreviewResult> actual = storyPersistence.getRecentStoryPreviews(10);

            assertAll(
                    () -> assertThat(actual).hasSize(2),
                    () -> assertThat(actual.get(0).story().getId()).isEqualTo(story2.getId()),
                    () -> assertThat(actual.get(1).images()).hasSize(1)
            );
        }

        @Test
        void 스토리가_없으면_빈_리스트를_반환한다() {
            List<StoryPreviewResult> actual = storyPersistence.getRecentStoryPreviews(10);

            assertThat(actual).isEmpty();
        }
    }

    @Nested
    class GetStoryDetailsByStoreKakaoId {

        @Test
        void 가게별_스토리_상세_목록을_조회할_수_있다() {
            Member member = memberGenerator.generateRegisteredMember("커찬", "ac@kakao.com", "123", "01012341235");
            Store store = storeGenerator.generate("1235", "서울시 강남구 역삼동 123-45");
            Story story = storyGenerator.generate(member, store.getKakaoId(), "맛집");
            storyImageGenerator.generate(story, "image.png", 1L);

            List<StoryDetailResult> actual = storyPersistence.getStoryDetailsByStoreKakaoId(store.getKakaoId(), 10);

            assertAll(
                    () -> assertThat(actual).hasSize(1),
                    () -> assertThat(actual.get(0).story().getId()).isEqualTo(story.getId()),
                    () -> assertThat(actual.get(0).storeId()).isEqualTo(store.getId()),
                    () -> assertThat(actual.get(0).images()).hasSize(1)
            );
        }

        @Test
        void 스토리가_없으면_빈_리스트를_반환한다() {
            List<StoryDetailResult> actual = storyPersistence.getStoryDetailsByStoreKakaoId("non-existent", 10);

            assertThat(actual).isEmpty();
        }
    }

    @Nested
    class CreateStory {

        @Test
        void 스토리를_등록할_수_있다() {
            Member member = memberGenerator.generateRegisteredMember("커찬", "ac@kakao.com", "123", "01012341235");
            StoryRegisterRequest request = new StoryRegisterRequest("맛집", "1235", "정말 맛있어요!", List.of());
            StoreSearchResult storeResult = new StoreSearchResult(
                    "1235", StoreCategory.KOREAN, "010-1234-5678", "맛집",
                    "https://place.kakao.com/1", "서울시 강남구 역삼동 123-45", "서울시 강남구 준비로 11길",
                    District.GANGNAM, 37.5665, 126.978);

            Story actual = storyPersistence.createStory(request, storeResult, member.getId());

            assertAll(
                    () -> assertThat(actual.getId()).isNotNull(),
                    () -> assertThat(actual.getDescription()).isEqualTo("정말 맛있어요!"),
                    () -> assertThat(actual.getMember().getId()).isEqualTo(member.getId())
            );
        }

    }

    @Nested
    class SaveStoryImages {

        @Test
        void 스토리_이미지를_저장할_수_있다() {
            Member member = memberGenerator.generateRegisteredMember("커찬", "ac@kakao.com", "123", "01012341235");
            Story story = storyGenerator.generate(member, "1235", "맛집");
            List<StoryRegisterImage> images = List.of(
                    new StoryRegisterImage("old/image1.png", 1L, "image/png", 1000L),
                    new StoryRegisterImage("old/image2.png", 2L, "image/png", 2000L));
            FileMovingResult movingResult = new FileMovingResult(
                    Map.of("old/image1.png", "new/image1.png", "old/image2.png", "new/image2.png"));

            storyPersistence.saveStoryImages(story.getId(), images, movingResult);

            List<StoryImage> savedImages = storyImageRepository.findByStoryId(story.getId());
            assertThat(savedImages).hasSize(2);
        }

    }

    @Nested
    class DeleteStoryById {

        @Test
        void 스토리를_삭제할_수_있다() {
            Member member = memberGenerator.generateRegisteredMember("커찬", "ac@kakao.com", "123", "01012341235");
            Story story = storyGenerator.generate(member, "1235", "맛집");

            storyPersistence.deleteStoryById(story.getId());

            assertThat(storyRepository.findById(story.getId())).isEmpty();
        }
    }
}
