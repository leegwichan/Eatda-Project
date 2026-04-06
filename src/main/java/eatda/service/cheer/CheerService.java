package eatda.service.cheer;

import eatda.client.file.FileClient;
import eatda.client.file.FileMovingResult;
import eatda.client.map.MapClient;
import eatda.client.map.MapClientStoreSearchResult;
import eatda.controller.cheer.CheerImageResponse;
import eatda.controller.cheer.CheerPreviewResponse;
import eatda.controller.cheer.CheerRegisterImage;
import eatda.controller.cheer.CheerRegisterRequest;
import eatda.controller.cheer.CheerResponse;
import eatda.controller.cheer.CheerSearchParameters;
import eatda.controller.cheer.CheersInStoreResponse;
import eatda.controller.cheer.CheersResponse;
import eatda.domain.ImageDomain;
import eatda.domain.cheer.Cheer;
import eatda.domain.cheer.CheerImage;
import eatda.domain.store.StoreSearchFilter;
import eatda.domain.store.StoreSearchResult;
import eatda.persistence.cheer.CheerDetailResult;
import eatda.persistence.cheer.CheerInStoreResult;
import eatda.persistence.cheer.CheerPersistence;
import eatda.persistence.cheer.CheerPreviewResult;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheerService {

    private static final ImageDomain IMAGE_DOMAIN = ImageDomain.CHEER;

    private final CheerPersistence cheerPersistence;
    private final FileClient fileClient;
    private final MapClient mapClient;
    private final StoreSearchFilter storeSearchFilter;

    public CheerResponse getCheer(Long cheerId) {
        CheerDetailResult result = cheerPersistence.getCheerById(cheerId);

        return new CheerResponse(result, toCheerImageResponses(result.images()));
    }

    public CheersResponse getCheers(CheerSearchParameters parameters) {
        List<CheerPreviewResult> results = cheerPersistence.getCheers(parameters);

        List<CheerPreviewResponse> cheerResponses = results.stream()
                .map(result -> new CheerPreviewResponse(result, toCheerImageResponses(result.images())))
                .toList();
        return new CheersResponse(cheerResponses);
    }

    private List<CheerImageResponse> toCheerImageResponses(List<CheerImage> images) {
        return images.stream()
                .map(img -> new CheerImageResponse(img, fileClient.getImageUrl(img.getImageKey())))
                .sorted(Comparator.comparingLong(CheerImageResponse::orderIndex))
                .toList();
    }

    public CheersInStoreResponse getCheersByStoreId(Long storeId, int page, int size) {
        List<CheerInStoreResult> results = cheerPersistence.getCheersByStoreId(storeId, page, size);
        return CheersInStoreResponse.from(results);
    }

    public CheerResponse registerCheer(CheerRegisterRequest request, long memberId) {
        List<MapClientStoreSearchResult> clientResult = mapClient.searchStores(request.storeName());
        StoreSearchResult filteredResult = storeSearchFilter.filterStoreByKakaoId(clientResult, request.storeKakaoId());

        CheerDetailResult result = cheerPersistence.createCheer(request, filteredResult, memberId);
        if (request.images() == null || request.images().isEmpty()) {
            return new CheerResponse(result, List.of());
        }

        List<CheerImage> cheerImages = saveCheerImages(result.cheer(), request.images());

        List<CheerImageResponse> imageResponses = cheerImages.stream()
                .map(image -> new CheerImageResponse(image, fileClient.getImageUrl(image.getImageKey())))
                .toList();
        return new CheerResponse(result, imageResponses);
    }

    private List<CheerImage> saveCheerImages(Cheer cheer, List<CheerRegisterImage> registerImages) {
        List<String> beforeImageKeys = registerImages.stream()
                .map(CheerRegisterImage::imageKey)
                .toList();

        FileMovingResult movingResult = null;
        try {
            movingResult = fileClient.moveFiles(IMAGE_DOMAIN.getName(), cheer.getId(), beforeImageKeys);
            return cheerPersistence.saveCheerImages(cheer.getId(), registerImages, movingResult);
        } catch (RuntimeException exception) {
            log.error("응원 등록 프로세스 실패. 롤백 수행. cheerId={}", cheer.getId(), exception);
            cheerPersistence.deleteCheerById(cheer.getId());
            if (movingResult != null) {
                fileClient.deleteFiles(movingResult.getResults());
            }
            throw exception;
        }
    }
}
