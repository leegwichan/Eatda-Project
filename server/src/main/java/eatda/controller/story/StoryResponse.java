package eatda.controller.story;

import eatda.domain.story.Story;
import java.util.List;
import org.springframework.lang.Nullable;

public record StoryResponse(
        @Nullable Long storeId,
        String storeKakaoId,
        String category,
        String storeName,
        String storeDistrict,
        String storeNeighborhood,
        String description,
        List<StoryImageResponse> images,
        long memberId,
        String memberNickname
) {

    public StoryResponse(Story story, @Nullable Long storeId, List<StoryImageResponse> images) {
        this(
                storeId,
                story.getStoreKakaoId(),
                story.getStoreCategory().getCategoryName(),
                story.getStoreName(),
                story.getAddressDistrict(),
                story.getAddressNeighborhood(),
                story.getDescription(),
                images,
                story.getMember().getId(),
                story.getMember().getNickname()
        );
    }
}
