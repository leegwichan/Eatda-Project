package eatda.persistence.member;

import eatda.domain.member.Member;

public record LoginResult(Member member, boolean isFirstLogin) {

}
