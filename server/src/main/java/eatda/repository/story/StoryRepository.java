package eatda.repository.story;

import eatda.domain.story.Story;
import eatda.exception.BusinessErrorCode;
import eatda.exception.BusinessException;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StoryRepository extends JpaRepository<Story, Long> {

    default Story getByIdOrThrow(Long id) {
        return findById(id)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.STORY_NOT_FOUND));
    }

    default StoryDetail getDetailByIdOrThrow(Long id) {
        return findDetailById(id)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.STORY_NOT_FOUND));
    }

    @Query("""
            SELECT y AS story, m AS member, e.id AS storeId FROM Story y
                LEFT JOIN Store e ON y.storeKakaoId = e.kakaoId
                LEFT JOIN Member m ON y.member = m
                WHERE y.id = :id
            """)
    Optional<StoryDetail> findDetailById(Long id);

    List<Story> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Story> findAllByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    @Query("""
            SELECT y AS story, m AS member, e.id AS storeId FROM Story y
                LEFT JOIN Store e ON y.storeKakaoId = e.kakaoId
                LEFT JOIN Member m ON y.member = m
                WHERE y.storeKakaoId = :storeKakaoId
                ORDER BY y.createdAt DESC
            """)
    List<StoryDetail> findAllDetailsByStoreKakaoIdOrderByCreatedAtDesc(String storeKakaoId, Pageable pageable);
}
