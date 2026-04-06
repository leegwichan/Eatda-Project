package eatda.repository.story;

import eatda.domain.member.Member;
import eatda.domain.story.Story;
import org.springframework.lang.Nullable;

public interface StoryDetail {

    Story getStory();

    Member getMember();

    @Nullable
    Long getStoreId();
}
