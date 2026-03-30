package eatda.repository.story;

import eatda.domain.story.Story;
import eatda.exception.BusinessErrorCode;
import eatda.exception.BusinessException;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoryRepository extends JpaRepository<Story, Long> {

    default Story getByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.STORY_NOT_FOUND));
    }

    List<Story> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Story> findAllByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    List<Story> findAllByStoreKakaoIdOrderByCreatedAtDesc(String storeKakaoId, Pageable pageable);
}
