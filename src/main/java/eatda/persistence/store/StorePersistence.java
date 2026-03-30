package eatda.persistence.store;

import eatda.controller.store.ImagesResponse;
import eatda.controller.store.StoreInMemberResponse;
import eatda.controller.store.StoreSearchParameters;
import eatda.controller.store.StoresInMemberResponse;
import eatda.controller.store.TagsResponse;
import eatda.domain.cheer.CheerImage;
import eatda.domain.cheer.CheerTag;
import eatda.domain.store.Store;
import eatda.repository.cheer.CheerImageRepository;
import eatda.repository.cheer.CheerRepository;
import eatda.repository.cheer.CheerTagRepository;
import eatda.repository.store.StoreRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class StorePersistence {

    private final StoreRepository storeRepository;
    private final CheerRepository cheerRepository;
    private final CheerTagRepository cheerTagRepository;
    private final CheerImageRepository cheerImageRepository;

    public Store getStore(long storeId) {
        return storeRepository.getById(storeId);
    }

    @Transactional(readOnly = true)
    @Nullable
    public Long getStoreIdByKakaoId(String kakaoId) {
        return storeRepository.findByKakaoId(kakaoId)
                .map(Store::getId)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Store> getStores(StoreSearchParameters parameters) {
        return storeRepository.findAllByConditions(
                parameters.getCategory(),
                parameters.getCheerTagNames(),
                parameters.getDistricts(),
                PageRequest.of(parameters.getPage(), parameters.getSize(), Sort.by(Direction.DESC, "createdAt"))
        ).getContent();
    }

    @Transactional(readOnly = true)
    public List<CheerTag> getStoreTags(long storeId) {
        Store store = storeRepository.getById(storeId);
        return cheerTagRepository.findAllByCheerStore(store);
    }

    @Transactional(readOnly = true)
    public Optional<CheerImage> getStoreThumbnailImage(long storeId) {
        return cheerImageRepository.findFirstByCheer_Store_IdOrderByCreatedAtDesc(storeId);
    }

    @Transactional(readOnly = true)
    public List<CheerImage> getStoreImages(long storeId) {
        Store store = storeRepository.getById(storeId);
        return cheerImageRepository.findAllByCheer_StoreOrderByOrderIndexAsc(store);
    }

    @Transactional(readOnly = true)
    public List<StoreInMemberResponse> getStoresByCheeredMember(long memberId) {
        List<Store> stores = storeRepository.findAllByCheeredMemberId(memberId);
        // TODO : N+1 문제 해결 (특정 회원의 가게는 3명 제한이라 중요도 낮음)
        // TODO : cheerCount는 별도의 쿼리로 조회하여 매핑하는 방식 고려 (도메인으로 반환 방법, ...)
        return stores.stream()
                .map(store -> new StoreInMemberResponse(store, cheerRepository.countByStore(store)))
                .toList();
    }
}
