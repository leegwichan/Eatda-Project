package eatda.repository.story;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import eatda.domain.member.Member;
import eatda.domain.store.Store;
import eatda.domain.story.Story;
import eatda.exception.BusinessErrorCode;
import eatda.exception.BusinessException;
import eatda.repository.BaseRepositoryTest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

class StoryRepositoryTest extends BaseRepositoryTest {

    @Nested
    class GetDetailByIdOrThrow {

        @Test
        void 스토리_상세를_조회할_수_있다() {
            Member member = memberGenerator.generateRegisteredMember("커찬", "ac@kakao.com", "123", "01012341235");
            Store store = storeGenerator.generate("1235", "서울시 강남구 역삼동 123-45");
            Story story = storyGenerator.generate(member, store.getKakaoId(), "맛집");

            StoryDetail actual = storyRepository.getDetailByIdOrThrow(story.getId());

            assertAll(
                    () -> assertThat(actual.getStory().getId()).isEqualTo(story.getId()),
                    () -> assertThat(actual.getMember().getId()).isEqualTo(member.getId()),
                    () -> assertThat(actual.getStoreId()).isEqualTo(store.getId())
            );
        }

        @Test
        void 존재하지_않는_스토리를_조회하면_예외를_던진다() {
            assertThatThrownBy(() -> storyRepository.getDetailByIdOrThrow(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(BusinessErrorCode.STORY_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class FindDetailById {

        @Test
        void 스토리_상세를_조회할_수_있다() {
            Member member = memberGenerator.generateRegisteredMember("커찬", "ac@kakao.com", "123", "01012341235");
            Store store = storeGenerator.generate("1235", "서울시 강남구 역삼동 123-45");
            Story story = storyGenerator.generate(member, store.getKakaoId(), "맛집");

            Optional<StoryDetail> actual = storyRepository.findDetailById(story.getId());

            assertAll(
                    () -> assertThat(actual).isPresent(),
                    () -> assertThat(actual.get().getStory().getId()).isEqualTo(story.getId()),
                    () -> assertThat(actual.get().getMember().getId()).isEqualTo(member.getId()),
                    () -> assertThat(actual.get().getStoreId()).isEqualTo(store.getId())
            );
        }

        @Test
        void 가게가_등록되지_않은_스토리도_조회할_수_있다() {
            Member member = memberGenerator.generateRegisteredMember("커찬", "ac@kakao.com", "123", "01012341235");
            Story story = storyGenerator.generate(member, "unregistered-kakao-id", "미등록 가게");

            Optional<StoryDetail> actual = storyRepository.findDetailById(story.getId());

            assertAll(
                    () -> assertThat(actual).isPresent(),
                    () -> assertThat(actual.get().getStory().getId()).isEqualTo(story.getId()),
                    () -> assertThat(actual.get().getMember().getId()).isEqualTo(member.getId()),
                    () -> assertThat(actual.get().getStoreId()).isNull()
            );
        }

        @Test
        void 존재하지_않는_스토리를_조회하면_빈_결과를_반환한다() {
            Optional<StoryDetail> actual = storyRepository.findDetailById(999L);

            assertThat(actual).isEmpty();
        }
    }

    @Nested
    class FindAllDetailsByStoreKakaoIdOrderByCreatedAtDesc {

        @Test
        void 가게의_스토리_상세_목록을_최신순으로_조회할_수_있다() {
            Member member1 = memberGenerator.generateRegisteredMember("커찬", "ac@kakao.com", "123", "01012341235");
            Member member2 = memberGenerator.generateRegisteredMember("지민", "ad@kakao.com", "124", "01012341236");
            Store store = storeGenerator.generate("1235", "서울시 강남구 역삼동 123-45");
            LocalDateTime baseTime = LocalDateTime.of(2023, 10, 1, 12, 0);
            Story story1 = storyGenerator.generate(member1, store.getKakaoId(), "맛집1", baseTime);
            Story story2 = storyGenerator.generate(member2, store.getKakaoId(), "맛집2", baseTime.plusHours(1));
            Story story3 = storyGenerator.generate(member1, store.getKakaoId(), "맛집3", baseTime.plusHours(2));

            List<StoryDetail> actual = storyRepository.findAllDetailsByStoreKakaoIdOrderByCreatedAtDesc(
                    store.getKakaoId(), PageRequest.of(0, 10));

            assertAll(
                    () -> assertThat(actual).hasSize(3),
                    () -> assertThat(actual).extracting(d -> d.getStory().getId())
                            .containsExactly(story3.getId(), story2.getId(), story1.getId()),
                    () -> assertThat(actual.get(0).getMember().getId()).isEqualTo(member1.getId()),
                    () -> assertThat(actual.get(1).getMember().getId()).isEqualTo(member2.getId()),
                    () -> assertThat(actual.get(0).getStoreId()).isEqualTo(store.getId())
            );
        }

        @Test
        void 가게가_등록되지_않은_스토리도_storeId가_null로_조회된다() {
            Member member = memberGenerator.generateRegisteredMember("커찬", "ac@kakao.com", "123", "01012341235");
            String unregisteredKakaoId = "unregistered-kakao-id";
            storyGenerator.generate(member, unregisteredKakaoId, "미등록 가게");

            List<StoryDetail> actual = storyRepository.findAllDetailsByStoreKakaoIdOrderByCreatedAtDesc(
                    unregisteredKakaoId, PageRequest.of(0, 10));

            assertAll(
                    () -> assertThat(actual).hasSize(1),
                    () -> assertThat(actual.get(0).getStoreId()).isNull()
            );
        }

        @Test
        void 페이징이_적용된다() {
            Member member = memberGenerator.generateRegisteredMember("커찬", "ac@kakao.com", "123", "01012341235");
            Store store = storeGenerator.generate("1235", "서울시 강남구 역삼동 123-45");
            LocalDateTime baseTime = LocalDateTime.of(2023, 10, 1, 12, 0);
            storyGenerator.generate(member, store.getKakaoId(), "맛집1", baseTime);
            storyGenerator.generate(member, store.getKakaoId(), "맛집2", baseTime.plusHours(1));
            Story story3 = storyGenerator.generate(member, store.getKakaoId(), "맛집3", baseTime.plusHours(2));

            List<StoryDetail> actual = storyRepository.findAllDetailsByStoreKakaoIdOrderByCreatedAtDesc(
                    store.getKakaoId(), PageRequest.of(0, 1));

            assertAll(
                    () -> assertThat(actual).hasSize(1),
                    () -> assertThat(actual.get(0).getStory().getId()).isEqualTo(story3.getId())
            );
        }

        @Test
        void 해당_가게의_스토리가_없으면_빈_리스트를_반환한다() {
            List<StoryDetail> actual = storyRepository.findAllDetailsByStoreKakaoIdOrderByCreatedAtDesc(
                    "non-existent", PageRequest.of(0, 10));

            assertThat(actual).isEmpty();
        }
    }
}
