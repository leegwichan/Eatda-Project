package eatda.controller.cheer;

public record CheerRegisterImage(
        String imageKey,
        long orderIndex,
        String contentType,
        long fileSize
) {
}
