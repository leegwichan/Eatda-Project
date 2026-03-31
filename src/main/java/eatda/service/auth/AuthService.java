package eatda.service.auth;

import eatda.client.oauth.OauthClient;
import eatda.client.oauth.OauthMemberInformation;
import eatda.client.oauth.OauthToken;
import eatda.controller.member.MemberResponse;
import eatda.domain.member.Member;
import eatda.repository.member.MemberRepository;
import java.net.URI;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final OauthClient oauthClient;

    @Transactional
    public MemberResponse login(OauthMemberInformation oauthInformation) {
        Optional<Member> optionalMember = memberRepository.findBySocialId(Long.toString(oauthInformation.socialId()));
        boolean isFirstLogin = optionalMember.isEmpty();
        return new MemberResponse(
                optionalMember.orElseGet(() -> memberRepository.save(oauthInformation.toMember())),
                isFirstLogin);
    }

    public URI getOauthLoginUrl(String origin) {
        return oauthClient.getOauthLoginUrl(origin);
    }

    public OauthMemberInformation getOAuthInformation(String code, String origin) {
        OauthToken oauthToken = oauthClient.requestOauthToken(code, origin);
        return oauthClient.requestMemberInformation(oauthToken);
    }
}
