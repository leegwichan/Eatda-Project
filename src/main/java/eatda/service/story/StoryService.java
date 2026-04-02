package eatda.service.story;

import eatda.client.file.FileClient;
import eatda.client.file.FileMovingResult;
import eatda.client.map.MapClient;
import eatda.client.map.MapClientStoreSearchResult;
import eatda.controller.story.StoriesDetailResponse;
import eatda.controller.story.StoriesDetailResponse.StoryDetailResponse;
import eatda.controller.story.StoriesInMemberResponse;
import eatda.controller.story.StoriesResponse;
import eatda.controller.story.StoriesResponse.StoryPreview;
import eatda.controller.story.StoryImageResponse;
import eatda.controller.story.StoryInMemberResponse;
import eatda.controller.story.StoryRegisterImage;
import eatda.controller.story.StoryRegisterRequest;
import eatda.controller.story.StoryRegisterResponse;
import eatda.controller.story.StoryResponse;
import eatda.domain.ImageDomain;
import eatda.domain.store.StoreSearchFilter;
import eatda.domain.store.StoreSearchResult;
import eatda.domain.story.Story;
import eatda.domain.story.StoryImage;
import eatda.exception.BusinessException;
import eatda.persistence.store.StorePersistence;
import eatda.persistence.story.StoryPersistence;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoryService {

    private static final ImageDomain IMAGE_DOMAIN = ImageDomain.STORY;

    private final StoryPersistence storyPersistence;
    private final StorePersistence storePersistence;
    private final FileClient fileClient;
    private final MapClient mapClient;
    private final StoreSearchFilter storeSearchFilter;

    @Transactional(readOnly = true)
    public StoryResponse getStory(long storyId) {
        Story story = storyPersistence.getStory(storyId);
        Long storeId = storePersistence.getStoreIdByKakaoId(story.getStoreKakaoId());

        return new StoryResponse(story, storeId, toStoryImageResponses(story.getImages()));
    }

    @Transactional(readOnly = true)
    public StoriesResponse getStoryPreviews(int size) {
        List<Story> stories = storyPersistence.getStories(size);

        List<StoryPreview> responses = stories.stream()
                .map(story -> new StoryPreview(story.getId(), toStoryImageResponses(story.getImages())))
                .toList();
        return new StoriesResponse(responses);
    }

    @Transactional(readOnly = true)
    public StoriesDetailResponse getStoriesDetails(String kakaoId, int size) {
        List<Story> stories = storyPersistence.getStoriesByKakaoId(kakaoId, size);

        List<StoryDetailResponse> responses = stories.stream()
                .map(story -> new StoryDetailResponse(story, toStoryImageResponses(story.getImages())))
                .toList();
        return new StoriesDetailResponse(responses);
    }

    @Transactional(readOnly = true)
    public StoriesInMemberResponse getStoriesByMemberId(long memberId, int page, int size) {
        List<Story> stories = storyPersistence.getStoriesByMemberId(memberId, page, size);

        List<StoryInMemberResponse> responses = stories.stream()
                .map(story -> new StoryInMemberResponse(story, toStoryImageResponses(story.getImages())))
                .toList();
        return new StoriesInMemberResponse(responses);
    }

    private List<StoryImageResponse> toStoryImageResponses(List<StoryImage> images) {
        return images.stream()
                .map(img -> new StoryImageResponse(img, fileClient.getImageUrl(img.getImageKey())))
                .sorted(Comparator.comparingLong(StoryImageResponse::orderIndex))
                .toList();
    }

    public StoryRegisterResponse registerStory(StoryRegisterRequest request, long memberId) {
        List<MapClientStoreSearchResult> searched = mapClient.searchStores(request.storeName());
        StoreSearchResult filtered = storeSearchFilter.filterStoreByKakaoId(searched, request.storeKakaoId());

        Story story = storyPersistence.createStory(request, filtered, memberId);
        if (request.images() == null || request.images().isEmpty()) {
            return new StoryRegisterResponse(story.getId());
        }

        saveStoryImages(story, request.images());
        return new StoryRegisterResponse(story.getId());
    }

    private List<StoryImage> saveStoryImages(Story story, List<StoryRegisterImage> registerImages) {
        List<String> beforeImageKeys = registerImages.stream()
                .map(StoryRegisterImage::imageKey)
                .toList();

        FileMovingResult movingResult = null;
        try {
            movingResult = fileClient.moveFiles(IMAGE_DOMAIN.getName(), story.getId(), beforeImageKeys);
            return storyPersistence.saveStoryImages(story.getId(), registerImages, movingResult);
        } catch (BusinessException exception) {
            log.error("스토리 등록 프로세스 실패. 롤백 수행. cheerId={}", story.getId(), exception);
            storyPersistence.deleteStoryById(story.getId());
            if (movingResult != null) {
                fileClient.deleteFiles(movingResult.getResults());
            }
            throw exception;
        }
    }
}
