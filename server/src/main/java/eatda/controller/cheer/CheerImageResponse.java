package eatda.controller.cheer;

import eatda.domain.cheer.CheerImage;

public record CheerImageResponse(
        String imageKey,
        long orderIndex,
        String contentType,
        long fileSize,
        String url
) {
    public CheerImageResponse(CheerImage cheerImage, String imageUrl) {
        this(
                cheerImage.getImageKey(),
                cheerImage.getOrderIndex(),
                cheerImage.getContentType(),
                cheerImage.getFileSize(),
                imageUrl
        );
    }
}
