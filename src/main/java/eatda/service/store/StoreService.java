package eatda.service.store;

import eatda.client.file.FileClient;
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
import eatda.persistence.store.StorePersistence;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StorePersistence storePersistence;
    private final FileClient fileClient;

    public StoreResponse getStore(long storeId) {
        Store store = storePersistence.getStore(storeId);
        return new StoreResponse(store);
    }

    // TODO : N+1 문제 해결
    @Transactional(readOnly = true)
    public StoresResponse getStores(StoreSearchParameters parameters) {
        List<Store> stores = storePersistence.getStores(parameters);

        List<StorePreviewResponse> responses = stores.stream()
                .map(store -> new StorePreviewResponse(store, getStoreThumbnailImage(store.getId())))
                .toList();
        return new StoresResponse(responses);
    }

    @Nullable
    private String getStoreThumbnailImage(long storeId) {
        return storePersistence.getStoreThumbnailImage(storeId)
                .map(CheerImage::getImageKey)
                .map(fileClient::getImageUrl)
                .orElse(null);
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
        List<StoreInMemberResponse> responses = storePersistence.getStoresByCheeredMember(memberId);
        return new StoresInMemberResponse(responses);
    }
}
