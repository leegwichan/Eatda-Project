package eatda.controller.story;

import java.util.List;

public record StoryRegisterRequest(
        String storeName,
        String storeKakaoId,
        String description,
        List<StoryRegisterImage> images
) {
}
