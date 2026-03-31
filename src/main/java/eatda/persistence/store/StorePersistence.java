package eatda.persistence.store;

import eatda.controller.store.StoreSearchParameters;
import eatda.domain.cheer.Cheer;
import eatda.domain.cheer.CheerImage;
import eatda.domain.cheer.CheerTag;
import eatda.domain.store.Store;
import eatda.repository.cheer.CheerImageRepository;
import eatda.repository.cheer.CheerRepository;
import eatda.repository.cheer.CheerTagRepository;
import eatda.repository.store.StorePopularity;
import eatda.repository.store.StoreRepository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
        return storeRepository.getByIdOrThrow(storeId);
    }

    @Transactional(readOnly = true)
    @Nullable
    public Long getStoreIdByKakaoId(String kakaoId) {
        return storeRepository.findByKakaoId(kakaoId)
                .map(Store::getId)
                .orElse(null);
    }

    // TODO : N+1 문제 성능 측정 필요
    @Transactional(readOnly = true)
    public List<StorePreviewResult> getStorePreviews(StoreSearchParameters parameters) {
        List<Store> stores = storeRepository.findAllByConditions(
                parameters.getCategory(),
                parameters.getCheerTagNames(),
                parameters.getDistricts(),
                PageRequest.of(parameters.getPage(), parameters.getSize(), Sort.by(Direction.DESC, "createdAt"))
        );
        return stores.stream()
                .map(store -> new StorePreviewResult(
                        store, getStoreThumbnailImageUrl(store.getId()), getCheerDescriptions(store)))
                .toList();
    }

    private String getStoreThumbnailImageUrl(long storeId) {
        return cheerImageRepository.findFirstByCheerStoreIdOrderByCreatedAtDesc(storeId)
                .map(CheerImage::getImageKey)
                .orElse(null);
    }

    private List<String> getCheerDescriptions(Store store) {
        return cheerRepository.findAllByStoreOrderByCreatedAtDesc(store, PageRequest.of(0, 3)).stream()
                .map(Cheer::getDescription)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CheerTag> getStoreTags(long storeId) {
        Store store = storeRepository.getByIdOrThrow(storeId);
        return cheerTagRepository.findAllByCheerStore(store);
    }

    @Transactional(readOnly = true)
    public List<CheerImage> getStoreImages(long storeId) {
        Store store = storeRepository.getByIdOrThrow(storeId);
        return cheerImageRepository.findAllByCheerStoreOrderByOrderIndexAsc(store);
    }

    @Transactional(readOnly = true)
    public List<StorePopularityResult> getStoresByCheeredMember(long memberId) {
        List<Store> stores = storeRepository.findAllByCheeredMemberIdOrderByCheerAt(memberId);
        if (stores.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Long> cheerCounts = storeRepository.findStorePopularity(stores).stream()
                .collect(Collectors.toMap(StorePopularity::getStoreId, StorePopularity::getCheerCount));
        return stores.stream()
                .map(store -> new StorePopularityResult(store, cheerCounts.getOrDefault(store.getId(), 0L)))
                .toList();
    }
}
