package eatda.controller.story;

import eatda.domain.member.Member;
import eatda.domain.story.Story;
import java.util.List;

public record StoriesDetailResponse(List<StoryDetailResponse> stories) {

    public record StoryDetailResponse(
            long storyId,
            List<StoryImageResponse> images,
            long memberId,
            String memberNickname
    ) {
        public StoryDetailResponse(Story story, Member member, List<StoryImageResponse> images) {
            this(
                    story.getId(),
                    images,
                    member.getId(),
                    member.getNickname());
        }
    }
}
