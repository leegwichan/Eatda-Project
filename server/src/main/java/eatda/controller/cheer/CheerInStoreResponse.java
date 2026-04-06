package eatda.controller.cheer;

import eatda.domain.cheer.CheerTagName;
import eatda.persistence.cheer.CheerInStoreResult;
import java.util.List;

public record CheerInStoreResponse(
        long id,
        long memberId,
        String memberNickname,
        String description,
        List<CheerTagName> tags
) {

    public CheerInStoreResponse(CheerInStoreResult result) {
        this(
                result.cheer().getId(),
                result.member().getId(),
                result.member().getNickname(),
                result.cheer().getDescription(),
                result.tags().getNames()
        );
    }
}
