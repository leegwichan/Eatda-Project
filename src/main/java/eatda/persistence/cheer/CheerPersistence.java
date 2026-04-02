package eatda.persistence.cheer;

import eatda.client.file.FileMovingResult;
import eatda.controller.cheer.CheerRegisterRequest;
import eatda.controller.cheer.CheerRegisterImage;
import eatda.controller.cheer.CheerSearchParameters;
import eatda.domain.cheer.Cheer;
import eatda.domain.cheer.CheerImage;
import eatda.domain.member.Member;
import eatda.domain.store.Store;
import eatda.domain.store.StoreSearchResult;
import eatda.exception.BusinessErrorCode;
import eatda.exception.BusinessException;
import eatda.repository.cheer.CheerRepository;
import eatda.repository.member.MemberRepository;
import eatda.repository.store.StoreRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CheerPersistence {

    private static final int MAX_CHEER_SIZE = 10_000; // 추후 3개로 변경
    private static final String SORTED_PROPERTIES = "createdAt";

    private final MemberRepository memberRepository;
    private final StoreRepository storeRepository;
    private final CheerRepository cheerRepository;

    @Transactional(readOnly = true)
    public Cheer getCheerById(Long cheerId) {
        return cheerRepository.getByIdOrThrow(cheerId);
    }

    @Transactional(readOnly = true)
    public List<Cheer> getCheers(CheerSearchParameters parameters) {
        return cheerRepository.findAllByConditions(
                parameters.getCategory(),
                parameters.getCheerTagNames(),
                parameters.getDistricts(),
                PageRequest.of(parameters.getPage(), parameters.getSize(), Sort.by(Direction.DESC, SORTED_PROPERTIES))
        );
    }

    @Transactional(readOnly = true)
    public List<Cheer> getCheersByStoreId(Long storeId, int page, int size) {
        Store store = storeRepository.getByIdOrThrow(storeId);
        return cheerRepository.findAllByStoreOrderByCreatedAtDesc(store, PageRequest.of(page, size));
    }

    @Transactional
    public Cheer createCheer(CheerRegisterRequest request,
                             StoreSearchResult result,
                             long memberId
    ) {
        Member member = memberRepository.getById(memberId);
        validateRegisterCheer(member, request.storeKakaoId());

        Store store = storeRepository.findByKakaoId(result.kakaoId())
                .orElseGet(() -> storeRepository.save(result.toStore())); // TODO 상점 조회/저장 동시성 이슈 해결
        Cheer cheer = new Cheer(member, store, request.description());
        cheer.setCheerTags(request.tags());
        return cheerRepository.save(cheer);
    }

    private void validateRegisterCheer(Member member, String storeKakaoId) {
        if (cheerRepository.countByMember(member) >= MAX_CHEER_SIZE) {
            throw new BusinessException(BusinessErrorCode.FULL_CHEER_SIZE_PER_MEMBER);
        }
        if (cheerRepository.existsByMemberAndStoreKakaoId(member, storeKakaoId)) {
            throw new BusinessException(BusinessErrorCode.ALREADY_CHEERED);
        }
    }

    @Transactional
    public List<CheerImage> saveCheerImages(Long cheerId,
                                            List<CheerRegisterImage> images,
                                            FileMovingResult movingResult) {

        Cheer cheer = cheerRepository.getByIdOrThrow(cheerId);
        return images.stream()
                .map(image -> saveCheerImage(image, cheer, movingResult))
                .toList();
    }

    private CheerImage saveCheerImage(CheerRegisterImage image, Cheer cheer, FileMovingResult movingResult) {
        CheerImage createdImage = new CheerImage(
                cheer,
                movingResult.findNewPath(image.imageKey()),
                image.orderIndex(),
                image.contentType(),
                image.fileSize()
        );
        cheer.addImage(createdImage);
    }

    @Transactional
    public void deleteCheerById(Long cheerId) {
        cheerRepository.deleteById(cheerId);
    }
}
