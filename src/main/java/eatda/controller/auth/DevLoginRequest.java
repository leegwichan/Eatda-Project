package eatda.controller.auth;

public record DevLoginRequest(String socialId, String email, String nickname) {
}
