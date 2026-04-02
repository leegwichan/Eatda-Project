package eatda.controller.story;

public record StoryRegisterImage(
        String imageKey,
        long orderIndex,
        String contentType,
        long fileSize
) {
}
