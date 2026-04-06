package eatda.persistence.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import eatda.controller.member.MemberUpdateRequest;
import eatda.domain.member.Member;
import eatda.exception.BusinessErrorCode;
import eatda.exception.BusinessException;
import eatda.persistence.BasePersistenceTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MemberPersistenceTest extends BasePersistenceTest {

    @Autowired
    private MemberPersistence memberPersistence;

    @Nested
    class GetMember {

        @Test
        void 회원을_조회할_수_있다() {
            Member member = memberGenerator.generateRegisteredMember("커찬", "ac@kakao.com", "123", "01012341235");

            Member actual = memberPersistence.getMember(member.getId());

            assertThat(actual.getId()).isEqualTo(member.getId());
        }

    }

    @Nested
    class ValidateNickname {

        @Test
        void 사용_가능한_닉네임이면_예외가_발생하지_않는다() {
            Member member = memberGenerator.generateRegisteredMember("커찬", "ac@kakao.com", "123", "01012341235");

            assertThatCode(() -> memberPersistence.validateNickname("새닉네임", member.getId()))
                    .doesNotThrowAnyException();
        }

        @Test
        void 이미_사용중인_닉네임이면_예외를_던진다() {
            memberGenerator.generateRegisteredMember("기존유저", "ab@kakao.com", "122", "01012341234");
            Member member = memberGenerator.generateRegisteredMember("커찬", "ac@kakao.com", "123", "01012341235");

            assertThatThrownBy(() -> memberPersistence.validateNickname("기존유저", member.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(BusinessErrorCode.DUPLICATE_NICKNAME.getMessage());
        }

        @Test
        void 본인의_현재_닉네임이면_예외가_발생하지_않는다() {
            Member member = memberGenerator.generateRegisteredMember("커찬", "ac@kakao.com", "123", "01012341235");

            assertThatCode(() -> memberPersistence.validateNickname("커찬", member.getId()))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    class ValidatePhoneNumber {

        @Test
        void 사용_가능한_전화번호이면_예외가_발생하지_않는다() {
            Member member = memberGenerator.generateRegisteredMember("커찬", "ac@kakao.com", "123", "01012341235");

            assertThatCode(() -> memberPersistence.validatePhoneNumber("01099998888", member.getId()))
                    .doesNotThrowAnyException();
        }

        @Test
        void 이미_사용중인_전화번호이면_예외를_던진다() {
            memberGenerator.generateRegisteredMember("기존유저", "ab@kakao.com", "122", "01012341234");
            Member member = memberGenerator.generateRegisteredMember("커찬", "ac@kakao.com", "123", "01012341235");

            assertThatThrownBy(() -> memberPersistence.validatePhoneNumber("01012341234", member.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(BusinessErrorCode.DUPLICATE_PHONE_NUMBER.getMessage());
        }

        @Test
        void 본인의_현재_전화번호이면_예외가_발생하지_않는다() {
            Member member = memberGenerator.generateRegisteredMember("커찬", "ac@kakao.com", "123", "01012341235");

            assertThatCode(() -> memberPersistence.validatePhoneNumber("01012341235", member.getId()))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    class Login {

        @Test
        void 신규_회원이면_저장_후_isFirstLogin이_true를_반환한다() {
            Member newMember = new Member("new-social-id", "new@kakao.com", "신규유저");

            LoginResult result = memberPersistence.login(newMember);

            assertAll(
                    () -> assertThat(result.isFirstLogin()).isTrue(),
                    () -> assertThat(result.member().getSocialId()).isEqualTo("new-social-id")
            );
        }

        @Test
        void 기존_회원이면_isFirstLogin이_false를_반환한다() {
            Member existing = memberGenerator.generate("existing-social-id", "ex@kakao.com", "기존유저");
            Member loginAttempt = new Member("existing-social-id", "ex@kakao.com", "기존유저");

            LoginResult result = memberPersistence.login(loginAttempt);

            assertAll(
                    () -> assertThat(result.isFirstLogin()).isFalse(),
                    () -> assertThat(result.member().getId()).isEqualTo(existing.getId())
            );
        }
    }

    @Nested
    class Update {

        @Test
        void 회원_정보를_수정할_수_있다() {
            Member member = memberGenerator.generateRegisteredMember("커찬", "ac@kakao.com", "123", "01012341235");
            MemberUpdateRequest request = new MemberUpdateRequest("새닉네임", "01099998888", true);

            Member updated = memberPersistence.update(member.getId(), request);

            assertAll(
                    () -> assertThat(updated.getNickname()).isEqualTo("새닉네임"),
                    () -> assertThat(updated.getPhoneNumber()).isEqualTo("01099998888"),
                    () -> assertThat(updated.isOptInMarketing()).isTrue()
            );
        }

        @Test
        void 중복_닉네임이면_예외를_던진다() {
            memberGenerator.generateRegisteredMember("기존유저", "ab@kakao.com", "122", "01012341234");
            Member member = memberGenerator.generateRegisteredMember("커찬", "ac@kakao.com", "123", "01012341235");
            MemberUpdateRequest request = new MemberUpdateRequest("기존유저", "01099998888", true);

            assertThatThrownBy(() -> memberPersistence.update(member.getId(), request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(BusinessErrorCode.DUPLICATE_NICKNAME.getMessage());
        }

        @Test
        void 중복_전화번호이면_예외를_던진다() {
            memberGenerator.generateRegisteredMember("기존유저", "ab@kakao.com", "122", "01012341234");
            Member member = memberGenerator.generateRegisteredMember("커찬", "ac@kakao.com", "123", "01012341235");
            MemberUpdateRequest request = new MemberUpdateRequest("새닉네임", "01012341234", true);

            assertThatThrownBy(() -> memberPersistence.update(member.getId(), request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(BusinessErrorCode.DUPLICATE_PHONE_NUMBER.getMessage());
        }
    }
}
