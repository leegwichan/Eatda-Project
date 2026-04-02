package eatda.controller.cheer;

import eatda.domain.cheer.Cheer;
import eatda.domain.cheer.CheerTagName;
import java.util.Collections;
import java.util.List;

public record CheerResponse(
        long storeId,
        long cheerId,
        List<CheerImageResponse> images,
        String cheerDescription,
        List<CheerTagName> tags
) {

    public CheerResponse(Cheer cheer, List<CheerImageResponse> images) {
        this(
                cheer.getStore().getId(),
                cheer.getId(),
                images,
                cheer.getDescription(),
                cheer.getCheerTagNames()
        );
    }

    public CheerResponse(Cheer cheer) {
        this(cheer, Collections.emptyList());
    }
}
