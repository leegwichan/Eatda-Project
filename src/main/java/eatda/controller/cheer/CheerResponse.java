package eatda.controller.cheer;

import eatda.domain.cheer.CheerTagName;
import eatda.persistence.cheer.CheerDetailResult;
import java.util.List;

public record CheerResponse(
        long storeId,
        long cheerId,
        List<CheerImageResponse> images,
        String cheerDescription,
        List<CheerTagName> tags
) {

    public CheerResponse(CheerDetailResult result, List<CheerImageResponse> images) {
        this(
                result.storeId(),
                result.cheer().getId(),
                images,
                result.cheer().getDescription(),
                result.tags().getNames()
        );
    }
}
