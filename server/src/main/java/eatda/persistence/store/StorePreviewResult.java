package eatda.persistence.store;

import eatda.domain.store.Store;
import jakarta.annotation.Nullable;
import java.util.List;

public record StorePreviewResult(
        Store store,
        @Nullable String thumbnailImageKey,
        List<String> cheerDescriptions) {
}
