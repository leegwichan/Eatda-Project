package eatda.service.cheer;

import eatda.client.file.FileClient;
import eatda.controller.cheer.CheerImageResponse;
import eatda.controller.cheer.CheerPreviewResponse;
import eatda.controller.cheer.CheerRegisterRequest;
import eatda.controller.cheer.CheerResponse;
import eatda.controller.cheer.CheerSearchParameters;
import eatda.controller.cheer.CheersInStoreResponse;
import eatda.controller.cheer.CheersResponse;
import eatda.domain.cheer.Cheer;
import eatda.domain.cheer.CheerImage;
import eatda.domain.store.StoreSearchResult;
import eatda.facade.CheerCreationResult;
import eatda.persistence.cheer.CheerPersistence;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CheerService {

    private final CheerPersistence cheerPersistence;
    private final FileClient fileClient;

    public CheerResponse getCheer(Long cheerId) {
        Cheer cheer = cheerPersistence.getCheerById(cheerId);

        return new CheerResponse(cheer, toCheerImageResponses(cheer.getImages()));
    }

    @Transactional(readOnly = true)
    public CheersResponse getCheers(CheerSearchParameters parameters) {
        List<Cheer> cheers = cheerPersistence.getCheers(parameters);

        List<CheerPreviewResponse> cheerResponses = cheers.stream()
                .map(cheer -> new CheerPreviewResponse(cheer, toCheerImageResponses(cheer.getImages())))
                .toList();
        return new CheersResponse(cheerResponses);
    }

    private List<CheerImageResponse> toCheerImageResponses(Set<CheerImage> images) {
        return images.stream()
                .map(img -> new CheerImageResponse(img, fileClient.getImageUrl(img.getImageKey())))
                .sorted(Comparator.comparingLong(CheerImageResponse::orderIndex))
                .toList();
    }

    @Transactional(readOnly = true)
    public CheersInStoreResponse getCheersByStoreId(Long storeId, int page, int size) {
        List<Cheer> cheers = cheerPersistence.getCheersByStoreId(storeId, page, size);
        return CheersInStoreResponse.from(cheers);
    }

    public CheerCreationResult createCheer(CheerRegisterRequest request, StoreSearchResult result, long memberId) {
        Cheer cheer = cheerPersistence.createCheer(request, result, memberId);
        return new CheerCreationResult(cheer, cheer.getStore());
    }

    public void saveCheerImages(Long cheerId,
                                List<CheerRegisterRequest.UploadedImageDetail> sortedImages,
                                List<String> permanentKeys) {
        cheerPersistence.saveCheerImages(cheerId, sortedImages, permanentKeys);
    }

    public void deleteCheer(Long cheerId) {
        cheerPersistence.deleteCheerById(cheerId);
    }
}
