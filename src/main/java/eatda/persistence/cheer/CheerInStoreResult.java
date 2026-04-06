package eatda.persistence.cheer;

import eatda.domain.cheer.Cheer;
import eatda.domain.cheer.CheerTags;
import eatda.domain.member.Member;

public record CheerInStoreResult(
        Cheer cheer,
        Member member,
        CheerTags tags
) {

}
