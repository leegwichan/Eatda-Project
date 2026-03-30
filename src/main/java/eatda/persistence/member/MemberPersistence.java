package eatda.persistence.member;

import eatda.controller.member.MemberUpdateRequest;
import eatda.domain.member.Member;
import eatda.exception.BusinessErrorCode;
import eatda.exception.BusinessException;
import eatda.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class MemberPersistence {

    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public Member getMember(long memberId) {
        return memberRepository.getById(memberId);
    }

    @Transactional(readOnly = true)
    public void validateNickname(String nickname, long memberId) {
        Member member = memberRepository.getById(memberId);
        validateNicknameNotDuplicate(member, nickname);
    }

    @Transactional(readOnly = true)
    public void validatePhoneNumber(String phoneNumber, long memberId) {
        Member member = memberRepository.getById(memberId);
        validatePhoneNumberNotDuplicate(member, phoneNumber);
    }

    @Transactional
    public Member update(long memberId, MemberUpdateRequest request) {
        Member member = memberRepository.getById(memberId);
        validatePhoneNumberNotDuplicate(member, request.phoneNumber());
        validateNicknameNotDuplicate(member, request.nickname());

        Member memberUpdater = request.toMemberUpdater();
        member.update(memberUpdater);
        return member;
    }

    private void validateNicknameNotDuplicate(Member member, String newNickname) {
        if (!member.isSameNickname(newNickname) && memberRepository.existsByNickname(newNickname)) {
            throw new BusinessException(BusinessErrorCode.DUPLICATE_NICKNAME);
        }
    }

    private void validatePhoneNumberNotDuplicate(Member member, String newPhoneNumber) {
        if (!member.isSameMobilePhoneNumber(newPhoneNumber)
                && memberRepository.existsByMobilePhoneNumberValue(newPhoneNumber)) {
            throw new BusinessException(BusinessErrorCode.DUPLICATE_PHONE_NUMBER);
        }
    }
}
