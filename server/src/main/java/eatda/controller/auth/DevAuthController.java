package eatda.controller.auth;

import eatda.config.DevLoginProperties;
import eatda.controller.member.MemberResponse;
import eatda.controller.web.jwt.JwtManager;
import eatda.domain.member.Member;
import eatda.persistence.member.LoginResult;
import eatda.persistence.member.MemberPersistence;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(name = "dev-login.enabled", havingValue = "true")
@EnableConfigurationProperties(DevLoginProperties.class)
public class DevAuthController {

    private final MemberPersistence memberPersistence;
    private final JwtManager jwtManager;

    @PostMapping("/api/auth/dev-login")
    public ResponseEntity<LoginResponse> devLogin(@RequestBody DevLoginRequest request) {
        Member member = new Member(request.socialId(), request.email(), request.nickname());
        LoginResult result = memberPersistence.login(member);
        MemberResponse memberResponse = new MemberResponse(result.member(), result.isFirstLogin());

        TokenResponse token = new TokenResponse(
                jwtManager.issueAccessToken(memberResponse.id()),
                jwtManager.issueRefreshToken(memberResponse.id()));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new LoginResponse(token, memberResponse));
    }
}
