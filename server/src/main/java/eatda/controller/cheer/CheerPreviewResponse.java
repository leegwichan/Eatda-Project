package eatda.controller.cheer;

import eatda.domain.cheer.CheerTagName;
import eatda.persistence.cheer.CheerPreviewResult;
import java.util.List;

public record CheerPreviewResponse(
        long storeId,
        List<CheerImageResponse> images,
        String storeName,
        String storeDistrict,
        String storeNeighborhood,
        String storeCategory,
        long cheerId,
        String cheerDescription,
        List<CheerTagName> tags,
        long memberId,
        String memberNickname
) {

    public CheerPreviewResponse(CheerPreviewResult result, List<CheerImageResponse> images) {
        this(
                result.store().getId(),
                images,
                result.store().getName(),
                result.store().getAddressDistrict(),
                result.store().getAddressNeighborhood(),
                result.store().getCategory().getCategoryName(),
                result.cheer().getId(),
                result.cheer().getDescription(),
                result.tags().getNames(),
                result.member().getId(),
                result.member().getNickname()
        );
    }
}
