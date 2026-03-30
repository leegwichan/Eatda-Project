package eatda.repository.story;

import eatda.domain.story.Story;
import eatda.exception.BusinessErrorCode;
import eatda.exception.BusinessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoryRepository extends JpaRepository<Story, Long> {

    default Story getByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.STORY_NOT_FOUND));
    }

    Page<Story> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Story> findAllByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    Page<Story> findAllByStoreKakaoIdOrderByCreatedAtDesc(String storeKakaoId, Pageable pageable);
}
