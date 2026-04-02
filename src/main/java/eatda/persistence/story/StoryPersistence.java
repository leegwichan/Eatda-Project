package eatda.persistence.story;

import eatda.client.file.FileMovingResult;
import eatda.controller.story.StoryRegisterImage;
import eatda.controller.story.StoryRegisterRequest;
import eatda.domain.member.Member;
import eatda.domain.store.StoreSearchResult;
import eatda.domain.story.Story;
import eatda.domain.story.StoryImage;
import eatda.repository.member.MemberRepository;
import eatda.repository.story.StoryRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class StoryPersistence {

    private static final int PAGE_START_NUMBER = 0;

    private final StoryRepository storyRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public Story getStory(long storyId) {
        return storyRepository.getByIdOrThrow(storyId);
    }

    @Transactional(readOnly = true)
    public List<Story> getStories(int size) {
        return storyRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(PAGE_START_NUMBER, size));
    }

    @Transactional(readOnly = true)
    public List<Story> getStoriesByKakaoId(String kakaoId, int size) {
        return storyRepository.findAllByStoreKakaoIdOrderByCreatedAtDesc(kakaoId,
                PageRequest.of(PAGE_START_NUMBER, size));
    }

    @Transactional(readOnly = true)
    public List<Story> getStoriesByMemberId(long memberId, int page, int size) {
        return storyRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId, PageRequest.of(page, size));
    }

    @Transactional
    public Story createStory(StoryRegisterRequest request, StoreSearchResult result, long memberId) {
        Member member = memberRepository.getById(memberId);

        Story story = Story.builder()
                .member(member)
                .storeKakaoId(result.kakaoId())
                .storeName(result.name())
                .storeRoadAddress(result.roadAddress())
                .storeLotNumberAddress(result.lotNumberAddress())
                .storeCategory(result.category())
                .description(request.description())
                .build();
        return storyRepository.save(story);
    }

    @Transactional
    public List<StoryImage> saveStoryImages(long storyId,
                                            List<StoryRegisterImage> registerImages,
                                            FileMovingResult movingResult) {
        Story story = storyRepository.getByIdOrThrow(storyId);
        return registerImages.stream()
                .sorted(Comparator.comparingLong(StoryRegisterImage::orderIndex))
                .map(image -> saveStoryImage(image, story, movingResult))
                .toList();
    }

    private StoryImage saveStoryImage(StoryRegisterImage image, Story story, FileMovingResult movingResult) {
        StoryImage createdImage = new StoryImage(
                story,
                movingResult.findNewPath(image.imageKey()),
                image.orderIndex(),
                image.contentType(),
                image.fileSize()
        );
        story.addImage(createdImage);
        return createdImage;
    }

    public void deleteStoryById(Long id) {
        storyRepository.deleteById(id);
    }
}
