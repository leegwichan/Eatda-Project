package eatda.service.auth;

import eatda.client.oauth.OauthClient;
import eatda.client.oauth.OauthMemberInformation;
import eatda.client.oauth.OauthToken;
import eatda.controller.member.MemberResponse;
import eatda.persistence.member.LoginResult;
import eatda.persistence.member.MemberPersistence;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberPersistence memberPersistence;
    private final OauthClient oauthClient;

    public MemberResponse login(String code, String origin) {
        OauthToken oauthToken = oauthClient.requestOauthToken(code, origin);
        OauthMemberInformation oauthInformation = oauthClient.requestMemberInformation(oauthToken);

        LoginResult result = memberPersistence.login(oauthInformation.toMember());
        return new MemberResponse(result.member(), result.isFirstLogin());
    }

    public URI getOauthLoginUrl(String origin) {
        return oauthClient.getOauthLoginUrl(origin);
    }
}
