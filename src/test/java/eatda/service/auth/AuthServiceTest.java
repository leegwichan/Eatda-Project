package eatda.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

import eatda.client.oauth.OauthMemberInformation;
import eatda.client.oauth.OauthToken;
import eatda.controller.member.MemberResponse;
import eatda.service.BaseServiceTest;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AuthServiceTest extends BaseServiceTest {

    private static final OauthToken DEFAULT_OAUTH_TOKEN = new OauthToken("oauth-access-token");
    private static final OauthMemberInformation DEFAULT_OAUTH_MEMBER_INFO =
            new OauthMemberInformation(123L, "authService@kakao.com", "nickname");

    @BeforeEach
    protected final void mockingClient() throws URISyntaxException {
        doReturn(new URI("http://localhost:8080/login/callback")).when(oauthClient).getOauthLoginUrl(anyString());
        doReturn(DEFAULT_OAUTH_TOKEN).when(oauthClient).requestOauthToken(anyString(), anyString());
        doReturn(DEFAULT_OAUTH_MEMBER_INFO).when(oauthClient).requestMemberInformation(DEFAULT_OAUTH_TOKEN);
    }

    @Nested
    class Login {

        @Test
        void 로그인_최초_요청_시_회원가입_및_로그인_처리를_한다() {
            String code = "oauth-code";
            String url = "http://localhost:3000";

            MemberResponse response = authService.login(code, url);

            assertAll(
                    () -> assertThat(response.isSignUp()).isTrue(),
                    () -> assertThat(response.id()).isNotZero(),
                    () -> assertThat(response.email()).isEqualTo(DEFAULT_OAUTH_MEMBER_INFO.email()),
                    () -> assertThat(response.nickname()).isEqualTo(DEFAULT_OAUTH_MEMBER_INFO.nickname())
            );
        }

        @Test
        void 로그인_최초_요청이_아닐_경우_로그인만_처리를_한다() {
            String code = "oauth-code";
            String url = "http://localhost:3000";
            memberGenerator.generate(Long.toString(DEFAULT_OAUTH_MEMBER_INFO.socialId()));

            MemberResponse response = authService.login(code, url);

            assertThat(response.isSignUp()).isFalse();
        }
    }

    @Nested
    class GetOauthLoginUrl {

        @Test
        void OAuth_로그인_URL을_반환한다() {
            String origin = "http://localhost:3000";

            URI oauthLoginUrl = authService.getOauthLoginUrl(origin);

            assertThat(oauthLoginUrl).isNotNull();
        }
    }
}
