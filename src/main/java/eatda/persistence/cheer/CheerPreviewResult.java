package eatda.persistence.cheer;

import eatda.domain.cheer.Cheer;
import eatda.domain.cheer.CheerImage;
import eatda.domain.cheer.CheerTags;
import eatda.domain.member.Member;
import eatda.domain.store.Store;
import java.util.List;

public record CheerPreviewResult(
        Cheer cheer,
        Store store,
        Member member,
        CheerTags tags,
        List<CheerImage> images
) {

}
