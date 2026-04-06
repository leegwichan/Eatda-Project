package eatda.service.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

import eatda.controller.member.MemberResponse;
import eatda.controller.member.MemberUpdateRequest;
import eatda.domain.member.Member;
import eatda.service.BaseServiceTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MemberServiceTest extends BaseServiceTest {

    @Autowired
    private MemberService memberService;

    @Nested
    class GetMember {

        @Test
        void 회원_정보를_조회할_수_있다() {
            Member member = memberGenerator.generateRegisteredMember("123", "abc@kakao.com", "nickname", "01012345678");

            MemberResponse response = memberService.getMember(member.getId());

            assertAll(
                    () -> assertThat(response.id()).isEqualTo(member.getId()),
                    () -> assertThat(response.nickname()).isEqualTo(member.getNickname()),
                    () -> assertThat(response.phoneNumber()).isEqualTo(member.getPhoneNumber()),
                    () -> assertThat(response.optInMarketing()).isEqualTo(member.isOptInMarketing()),
                    () -> assertThat(response.isSignUp()).isFalse()
            );
        }

    }

    @Nested
    class ValidateNickname {

        @Test
        void 중복되지_않은_닉네임이면_예외가_발생하지_않는다() {
            memberGenerator.generate("123", "abc@kakao.com", "nickname");
            Member member = memberGenerator.generate("456", "def@kakao.com", "unique-nickname");
            String newNickname = "new-unique-nickname";

            assertThatCode(() -> memberService.validateNickname(newNickname, member.getId()))
                    .doesNotThrowAnyException();
        }

    }

    @Nested
    class ValidatePhoneNumber {

        @Test
        void 중복되지_않은_전화번호이면_예외가_발생하지_않는다() {
            memberGenerator.generate("123", "abc@kakao.com", "nickname");
            Member member = memberGenerator.generate("456", "def@kakao.com", "unique-nickname");
            String newPhoneNumber = "01012345678";

            assertThatCode(() -> memberService.validatePhoneNumber(newPhoneNumber, member.getId()))
                    .doesNotThrowAnyException();
        }

    }

    @Nested
    class Update {

        @Test
        void 회원_정보를_수정할_수_있다() {
            Member member = memberGenerator.generate("123");
            MemberUpdateRequest request = new MemberUpdateRequest("update-nickname", "01012345678", true);

            MemberResponse response = memberService.update(member.getId(), request);

            assertAll(
                    () -> assertThat(response.id()).isEqualTo(member.getId()),
                    () -> assertThat(response.isSignUp()).isFalse(),
                    () -> assertThat(response.nickname()).isEqualTo("update-nickname"),
                    () -> assertThat(response.phoneNumber()).isEqualTo("01012345678"),
                    () -> assertThat(response.optInMarketing()).isTrue()
            );
        }

    }
}
