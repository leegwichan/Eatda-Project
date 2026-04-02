package eatda.repository.cheer;

import eatda.domain.cheer.CheerImage;
import eatda.domain.store.Store;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheerImageRepository extends JpaRepository<CheerImage, Long> {

    List<CheerImage> findAllByCheerStoreOrderByOrderIndexAsc(Store store);

    Optional<CheerImage> findFirstByCheerStoreIdOrderByCreatedAtDesc(Long storeId);
}
