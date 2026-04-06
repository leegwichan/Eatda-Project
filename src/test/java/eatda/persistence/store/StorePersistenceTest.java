package eatda.persistence.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import eatda.controller.store.StoreSearchParameters;
import eatda.domain.cheer.Cheer;
import eatda.domain.cheer.CheerTagName;
import eatda.domain.member.Member;
import eatda.domain.store.Store;
import eatda.domain.store.StoreCategory;
import eatda.persistence.BasePersistenceTest;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class StorePersistenceTest extends BasePersistenceTest {

    @Autowired
    private StorePersistence storePersistence;

    @Nested
    class GetStore {

        @Test
        void 가게를_조회할_수_있다() {
            Store store = storeGenerator.generate("1235", "서울시 강남구 역삼동 123-45");

            Store actual = storePersistence.getStore(store.getId());

            assertThat(actual.getId()).isEqualTo(store.getId());
        }

    }

    @Nested
    class GetStorePreviews {

        @Test
        void 가게_미리보기_목록을_조회할_수_있다() {
            Member member = memberGenerator.generateRegisteredMember("커찬", "ac@kakao.com", "123", "01012341235");
            LocalDateTime baseTime = LocalDateTime.of(2023, 10, 1, 12, 0);
            Store store = storeGenerator.generate("1235", "서울시 강남구 역삼동 123-45", StoreCategory.KOREAN, baseTime);
            Cheer cheer = cheerGenerator.generate(member, store, baseTime);
            cheerImageGenerator.generate(cheer, "thumbnail.png", 1L);
            StoreSearchParameters parameters = new StoreSearchParameters(0, 10, null, null, null);

            List<StorePreviewResult> actual = storePersistence.getStorePreviews(parameters);

            assertAll(
                    () -> assertThat(actual).hasSize(1),
                    () -> assertThat(actual.get(0).store().getId()).isEqualTo(store.getId()),
                    () -> assertThat(actual.get(0).thumbnailImageUrl()).isEqualTo("thumbnail.png"),
                    () -> assertThat(actual.get(0).cheerDescriptions()).hasSize(1)
            );
        }

        @Test
        void 가게가_없으면_빈_리스트를_반환한다() {
            StoreSearchParameters parameters = new StoreSearchParameters(0, 10, null, null, null);

            List<StorePreviewResult> actual = storePersistence.getStorePreviews(parameters);

            assertThat(actual).isEmpty();
        }
    }

    @Nested
    class GetStoreTags {

        @Test
        void 가게의_응원_태그를_조회할_수_있다() {
            Member member = memberGenerator.generateRegisteredMember("커찬", "ac@kakao.com", "123", "01012341235");
            Store store = storeGenerator.generate("1235", "서울시 강남구 역삼동 123-45");
            Cheer cheer = cheerGenerator.generateCommon(member, store);
            cheerTagGenerator.generate(cheer, List.of(CheerTagName.INSTAGRAMMABLE, CheerTagName.ENERGETIC));

            var actual = storePersistence.getStoreTags(store.getId());

            assertThat(actual).hasSize(2);
        }

    }

    @Nested
    class GetStoreImages {

        @Test
        void 가게의_응원_이미지를_조회할_수_있다() {
            Member member = memberGenerator.generateRegisteredMember("커찬", "ac@kakao.com", "123", "01012341235");
            Store store = storeGenerator.generate("1235", "서울시 강남구 역삼동 123-45");
            Cheer cheer = cheerGenerator.generateCommon(member, store);
            cheerImageGenerator.generate(cheer, "image1.png", 1L);
            cheerImageGenerator.generate(cheer, "image2.png", 2L);

            var actual = storePersistence.getStoreImages(store.getId());

            assertAll(
                    () -> assertThat(actual).hasSize(2),
                    () -> assertThat(actual.get(0).getImageKey()).isEqualTo("image1.png"),
                    () -> assertThat(actual.get(1).getImageKey()).isEqualTo("image2.png")
            );
        }

    }

    @Nested
    class GetStoresByCheeredMember {

        @Test
        void 멤버가_응원한_가게와_응원수를_조회할_수_있다() {
            Member member1 = memberGenerator.generateRegisteredMember("커찬", "ac@kakao.com", "123", "01012341235");
            Member member2 = memberGenerator.generateRegisteredMember("지민", "ad@kakao.com", "124", "01012341236");
            LocalDateTime baseTime = LocalDateTime.of(2023, 10, 1, 12, 0);
            Store store = storeGenerator.generate("1235", "서울시 강남구 역삼동 123-45");
            cheerGenerator.generate(member1, store, baseTime);
            cheerGenerator.generate(member2, store, baseTime.plusHours(1));

            List<StorePopularityResult> actual = storePersistence.getStoresByCheeredMember(member1.getId());

            assertAll(
                    () -> assertThat(actual).hasSize(1),
                    () -> assertThat(actual.get(0).store().getId()).isEqualTo(store.getId()),
                    () -> assertThat(actual.get(0).cheerCount()).isEqualTo(2)
            );
        }

        @Test
        void 응원한_가게가_없으면_빈_리스트를_반환한다() {
            Member member = memberGenerator.generateRegisteredMember("커찬", "ac@kakao.com", "123", "01012341235");

            List<StorePopularityResult> actual = storePersistence.getStoresByCheeredMember(member.getId());

            assertThat(actual).isEmpty();
        }
    }
}
