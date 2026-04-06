package eatda.persistence.story;

import eatda.domain.story.Story;
import eatda.domain.story.StoryImage;
import java.util.List;

public record StoryPreviewResult(
        Story story,
        List<StoryImage> images
) {
}
