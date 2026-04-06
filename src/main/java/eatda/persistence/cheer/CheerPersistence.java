package eatda.persistence.cheer;

import eatda.client.file.FileMovingResult;
import eatda.controller.cheer.CheerRegisterImage;
import eatda.controller.cheer.CheerRegisterRequest;
import eatda.controller.cheer.CheerSearchParameters;
import eatda.domain.cheer.Cheer;
import eatda.domain.cheer.CheerImage;
import eatda.domain.cheer.CheerTag;
import eatda.domain.cheer.CheerTags;
import eatda.domain.member.Member;
import eatda.domain.store.Store;
import eatda.domain.store.StoreSearchResult;
import eatda.exception.BusinessErrorCode;
import eatda.exception.BusinessException;
import eatda.repository.cheer.CheerImageRepository;
import eatda.repository.cheer.CheerRepository;
import eatda.repository.cheer.CheerTagRepository;
import eatda.repository.member.MemberRepository;
import eatda.repository.store.StoreRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
    private final CheerTagRepository cheerTagRepository;
    private final CheerImageRepository cheerImageRepository;

    @Transactional(readOnly = true)
    public CheerDetailResult getCheerById(Long cheerId) {
        Cheer cheer = cheerRepository.getByIdOrThrow(cheerId);
        CheerTags cheerTags = new CheerTags(cheerTagRepository.findAllByCheerId(cheerId));
        List<CheerImage> cheerImages = cheerImageRepository.findAllByCheerIdOrderByOrderIndexAsc(cheerId);
        return new CheerDetailResult(cheer, cheer.getStore().getId(), cheerTags, cheerImages);
    }

    @Transactional(readOnly = true)
    public List<CheerPreviewResult> getCheers(CheerSearchParameters parameters) {
        List<Cheer> cheers = cheerRepository.findAllByConditions(
                parameters.getCategory(),
                parameters.getCheerTagNames(),
                parameters.getDistricts(),
                PageRequest.of(parameters.getPage(), parameters.getSize(), Sort.by(Direction.DESC, SORTED_PROPERTIES))
        );
        Map<Long, List<CheerTag>> tags = cheerTagRepository.findAllByCheerIn(cheers)
                .stream()
                .collect(Collectors.groupingBy(tag -> tag.getCheer().getId()));
        Map<Long, List<CheerImage>> images = cheerImageRepository.findAllByCheerIn(cheers)
                .stream()
                .collect(Collectors.groupingBy(image -> image.getCheer().getId()));

        return cheers.stream()
                .map(cheer -> new CheerPreviewResult(
                        cheer, cheer.getStore(), cheer.getMember(),
                        new CheerTags(tags.getOrDefault(cheer.getId(), new LinkedList<>())),
                        images.getOrDefault(cheer.getId(), Collections.emptyList())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CheerInStoreResult> getCheersByStoreId(Long storeId, int page, int size) {
        Store store = storeRepository.getByIdOrThrow(storeId);
        List<Cheer> cheers = cheerRepository.findAllByStoreOrderByCreatedAtDesc(store, PageRequest.of(page, size));
        Map<Long, List<CheerTag>> tags = cheerTagRepository.findAllByCheerIn(cheers)
                .stream()
                .collect(Collectors.groupingBy(tag -> tag.getCheer().getId()));

        return cheers.stream()
                .map(cheer -> new CheerInStoreResult(
                        cheer, cheer.getMember(),
                        new CheerTags(tags.getOrDefault(cheer.getId(), new LinkedList<>()))))
                .toList();
    }

    @Transactional
    public CheerDetailResult createCheer(CheerRegisterRequest request,
                                         StoreSearchResult result,
                                         long memberId
    ) {
        Member member = memberRepository.getById(memberId);
        validateRegisterCheer(member, request.storeKakaoId());

        Store store = storeRepository.findByKakaoId(result.kakaoId())
                .orElseGet(() -> storeRepository.save(result.toStore())); // TODO 상점 조회/저장 동시성 이슈 해결
        Cheer cheer = new Cheer(member, store, request.description());
        cheer.setCheerTags(request.tags());
        Cheer savedCheer = cheerRepository.save(cheer);
        return new CheerDetailResult(
                savedCheer,
                store.getId(),
                savedCheer.getCheerTags(),
                new ArrayList<>(savedCheer.getImages())
        );
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
                .sorted(Comparator.comparingLong(CheerRegisterImage::orderIndex))
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
        return createdImage;
    }

    @Transactional
    public void deleteCheerById(Long cheerId) {
        cheerRepository.deleteById(cheerId);
    }
}
