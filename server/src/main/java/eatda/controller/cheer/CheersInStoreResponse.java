package eatda.controller.cheer;

import eatda.domain.cheer.Cheer;
import java.util.List;

public record CheersInStoreResponse(List<CheerInStoreResponse> cheers) {

    public static CheersInStoreResponse from(List<Cheer> cheers) {
        List<CheerInStoreResponse> responses = cheers.stream()
                .map(CheerInStoreResponse::new)
                .toList();
        return new CheersInStoreResponse(responses);
    }
}
