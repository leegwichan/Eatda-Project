package eatda.service.store;

import eatda.client.file.FileClient;
import eatda.client.map.MapClient;
import eatda.client.map.MapClientStoreSearchResult;
import eatda.controller.store.ImagesResponse;
import eatda.controller.store.StoreInMemberResponse;
import eatda.controller.store.StorePreviewResponse;
import eatda.controller.store.StoreResponse;
import eatda.controller.store.StoreSearchParameters;
import eatda.controller.store.StoresInMemberResponse;
import eatda.controller.store.StoresResponse;
import eatda.controller.store.TagsResponse;
import eatda.domain.cheer.CheerImage;
import eatda.domain.cheer.CheerTag;
import eatda.domain.store.Store;
import eatda.domain.store.StoreSearchFilter;
import eatda.domain.store.StoreSearchResult;
import eatda.persistence.store.StorePersistence;
import eatda.persistence.store.StorePopularityResult;
import eatda.persistence.store.StorePreviewResult;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StorePersistence storePersistence;
    private final FileClient fileClient;
    private final MapClient mapClient;
    private final StoreSearchFilter storeSearchFilter;

    public StoreResponse getStore(long storeId) {
        Store store = storePersistence.getStore(storeId);
        return new StoreResponse(store);
    }

    public StoresResponse getStores(StoreSearchParameters parameters) {
        List<StorePreviewResult> results = storePersistence.getStorePreviews(parameters);

        List<StorePreviewResponse> responses = results.stream()
                .map(result -> new StorePreviewResponse(
                        result.store(), result.thumbnailImageUrl(), result.cheerDescriptions()))
                .toList();
        return new StoresResponse(responses);
    }

    public TagsResponse getStoreTags(long storeId) {
        List<CheerTag> cheerTags = storePersistence.getStoreTags(storeId);
        return TagsResponse.from(cheerTags);
    }

    public ImagesResponse getStoreImages(long storeId) {
        List<CheerImage> cheerImages = storePersistence.getStoreImages(storeId);
        List<String> imageUrls = cheerImages.stream()
                .map(CheerImage::getImageKey)
                .map(fileClient::getImageUrl)
                .toList();
        return new ImagesResponse(imageUrls);
    }

    public StoresInMemberResponse getStoresByCheeredMember(long memberId) {
        List<StorePopularityResult> results = storePersistence.getStoresByCheeredMember(memberId);

        List<StoreInMemberResponse> responses = results.stream()
                .map(result -> new StoreInMemberResponse(result.store(), result.cheerCount()))
                .toList();
        return new StoresInMemberResponse(responses);
    }

    public StoreSearchResult searchStoreByKakaoId(String name, String kakaoId) {
        List<MapClientStoreSearchResult> searchResults = mapClient.searchStores(name);
        return storeSearchFilter.filterStoreByKakaoId(searchResults, kakaoId);
    }

    public List<StoreSearchResult> searchStores(String name) {
        List<MapClientStoreSearchResult> searchResults = mapClient.searchStores(name);
        return storeSearchFilter.filterSearchedStores(searchResults);
    }
}
