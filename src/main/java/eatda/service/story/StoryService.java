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
import eatda.persistence.story.StoryDetailResult;
import eatda.persistence.story.StoryPersistence;
import eatda.persistence.story.StoryPreviewResult;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoryService {

    private static final ImageDomain IMAGE_DOMAIN = ImageDomain.STORY;

    private final StoryPersistence storyPersistence;
    private final FileClient fileClient;
    private final MapClient mapClient;
    private final StoreSearchFilter storeSearchFilter;

    public StoryResponse getStory(long storyId) {
        StoryDetailResult result = storyPersistence.getStoryDetail(storyId);

        return new StoryResponse(result.story(), result.storeId(), toStoryImageResponses(result.images()));
    }

    public StoriesResponse getStoryPreviews(int size) {
        List<StoryPreviewResult> results = storyPersistence.getRecentStoryPreviews(size);

        List<StoryPreview> responses = results.stream()
                .map(result -> new StoryPreview(result.story().getId(), toStoryImageResponses(result.images())))
                .toList();
        return new StoriesResponse(responses);
    }

    public StoriesDetailResponse getStoriesDetails(String kakaoId, int size) {
        List<StoryDetailResult> results = storyPersistence.getStoryDetailsByStoreKakaoId(kakaoId, size);

        List<StoryDetailResponse> responses = results.stream()
                .map(result -> new StoryDetailResponse(result.story(), result.member(),
                        toStoryImageResponses(result.images())))
                .toList();
        return new StoriesDetailResponse(responses);
    }

    public StoriesInMemberResponse getStoriesByMemberId(long memberId, int page, int size) {
        List<StoryPreviewResult> results = storyPersistence.getStoryPreviewsByMemberId(memberId, page, size);

        List<StoryInMemberResponse> responses = results.stream()
                .map(result -> new StoryInMemberResponse(result.story(), toStoryImageResponses(result.images())))
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

    private void saveStoryImages(Story story, List<StoryRegisterImage> registerImages) {
        List<String> beforeImageKeys = registerImages.stream()
                .map(StoryRegisterImage::imageKey)
                .toList();

        FileMovingResult movingResult = null;
        try {
            movingResult = fileClient.moveFiles(IMAGE_DOMAIN.getName(), story.getId(), beforeImageKeys);
            storyPersistence.saveStoryImages(story.getId(), registerImages, movingResult);
        } catch (RuntimeException exception) {
            log.error("스토리 등록 프로세스 실패. 롤백 수행. storyId={}", story.getId(), exception);
            storyPersistence.deleteStoryById(story.getId());
            if (movingResult != null) {
                fileClient.deleteFiles(movingResult.getResults());
            }
            throw exception;
        }
    }
}
