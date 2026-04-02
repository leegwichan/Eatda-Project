package eatda.controller.store;

import eatda.domain.store.Store;
import java.util.List;
import org.springframework.lang.Nullable;

public record StorePreviewResponse(
        long id,
        @Nullable String imageUrl,
        String name,
        String district,
        String neighborhood,
        String category,
        List<String> cheerDescriptions
) {

    public StorePreviewResponse(Store store, @Nullable String imageUrl, List<String> cheerDescriptions) {
        this(
                store.getId(),
                imageUrl,
                store.getName(),
                store.getAddressDistrict(),
                store.getAddressNeighborhood(),
                store.getCategoryName(),
                cheerDescriptions
        );
    }
}
