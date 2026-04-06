package eatda.controller.cheer;

import eatda.persistence.cheer.CheerInStoreResult;
import java.util.List;

public record CheersInStoreResponse(List<CheerInStoreResponse> cheers) {

    public static CheersInStoreResponse from(List<CheerInStoreResult> results) {
        List<CheerInStoreResponse> responses = results.stream()
                .map(CheerInStoreResponse::new)
                .toList();
        return new CheersInStoreResponse(responses);
    }
}
