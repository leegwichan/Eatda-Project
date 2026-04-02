package eatda.persistence.store;

import eatda.domain.store.Store;
import jakarta.annotation.Nullable;
import java.util.List;

public record StorePreviewResult(
        Store store,
        @Nullable String thumbnailImageUrl,
        List<String> cheerDescriptions) {

}
