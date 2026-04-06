package eatda.persistence.story;

import eatda.domain.member.Member;
import eatda.domain.story.Story;
import eatda.domain.story.StoryImage;
import jakarta.annotation.Nullable;
import java.util.List;

public record StoryDetailResult(
        Story story,
        Member member,
        @Nullable Long storeId,
        List<StoryImage> images
) {
}
