package eatda.facade;

import eatda.client.file.FileClient;
import eatda.controller.cheer.CheerRegisterImage;
import eatda.controller.cheer.CheerRegisterRequest;
import eatda.controller.cheer.CheerResponse;
import eatda.domain.ImageDomain;
import eatda.domain.cheer.Cheer;
import eatda.domain.store.StoreSearchResult;
import eatda.service.cheer.CheerService;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CheerRegisterFacade {

    private final CheerService cheerService;
    private final FileClient fileClient;

    public CheerResponse registerCheer(CheerRegisterRequest request,
                                       StoreSearchResult result,
                                       long memberId,
                                       ImageDomain domain
    ) {
        CheerCreationResult creationResult = cheerService.createCheer(request, result, memberId);
        Cheer cheer = creationResult.cheer();

        if (request.images() == null || request.images().isEmpty()) {
            return cheerService.getCheer(cheer.getId());
        }

        List<String> permanentKeys = Collections.emptyList();
        try {
            permanentKeys = moveImages(domain, cheer.getId(), request.images());
            cheerService.saveCheerImages(cheer.getId(), request.images(), permanentKeys);

        } catch (Exception e) {
            log.error("응원 등록 프로세스 실패. 롤백 수행. cheerId={}", cheer.getId(), e);

            cheerService.deleteCheer(cheer.getId());

            if (!permanentKeys.isEmpty()) {
                fileClient.deleteFiles(permanentKeys);
            }
            throw e;
        }

        return cheerService.getCheer(cheer.getId());
    }

    private List<String> moveImages(ImageDomain domain,
                                    long cheerId,
                                    List<CheerRegisterImage> sortedImages) {
        if (sortedImages.isEmpty()) {
            return List.of();
        }

        List<String> tempKeys = sortedImages.stream()
                .map(CheerRegisterImage::imageKey)
                .toList();
        return fileClient.moveFiles(domain.getName(), cheerId, tempKeys);
    }
}
