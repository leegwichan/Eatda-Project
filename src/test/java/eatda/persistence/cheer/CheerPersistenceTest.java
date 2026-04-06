package eatda.persistence.cheer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import eatda.client.file.FileMovingResult;
import eatda.controller.cheer.CheerRegisterImage;
import eatda.controller.cheer.CheerRegisterRequest;
import eatda.controller.cheer.CheerSearchParameters;
import eatda.domain.cheer.Cheer;
import eatda.domain.cheer.CheerImage;
import eatda.domain.cheer.CheerTagName;
import eatda.domain.member.Member;
import eatda.domain.store.District;
import eatda.domain.store.Store;
import eatda.domain.store.StoreCategory;
import eatda.domain.store.StoreSearchResult;
import eatda.exception.BusinessErrorCode;
import eatda.exception.BusinessException;
import eatda.persistence.BasePersistenceTest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CheerPersistenceTest extends BasePersistenceTest {

    @Autowired
    private CheerPersistence cheerPersistence;

    @Nested
    class GetCheerById {

        @Test
        void 응원_상세를_조회할_수_있다() {
            Member member = memberGenerator.generateRegisteredMember("커찬", "ac@kakao.com", "123", "01012341235");
            Store store = storeGenerator.generate("1235", "서울시 강남구 역삼동 123-45");
            Cheer cheer = cheerGenerator.generateCommon(member, store);
            cheerTagGenerator.generate(cheer, List.of(CheerTagName.INSTAGRAMMABLE));
            cheerImageGenerator.generate(cheer, "image.png", 1L);

            CheerDetailResult actual = cheerPersistence.getCheerById(cheer.getId());

            assertAll(
                    () -> assertThat(actual.cheer().getId()).isEqualTo(cheer.getId()),
                    () -> assertThat(actual.storeId()).isEqualTo(store.getId()),
                    () -> assertThat(actual.tags().getNames()).containsExactly(CheerTagName.INSTAGRAMMABLE),
                    () -> assertThat(actual.images()).hasSize(1)
            );
        }

    }

    @Nested
    class GetCheers {

        @Test
        void 응원_미리보기_목록을_조회할_수_있다() {
            Member member = memberGenerator.generateRegisteredMember("커찬", "ac@kakao.com", "123", "01012341235");
            Store store = storeGenerator.generate("1235", "서울시 강남구 역삼동 123-45", StoreCategory.KOREAN,
                    LocalDateTime.of(2023, 10, 1, 12, 0));
            Cheer cheer = cheerGenerator.generateCommon(member, store);
            cheerTagGenerator.generate(cheer, List.of(CheerTagName.INSTAGRAMMABLE));
            cheerImageGenerator.generate(cheer, "image.png", 1L);
            CheerSearchParameters parameters = new CheerSearchParameters(0, 10, null, null, null);

            List<CheerPreviewResult> actual = cheerPersistence.getCheers(parameters);

            assertAll(
                    () -> assertThat(actual).hasSize(1),
                    () -> assertThat(actual.get(0).cheer().getId()).isEqualTo(cheer.getId()),
                    () -> assertThat(actual.get(0).tags().getNames()).containsExactly(CheerTagName.INSTAGRAMMABLE),
                    () -> assertThat(actual.get(0).images()).hasSize(1)
            );
        }

        @Test
        void 응원이_없으면_빈_리스트를_반환한다() {
            CheerSearchParameters parameters = new CheerSearchParameters(0, 10, null, null, null);

            List<CheerPreviewResult> actual = cheerPersistence.getCheers(parameters);

            assertThat(actual).isEmpty();
        }
    }

    @Nested
    class GetCheersByStoreId {

        @Test
        void 가게별_응원을_조회할_수_있다() {
            Member member = memberGenerator.generateRegisteredMember("커찬", "ac@kakao.com", "123", "01012341235");
            Store store = storeGenerator.generate("1235", "서울시 강남구 역삼동 123-45");
            Cheer cheer = cheerGenerator.generateCommon(member, store);
            cheerTagGenerator.generate(cheer, List.of(CheerTagName.ENERGETIC));

            List<CheerInStoreResult> actual = cheerPersistence.getCheersByStoreId(store.getId(), 0, 10);

            assertAll(
                    () -> assertThat(actual).hasSize(1),
                    () -> assertThat(actual.get(0).cheer().getId()).isEqualTo(cheer.getId()),
                    () -> assertThat(actual.get(0).tags().getNames()).containsExactly(CheerTagName.ENERGETIC)
            );
        }

        @Test
        void 응원이_없으면_빈_리스트를_반환한다() {
            Store store = storeGenerator.generate("1235", "서울시 강남구 역삼동 123-45");

            List<CheerInStoreResult> actual = cheerPersistence.getCheersByStoreId(store.getId(), 0, 10);

            assertThat(actual).isEmpty();
        }
    }

    @Nested
    class CreateCheer {

        @Test
        void 신규_가게에_응원을_등록할_수_있다() {
            Member member = memberGenerator.generateRegisteredMember("커찬", "ac@kakao.com", "123", "01012341235");
            CheerRegisterRequest request = new CheerRegisterRequest(
                    "new-kakao-id", "새 가게", "응원합니다!", List.of(), List.of(CheerTagName.INSTAGRAMMABLE));
            StoreSearchResult storeResult = new StoreSearchResult(
                    "new-kakao-id", StoreCategory.KOREAN, "010-1234-5678", "새 가게",
                    "https://place.kakao.com/1", "서울시 강남구 역삼동 123-45", "서울시 강남구 준비로 11길",
                    District.GANGNAM, 37.5665, 126.978);

            CheerDetailResult actual = cheerPersistence.createCheer(request, storeResult, member.getId());

            assertAll(
                    () -> assertThat(actual.cheer().getDescription()).isEqualTo("응원합니다!"),
                    () -> assertThat(actual.tags().getNames()).containsExactly(CheerTagName.INSTAGRAMMABLE)
            );
        }

        @Test
        void 기존_가게에_응원을_등록할_수_있다() {
            Member member = memberGenerator.generateRegisteredMember("커찬", "ac@kakao.com", "123", "01012341235");
            Store store = storeGenerator.generate("1235", "서울시 강남구 역삼동 123-45");
            CheerRegisterRequest request = new CheerRegisterRequest(
                    store.getKakaoId(), "가게", "응원합니다!", List.of(), List.of());
            StoreSearchResult storeResult = new StoreSearchResult(
                    store.getKakaoId(), StoreCategory.KOREAN, "010-1234-5678", "가게",
                    "https://place.kakao.com/1", "서울시 강남구 역삼동 123-45", "",
                    District.GANGNAM, 37.5665, 126.978);

            CheerDetailResult actual = cheerPersistence.createCheer(request, storeResult, member.getId());

            assertThat(actual.storeId()).isEqualTo(store.getId());
        }

        @Test
        void 이미_응원한_가게이면_예외를_던진다() {
            Member member = memberGenerator.generateRegisteredMember("커찬", "ac@kakao.com", "123", "01012341235");
            Store store = storeGenerator.generate("1235", "서울시 강남구 역삼동 123-45");
            cheerGenerator.generateCommon(member, store);
            CheerRegisterRequest request = new CheerRegisterRequest(
                    store.getKakaoId(), "가게", "또 응원!", List.of(), List.of());
            StoreSearchResult storeResult = new StoreSearchResult(
                    store.getKakaoId(), StoreCategory.KOREAN, "010-1234-5678", "가게",
                    "https://place.kakao.com/1", "서울시 강남구 역삼동 123-45", "",
                    District.GANGNAM, 37.5665, 126.978);

            assertThatThrownBy(() -> cheerPersistence.createCheer(request, storeResult, member.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(BusinessErrorCode.ALREADY_CHEERED.getMessage());
        }
    }

    @Nested
    class SaveCheerImages {

        @Test
        void 응원_이미지를_저장할_수_있다() {
            Member member = memberGenerator.generateRegisteredMember("커찬", "ac@kakao.com", "123", "01012341235");
            Store store = storeGenerator.generate("1235", "서울시 강남구 역삼동 123-45");
            Cheer cheer = cheerGenerator.generateCommon(member, store);
            List<CheerRegisterImage> images = List.of(
                    new CheerRegisterImage("old/image1.png", 1L, "image/png", 1000L),
                    new CheerRegisterImage("old/image2.png", 2L, "image/png", 2000L));
            FileMovingResult movingResult = new FileMovingResult(
                    Map.of("old/image1.png", "new/image1.png", "old/image2.png", "new/image2.png"));

            List<CheerImage> actual = cheerPersistence.saveCheerImages(cheer.getId(), images, movingResult);

            assertAll(
                    () -> assertThat(actual).hasSize(2),
                    () -> assertThat(actual.get(0).getImageKey()).isEqualTo("new/image1.png"),
                    () -> assertThat(actual.get(1).getImageKey()).isEqualTo("new/image2.png")
            );
        }

    }

    @Nested
    class DeleteCheerById {

        @Test
        void 응원을_삭제할_수_있다() {
            Member member = memberGenerator.generateRegisteredMember("커찬", "ac@kakao.com", "123", "01012341235");
            Store store = storeGenerator.generate("1235", "서울시 강남구 역삼동 123-45");
            Cheer cheer = cheerGenerator.generateCommon(member, store);

            cheerPersistence.deleteCheerById(cheer.getId());

            assertThat(cheerRepository.findById(cheer.getId())).isEmpty();
        }
    }
}
