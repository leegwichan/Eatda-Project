package eatda.service.member;

import eatda.controller.member.MemberResponse;
import eatda.controller.member.MemberUpdateRequest;
import eatda.domain.member.Member;
import eatda.persistence.member.MemberPersistence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberPersistence memberPersistence;

    public MemberResponse getMember(long memberId) {
        Member member = memberPersistence.getMember(memberId);
        return new MemberResponse(member);
    }

    public void validateNickname(String nickname, long memberId) {
        memberPersistence.validateNickname(nickname, memberId);
    }

    public void validatePhoneNumber(String phoneNumber, long memberId) {
        memberPersistence.validatePhoneNumber(phoneNumber, memberId);
    }

    public MemberResponse update(long memberId, MemberUpdateRequest request) {
        Member member = memberPersistence.update(memberId, request);
        return new MemberResponse(member);
    }
}
