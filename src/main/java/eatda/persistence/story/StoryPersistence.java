package eatda.persistence.story;

import eatda.client.file.FileMovingResult;
import eatda.controller.story.StoryRegisterImage;
import eatda.controller.story.StoryRegisterRequest;
import eatda.domain.member.Member;
import eatda.domain.store.StoreSearchResult;
import eatda.domain.story.Story;
import eatda.domain.story.StoryImage;
import eatda.repository.member.MemberRepository;
import eatda.repository.story.StoryDetail;
import eatda.repository.story.StoryImageRepository;
import eatda.repository.story.StoryRepository;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class StoryPersistence {

    private static final int PAGE_START_NUMBER = 0;

    private final StoryRepository storyRepository;
    private final StoryImageRepository storyImageRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public StoryDetailResult getStoryDetail(long id) {
        StoryDetail storyDetail = storyRepository.getDetailByIdOrThrow(id);
        List<StoryImage> images = storyImageRepository.findByStoryId(id);

        return new StoryDetailResult(storyDetail.getStory(), storyDetail.getMember(), storyDetail.getStoreId(), images);
    }

    public List<StoryPreviewResult> getStoryPreviewsByMemberId(long memberId, int page, int size) {
        List<Story> stories = storyRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId,
                PageRequest.of(page, size));
        Map<Long, List<StoryImage>> images = storyImageRepository.findAllByStoryIn(stories)
                .stream()
                .collect(Collectors.groupingBy(image -> image.getStory().getId()));

        return stories.stream()
                .map(story -> new StoryPreviewResult(
                        story, images.getOrDefault(story.getId(), Collections.emptyList())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StoryPreviewResult> getRecentStoryPreviews(int size) {
        List<Story> storyIds = storyRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(PAGE_START_NUMBER, size));
        Map<Long, List<StoryImage>> images = storyImageRepository.findAllByStoryIn(storyIds)
                .stream()
                .collect(Collectors.groupingBy(image -> image.getStory().getId()));

        return storyIds.stream()
                .map(story -> new StoryPreviewResult(
                        story, images.getOrDefault(story.getId(), Collections.emptyList())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StoryDetailResult> getStoryDetailsByKakaoId(String kakaoId, int size) {
        List<StoryDetail> storyDetails = storyRepository.findAllDetailsByStoreKakaoIdOrderByCreatedAtDesc(
                kakaoId, PageRequest.of(PAGE_START_NUMBER, size));

        List<Story> stories = storyDetails.stream()
                .map(StoryDetail::getStory)
                .toList();
        Map<Long, List<StoryImage>> images = storyImageRepository.findAllByStoryIn(stories)
                .stream()
                .collect(Collectors.groupingBy(image -> image.getStory().getId()));

        return storyDetails.stream()
                .map(detail -> new StoryDetailResult(
                        detail.getStory(), detail.getMember(), detail.getStoreId(),
                        images.getOrDefault(detail.getStory().getId(), Collections.emptyList())))
                .toList();
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
    public void saveStoryImages(long storyId,
                                List<StoryRegisterImage> registerImages,
                                FileMovingResult movingResult) {
        Story story = storyRepository.getByIdOrThrow(storyId);
        registerImages.stream()
                .sorted(Comparator.comparingLong(StoryRegisterImage::orderIndex))
                .forEach(image -> saveStoryImage(image, story, movingResult));
    }

    private void saveStoryImage(StoryRegisterImage image, Story story, FileMovingResult movingResult) {
        StoryImage createdImage = new StoryImage(
                story,
                movingResult.findNewPath(image.imageKey()),
                image.orderIndex(),
                image.contentType(),
                image.fileSize()
        );
        story.addImage(createdImage);
    }

    public void deleteStoryById(Long id) {
        storyRepository.deleteById(id);
    }
}
